package com.mrtripop.transaction.models.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationEntryDto {

  private String actionType;
  private String entityName;
  private String entityId;
  private String oldValue;
  private String newValue;
  private LocalDateTime timestamp;
  private boolean orphaned;
}
