package com.mrtripop.transaction.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.transaction.constant.ErrorCode;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.InvoiceStatus;
import com.mrtripop.transaction.models.dto.ReceiptDto;
import com.mrtripop.transaction.models.dto.ReceiptItemDto;
import com.mrtripop.transaction.repository.InvoiceItemRepository;
import com.mrtripop.transaction.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReceiptServiceImpl")
class ReceiptServiceImplTest {

  private static final Long INVOICE_ID = 1L;
  private static final UUID STORE_ID = UUID.randomUUID();
  private static final UUID MOLECULE_ID = UUID.randomUUID();
  private static final UUID BRAND_ID = UUID.randomUUID();
  private static final String STORE_NAME = "Main Pharmacy";
  private static final String BRAND_NAME = "Paracetamol 500mg";
  private static final String BATCH_NUMBER = "BATCH-001";
  private static final String DOSAGE_INSTRUCTIONS = "Take 1 tablet every 8 hours with water.";
  private static final String SAFETY_WARNINGS = "Do not exceed 4 tablets in 24 hours.";
  private static final String DIGITAL_LEAFLET_BASE_URL =
      "https://leaflet.pharmacy.example.com/molecule/";

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceItemRepository invoiceItemRepository;

  @InjectMocks private ReceiptServiceImpl receiptService;

  @Nested
  @DisplayName("generateReceipt")
  class GenerateReceipt {

    @Test
    @DisplayName("should generate receipt with clinical info when invoice and molecule exist")
    void generateReceipt_withClinicalInfo_returnsReceiptWithDosageAndWarnings()
        throws ApplicationException {
      // Arrange
      Molecule molecule =
          Molecule.builder()
              .id(MOLECULE_ID)
              .genericName("Paracetamol")
              .dosageInstructions(DOSAGE_INSTRUCTIONS)
              .safetyWarnings(SAFETY_WARNINGS)
              .build();

      Brand brand =
          Brand.builder().id(BRAND_ID).brandName(BRAND_NAME).molecule(molecule).build();

      Store store = Store.builder().id(STORE_ID).name(STORE_NAME).type(StoreType.PHYSICAL).build();

      Batch batch = Batch.builder().id(1L).batchNumber(BATCH_NUMBER).brand(brand).build();

      Invoice invoice =
          Invoice.builder()
              .id(INVOICE_ID)
              .store(store)
              .status(InvoiceStatus.COMPLETED)
              .totalAmount(new BigDecimal("25.00"))
              .patientOwed(new BigDecimal("10.00"))
              .insuranceClaimAmount(new BigDecimal("15.00"))
              .build();

      InvoiceItem invoiceItem =
          InvoiceItem.builder()
              .id(1L)
              .invoice(invoice)
              .brand(brand)
              .batch(batch)
              .quantity(2L)
              .unitPrice(new BigDecimal("12.50"))
              .lineTotal(new BigDecimal("25.00"))
              .patientOwed(new BigDecimal("10.00"))
              .insuranceClaimAmount(new BigDecimal("15.00"))
              .insuranceCoveragePercent(60)
              .build();

      when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(invoiceItem));

      // Act
      ReceiptDto result = receiptService.generateReceipt(INVOICE_ID);

      // Assert
      assertNotNull(result);
      assertEquals(INVOICE_ID, result.getInvoiceId());
      assertEquals(STORE_NAME, result.getStoreName());
      assertEquals(InvoiceStatus.COMPLETED.name(), result.getStatus());
      assertEquals(new BigDecimal("25.00"), result.getTotalAmount());
      assertEquals(new BigDecimal("10.00"), result.getPatientOwed());
      assertEquals(new BigDecimal("15.00"), result.getInsuranceClaimAmount());
      assertNotNull(result.getGeneratedAt());
      assertNotNull(result.getItems());
      assertEquals(1, result.getItems().size());

