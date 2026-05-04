package com.mrtripop.transaction.services.impl;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.transaction.constant.ErrorCode;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceStatus;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.dto.ReceiptDto;
import com.mrtripop.transaction.models.dto.ReceiptItemDto;
import com.mrtripop.transaction.repository.InvoiceItemRepository;
import com.mrtripop.transaction.repository.InvoiceRepository;
import com.mrtripop.transaction.services.ReceiptService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

  private static final String DIGITAL_LEAFLET_URL_FORMAT =
      "https://leaflet.pharmacy.example.com/molecule/%s";

  private final InvoiceRepository invoiceRepository;
  private final InvoiceItemRepository invoiceItemRepository;

  @Override
  @Transactional(readOnly = true)
  public ReceiptDto generateReceipt(Long invoiceId) throws ApplicationException {
    log.info("Generating receipt for invoice id: {}", invoiceId);

    Invoice invoice =
        invoiceRepository
            .findWithStoreById(invoiceId)
            .orElseThrow(
                () -> new ApplicationException(ErrorCode.RECEIPT_NOT_FOUND, HttpStatus.NOT_FOUND));

    if (invoice.getStatus() != InvoiceStatus.COMPLETED) {
      throw new ApplicationException(ErrorCode.RECEIPT_NOT_AVAILABLE, HttpStatus.BAD_REQUEST);
    }

    List<InvoiceItem> invoiceItems = invoiceItemRepository.findWithDetailsByInvoiceId(invoiceId);
    List<ReceiptItemDto> receiptItems = invoiceItems.stream().map(this::buildReceiptItem).toList();

    return ReceiptDto.builder()
        .invoiceId(invoice.getId())
        .storeName(invoice.getStore().getName())
        .status(invoice.getStatus().name())
        .totalAmount(invoice.getTotalAmount())
        .patientOwed(invoice.getPatientOwed())
        .insuranceClaimAmount(invoice.getInsuranceClaimAmount())
        .items(receiptItems)
        .generatedAt(LocalDateTime.now())
        .build();
  }

  private ReceiptItemDto buildReceiptItem(InvoiceItem item) {
    Brand brand = item.getBrand();
    Molecule molecule = brand.getMolecule();

    String dosageInstructions = null;
    String safetyWarnings = null;
    String digitalLeafletUrl = null;

    if (molecule != null) {
      dosageInstructions = molecule.getDosageInstructions();
      safetyWarnings = molecule.getSafetyWarnings();
      digitalLeafletUrl = String.format(DIGITAL_LEAFLET_URL_FORMAT, molecule.getId());
    }

    return ReceiptItemDto.builder()
        .brandName(brand.getBrandName())
        .batchNumber(item.getBatch().getBatchNumber())
        .quantity(item.getQuantity())
        .unitPrice(item.getUnitPrice())
        .lineTotal(item.getLineTotal())
        .patientOwed(item.getPatientOwed())
        .insuranceClaimAmount(item.getInsuranceClaimAmount())
        .dosageInstructions(dosageInstructions)
        .safetyWarnings(safetyWarnings)
        .digitalLeafletUrl(digitalLeafletUrl)
        .build();
  }
}
