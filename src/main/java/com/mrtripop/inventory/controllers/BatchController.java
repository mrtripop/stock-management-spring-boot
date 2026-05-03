package com.mrtripop.inventory.controllers;

import com.mrtripop.clinical.component.ClinicalMapper;
import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.component.BatchMapper;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.dto.BatchDto;
import com.mrtripop.inventory.models.dto.StockEntryRequest;
import com.mrtripop.inventory.models.dto.StockEntryResponseDto;
import com.mrtripop.inventory.models.dto.StoreStockDto;
import com.mrtripop.inventory.repository.BatchRepository;
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
  private final BatchRepository batchRepository;
  private final BatchMapper batchMapper;
  private final BrandRepository brandRepository;
  private final ClinicalMapper clinicalMapper;

  @PostMapping("/batches/stock-in")
  public ResponseEntity<Object> createBatchFromBarcode(
      @Valid @RequestBody StockEntryRequest request) throws ApplicationException {
    StockEntryResponseDto result = batchService.createBatchFromBarcode(request);
    return ResponseBody.builder()
        .code("STOCK_CREATED")
        .message("Batch and store stock created successfully")
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @GetMapping("/batches/{id}")
  public ResponseEntity<Object> getBatchById(@PathVariable Long id) throws ApplicationException {
    BatchDto result = batchService.getBatchById(id);
    return ResponseBody.builder()
        .code("BATCH_FOUND")
        .message("Batch retrieved successfully")
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/batches")
  public ResponseEntity<Object> getBatches(
      @RequestParam(required = false) UUID brandId, @Valid BaseQueryParams params) {
    Page<BatchDto> result;
    if (brandId != null) {
      result =
          batchService.getBatchesByBrandId(
              brandId,
              PageRequest.of(
                  params.getPage() - 1, params.getSize(), Sort.by(params.getOrderBy(), "id")));
    } else {
      result =
          batchRepository
              .findAll(
                  PageRequest.of(
                      params.getPage() - 1, params.getSize(), Sort.by(params.getOrderBy(), "id")))
              .map(batchMapper::toBatchDto);
    }
    return ResponseBody.builder()
        .code("BATCHES_FOUND")
        .message("Batches retrieved successfully")
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/stores/{storeId}/stock")
  public ResponseEntity<Object> getStoreStocks(
      @PathVariable UUID storeId, @Valid BaseQueryParams params) {
    Page<StoreStockDto> result =
        batchService.getStoreStocksByStoreId(
            storeId,
            PageRequest.of(
                params.getPage() - 1, params.getSize(), Sort.by(params.getOrderBy(), "id")));
    return ResponseBody.builder()
        .code("STORE_STOCK_FOUND")
        .message("Store stock retrieved successfully")
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/barcode/resolve")
  public ResponseEntity<Object> resolveBarcode(@RequestParam String barcode)
      throws ApplicationException {
    Brand brand =
        brandRepository
            .findByBarcode(barcode)
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.BARCODE_NOT_RECOGNIZED, HttpStatus.NOT_FOUND));
    BrandDto brandDto = clinicalMapper.toBrandDto(brand);
    return ResponseBody.builder()
        .code("BARCODE_RESOLVED")
        .message("Barcode resolved successfully")
        .data(brandDto)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}
