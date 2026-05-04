package com.mrtripop.transaction.fixture;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.InvoiceStatus;
import com.mrtripop.transaction.models.dto.CreateInvoiceRequest;
import com.mrtripop.transaction.models.dto.InvoiceItemRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class InvoiceFixture {

  private InvoiceFixture() {}

  public static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final String STORE_NAME = "Main Pharmacy";
  public static final UUID BRAND_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
  public static final String BRAND_NAME = "Tylenol";
  public static final UUID MOLECULE_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
  public static final Long BATCH_ID = 1L;
  public static final String BATCH_NUMBER = "BATCH-001";
  public static final int VALID_INSURANCE_PERCENT = 30;
  public static final Long VALID_QUANTITY = 5L;
  public static final BigDecimal UNIT_PRICE = new BigDecimal("10.00");

  public static Store validStore() {
    return Store.builder().id(STORE_ID).name(STORE_NAME).type(StoreType.PHYSICAL).active(true).build();
  }

  public static Brand validBrand() {
    Molecule molecule = Molecule.builder().id(MOLECULE_ID).genericName("Paracetamol").build();
    return Brand.builder().id(BRAND_ID).brandName(BRAND_NAME).molecule(molecule).build();
  }

  public static Batch validBatch() {
    return Batch.builder()
        .id(BATCH_ID)
        .brand(validBrand())
        .batchNumber(BATCH_NUMBER)
        .expiryDate(LocalDate.now().plusYears(1))
        .quantity(100L)
        .status(BatchStatus.AVAILABLE)
        .build();
  }

  public static StoreProduct validStoreProduct() {
    return StoreProduct.builder()
        .id(UUID.randomUUID())
        .store(validStore())
        .brand(validBrand())
        .price(UNIT_PRICE)
        .isActive(true)
        .build();
  }

  public static StoreStock validStoreStock() {
    return StoreStock.builder()
        .id(1L)
        .store(validStore())
        .batch(validBatch())
        .quantity(100L)
        .build();
  }

  public static InvoiceItemRequest validItemRequest() {
    return InvoiceItemRequest.builder()
        .brandId(BRAND_ID)
        .batchId(BATCH_ID)
        .quantity(VALID_QUANTITY)
        .insuranceCoveragePercent(VALID_INSURANCE_PERCENT)
        .build();
  }

  public static InvoiceItemRequest itemRequestNoInsurance() {
    return InvoiceItemRequest.builder()
        .brandId(BRAND_ID)
        .batchId(BATCH_ID)
        .quantity(VALID_QUANTITY)
        .insuranceCoveragePercent(0)
        .build();
  }

  public static InvoiceItemRequest itemRequestFullInsurance() {
    return InvoiceItemRequest.builder()
        .brandId(BRAND_ID)
        .batchId(BATCH_ID)
        .quantity(VALID_QUANTITY)
        .insuranceCoveragePercent(100)
        .build();
  }

  public static CreateInvoiceRequest validCreateRequest() {
    return CreateInvoiceRequest.builder().storeId(STORE_ID).items(List.of(validItemRequest())).build();
  }

  public static CreateInvoiceRequest createRequestNoInsurance() {
    return CreateInvoiceRequest.builder().storeId(STORE_ID).items(List.of(itemRequestNoInsurance())).build();
  }

  public static CreateInvoiceRequest createRequestFullInsurance() {
    return CreateInvoiceRequest.builder()
        .storeId(STORE_ID)
        .items(List.of(itemRequestFullInsurance()))
        .build();
  }

  public static Invoice pendingInvoice() {
    return Invoice.builder()
        .id(1L)
        .store(validStore())
        .status(InvoiceStatus.PENDING)
        .totalAmount(new BigDecimal("50.00"))
        .patientOwed(new BigDecimal("35.00"))
        .insuranceClaimAmount(new BigDecimal("15.00"))
        .version(0L)
        .build();
  }

  public static Invoice completedInvoice() {
    return Invoice.builder()
        .id(1L)
        .store(validStore())
        .status(InvoiceStatus.COMPLETED)
        .totalAmount(new BigDecimal("50.00"))
        .patientOwed(new BigDecimal("35.00"))
        .insuranceClaimAmount(new BigDecimal("15.00"))
        .version(0L)
        .build();
  }

  public static Invoice voidedInvoice() {
    return Invoice.builder()
        .id(1L)
        .store(validStore())
        .status(InvoiceStatus.VOIDED)
        .totalAmount(new BigDecimal("50.00"))
        .patientOwed(new BigDecimal("35.00"))
        .insuranceClaimAmount(new BigDecimal("15.00"))
        .version(0L)
        .build();
  }

  public static InvoiceItem validInvoiceItem(Invoice invoice) {
    return InvoiceItem.builder()
        .id(1L)
        .invoice(invoice)
        .brand(validBrand())
        .batch(validBatch())
        .quantity(VALID_QUANTITY)
        .unitPrice(UNIT_PRICE)
        .lineTotal(new BigDecimal("50.00"))
        .patientOwed(new BigDecimal("35.00"))
        .insuranceClaimAmount(new BigDecimal("15.00"))
        .insuranceCoveragePercent(VALID_INSURANCE_PERCENT)
        .build();
  }
}
