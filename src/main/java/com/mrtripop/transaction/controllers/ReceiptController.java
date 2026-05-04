package com.mrtripop.transaction.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.transaction.constant.SuccessCode;
import com.mrtripop.transaction.models.dto.ReceiptDto;
import com.mrtripop.transaction.services.ReceiptService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transaction/invoices")
@Validated
public class ReceiptController {

  private final ReceiptService receiptService;

  @GetMapping("/{invoiceId}/receipt")
  public ResponseEntity<Object> generateReceipt(
      @PathVariable @Min(1) Long invoiceId) throws ApplicationException {
    ReceiptDto result = receiptService.generateReceipt(invoiceId);
    BaseStatusCode success = SuccessCode.TXN2006_GENERATE_RECEIPT_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}
