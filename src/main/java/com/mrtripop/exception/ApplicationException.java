package com.mrtripop.exception;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {

  private final BaseStatusCode errorCode;
  private final HttpStatus httpStatus;

  public ApplicationException(BaseStatusCode errorCode, HttpStatus httpStatus) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }
}
