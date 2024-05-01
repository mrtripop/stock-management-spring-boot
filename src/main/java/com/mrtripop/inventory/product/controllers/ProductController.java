package com.mrtripop.inventory.product.controllers;

import com.mrtripop.inventory.constant.BaseStatusCode;
import com.mrtripop.inventory.exception.GlobalThrowable;
import com.mrtripop.inventory.model.ResponseBody;
import com.mrtripop.inventory.product.constant.SuccessCode;
import com.mrtripop.inventory.product.interfaces.ProductService;
import com.mrtripop.inventory.product.models.ProductDTO;
import com.mrtripop.inventory.product.services.ProductServiceImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/inventory/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductServiceImpl productService) {
    this.productService = productService;
  }

  @GetMapping
  public HttpEntity<ResponseBody> getAllProducts(
      @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
      @RequestParam(name = "size", defaultValue = "200", required = false) Integer size,
      @RequestParam(name = "order_by", defaultValue = "ASC", required = false) String orderBy) {
    try {
      List<ProductDTO> products = this.productService.getAllProducts(page, size, orderBy);
      BaseStatusCode successCode = SuccessCode.PRO2001_GET_ALL_PRODUCTS_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .data(products)
          .build()
          .buildResponseEntity(HttpStatus.OK);
    } catch (GlobalThrowable e) {
      log.error("Cannot get all product: {}", e.getErrorCode().getMessage());
      BaseStatusCode errorCode = e.getErrorCode();
      return ResponseBody.builder()
          .code(errorCode.getCode())
          .message(errorCode.getMessage())
          .build()
          .buildResponseEntity(e.getHttpStatus());
    }
  }

  @GetMapping("/{product_id}")
  public HttpEntity<ResponseBody> getProductById(
      @PathVariable(name = "product_id") Long productId) {
    try {
      ProductDTO product = this.productService.getProductById(productId);
      BaseStatusCode successCode = SuccessCode.PRO2002_GET_PRODUCTS_BY_ID_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .data(product)
          .build()
          .buildResponseEntity(HttpStatus.OK);
    } catch (GlobalThrowable e) {
      log.error("Cannot get the product by ID: {}", e.getErrorCode().getMessage());
      BaseStatusCode errorCode = e.getErrorCode();
      return ResponseBody.builder()
          .code(errorCode.getCode())
          .message(errorCode.getMessage())
          .build()
          .buildResponseEntity(e.getHttpStatus());
    }
  }

  @PostMapping
  public HttpEntity<ResponseBody> createNewProduct(@RequestBody @Valid ProductDTO product) {
    try {
      ProductDTO createdProduct = this.productService.createProduct(product);
      BaseStatusCode successCode = SuccessCode.PRO2003_CREATE_NEW_PRODUCT_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .data(createdProduct)
          .build()
          .buildResponseEntity(HttpStatus.CREATED);
    } catch (GlobalThrowable e) {
      log.error("Cannot create a new product: {}", e.getErrorCode().getMessage());
      BaseStatusCode errorCode = e.getErrorCode();
      return ResponseBody.builder()
          .code(errorCode.getCode())
          .message(errorCode.getMessage())
          .build()
          .buildResponseEntity(e.getHttpStatus());
    }
  }

  @PutMapping("/{product_id}")
  public HttpEntity<ResponseBody> updateProductById(
      @PathVariable(name = "product_id") Long productId, @RequestBody @Valid ProductDTO product) {
    try {
      ProductDTO updatedProduct = productService.updateProduct(productId, product);
      BaseStatusCode successCode = SuccessCode.PRO2004_UPDATE_PRODUCT_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .data(updatedProduct)
          .build()
          .buildResponseEntity(HttpStatus.OK);
    } catch (GlobalThrowable e) {
      log.error("Cannot update the product by ID: {}", e.getErrorCode().getMessage());
      BaseStatusCode errorCode = e.getErrorCode();
      return ResponseBody.builder()
          .code(errorCode.getCode())
          .message(errorCode.getMessage())
          .build()
          .buildResponseEntity(e.getHttpStatus());
    }
  }

  @DeleteMapping("/{product_id}")
  public HttpEntity<ResponseBody> deleteProductById(
      @PathVariable(name = "product_id") Long productId) {
    try {
      productService.deleteProduct(productId);
      BaseStatusCode successCode = SuccessCode.PRO2005_DELETE_PRODUCT_IS_SUCCESS;
      return ResponseBody.builder()
          .code(successCode.getCode())
          .message(successCode.getMessage())
          .build()
          .buildResponseEntity(HttpStatus.OK);
    } catch (GlobalThrowable e) {
      log.error("Cannot delete the product by ID: {}", e.getErrorCode().getMessage());
      BaseStatusCode errorCode = e.getErrorCode();
      return ResponseBody.builder()
          .code(errorCode.getCode())
          .message(errorCode.getMessage())
          .build()
          .buildResponseEntity(e.getHttpStatus());
    }
  }
}
