package com.mrtripop.inventory.models.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitConversionDto {
  private Long id;
  private UUID brandId;
  private String fromUnit;
  private String toUnit;
  private Integer ratio;
  private Long createdAt;
  private Long updatedAt;
}
