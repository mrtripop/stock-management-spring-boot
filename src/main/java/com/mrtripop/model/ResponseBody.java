package com.mrtripop.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
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
public class ResponseBody {
  private String code;
  private String message;
  private Object data;
  private Object error;
  private LocalDateTime timestamp = LocalDateTime.from(Instant.now());

  public ResponseEntity<Object> toResponseEntity(HttpStatus httpStatus) {
    return new ResponseEntity<>(
        new ResponseBody(code, message, data, error, timestamp), httpStatus);
  }
}
