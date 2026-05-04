package com.mrtripop.inventory.models.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUnitConversionRequest {
  @NotNull(message = "Brand ID is required")
  private UUID brandId;

  @NotBlank(message = "From unit is required")
  @Length(max = 50)
  private String fromUnit;

  @NotBlank(message = "To unit is required")
  @Length(max = 50)
  private String toUnit;

  @NotNull(message = "Ratio is required")
  @Min(value = 2, message = "Ratio must be at least 2")
  private Integer ratio;
}
