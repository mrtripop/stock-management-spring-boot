package com.mrtripop.transaction.models.dto;

import jakarta.validation.constraints.Max;
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
public class InvoiceItemRequest {

  @NotNull(message = "Brand ID is required")
  private UUID brandId;

  @NotNull(message = "Batch ID is required")
  private Long batchId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Long quantity;

  @Min(value = 0, message = "Insurance coverage percent must be at least 0")
  @Max(value = 100, message = "Insurance coverage percent must be at most 100")
  private Integer insuranceCoveragePercent = 0;
}
