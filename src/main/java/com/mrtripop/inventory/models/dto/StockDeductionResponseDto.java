package com.mrtripop.inventory.models.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductionResponseDto {
  private String barcode;
  private UUID brandId;
  private String brandName;
  private Long requestedQuantity;
  private Long deductedQuantity;
  private List<DeductedBatchDto> items;
}
