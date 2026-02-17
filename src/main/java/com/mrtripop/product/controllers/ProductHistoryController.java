package com.mrtripop.product.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.product.constant.ErrorCode;
import com.mrtripop.product.constant.SuccessCode;
import com.mrtripop.product.models.dto.ProductDTO;
import com.mrtripop.product.services.ProductHistoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/inventory/products/histories")
public class ProductHistoryController {

  private ProductHistoryService productHistoryService;

  @GetMapping
  public ResponseEntity<Object> getProductsHistories(@Valid BaseQueryParams queryParams)
      throws ApplicationException {
    try {
      List<ProductDTO> productHistories =
          this.productHistoryService.getProductsHistories(queryParams);
      BaseStatusCode statusCode = SuccessCode.PRO2006_GET_ALL_PRODUCT_HISTORIES_IS_SUCCESS;
      return ResponseBody.builder()
          .code(statusCode.getCode())
          .message(statusCode.getMessage())
          .data(productHistories)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot get product histories: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1007_CANNOT_GET_ALL_PRODUCT_HISTORIES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{product_code}")
  public ResponseEntity<Object> getProductsHistoriesByCode(
      @PathVariable(name = "product_code")
          @NotBlank(message = "Product code query param must not be blank")
          @NotNull(message = "Product code query param must not be null")
          @NotEmpty(message = "Product code query param must not be empty")
          String productCode,
      @Valid BaseQueryParams queryParams)
      throws ApplicationException {
    try {
      List<ProductDTO> productHistories =
          this.productHistoryService.getProductHistoriesByCode(productCode, queryParams);
      BaseStatusCode statusCode = SuccessCode.PRO2006_GET_ALL_PRODUCT_HISTORIES_IS_SUCCESS;
      return ResponseBody.builder()
          .code(statusCode.getCode())
          .message(statusCode.getMessage())
          .data(productHistories)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot get product histories by product code: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1011_CANNOT_GET_PRODUCT_HISTORIES_BY_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
