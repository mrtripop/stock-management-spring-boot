package com.mrtripop.product.component;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.product.constant.ErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(annotations = RestController.class)
public class ControllerExceptionHandler {

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<Object> handleGlobalThrowable(ApplicationException ex) {
    BaseStatusCode errorCode = ex.getErrorCode();
    return ResponseBody.builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build()
        .toResponseEntity(ex.getHttpStatus());
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, MissingPathVariableException.class})
  public ResponseEntity<Object> handleException(Exception ex) {
    BaseStatusCode errorCode = ErrorCode.GB4044_GLOBAL_ERROR_IS_OCCURRED;
    return ResponseBody.builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .error(ex.getMessage())
        .build()
        .toResponseEntity(HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
    BaseStatusCode errorCode = ErrorCode.GB4041_REQUEST_BODY_IS_NOT_VALID;
    return ResponseBody.builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .error(getArgumentNotValidErrorMessage(errors))
        .build()
        .toResponseEntity(HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<Object> handleMethodValidation(HandlerMethodValidationException ex) {
    List<String> errors =
        ex.getAllErrors().stream().map(MessageSourceResolvable::getDefaultMessage).toList();
    BaseStatusCode errorCode = ErrorCode.GB4042_QUERY_PARAMETER_IS_NOT_VALID;
    return ResponseBody.builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .error(getArgumentNotValidErrorMessage(errors))
        .build()
        .toResponseEntity(HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    BaseStatusCode errorCode = ErrorCode.GB4043_METHOD_ARGUMENT_TYPE_MISMATCH;
    return ResponseBody.builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .error(ex.getParameter().getParameterName())
        .build()
        .toResponseEntity(HttpStatus.BAD_REQUEST);
  }

  private Map<String, List<String>> getArgumentNotValidErrorMessage(List<String> errors) {
    Map<String, List<String>> errorResponse = new HashMap<>();
    errorResponse.put("error_message", errors);
    return errorResponse;
  }
}
