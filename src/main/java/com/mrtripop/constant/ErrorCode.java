package com.mrtripop.constant;

import lombok.Getter;

/**
 * Global error codes. Domain-specific error codes have been migrated to their respective packages:
 * <ul>
 *   <li>{@link com.mrtripop.inventory.constant.ErrorCode} — inventory module</li>
 *   <li>{@link com.mrtripop.clinical.constant.ErrorCode} — clinical module</li>
 *   <li>{@link com.mrtripop.users.constant.ErrorCode} — users module</li>
 *   <li>{@link com.mrtripop.product.constant.ErrorCode} — product module</li>
 *   <li>{@link com.mrtripop.transaction.constant.ErrorCode} — transaction module</li>
 *   <li>{@link com.mrtripop.location.constant.ErrorCode} — location module</li>
 * </ul>
 */
@Getter
public enum ErrorCode implements BaseStatusCode {

  // All codes migrated to domain-specific packages. Add new codes there.

  ;

  private final String code;
  private final String message;

  ErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }
}