package com.mrtripop.transaction.models.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationRequest {

  @NotNull(message = "Store ID is required")
  private UUID storeId;

  @NotNull(message = "Report date is required")
  private LocalDate reportDate;

  @NotNull(message = "Period start is required")
  private Long periodStart;

  @NotNull(message = "Period end is required")
  private Long periodEnd;
}
