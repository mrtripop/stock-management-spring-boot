package com.mrtripop.inventory.models.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductionRequest {
  @NotBlank(message = "Barcode is required")
  private String barcode;

  @NotNull(message = "Store ID is required")
  private UUID storeId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Long quantity;

  private String unit;

  private DigitalSignatureRequest signature;
}
