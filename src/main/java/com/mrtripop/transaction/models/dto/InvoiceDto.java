package com.mrtripop.transaction.models.dto;

import com.mrtripop.transaction.models.db.InvoiceStatus;
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
public class InvoiceDto {

  private Long id;
  private UUID storeId;
  private String storeName;
  private InvoiceStatus status;
  private BigDecimal totalAmount;
  private BigDecimal patientOwed;
  private BigDecimal insuranceClaimAmount;
  private List<InvoiceItemDto> items;
  private Long createdAt;
  private Long updatedAt;
}
