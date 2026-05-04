package com.mrtripop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "auth.totp")
public class TotpProperties {
  private String issuer = "StockManagement";
  private int digits = 6;
  private int period = 30;
}