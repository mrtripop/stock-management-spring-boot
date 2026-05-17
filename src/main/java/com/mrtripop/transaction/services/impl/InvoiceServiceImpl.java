package com.mrtripop.transaction.services.impl;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.BatchService;
import com.mrtripop.transaction.component.InvoiceMapper;
import com.mrtripop.transaction.constant.ErrorCode;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.InvoiceStatus;
import com.mrtripop.transaction.models.dto.CreateInvoiceRequest;
import com.mrtripop.transaction.models.dto.InvoiceDto;
import com.mrtripop.transaction.models.dto.InvoiceItemRequest;
import com.mrtripop.transaction.repository.InvoiceItemRepository;
import com.mrtripop.transaction.repository.InvoiceRepository;
import com.mrtripop.transaction.services.InvoiceService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
public class InvoiceServiceImpl implements InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final InvoiceItemRepository invoiceItemRepository;
  private final StoreRepository storeRepository;
  private final BrandRepository brandRepository;
  private final BatchRepository batchRepository;
  private final StoreProductRepository storeProductRepository;
  private final StoreStockRepository storeStockRepository;
  private final InvoiceMapper invoiceMapper;
  private final AuditService auditService;
  private final BatchService batchService;

  @Override
  @Transactional(readOnly = true)
  public Page<InvoiceDto> findAll(UUID storeId, Pageable pageable) throws ApplicationException {
    return invoiceRepository.findByStoreId(storeId, pageable).map(invoiceMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public InvoiceDto findById(Long id) throws ApplicationException {
    Invoice invoice = invoiceRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));

    InvoiceDto dto = invoiceMapper.toDto(invoice);
    List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
    dto.setItems(invoiceMapper.toItemDtoList(items));
    return dto;
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public InvoiceDto create(CreateInvoiceRequest request) throws ApplicationException {
    Store store = storeRepository.findById(request.getStoreId())
        .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND, HttpStatus.NOT_FOUND));

    List<InvoiceItem> invoiceItems = new ArrayList<>();
    BigDecimal totalAmount = BigDecimal.ZERO;
    BigDecimal totalPatientOwed = BigDecimal.ZERO;
    BigDecimal totalInsuranceClaim = BigDecimal.ZERO;

    for (InvoiceItemRequest itemRequest : request.getItems()) {
      Brand brand = brandRepository.findById(itemRequest.getBrandId())
          .orElseThrow(() -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND, HttpStatus.NOT_FOUND));

      Batch batch = batchRepository.findById(itemRequest.getBatchId())
          .orElseThrow(() -> new ApplicationException(ErrorCode.BATCH_NOT_FOUND, HttpStatus.NOT_FOUND));

      Optional<StoreStock> storeStock =
          storeStockRepository.findByStoreIdAndBatchId(request.getStoreId(), itemRequest.getBatchId());
      if (storeStock.isEmpty()
          || storeStock.get().getQuantity() == null
          || storeStock.get().getQuantity() < itemRequest.getQuantity()) {
        throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK, HttpStatus.BAD_REQUEST);
      }

      StoreProduct storeProduct = storeProductRepository
          .findByStoreIdAndBrandId(request.getStoreId(), itemRequest.getBrandId())
          .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_INVOICE_ITEM, HttpStatus.BAD_REQUEST));

      if (storeProduct.getPrice() == null) {
        throw new ApplicationException(ErrorCode.INVALID_INVOICE_ITEM, HttpStatus.BAD_REQUEST);
      }
      BigDecimal unitPrice = storeProduct.getPrice();
      BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
      int coveragePercent = itemRequest.getInsuranceCoveragePercent() != null
          ? itemRequest.getInsuranceCoveragePercent()
          : 0;
      BigDecimal insuranceClaim = lineTotal
          .multiply(BigDecimal.valueOf(coveragePercent))
          .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      BigDecimal patientOwed = lineTotal.subtract(insuranceClaim);

      InvoiceItem item = InvoiceItem.builder()
          .brand(brand)
          .batch(batch)
          .quantity(itemRequest.getQuantity())
          .unitPrice(unitPrice)
          .lineTotal(lineTotal)
          .patientOwed(patientOwed)
          .insuranceClaimAmount(insuranceClaim)
          .insuranceCoveragePercent(coveragePercent)
          .build();

      invoiceItems.add(item);
      totalAmount = totalAmount.add(lineTotal);
      totalPatientOwed = totalPatientOwed.add(patientOwed);
      totalInsuranceClaim = totalInsuranceClaim.add(insuranceClaim);
    }

    Invoice invoice = Invoice.builder()
        .store(store)
        .status(InvoiceStatus.PENDING)
        .totalAmount(totalAmount)
        .patientOwed(totalPatientOwed)
        .insuranceClaimAmount(totalInsuranceClaim)
        .build();

    Invoice savedInvoice = invoiceRepository.save(invoice);

    for (InvoiceItem item : invoiceItems) {
      item.setInvoice(savedInvoice);
    }
    invoiceItemRepository.saveAll(invoiceItems);

    recordAudit("CREATE", "Invoice", String.valueOf(savedInvoice.getId()), null,
        savedInvoice.getTotalAmount().toPlainString());

    InvoiceDto dto = invoiceMapper.toDto(savedInvoice);
    dto.setItems(invoiceMapper.toItemDtoList(invoiceItems));
    return dto;
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public InvoiceDto complete(Long id) throws ApplicationException {
    Invoice invoice = invoiceRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));

    if (invoice.getStatus() != InvoiceStatus.PENDING) {
      if (invoice.getStatus() == InvoiceStatus.COMPLETED) {
        throw new ApplicationException(ErrorCode.INVOICE_ALREADY_COMPLETED, HttpStatus.CONFLICT);
      }
      throw new ApplicationException(ErrorCode.INVOICE_ALREADY_VOIDED, HttpStatus.CONFLICT);
    }

    List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
    for (InvoiceItem item : items) {
      batchService.deductStockByBatch(
          invoice.getStore().getId(), item.getBatch().getId(), item.getQuantity());
    }

    String oldValue = invoice.getStatus().name();
    invoice.setStatus(InvoiceStatus.COMPLETED);
    Invoice savedInvoice = invoiceRepository.save(invoice);

    recordAudit("COMPLETE", "Invoice", String.valueOf(id), oldValue,
        savedInvoice.getTotalAmount().toPlainString());

    InvoiceDto dto = invoiceMapper.toDto(savedInvoice);
    dto.setItems(invoiceMapper.toItemDtoList(items));
    return dto;
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public InvoiceDto voidInvoice(Long id) throws ApplicationException {
    Invoice invoice = invoiceRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));

    if (invoice.getStatus() == InvoiceStatus.VOIDED) {
      throw new ApplicationException(ErrorCode.INVOICE_ALREADY_VOIDED, HttpStatus.CONFLICT);
    }

    if (invoice.getStatus() == InvoiceStatus.COMPLETED) {
      List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
      for (InvoiceItem item : items) {
        batchService.restoreStock(
            invoice.getStore().getId(), item.getBatch().getId(), item.getQuantity());
      }
    }

    String oldValue = invoice.getStatus().name();
    invoice.setStatus(InvoiceStatus.VOIDED);
    Invoice savedInvoice = invoiceRepository.save(invoice);

    recordAudit("VOID", "Invoice", String.valueOf(id), oldValue,
        savedInvoice.getTotalAmount().toPlainString());

    InvoiceDto dto = invoiceMapper.toDto(savedInvoice);
    List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
    dto.setItems(invoiceMapper.toItemDtoList(items));
    return dto;
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public InvoiceDto dispense(CreateInvoiceRequest request) throws ApplicationException {
    InvoiceDto created = create(request);
    return complete(created.getId());
  }

  private void recordAudit(String actionType, String entityName, String entityId, String oldValue,
      String newValue) {
    auditService.recordAudit(actionType, entityName, entityId, oldValue, newValue);
  }
}
