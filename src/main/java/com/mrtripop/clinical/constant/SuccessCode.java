package com.mrtripop.clinical.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseStatusCode {
  CL2001_GET_ALL_STORES_IS_SUCCESS("CL2001", "Get all stores is success"),
  CL2002_GET_STORE_BY_ID_IS_SUCCESS("CL2002", "Get store by ID is success"),
  CL2003_CREATE_STORE_IS_SUCCESS("CL2003", "Create store is success"),
  CL2004_UPDATE_STORE_IS_SUCCESS("CL2004", "Update store is success"),
  CL2005_DELETE_STORE_IS_SUCCESS("CL2005", "Delete store is success");

  private final String code;
  private final String message;
}