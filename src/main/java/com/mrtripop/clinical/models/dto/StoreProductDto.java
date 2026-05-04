package com.mrtripop.clinical.models.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreProductDto {
  private UUID id;
  private UUID storeId;
  private UUID brandId;
  private BigDecimal price;
  private String shelfLocation;
  private Boolean isActive;
  private Long createdAt;
  private Long updatedAt;

  private String brandName;
  private String strength;
  private String form;
  private String baseUnit;
  private UUID moleculeId;
  private String moleculeGenericName;
  private String therapeuticClass;
  private String regulatorySchedule;
}
