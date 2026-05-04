package com.mrtripop.inventory.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSignatureRequest {
  private String licenseNumber;
  private String signaturePayload;
}
