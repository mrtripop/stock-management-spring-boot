package com.mrtripop.product.constant;

import com.mrtripop.constant.BaseStatusCode;

public enum ErrorCode implements BaseStatusCode {
  GB4041_REQUEST_BODY_IS_NOT_VALID("GB4041", "Request body is not valid"),
  GB4042_QUERY_PARAMETER_IS_NOT_VALID("GB4042", "Query parameter is not valid"),
  GB4043_METHOD_ARGUMENT_TYPE_MISMATCH("GB4043", "Method argument type mismatch"),
  GB4044_GLOBAL_ERROR_IS_OCCURRED("GB4044", "Global error is occurred"),
  PRO1001_CANNOT_GET_ALL_PRODUCTS("PRO1001", "Cannot get all products"),
  PRO1002_CANNOT_CREATE_NEW_PRODUCT("PRO1002", "Cannot create new product"),
  PRO1003_CANNOT_GET_PRODUCT_BY_ID("PRO1003", "Your product ID is not valid"),
  PRO1004_CANNOT_UPDATE_EXISTING_PRODUCT("PRO1004", "Cannot update existing product"),
  PRO1005_CANNOT_DELETE_EXISTING_PRODUCT("PRO1005", "Cannot delete the product"),
  PRO1007_CANNOT_GET_ALL_PRODUCT_HISTORIES("PRO1007", "Cannot get all products histories"),
  PRO1011_CANNOT_GET_PRODUCT_HISTORIES_BY_CODE("PRO1011", "Cannot get products histories by code"),
  PRO1008_CANNOT_CONVERT_PRODUCT_TO_PRODUCT_HISTORY(
      "PRO1008", "Cannot convert product to product history"),
  PRO1009_CANNOT_CONVERT_PRODUCT_TO_PRODUCT_HISTORY(
      "PRO1009", "Cannot convert product DTO to product"),
  PRO1010_CANNOT_CONVERT_PRODUCT_OBJECT_TO_PRODUCT_DTO(
      "PRO1010", "Cannot convert product object to product DTO"),
  PRO4001_USER_NEED_TO_UPLOAD_PRODUCT_CSV_FILE(
      "PRO4001", "CSV content missing, please upload a products CSV file"),
  PRO5001_READ_PRODUCTS_FROM_CSV_FAILED("PRO5001", "Read products from CSV failed"),
  PRO5002_CANNOT_UPDATE_PRODUCTS_FROM_CSV_FILE(
      "PRO5002", "Cannot update products due to internal error");

  private final String code;
  private final String message;

  ErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
