package com.mrtripop.clinical.models.dto;

import com.mrtripop.clinical.models.db.StoreType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreRequest {
  @Null(message = "Store ID should be auto-generated")
  private UUID id;

  @NotBlank(message = "Store name is required")
  @Size(max = 255)
  private String name;

  @NotNull(message = "Store type is required")
  private StoreType type;
}