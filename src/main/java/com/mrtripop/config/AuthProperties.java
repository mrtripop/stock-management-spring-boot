package com.mrtripop.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "auth.jwt")
public class AuthProperties {

  @NotBlank(message = "JWT secret must not be blank")
  private String secret;

  @Min(value = 60000, message = "Access token expiration must be at least 60000ms (1 minute)")
  private long accessTokenExpiration = 86400000;

  @Min(value = 60000, message = "Temp token expiration must be at least 60000ms (1 minute)")
  private long tempTokenExpiration = 300000;
}