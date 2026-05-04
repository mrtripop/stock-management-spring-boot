package com.mrtripop.inventory.models.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class StockEntryRequest {
  @NotBlank(message = "Barcode is required")
  private String barcode;

  @NotBlank(message = "Batch number is required")
  @Size(max = 100, message = "Batch number must not exceed 100 characters")
  private String batchNumber;

  @NotNull(message = "Expiry date is required")
  @Future(message = "Expiry date must be in the future")
  private LocalDate expiryDate;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Long quantity;

  @NotNull(message = "Store ID is required")
  private UUID storeId;

  private String supplierReference;
  private String manufacturerLotNumber;
  private String storageConditions;
}