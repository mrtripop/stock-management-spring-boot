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
public class MeshStockDto {

  private UUID storeId;
  private String storeName;
  private UUID brandId;
  private String brandName;
  private String genericName;
  private Long totalQuantity;
  private Long batchCount;
}
