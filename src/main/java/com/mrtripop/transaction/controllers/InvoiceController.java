package com.mrtripop.transaction.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.transaction.constant.SuccessCode;
import com.mrtripop.transaction.models.dto.CreateInvoiceRequest;
import com.mrtripop.transaction.models.dto.DailySalesSummaryDto;
import com.mrtripop.transaction.models.dto.InvoiceDto;
import com.mrtripop.transaction.services.InvoiceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.UUID;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transaction/invoices")
@Validated
public class InvoiceController {

  private final InvoiceService invoiceService;

  @GetMapping
  public ResponseEntity<Object> findAll(
      @RequestParam UUID storeId, @Valid BaseQueryParams params) throws ApplicationException {
    Pageable pageable =
        PageRequest.of(params.getPage() - 1, params.getSize(), Sort.by(params.getOrderBy(), "id"));
    Page<InvoiceDto> result = invoiceService.findAll(storeId, pageable);
    BaseStatusCode success = SuccessCode.TXN2001_GET_ALL_INVOICES_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/{invoiceId}")
  public ResponseEntity<Object> findById(@PathVariable @Min(1) Long invoiceId)
      throws ApplicationException {
    InvoiceDto result = invoiceService.findById(invoiceId);
    BaseStatusCode success = SuccessCode.TXN2002_GET_INVOICE_BY_ID_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> create(@Valid @RequestBody CreateInvoiceRequest request)
      throws ApplicationException {
    InvoiceDto result = invoiceService.create(request);
    BaseStatusCode success = SuccessCode.TXN2003_CREATE_INVOICE_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @PostMapping("/{invoiceId}/complete")
  public ResponseEntity<Object> complete(@PathVariable @Min(1) Long invoiceId)
      throws ApplicationException {
    InvoiceDto result = invoiceService.complete(invoiceId);
    BaseStatusCode success = SuccessCode.TXN2004_COMPLETE_INVOICE_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PostMapping("/{invoiceId}/void")
  public ResponseEntity<Object> voidInvoice(@PathVariable @Min(1) Long invoiceId)
      throws ApplicationException {
    InvoiceDto result = invoiceService.voidInvoice(invoiceId);
    BaseStatusCode success = SuccessCode.TXN2005_VOID_INVOICE_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PostMapping("/dispense")
  public ResponseEntity<Object> dispense(@Valid @RequestBody CreateInvoiceRequest request)
      throws ApplicationException {
    InvoiceDto result = invoiceService.dispense(request);
    BaseStatusCode success = SuccessCode.TXN2008_DISPENSE_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @GetMapping("/daily-summary")
  public ResponseEntity<Object> getDailySummary(
      @RequestParam UUID storeId,
      @RequestParam(required = false) LocalDate date) throws ApplicationException {
    DailySalesSummaryDto result = invoiceService.getDailySummary(storeId, date);
    BaseStatusCode success = SuccessCode.TXN2009_GET_DAILY_SUMMARY_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}
