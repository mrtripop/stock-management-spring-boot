package com.mrtripop.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
  private Date timestamp;

  private int code;
  private String status;
  private String message;
  private String stackTrace;
  private Object data;

  public ErrorResponse(ErrorResponseBuilder builder) {
    this.timestamp = builder.timestamp;
    this.code = builder.code;
    this.status = builder.status;
    this.message = builder.message;
    this.stackTrace = builder.stackTrace;
    this.data = builder.data;
  }

  public static class ErrorResponseBuilder {
    private final int code;
    private final String status;
    private final String message;
    private String stackTrace;
    private Object data;
    private Date timestamp;

    public ErrorResponseBuilder(String status, int code, String message) {
      this.status = status;
      this.code = code;
      this.message = message;
    }

    public ErrorResponseBuilder withData(Object data) {
      this.data = data;
      return this;
    }

    public ErrorResponseBuilder withStacktrace(String stackTrace) {
      this.stackTrace = stackTrace;
      return this;
    }

    public ErrorResponseBuilder withTimestamp() {
      this.timestamp = Date.from(Instant.now());
      return this;
    }

    public ErrorResponse build() {
      return new ErrorResponse(this);
    }
  }
}
