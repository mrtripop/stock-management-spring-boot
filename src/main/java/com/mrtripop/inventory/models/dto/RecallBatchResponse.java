package com.mrtripop.inventory.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecallBatchResponse {
  private Long batchId;
  private String batchNumber;
  private String brandName;
  private int affectedStores;
  private String recallStatus;
}