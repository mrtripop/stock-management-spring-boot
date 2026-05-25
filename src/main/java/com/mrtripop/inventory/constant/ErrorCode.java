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
  EXPIRED_BATCH_DEDUCTION("INV4009", "Cannot deduct from expired batch"),
  UNIT_CONVERSION_NOT_FOUND("INV4010", "No unit conversion found for this product and unit"),
  INVALID_UNIT_CONVERSION("INV4011", "Unit conversion ratio must be at least 2"),
  TASK_NOT_FOUND("INV4012", "Task not found"),
  TASK_ALREADY_RESOLVED("INV4013", "Task is already resolved"),
  INVALID_TASK_STATUS("INV4014", "Invalid task status for this operation"),
  CONTROLLED_SUBSTANCE_REQUIRES_SIGNATURE("INV4015", "Controlled substance requires pharmacist digital signature"),
  INVALID_DIGITAL_SIGNATURE("INV4016", "Invalid digital signature: license number or signature payload is malformed"),
  SIGNATURE_VERIFICATION_FAILED("INV4017", "Digital signature verification failed"),
  MESH_SEARCH_REQUIRES_PARAM("INV4018", "At least one search parameter (moleculeId or genericName) is required"),
  MOLECULE_NOT_FOUND("INV4019", "Molecule not found"),
  BATCH_NOT_RECALLABLE("INV4020", "Batch not found or cannot be recalled"),
  BATCH_ALREADY_RECALLED("INV4021", "Batch is already recalled"),
  BATCH_ALREADY_QUARANTINED("INV4022", "Batch is already quarantined"),
  STOCK_NOT_FOUND_FOR_BATCH("INV4023", "Store stock not found for the specified batch"),
  INSUFFICIENT_BATCH_QUANTITY("INV4024", "Insufficient quantity in batch for deduction");

  private final String code;
  private final String message;
}