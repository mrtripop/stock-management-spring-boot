package com.mrtripop.inventory.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseStatusCode {
  BATCH_NOT_FOUND("INV4001", "Batch not found"),
  BATCH_ALREADY_EXISTS("INV4002", "Batch with this number already exists for this brand"),
  BARCODE_NOT_RECOGNIZED("INV4003", "Barcode not recognized in Master Catalog"),
  INVALID_EXPIRY_DATE("INV4004", "Expiry date must be in the future"),
  INSUFFICIENT_QUANTITY("INV4005", "Insufficient stock quantity"),
  STOCK_NOT_FOUND("INV4006", "Store stock record not found"),
  STORE_NOT_FOUND("INV4007", "Store not found"),
  NO_AVAILABLE_BATCHES("INV4008", "No available batches for this product in this store"),
  EXPIRED_BATCH_DEDUCTION("INV4009", "Cannot deduct from expired batch");

  private final String code;
  private final String message;
}