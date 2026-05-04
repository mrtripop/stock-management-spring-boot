package com.mrtripop.clinical.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseStatusCode {
  STORE_NOT_FOUND("CL4001", "Store not found"),
  DUPLICATE_STORE_NAME("CL4002", "Store with this name already exists");

  private final String code;
  private final String message;
}