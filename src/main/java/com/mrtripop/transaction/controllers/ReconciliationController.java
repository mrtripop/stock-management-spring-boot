package com.mrtripop.transaction.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.transaction.constant.SuccessCode;
import com.mrtripop.transaction.models.dto.ReconciliationReportDto;
import com.mrtripop.transaction.models.dto.ReconciliationRequest;
import com.mrtripop.transaction.services.ReconciliationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transaction/reports")
@Validated
public class ReconciliationController {

  private final ReconciliationService reconciliationService;

  @PostMapping("/reconciliation")
  public ResponseEntity<Object> generateReport(@Valid @RequestBody ReconciliationRequest request)
      throws ApplicationException {
    ReconciliationReportDto result = reconciliationService.generateReport(request);
    BaseStatusCode success = SuccessCode.TXN2007_GENERATE_RECONCILIATION_REPORT_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}
