package com.mrtripop.transaction.models.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreateInvoiceRequest {

  @NotNull(message = "Store ID is required")
  private UUID storeId;

  @NotEmpty(message = "Invoice items must not be empty")
  private List<InvoiceItemRequest> items;
}
