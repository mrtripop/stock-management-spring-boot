package com.mrtripop.inventory.controllers;

import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.inventory.constant.SuccessCode;
import com.mrtripop.inventory.models.dto.BatchDto;
import com.mrtripop.inventory.models.dto.StockDeductionRequest;
import com.mrtripop.inventory.models.dto.StockDeductionResponseDto;
import com.mrtripop.inventory.models.dto.StockEntryRequest;
import com.mrtripop.inventory.models.dto.StockEntryResponseDto;
import com.mrtripop.inventory.models.dto.StoreStockDto;
import com.mrtripop.inventory.services.BatchService;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/api/v1/inventory")
@Validated
public class BatchController {

  private final BatchService batchService;

  @PostMapping("/batches/stock-in")
  public ResponseEntity<Object> createBatchFromBarcode(@Valid @RequestBody StockEntryRequest request) {
    StockEntryResponseDto result = batchService.createBatchFromBarcode(request);
    return ResponseBody.builder()
        .code(SuccessCode.INV2006_STOCK_IN_SUCCESS.getCode())
        .message(SuccessCode.INV2006_STOCK_IN_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @PostMapping("/stock/deduct")
  public ResponseEntity<Object> deductStock(@Valid @RequestBody StockDeductionRequest request) {
    StockDeductionResponseDto result = batchService.deductStock(request);
    return ResponseBody.builder()
        .code(SuccessCode.INV2007_STOCK_DEDUCT_SUCCESS.getCode())
        .message(SuccessCode.INV2007_STOCK_DEDUCT_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/batches/{id}")
  public ResponseEntity<Object> getBatchById(@PathVariable Long id) {
    BatchDto result = batchService.getBatchById(id);
    return ResponseBody.builder()
        .code(SuccessCode.INV2008_GET_BATCH_SUCCESS.getCode())
        .message(SuccessCode.INV2008_GET_BATCH_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/batches")
  public ResponseEntity<Object> getBatches(
      @RequestParam(required = false) UUID brandId, @Valid BaseQueryParams params) {
    Page<BatchDto> result;
    PageRequest pageable = PageRequest.of(
        params.getPage() - 1, params.getSize(), Sort.by(params.getOrderBy(), "id"));
    if (brandId != null) {
      result = batchService.getBatchesByBrandId(brandId, pageable);
    } else {
      result = batchService.getBatches(pageable);
    }
    return ResponseBody.builder()
        .code(SuccessCode.INV2009_GET_BATCHES_SUCCESS.getCode())
        .message(SuccessCode.INV2009_GET_BATCHES_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/stores/{storeId}/stock")
  public ResponseEntity<Object> getStoreStocks(
      @PathVariable UUID storeId, @Valid BaseQueryParams params) {
    Page<StoreStockDto> result = batchService.getStoreStocksByStoreId(
        storeId,
        PageRequest.of(params.getPage() - 1, params.getSize(), Sort.by(params.getOrderBy(), "id")));
    return ResponseBody.builder()
        .code(SuccessCode.INV2010_GET_STORE_STOCK_SUCCESS.getCode())
        .message(SuccessCode.INV2010_GET_STORE_STOCK_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/barcode/resolve")
  public ResponseEntity<Object> resolveBarcode(@RequestParam String barcode) {
    BrandDto result = batchService.resolveBarcode(barcode);
    return ResponseBody.builder()
        .code(SuccessCode.INV2011_RESOLVE_BARCODE_SUCCESS.getCode())
        .message(SuccessCode.INV2011_RESOLVE_BARCODE_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}