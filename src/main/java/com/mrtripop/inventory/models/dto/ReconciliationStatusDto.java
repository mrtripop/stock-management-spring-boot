package com.mrtripop.inventory.models.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationStatusDto {
  private String status;
  private int progress;
  private Instant startTime;
  private Instant updatedTime;
}
