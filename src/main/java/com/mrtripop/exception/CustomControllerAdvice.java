package com.mrtripop.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomControllerAdvice {

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException e) {
    log.error("Application exception: code={}, status={}", e.getErrorCode().getCode(), e.getHttpStatus(), e);
    ErrorResponse error =
        new ErrorResponse.ErrorResponseBuilder(
                e.getHttpStatus().toString(), e.getHttpStatus().value(), e.getErrorCode().getMessage())
            .withData(e.getErrorCode().getCode())
            .withTimestamp()
            .build();
    return new ResponseEntity<>(error, e.getHttpStatus());
  }

  @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception e) {
    log.error("Access denied", e);
    HttpStatus status = HttpStatus.FORBIDDEN;
    ErrorResponse error =
        new ErrorResponse.ErrorResponseBuilder(status.toString(), status.value(), "Access Denied")
            .withTimestamp()
            .build();
    return new ResponseEntity<>(error, status);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
    log.error("Not found: {}", e.getMessage(), e);
    HttpStatus status = HttpStatus.NOT_FOUND;
    ErrorResponse error =
        new ErrorResponse.ErrorResponseBuilder(status.toString(), status.value(), e.getMessage())
            .withTimestamp()
            .build();
    return new ResponseEntity<>(error, status);
  }

  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<ErrorResponse> handleNullPointerExceptions(NullPointerException e) {
    log.error("Unexpected null pointer exception", e);
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    ErrorResponse error =
        new ErrorResponse.ErrorResponseBuilder(
                status.toString(), status.value(), "An unexpected error occurred")
            .withTimestamp()
            .build();
    return new ResponseEntity<>(error, status);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleExceptions(Exception e) {
    log.error("Unhandled exception", e);
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    ErrorResponse error =
        new ErrorResponse.ErrorResponseBuilder(
                status.toString(), status.value(), status.getReasonPhrase())
            .withTimestamp()
            .build();
    return new ResponseEntity<>(error, status);
  }
}