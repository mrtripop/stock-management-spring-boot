package com.mrtripop.transaction.fixture;

import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.Return;
import com.mrtripop.transaction.models.db.ReturnItem;
import com.mrtripop.transaction.models.db.ReturnReason;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnItemRequest;
import java.math.BigDecimal;
import java.util.List;

public final class ReturnFixture {

  private ReturnFixture() {}

  // Reuses InvoiceFixture.validInvoiceItem(invoice): quantity=5, unitPrice=10.00,
  // patientOwed=35.00, insuranceClaimAmount=15.00 — a clean 70/30 split with no rounding.
  public static final Long RETURN_QUANTITY = 2L;
  public static final BigDecimal EXPECTED_REFUND_AMOUNT = new BigDecimal("20.00");
  public static final BigDecimal EXPECTED_REFUND_PATIENT_OWED = new BigDecimal("14.00");
  public static final BigDecimal EXPECTED_REFUND_INSURANCE_CLAIM = new BigDecimal("6.00");
  public static final BigDecimal EXPECTED_NEW_TOTAL_AMOUNT = new BigDecimal("30.00");
  public static final BigDecimal EXPECTED_NEW_PATIENT_OWED = new BigDecimal("21.00");
  public static final BigDecimal EXPECTED_NEW_INSURANCE_CLAIM = new BigDecimal("9.00");

  // A line whose quantity (3) does not evenly divide its money fields, to exercise
  // HALF_UP rounding on a 1-unit return: 10.00*1/3 = 3.33(3)->3.33, 5.00*1/3 = 1.66(6)->1.67.
  public static final Long ROUNDING_ITEM_QUANTITY = 3L;
  public static final BigDecimal ROUNDING_UNIT_PRICE = new BigDecimal("5.00");
  public static final BigDecimal ROUNDING_PATIENT_OWED = new BigDecimal("10.00");
  public static final BigDecimal ROUNDING_INSURANCE_CLAIM = new BigDecimal("5.00");
  public static final Long ROUNDING_RETURN_QUANTITY = 1L;
  public static final BigDecimal EXPECTED_ROUNDED_REFUND_AMOUNT = new BigDecimal("5.00");
  public static final BigDecimal EXPECTED_ROUNDED_PATIENT_OWED = new BigDecimal("3.33");
  public static final BigDecimal EXPECTED_ROUNDED_INSURANCE_CLAIM = new BigDecimal("1.67");

  public static InvoiceItem invoiceItemForRounding(Invoice invoice) {
    return InvoiceItem.builder()
        .id(2L)
        .invoice(invoice)
        .brand(InvoiceFixture.validBrand())
        .batch(InvoiceFixture.validBatch())
        .quantity(ROUNDING_ITEM_QUANTITY)
        .unitPrice(ROUNDING_UNIT_PRICE)
        .lineTotal(new BigDecimal("15.00"))
        .patientOwed(ROUNDING_PATIENT_OWED)
        .insuranceClaimAmount(ROUNDING_INSURANCE_CLAIM)
        .insuranceCoveragePercent(33)
        .build();
  }

  public static ReturnItemRequest validItemRequest(Long invoiceItemId) {
    return ReturnItemRequest.builder().invoiceItemId(invoiceItemId).quantity(RETURN_QUANTITY).build();
  }

  public static CreateReturnRequest validCreateRequest(Long invoiceItemId) {
    return CreateReturnRequest.builder()
        .reason(ReturnReason.CUSTOMER_CHANGED_MIND)
        .items(List.of(validItemRequest(invoiceItemId)))
        .build();
  }

  public static Return validReturn(Invoice invoice) {
    return Return.builder()
        .id(1L)
        .invoice(invoice)
        .reason(ReturnReason.CUSTOMER_CHANGED_MIND)
        .totalRefundAmount(EXPECTED_REFUND_AMOUNT)
        .build();
  }

  public static ReturnItem validReturnItem(Return parentReturn, InvoiceItem invoiceItem) {
    return ReturnItem.builder()
        .id(1L)
        .parentReturn(parentReturn)
        .invoiceItem(invoiceItem)
        .quantity(RETURN_QUANTITY)
        .refundAmount(EXPECTED_REFUND_AMOUNT)
        .build();
  }
}
