package com.mrtripop.inventory.models.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeshStockResponseDto {

  private List<MeshStockDto> localStoreStocks;
  private List<MeshStockDto> meshStoreStocks;
  private Long totalMeshQuantity;
}
