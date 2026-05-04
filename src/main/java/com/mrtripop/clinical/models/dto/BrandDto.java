package com.mrtripop.clinical.models.dto;

import jakarta.validation.constraints.NotBlank;
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
public class BrandDto {
  private UUID id;

  @NotNull(message = "Molecule ID is required")
  private UUID moleculeId;

  @NotBlank(message = "Brand name is required")
  private String brandName;

  private String strength;
  private String form;
  private String baseUnit;
  private String barcode;
  private Long createdAt;
  private Long updatedAt;
}
