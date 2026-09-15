package com.mrtripop.transaction.models.dto;

import com.mrtripop.transaction.models.db.ReturnReason;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDto {

  private Long id;
  private Long invoiceId;
  private ReturnReason reason;
  private BigDecimal totalRefundAmount;
  private List<ReturnItemDto> items;
  private Long createdAt;
  private Long updatedAt;
}
