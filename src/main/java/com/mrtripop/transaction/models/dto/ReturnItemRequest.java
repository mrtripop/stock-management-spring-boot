package com.mrtripop.transaction.models.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemRequest {

  @NotNull(message = "Invoice item ID is required")
  @Min(value = 1, message = "Invoice item ID must be a positive number")
  private Long invoiceItemId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Long quantity;
}
