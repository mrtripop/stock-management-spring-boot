package com.mrtripop.clinical.controllers;

import com.mrtripop.clinical.constant.SuccessCode;
import com.mrtripop.clinical.models.dto.CreateStoreRequest;
import com.mrtripop.clinical.models.dto.StoreDto;
import com.mrtripop.clinical.models.dto.UpdateStoreRequest;
import com.mrtripop.clinical.services.StoreService;
import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clinical/stores")
@Validated
public class StoreController {
  private final StoreService storeService;

  @GetMapping
  public ResponseEntity<Object> findAll(@Valid BaseQueryParams params) throws ApplicationException {
    Pageable pageable = PageRequest.of(params.getPage() - 1, params.getSize(), Sort.by(params.getOrderBy(), "id"));
    Page<StoreDto> result = storeService.findAll(pageable);
    BaseStatusCode success = SuccessCode.CL2001_GET_ALL_STORES_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/{storeId}")
  public ResponseEntity<Object> findById(@PathVariable UUID storeId) throws ApplicationException {
    StoreDto result = storeService.findById(storeId);
    BaseStatusCode success = SuccessCode.CL2002_GET_STORE_BY_ID_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> create(@Valid @RequestBody CreateStoreRequest request) throws ApplicationException {
    StoreDto result = storeService.create(request);
    BaseStatusCode success = SuccessCode.CL2003_CREATE_STORE_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @PatchMapping("/{storeId}")
  public ResponseEntity<Object> update(@PathVariable UUID storeId, @RequestBody UpdateStoreRequest request)
      throws ApplicationException {
    StoreDto result = storeService.update(storeId, request);
    BaseStatusCode success = SuccessCode.CL2004_UPDATE_STORE_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @DeleteMapping("/{storeId}")
  public ResponseEntity<Object> delete(@PathVariable UUID storeId) throws ApplicationException {
    storeService.delete(storeId);
    BaseStatusCode success = SuccessCode.CL2005_DELETE_STORE_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}