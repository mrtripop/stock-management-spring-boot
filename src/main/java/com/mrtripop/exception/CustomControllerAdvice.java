package com.mrtripop.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
    ResponseStatus responseStatus = e.getClass().getAnnotation(ResponseStatus.class);
    HttpStatus status =
        responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    String message =
        responseStatus != null
            ? (e.getMessage() != null ? e.getMessage() : status.getReasonPhrase())
            : status.getReasonPhrase();
    String stackTrace = ExceptionUtils.getStackTrace(e);
    ErrorResponse error =
        new ErrorResponse.ErrorResponseBuilder(status.toString(), status.value(), message)
            .withStacktrace(stackTrace)
            .withTimestamp()
            .build();
    return new ResponseEntity<>(error, status);
  }
}
