package com.mrtripop.constant;

import lombok.Getter;

/**
 * Global success codes. Domain-specific success codes have been migrated to their respective packages:
 * <ul>
 *   <li>{@link com.mrtripop.inventory.constant.SuccessCode} — inventory module</li>
 *   <li>{@link com.mrtripop.clinical.constant.SuccessCode} — clinical module</li>
 *   <li>{@link com.mrtripop.users.constant.SuccessCode} — users module</li>
 * </ul>
 */
@Getter
public enum SuccessCode implements BaseStatusCode {

  // All codes migrated to domain-specific packages. Add new codes there.

  ;

  private final String code;
  private final String message;

  SuccessCode(String code, String message) {
    this.code = code;
    this.message = message;
  }
}