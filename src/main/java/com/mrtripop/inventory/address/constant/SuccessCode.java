package com.mrtripop.inventory.address.constant;

import com.mrtripop.inventory.constant.BaseStatusCode;

public enum SuccessCode implements BaseStatusCode {
  SUCCESS("200", "SUCCESS");

  private final String code;
  private final String message;

  SuccessCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  @Override
  public String getCode() {
    return this.code;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
