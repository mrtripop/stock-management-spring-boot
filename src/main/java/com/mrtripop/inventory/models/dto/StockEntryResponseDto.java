package com.mrtripop.inventory.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEntryResponseDto {
  private BatchDto batch;
  private StoreStockDto storeStock;
}