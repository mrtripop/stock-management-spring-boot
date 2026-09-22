package com.mrtripop.clinical.models.dto;

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
public class ActivateProductRequest {
  @NotNull(message = "Brand ID is required")
  private UUID brandId;

  @Min(value = 0, message = "Target stock must be greater than or equal to 0")
  private Long targetStock;
}
