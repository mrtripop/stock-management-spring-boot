package com.mrtripop.transaction.models.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemDto {

  private Long id;
  private Long invoiceItemId;
  private String brandName;
  private String batchNumber;
  private Long quantity;
  private BigDecimal refundAmount;
}
