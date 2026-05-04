package com.mrtripop.transaction.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseStatusCode {
  INVOICE_NOT_FOUND("TXN4001", "Invoice not found"),
  INVALID_INVOICE_ITEM("TXN4002", "Invoice item is invalid"),
  INSUFFICIENT_STOCK("TXN4003", "Insufficient stock for invoice item"),
  INVOICE_ALREADY_COMPLETED("TXN4004", "Invoice is already completed"),
  INVOICE_ALREADY_VOIDED("TXN4005", "Invoice is already voided"),
  STORE_NOT_FOUND("TXN4006", "Store not found"),
  BRAND_NOT_FOUND("TXN4007", "Brand not found"),
  BATCH_NOT_FOUND("TXN4008", "Batch not found"),
  RECEIPT_NOT_FOUND("TXN4009", "Receipt not found"),
  INVALID_RECONCILIATION_PERIOD("TXN4010", "Period end must be greater than or equal to period start"),
  RECEIPT_NOT_AVAILABLE("TXN4011", "Receipt is not available for this invoice status");

  private final String code;
  private final String message;
}
