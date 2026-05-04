package com.mrtripop.inventory.models.dto;

import com.mrtripop.inventory.models.db.BatchStatus;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDto {
  private Long id;
  private UUID brandId;
  private String batchNumber;
  private LocalDate expiryDate;
  private Long quantity;
  private String supplierReference;
  private String manufacturerLotNumber;
  private String storageConditions;
  private BatchStatus status;
  private Long createdAt;
  private Long updatedAt;
}