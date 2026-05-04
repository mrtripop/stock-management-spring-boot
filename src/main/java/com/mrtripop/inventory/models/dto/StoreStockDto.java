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
public class StoreStockDto {
  private Long id;
  private UUID storeId;
  private Long batchId;
  private Long quantity;
  private String location;
  private Long createdAt;
  private Long updatedAt;
}