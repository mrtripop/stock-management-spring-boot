package com.mrtripop.clinical.controllers;

import com.mrtripop.clinical.models.dto.ActivateProductRequest;
import com.mrtripop.clinical.models.dto.StoreProductDto;
import com.mrtripop.clinical.models.dto.UpdateOverrideRequest;
import com.mrtripop.clinical.services.StoreProductService;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clinical/catalog/stores/{storeId}/products")
@RequiredArgsConstructor
public class StoreProductController {

  private final StoreProductService storeProductService;

  @PostMapping
  public ResponseEntity<ResponseBody<StoreProductDto>> activateProduct(
      @PathVariable UUID storeId, @Valid @RequestBody ActivateProductRequest request) {
    StoreProductDto result = storeProductService.activateProduct(storeId, request.getBrandId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ResponseBody<>(
                String.valueOf(HttpStatus.CREATED.value()),
                "Product activated successfully",
                result));
  }

  @GetMapping
  public ResponseEntity<ResponseBody<Page<StoreProductDto>>> getActiveProducts(
      @PathVariable UUID storeId, @Valid BaseQueryParams queryParams) {
    Pageable pageable = PageRequest.of(queryParams.getPage() - 1, queryParams.getSize(), Sort.by(queryParams.getOrderBy(), "id"));
    Page<StoreProductDto> result = storeProductService.getActiveProducts(storeId, pageable);
    return ResponseEntity.ok(
        new ResponseBody<>(
            String.valueOf(HttpStatus.OK.value()), "Active products retrieved successfully", result));
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ResponseBody<StoreProductDto>> getStoreProduct(
      @PathVariable UUID storeId, @PathVariable UUID productId) {
    StoreProductDto result = storeProductService.getStoreProduct(storeId, productId);
    return ResponseEntity.ok(
        new ResponseBody<>(
            String.valueOf(HttpStatus.OK.value()), "Store product retrieved successfully", result));
  }

  @PatchMapping("/{productId}")
  public ResponseEntity<ResponseBody<StoreProductDto>> updateOverride(
      @PathVariable UUID storeId,
      @PathVariable UUID productId,
      @RequestBody UpdateOverrideRequest request) {
    StoreProductDto result = storeProductService.updateOverride(storeId, productId, request);
    return ResponseEntity.ok(
        new ResponseBody<>(
            String.valueOf(HttpStatus.OK.value()), "Override updated successfully", result));
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> deactivateProduct(
      @PathVariable UUID storeId, @PathVariable UUID productId) {
    storeProductService.deactivateProduct(storeId, productId);
    return ResponseEntity.noContent().build();
  }
}
