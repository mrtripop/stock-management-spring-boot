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
public class InvoiceItemDto {

  private Long id;
  private String brandName;
  private String batchNumber;
  private Long quantity;
  private BigDecimal unitPrice;
  private BigDecimal lineTotal;
  private BigDecimal patientOwed;
  private BigDecimal insuranceClaimAmount;
  private Integer insuranceCoveragePercent;
}
