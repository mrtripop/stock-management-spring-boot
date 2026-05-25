package com.mrtripop.transaction.models.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesSummaryDto {

  private LocalDate date;
  private int totalInvoices;
  private BigDecimal totalRevenue;
  private BigDecimal totalPatientPaid;
  private BigDecimal totalInsuranceClaims;
  private long totalItemsDispensed;
  private int voidedCount;
}
