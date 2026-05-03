package com.mrtripop.inventory.models.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductedBatchDto {
  private Long batchId;
  private String batchNumber;
  private LocalDate expiryDate;
  private Long deductedQuantity;
  private Long remainingQuantity;
}
