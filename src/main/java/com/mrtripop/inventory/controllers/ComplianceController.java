package com.mrtripop.inventory.controllers;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.SuccessCode;
import com.mrtripop.inventory.models.dto.RecallBatchRequest;
import com.mrtripop.inventory.models.dto.RecallBatchResponse;
import com.mrtripop.inventory.services.ComplianceService;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
@Validated
public class ComplianceController {
  private final ComplianceService complianceService;

  @PostMapping("/compliance/recall")
  public ResponseEntity<Object> recallBatch(@Valid @RequestBody RecallBatchRequest request)
      throws ApplicationException {
    RecallBatchResponse result = complianceService.recallBatch(request.getBatchId());
    return ResponseBody.builder()
        .code(SuccessCode.INV2003_RECALL_BATCH_IS_SUCCESS.getCode())
        .message(SuccessCode.INV2003_RECALL_BATCH_IS_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}