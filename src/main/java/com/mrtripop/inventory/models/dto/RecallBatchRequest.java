package com.mrtripop.inventory.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecallBatchRequest {
  @NotNull(message = "Batch ID is required")
  private Long batchId;
}