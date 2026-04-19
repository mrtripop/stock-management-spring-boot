package com.mrtripop.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomControllerAdvice {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
    String stackTrace = ExceptionUtils.getStackTrace(e);
    HttpStatus status = HttpStatus.NOT_FOUND;
    ErrorResponse error =
        new ErrorResponse.ErrorResponseBuilder(status.toString(), status.value(), e.getMessage())
            .withStacktrace(stackTrace)
            .withTimestamp()
            .build();
    return new ResponseEntity<>(error, status);
  }

  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<ErrorResponse> handleNullPointerExceptions(Exception e) {
    String stackTrace = ExceptionUtils.getStackTrace(e);
    HttpStatus status = HttpStatus.NOT_FOUND; // 404
    ErrorResponse error =
        new ErrorResponse.ErrorResponseBuilder(status.toString(), status.value(), e.getMessage())
            .withStacktrace(stackTrace)
            .withTimestamp()
            .build();
    return new ResponseEntity<>(error, status);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleExceptions(Exception e) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
    String stackTrace = ExceptionUtils.getStackTrace(e);
    ErrorResponse error =
        new ErrorResponse.ErrorResponseBuilder(
                status.toString(), status.value(), status.getReasonPhrase())
            .withStacktrace(stackTrace)
            .withTimestamp()
            .build();
    return new ResponseEntity<>(error, status);
  }
}
