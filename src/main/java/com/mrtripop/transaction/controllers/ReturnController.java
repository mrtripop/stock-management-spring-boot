package com.mrtripop.transaction.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.transaction.constant.SuccessCode;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnDto;
import com.mrtripop.transaction.services.ReturnService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transaction")
@Validated
public class ReturnController {

  private final ReturnService returnService;

  @PostMapping("/invoices/{invoiceId}/returns")
  public ResponseEntity<Object> createReturn(
      @PathVariable @Min(1) Long invoiceId, @Valid @RequestBody CreateReturnRequest request)
      throws ApplicationException {
    ReturnDto result = returnService.createReturn(invoiceId, request);
    BaseStatusCode success = SuccessCode.TXN2010_CREATE_RETURN_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @GetMapping("/invoices/{invoiceId}/returns")
  public ResponseEntity<Object> findByInvoiceId(
      @PathVariable @Min(1) Long invoiceId, @Valid BaseQueryParams params)
      throws ApplicationException {
    Pageable pageable =
        PageRequest.of(params.getPage() - 1, params.getSize(), Sort.by(params.getOrderBy(), "id"));
    Page<ReturnDto> result = returnService.findByInvoiceId(invoiceId, pageable);
    BaseStatusCode success = SuccessCode.TXN2011_GET_RETURNS_BY_INVOICE_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/returns/{returnId}")
  public ResponseEntity<Object> findById(@PathVariable @Min(1) Long returnId)
      throws ApplicationException {
    ReturnDto result = returnService.findById(returnId);
    BaseStatusCode success = SuccessCode.TXN2012_GET_RETURN_BY_ID_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}
