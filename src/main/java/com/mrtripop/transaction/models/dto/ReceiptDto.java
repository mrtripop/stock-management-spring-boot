package com.mrtripop.transaction.models.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDto {

  private Long invoiceId;
  private String storeName;
  private String status;
  private BigDecimal totalAmount;
  private BigDecimal patientOwed;
  private BigDecimal insuranceClaimAmount;
  private List<ReceiptItemDto> items;
  private LocalDateTime generatedAt;
}
