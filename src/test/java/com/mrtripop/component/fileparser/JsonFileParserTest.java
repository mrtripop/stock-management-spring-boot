package com.mrtripop.component.fileparser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mrtripop.component.fileparser.fixture.ProductDtoFixture;
import com.mrtripop.product.models.dto.ProductDTO;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product catalog data can be exported to JSON")
class JsonFileParserTest {

  private final JsonFileParser jsonFileParser = new JsonFileParser();

  @Test
  @DisplayName("Staff export the product catalog to JSON for external systems to consume")
  void shouldExportProductCatalogToJson() {
    // Arrange
    ProductDTO product = ProductDtoFixture.minimalProduct();
    List<ProductDTO> data = Arrays.asList(product);

    // Act
    byte[] result = jsonFileParser.export(data);

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);
    String content = new String(result);
    assertTrue(content.contains(ProductDtoFixture.SKU_CODE));
    assertTrue(content.contains(ProductDtoFixture.PRODUCT_NAME));
  }
}
