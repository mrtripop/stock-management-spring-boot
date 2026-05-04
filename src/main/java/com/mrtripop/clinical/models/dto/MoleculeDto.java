package com.mrtripop.clinical.models.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoleculeDto {
  private UUID id;

  @NotBlank(message = "Generic name is required")
  private String genericName;

  private String therapeuticClass;
  private String regulatorySchedule;
  private String dosageInstructions;
  private String safetyWarnings;
  private Long createdAt;
  private Long updatedAt;
}
