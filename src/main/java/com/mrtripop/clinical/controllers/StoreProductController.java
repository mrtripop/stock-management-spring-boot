package com.mrtripop.clinical.controllers;

import com.mrtripop.clinical.constant.SuccessCode;
import com.mrtripop.clinical.models.dto.ActivateProductRequest;
import com.mrtripop.clinical.models.dto.StoreProductDto;
import com.mrtripop.clinical.models.dto.UpdateOverrideRequest;
import com.mrtripop.clinical.services.StoreProductService;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/clinical/catalog/stores/{storeId}/products")
@RequiredArgsConstructor
@Validated
public class StoreProductController {

  private final StoreProductService storeProductService;

  @PostMapping
  public ResponseEntity<Object> activateProduct(
      @PathVariable UUID storeId, @Valid @RequestBody ActivateProductRequest request) {
    StoreProductDto result = storeProductService.activateProduct(storeId, request.getBrandId());
    return ResponseBody.builder()
        .code(SuccessCode.CL2006_ACTIVATE_PRODUCT_SUCCESS.getCode())
        .message(SuccessCode.CL2006_ACTIVATE_PRODUCT_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<Object> getActiveProducts(
      @PathVariable UUID storeId, @Valid BaseQueryParams queryParams) {
    Pageable pageable = PageRequest.of(
        queryParams.getPage() - 1, queryParams.getSize(), Sort.by(queryParams.getOrderBy(), "id"));
    Page<StoreProductDto> result = storeProductService.getActiveProducts(storeId, pageable);
    return ResponseBody.builder()
        .code(SuccessCode.CL2007_GET_ACTIVE_PRODUCTS_SUCCESS.getCode())
        .message(SuccessCode.CL2007_GET_ACTIVE_PRODUCTS_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/{productId}")
  public ResponseEntity<Object> getStoreProduct(
      @PathVariable UUID storeId, @PathVariable UUID productId) {
    StoreProductDto result = storeProductService.getStoreProduct(storeId, productId);
    return ResponseBody.builder()
        .code(SuccessCode.CL2008_GET_STORE_PRODUCT_SUCCESS.getCode())
        .message(SuccessCode.CL2008_GET_STORE_PRODUCT_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("/{productId}")
  public ResponseEntity<Object> updateOverride(
      @PathVariable UUID storeId,
      @PathVariable UUID productId,
      @RequestBody UpdateOverrideRequest request) {
    StoreProductDto result = storeProductService.updateOverride(storeId, productId, request);
    return ResponseBody.builder()
        .code(SuccessCode.CL2009_UPDATE_OVERRIDE_SUCCESS.getCode())
        .message(SuccessCode.CL2009_UPDATE_OVERRIDE_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<Object> deactivateProduct(
      @PathVariable UUID storeId, @PathVariable UUID productId) {
    storeProductService.deactivateProduct(storeId, productId);
    return ResponseBody.builder()
        .code(SuccessCode.CL2010_DEACTIVATE_PRODUCT_SUCCESS.getCode())
        .message(SuccessCode.CL2010_DEACTIVATE_PRODUCT_SUCCESS.getMessage())
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}