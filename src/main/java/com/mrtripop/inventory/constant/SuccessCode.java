package com.mrtripop.inventory.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseStatusCode {
  // Batch & stock operations
  INV2003_RECALL_BATCH_IS_SUCCESS("INV2003", "Batch recalled successfully"),
  INV2004_RECONCILE_ALL_SUCCESS("INV2004", "Stock reconciliation triggered successfully"),
  INV2005_GET_RECONCILE_STATUS_SUCCESS("INV2005", "Reconciliation status retrieved successfully"),
  INV2006_STOCK_IN_SUCCESS("INV2006", "Batch and store stock created successfully"),
  INV2007_STOCK_DEDUCT_SUCCESS("INV2007", "Stock deducted successfully"),
  INV2008_GET_BATCH_SUCCESS("INV2008", "Batch retrieved successfully"),
  INV2009_GET_BATCHES_SUCCESS("INV2009", "Batches retrieved successfully"),
  INV2010_GET_STORE_STOCK_SUCCESS("INV2010", "Store stock retrieved successfully"),
  INV2011_RESOLVE_BARCODE_SUCCESS("INV2011", "Barcode resolved successfully"),
  // Action queue operations
  INV2012_GET_TASKS_SUCCESS("INV2012", "Tasks retrieved successfully"),
  INV2013_GET_TASK_BY_ID_SUCCESS("INV2013", "Task retrieved successfully"),
  INV2014_ACKNOWLEDGE_TASK_SUCCESS("INV2014", "Task acknowledged successfully"),
  INV2015_RESOLVE_TASK_SUCCESS("INV2015", "Task resolved successfully"),
  INV2016_SCAN_TRIGGERED_SUCCESS("INV2016", "Scan completed successfully");

  private final String code;
  private final String message;
}