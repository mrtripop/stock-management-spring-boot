package com.mrtripop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth.jwt")
public class AuthProperties {
  private String secret;
  private long accessTokenExpiration = 86400000;
  private long tempTokenExpiration = 300000;
}