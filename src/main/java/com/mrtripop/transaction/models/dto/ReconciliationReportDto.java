package com.mrtripop.transaction.models.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationReportDto {

  private UUID storeId;
  private String storeName;
  private LocalDate reportDate;
  private Long periodStart;
  private Long periodEnd;
  private BigDecimal ledgerTotal;
  private BigDecimal invoiceTotal;
  private BigDecimal discrepancy;
  private int discrepancyCount;
  private List<ReconciliationEntryDto> entries;
}
