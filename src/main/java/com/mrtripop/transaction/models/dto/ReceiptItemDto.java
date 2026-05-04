package com.mrtripop.transaction.models.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemDto {

  private String brandName;
  private String batchNumber;
  private Long quantity;
  private BigDecimal unitPrice;
  private BigDecimal lineTotal;
  private BigDecimal patientOwed;
  private BigDecimal insuranceClaimAmount;
  private String dosageInstructions;
  private String safetyWarnings;
  private String digitalLeafletUrl;
}
