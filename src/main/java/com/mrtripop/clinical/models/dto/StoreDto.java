package com.mrtripop.clinical.models.dto;

import com.mrtripop.clinical.models.db.StoreType;
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
public class StoreDto {
  private UUID id;

  @NotBlank(message = "Store name is required")
  private String name;

  @NotNull(message = "Store type is required")
  private StoreType type;

  private Long createdAt;
  private Long updatedAt;
}
