package com.mrtripop.clinical.models.dto;

import com.mrtripop.clinical.models.db.StoreType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreRequest {
  @Size(max = 255)
  private String name;

  private StoreType type;
}