      ReceiptItemDto itemDto = result.getItems().get(0);
      assertEquals(BRAND_NAME, itemDto.getBrandName());
      assertEquals(BATCH_NUMBER, itemDto.getBatchNumber());
      assertEquals(2L, itemDto.getQuantity());
      assertEquals(new BigDecimal("12.50"), itemDto.getUnitPrice());
      assertEquals(new BigDecimal("25.00"), itemDto.getLineTotal());
      assertEquals(DOSAGE_INSTRUCTIONS, itemDto.getDosageInstructions());
      assertEquals(SAFETY_WARNINGS, itemDto.getSafetyWarnings());
      assertEquals(DIGITAL_LEAFLET_BASE_URL + MOLECULE_ID, itemDto.getDigitalLeafletUrl());
    }

    @Test
    @DisplayName("should generate receipt with empty clinical fields when molecule has no dosage or warnings")
    void generateReceipt_withoutClinicalInfo_returnsReceiptWithEmptyClinicalFields()
        throws ApplicationException {
      // Arrange
      Molecule molecule =
          Molecule.builder().id(MOLECULE_ID).genericName("Ibuprofen").build();

      Brand brand =
          Brand.builder().id(BRAND_ID).brandName("Ibuprofen 400mg").molecule(molecule).build();

      Store store = Store.builder().id(STORE_ID).name(STORE_NAME).type(StoreType.PHYSICAL).build();

      Batch batch = Batch.builder().id(1L).batchNumber(BATCH_NUMBER).brand(brand).build();

      Invoice invoice =
          Invoice.builder()
              .id(INVOICE_ID)
              .store(store)
              .status(InvoiceStatus.COMPLETED)
              .totalAmount(new BigDecimal("15.00"))
              .patientOwed(new BigDecimal("15.00"))
              .insuranceClaimAmount(BigDecimal.ZERO)
              .build();

      InvoiceItem invoiceItem =
          InvoiceItem.builder()
              .id(1L)
              .invoice(invoice)
              .brand(brand)
              .batch(batch)
              .quantity(1L)
              .unitPrice(new BigDecimal("15.00"))
              .lineTotal(new BigDecimal("15.00"))
              .patientOwed(new BigDecimal("15.00"))
              .insuranceClaimAmount(BigDecimal.ZERO)
              .insuranceCoveragePercent(0)
              .build();

      when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(invoiceItem));

      // Act
      ReceiptDto result = receiptService.generateReceipt(INVOICE_ID);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.getItems().size());

      ReceiptItemDto itemDto = result.getItems().get(0);
      assertNull(itemDto.getDosageInstructions());
      assertNull(itemDto.getSafetyWarnings());
      assertEquals(DIGITAL_LEAFLET_BASE_URL + MOLECULE_ID, itemDto.getDigitalLeafletUrl());
    }

    @Test
    @DisplayName("should throw RECEIPT_NOT_FOUND with 404 when invoice does not exist")
    void generateReceipt_invoiceNotFound_throwsReceiptNotFound() {
      // Arrange
      when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(
              ApplicationException.class, () -> receiptService.generateReceipt(999L));
      assertEquals(ErrorCode.RECEIPT_NOT_FOUND, ex.getErrorCode());
      assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    @DisplayName("should generate receipt with empty items list when invoice has no items")
    void generateReceipt_noItems_returnsReceiptWithEmptyItems() throws ApplicationException {
      // Arrange
      Store store = Store.builder().id(STORE_ID).name(STORE_NAME).type(StoreType.PHYSICAL).build();

      Invoice invoice =
          Invoice.builder()
              .id(INVOICE_ID)
              .store(store)
              .status(InvoiceStatus.COMPLETED)
              .totalAmount(BigDecimal.ZERO)
              .patientOwed(BigDecimal.ZERO)
              .insuranceClaimAmount(BigDecimal.ZERO)
              .build();

      when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of());

      // Act
      ReceiptDto result = receiptService.generateReceipt(INVOICE_ID);

      // Assert
      assertNotNull(result);
      assertEquals(INVOICE_ID, result.getInvoiceId());
      assertNotNull(result.getItems());
      assertTrue(result.getItems().isEmpty());
    }
  }
}
