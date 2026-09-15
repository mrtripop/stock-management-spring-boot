package com.mrtripop.component.fileparser.fixture;

import com.mrtripop.product.models.dto.ProductDTO;

public final class ProductDtoFixture {

  private ProductDtoFixture() {}

  // Shared constants — single source of truth for both setup and assertions
  public static final String SKU_CODE = "SKU001";
  public static final String PRODUCT_NAME = "Test Product";

  public static ProductDTO fullProduct() {
    return ProductDTO.builder()
        .id(1L)
        .code(SKU_CODE)
        .barcode("123456789")
        .name(PRODUCT_NAME)
        .description("Description")
        .category("Category")
        .reorderQuantity(10)
        .packedWeight(1.0)
        .packedHeight(1.0)
        .packedWidth(1.0)
        .packedDepth(1.0)
        .isActive(true)
        .build();
  }

  public static ProductDTO minimalProduct() {
    return ProductDTO.builder().id(1L).code(SKU_CODE).name(PRODUCT_NAME).isActive(true).build();
  }
}
