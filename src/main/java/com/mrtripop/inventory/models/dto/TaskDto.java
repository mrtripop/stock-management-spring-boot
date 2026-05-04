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
public class TaskDto {
    private Long id;
    private UUID storeId;
    private String storeName;
    private String taskType;
    private String status;
    private Long batchId;
    private String batchNumber;
    private UUID brandId;
    private String brandName;
    private String message;
    private Long currentQuantity;
    private Long thresholdQuantity;
    private Integer daysUntilExpiry;
    private Long createdAt;
    private Long updatedAt;
}