package com.mrtripop.transaction.fixture;

import com.mrtripop.clinical.models.db.AuditLedger;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceStatus;
import com.mrtripop.transaction.models.dto.ReconciliationRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class ReconciliationFixture {

  private ReconciliationFixture() {}

  // Shared constants
  public static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final String STORE_NAME = "Main Pharmacy";
  public static final LocalDate REPORT_DATE = LocalDate.of(2026, 5, 5);

  // Period: May 5, 2026 00:00:00 to May 5, 2026 23:59:59 UTC
  public static final Long PERIOD_START = 1746403200000L;
  public static final Long PERIOD_END = 1746489599000L;
  public static final LocalDateTime PERIOD_START_DATETIME = LocalDateTime.of(2026, 5, 5, 0, 0, 0);
  public static final LocalDateTime PERIOD_END_DATETIME = LocalDateTime.of(2026, 5, 5, 23, 59, 59);

  public static final BigDecimal INVOICE_TOTAL_AMOUNT = new BigDecimal("150.00");
  public static final BigDecimal LEDGER_AMOUNT = new BigDecimal("150.00");
  public static final BigDecimal DISCREPANCY_ZERO = BigDecimal.ZERO;

  public static final String ACTION_INVOICE_CREATED = "CREATE";
  public static final String ACTION_INVENTORY_OUT = "INVENTORY_OUT";
  public static final String ENTITY_NAME_INVOICE = "Invoice";
  public static final String ENTITY_ID_INVOICE_1 = "1001";
  public static final String ENTITY_ID_INVOICE_2 = "1002";
  public static final String ENTITY_ID_STOCK_MOVE = "STOCK-2001";

  public static Store defaultStore() {
    return Store.builder()
        .id(STORE_ID)
        .name(STORE_NAME)
        .type(StoreType.PHYSICAL)
        .active(true)
        .build();
  }

  public static ReconciliationRequest validRequest() {
    return ReconciliationRequest.builder()
        .storeId(STORE_ID)
        .reportDate(REPORT_DATE)
        .periodStart(PERIOD_START)
        .periodEnd(PERIOD_END)
        .build();
  }

  public static AuditLedger invoiceCreatedEntry(String entityId, BigDecimal amount) {
    return AuditLedger.builder()
        .id(UUID.randomUUID())
        .timestamp(PERIOD_START_DATETIME.plusHours(10))
        .userId("admin")
        .actionType(ACTION_INVOICE_CREATED)
        .entityName(ENTITY_NAME_INVOICE)
        .entityId(entityId)
        .oldValue(null)
        .newValue(amount.toPlainString())
        .build();
  }

  public static AuditLedger inventoryOutEntry(String entityId) {
    return AuditLedger.builder()
        .id(UUID.randomUUID())
        .timestamp(PERIOD_START_DATETIME.plusHours(10))
        .userId("admin")
        .actionType(ACTION_INVENTORY_OUT)
        .entityName("StockMove")
        .entityId(entityId)
        .oldValue(null)
        .newValue("5")
        .build();
  }

  public static Invoice completedInvoice(UUID storeId, BigDecimal totalAmount) {
    return Invoice.builder()
        .id(1L)
        .store(ReconciliationFixture.defaultStore())
        .status(InvoiceStatus.COMPLETED)
        .totalAmount(totalAmount)
        .patientOwed(totalAmount)
        .insuranceClaimAmount(BigDecimal.ZERO)
        .version(0L)
        .createdAt(PERIOD_START)
        .updatedAt(PERIOD_START)
        .build();
  }
}
