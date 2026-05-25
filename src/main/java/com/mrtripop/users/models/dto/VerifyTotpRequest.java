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
public class VerifyTotpRequest {
  @NotBlank(message = "TOTP code is required")
  @Size(min = 6, max = 6, message = "TOTP code must be 6 digits")
  private String code;
}
