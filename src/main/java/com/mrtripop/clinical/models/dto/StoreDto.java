package com.mrtripop.clinical.models.dto;

import com.mrtripop.clinical.models.db.StoreType;
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
  private String name;
  private StoreType type;
  private Long createdAt;
  private Long updatedAt;
  private boolean active;
}
