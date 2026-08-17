package com.mrtripop.inventory.constant;

/**
 * Constants for audit action types used across the inventory module.
 * Replace hardcoded audit action strings with these constants for consistency and maintainability.
 */
public final class AuditAction {

  private AuditAction() {
    // Prevent instantiation
  }

  // Stock movement actions
  public static final String INVENTORY_IN = "INVENTORY_IN";
  public static final String INVENTORY_OUT = "INVENTORY_OUT";
  public static final String CONTROLLED_SUBSTANCE_SIGNED = "CONTROLLED_SUBSTANCE_SIGNED";

  // Action queue actions
  public static final String ACTION_QUEUE_ACKNOWLEDGED = "ACTION_QUEUE_ACKNOWLEDGED";
  public static final String ACTION_QUEUE_RESOLVED = "ACTION_QUEUE_RESOLVED";
  public static final String ACTION_QUEUE_CREATED = "ACTION_QUEUE_CREATED";
  public static final String ACTION_QUEUE_UPDATED = "ACTION_QUEUE_UPDATED";

  // Compliance actions
  public static final String COMPLIANCE_BATCH_RECALLED = "COMPLIANCE_BATCH_RECALLED";
  public static final String COMPLIANCE_RECALL_ALERT_CREATED = "COMPLIANCE_RECALL_ALERT_CREATED";

  // Entity type names (used as the second parameter to auditService.recordAudit)
  public static final String ENTITY_BATCH = "Batch";
  public static final String ENTITY_STORE_STOCK = "StoreStock";
  public static final String ENTITY_TASK = "Task";
  public static final String ENTITY_STOCK_DEDUCTION = "StockDeduction";
}