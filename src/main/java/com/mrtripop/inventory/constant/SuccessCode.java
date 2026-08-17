package com.mrtripop.inventory.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseStatusCode {
  INV2003_RECALL_BATCH_IS_SUCCESS("INV2003", "Batch recalled successfully"),
  INV2004_RECONCILE_ALL_SUCCESS("INV2004", "Stock reconciliation triggered successfully");

  private final String code;
  private final String message;
}