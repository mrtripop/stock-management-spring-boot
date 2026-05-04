package com.mrtripop.inventory.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSignatureDto {
  private Long id;
  private Long storeStockId;
  private String pharmacistLicenseNumber;
  private String verificationStatus;
  private Long verifiedAt;
  private Long createdAt;
}
