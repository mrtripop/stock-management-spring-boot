package com.mrtripop.inventory.models.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BrandSubstituteDto {
  UUID brandId;
  String brandName;
  String strength;
  String form;
  BigDecimal price;
  Long availableQuantity;
  LocalDate nearestExpiryDate;
  Long nearestExpiryBatchId;
}
