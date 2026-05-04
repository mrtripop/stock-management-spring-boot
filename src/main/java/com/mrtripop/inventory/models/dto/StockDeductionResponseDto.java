package com.mrtripop.inventory.models.dto;

import java.math.BigDecimal;
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
  private String requestedUnit;
  private Long requestedQuantity;
  private String baseUnit;
  private Long deductedQuantity;
  private BigDecimal unitPrice;
  private BigDecimal totalAmount;
  private List<DeductedBatchDto> items;
  private SignatureVerificationDto signatureVerification;
}
