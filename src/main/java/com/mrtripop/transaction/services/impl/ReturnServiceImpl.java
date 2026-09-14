package com.mrtripop.transaction.services.impl;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.services.BatchService;
import com.mrtripop.transaction.component.ReturnMapper;
import com.mrtripop.transaction.constant.ErrorCode;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.InvoiceStatus;
import com.mrtripop.transaction.models.db.Return;
import com.mrtripop.transaction.models.db.ReturnItem;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnDto;
import com.mrtripop.transaction.models.dto.ReturnItemRequest;
import com.mrtripop.transaction.repository.InvoiceItemRepository;
import com.mrtripop.transaction.repository.InvoiceRepository;
import com.mrtripop.transaction.repository.ReturnItemRepository;
import com.mrtripop.transaction.repository.ReturnRepository;
import com.mrtripop.transaction.services.ReturnService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

  private final InvoiceRepository invoiceRepository;
  private final InvoiceItemRepository invoiceItemRepository;
  private final ReturnRepository returnRepository;
  private final ReturnItemRepository returnItemRepository;
  private final ReturnMapper returnMapper;
  private final AuditService auditService;
  private final BatchService batchService;

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public ReturnDto createReturn(Long invoiceId, CreateReturnRequest request)
      throws ApplicationException {
    Invoice invoice = invoiceRepository.findById(invoiceId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));

    if (invoice.getStatus() != InvoiceStatus.COMPLETED) {
      throw new ApplicationException(ErrorCode.INVOICE_NOT_COMPLETED, HttpStatus.CONFLICT);
    }

    List<ReturnItem> returnItems = new ArrayList<>();
    BigDecimal totalRefundAmount = BigDecimal.ZERO;
    BigDecimal totalRefundPatientOwed = BigDecimal.ZERO;
    BigDecimal totalRefundInsuranceClaim = BigDecimal.ZERO;

    for (ReturnItemRequest itemRequest : request.getItems()) {
      InvoiceItem invoiceItem = invoiceItemRepository.findById(itemRequest.getInvoiceItemId())
          .orElseThrow(() -> new ApplicationException(
              ErrorCode.INVOICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));

      if (!invoiceItem.getInvoice().getId().equals(invoiceId)) {
        throw new ApplicationException(ErrorCode.INVOICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
      }

      Long alreadyReturned =
          returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId());
      long remaining = invoiceItem.getQuantity() - alreadyReturned;
      if (itemRequest.getQuantity() > remaining) {
        throw new ApplicationException(
            ErrorCode.RETURN_QUANTITY_EXCEEDS_AVAILABLE, HttpStatus.BAD_REQUEST);
      }

      BigDecimal lineRefundAmount =
          invoiceItem.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
      BigDecimal lineRefundPatientOwed = invoiceItem.getPatientOwed()
          .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
          .divide(BigDecimal.valueOf(invoiceItem.getQuantity()), 2, RoundingMode.HALF_UP);
      BigDecimal lineRefundInsuranceClaim = invoiceItem.getInsuranceClaimAmount()
          .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
          .divide(BigDecimal.valueOf(invoiceItem.getQuantity()), 2, RoundingMode.HALF_UP);

      batchService.restoreStock(
          invoice.getStore().getId(), invoiceItem.getBatch().getId(), itemRequest.getQuantity());

      returnItems.add(ReturnItem.builder()
          .invoiceItem(invoiceItem)
          .quantity(itemRequest.getQuantity())
          .refundAmount(lineRefundAmount)
          .build());

      totalRefundAmount = totalRefundAmount.add(lineRefundAmount);
      totalRefundPatientOwed = totalRefundPatientOwed.add(lineRefundPatientOwed);
      totalRefundInsuranceClaim = totalRefundInsuranceClaim.add(lineRefundInsuranceClaim);
    }

    Return newReturn = Return.builder()
        .invoice(invoice)
        .reason(request.getReason())
        .totalRefundAmount(totalRefundAmount)
        .build();
    Return savedReturn = returnRepository.save(newReturn);

    for (ReturnItem item : returnItems) {
      item.setParentReturn(savedReturn);
    }
    returnItemRepository.saveAll(returnItems);

    String oldTotalAmount = invoice.getTotalAmount().toPlainString();
    invoice.setTotalAmount(invoice.getTotalAmount().subtract(totalRefundAmount));
    invoice.setPatientOwed(invoice.getPatientOwed().subtract(totalRefundPatientOwed));
    invoice.setInsuranceClaimAmount(
        invoice.getInsuranceClaimAmount().subtract(totalRefundInsuranceClaim));
    Invoice savedInvoice = invoiceRepository.save(invoice);

    auditService.recordAudit("RETURN", "Invoice", String.valueOf(invoiceId), oldTotalAmount,
        savedInvoice.getTotalAmount().toPlainString());

    ReturnDto dto = returnMapper.toDto(savedReturn);
    dto.setItems(returnMapper.toItemDtoList(returnItems));
    return dto;
  }

  @Override
  @Transactional(readOnly = true)
  public ReturnDto findById(Long id) throws ApplicationException {
    Return foundReturn = returnRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_NOT_FOUND, HttpStatus.NOT_FOUND));

    List<ReturnItem> items = returnItemRepository.findByParentReturnId(id);
    ReturnDto dto = returnMapper.toDto(foundReturn);
    dto.setItems(returnMapper.toItemDtoList(items));
    return dto;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ReturnDto> findByInvoiceId(Long invoiceId, Pageable pageable)
      throws ApplicationException {
    return returnRepository.findByInvoiceId(invoiceId, pageable).map(returnMapper::toDto);
  }
}
