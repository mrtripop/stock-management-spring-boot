package com.mrtripop.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseBody<T> {
  private String code;
  private String message;
  private T data;
  private Object error;
  private LocalDateTime timestamp = LocalDateTime.now();

  public ResponseBody(String code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
    this.timestamp = LocalDateTime.now();
  }

  public ResponseEntity<Object> toResponseEntity(HttpStatus httpStatus) {
    return new ResponseEntity<>(this, httpStatus);
  }
}
