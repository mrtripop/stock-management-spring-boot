package com.mrtripop.inventory.models.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductionByBatchRequest {

  @NotNull(message = "Store ID is required")
  private UUID storeId;

  @NotNull(message = "Batch ID is required")
  private Long batchId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Long quantity;
}