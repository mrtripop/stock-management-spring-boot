package com.mrtripop.clinical.models.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOverrideRequest {
  @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimal places")
  private BigDecimal price;

  @Size(max = 100, message = "Shelf location must not exceed 100 characters")
  private String shelfLocation;
}
