package com.mrtripop.clinical.models.dto;

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
}
