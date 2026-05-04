package com.mrtripop.users.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaVerifyRequest {
  @NotBlank(message = "Temp token is required")
  private String tempToken;

  @NotBlank(message = "TOTP code is required")
  @Size(min = 6, max = 6, message = "TOTP code must be 6 digits")
  private String totpCode;
}