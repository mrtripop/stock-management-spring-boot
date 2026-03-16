package com.mrtripop.product.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.product.constant.ErrorCode;
import com.mrtripop.product.constant.SuccessCode;
import com.mrtripop.product.models.dto.ProductDTO;
import com.mrtripop.product.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/inventory/products")
public class ProductController {

  private ProductService productService;

  @GetMapping
  public ResponseEntity<Object> getProducts(@Valid BaseQueryParams queryParams)
      throws ApplicationException {
    try {
      List<ProductDTO> products = this.productService.getProducts(queryParams);
      BaseStatusCode successCode = SuccessCode.PRO2001_GET_ALL_PRODUCTS_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .data(products)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot get all products: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1001_CANNOT_GET_ALL_PRODUCTS, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{product_id}")
  public ResponseEntity<Object> getProductById(
      @PathVariable(name = "product_id") @Min(value = 1, message = "Product ID must not less than one") @NotNull(message = "Product ID must not be null") Long productId)
      throws ApplicationException {
    try {
      ProductDTO product = this.productService.getProductById(productId);
      BaseStatusCode successCode = SuccessCode.PRO2002_GET_PRODUCTS_BY_ID_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .data(product)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot get product by ID: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1003_CANNOT_GET_PRODUCT_BY_ID, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping
  public ResponseEntity<Object> createNewProduct(@RequestBody @Valid ProductDTO product)
      throws ApplicationException {
    try {
      ProductDTO createdProduct = this.productService.createProduct(product);
      BaseStatusCode successCode = SuccessCode.PRO2003_CREATE_NEW_PRODUCT_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .data(createdProduct)
          .build()
          .toResponseEntity(HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Cannot create a new product: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1002_CANNOT_CREATE_NEW_PRODUCT, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/{product_id}")
  public ResponseEntity<Object> updateProductById(
      @PathVariable(name = "product_id") @Min(value = 1, message = "Product ID must not less than one") @NotNull(message = "Product ID must not be null") Long productId,
      @RequestBody @Valid ProductDTO product)
      throws ApplicationException {
    try {
      ProductDTO updatedProduct = productService.updateProduct(productId, product);
      BaseStatusCode successCode = SuccessCode.PRO2004_UPDATE_PRODUCT_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .data(updatedProduct)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot update the product by ID: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1004_CANNOT_UPDATE_EXISTING_PRODUCT, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{product_id}")
  public ResponseEntity<Object> deleteProductById(
      @PathVariable(name = "product_id") @Min(value = 1, message = "Product ID must not less than one") @NotNull(message = "Product ID must not be null") Long productId)
      throws ApplicationException {
    try {
      productService.deleteProduct(productId);
      BaseStatusCode successCode = SuccessCode.PRO2005_DELETE_PRODUCT_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot delete the product by ID: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1005_CANNOT_DELETE_EXISTING_PRODUCT, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /// Upload products via CSV file

  @PutMapping("/upload")
  public ResponseEntity<Object> uploadProductsByCsv(@RequestParam("file") MultipartFile csvFile)
      throws ApplicationException {
    try {
      BaseStatusCode successCode = SuccessCode.PRO2007_UPLOAD_PRODUCTS_BY_CSV_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .data(productService.uploadProductByCsv(csvFile))
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Could not upload the file: {}", csvFile.getOriginalFilename());
      throw new ApplicationException(
          ErrorCode.PRO5002_CANNOT_UPDATE_PRODUCTS_FROM_CSV_FILE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
