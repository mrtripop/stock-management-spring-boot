package com.mrtripop.transaction.models.dto;

import com.mrtripop.transaction.models.db.ReturnReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnRequest {

  @NotNull(message = "Reason is required")
  private ReturnReason reason;

  @NotEmpty(message = "Return items must not be empty")
  @Valid
  private List<ReturnItemRequest> items;
}
