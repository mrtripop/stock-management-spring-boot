package com.mrtripop.component.fileparser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mrtripop.component.fileparser.fixture.ProductDtoFixture;
import com.mrtripop.product.models.dto.ProductDTO;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product catalog data can be exported to CSV")
class CsvFileParserTest {

  private final CsvFileParser csvFileParser = new CsvFileParser();

  @Test
  @DisplayName("Staff export the product catalog to CSV for external systems to consume")
  void shouldExportProductCatalogToCsv() {
    // Arrange
    ProductDTO product = ProductDtoFixture.fullProduct();
    List<ProductDTO> data = Arrays.asList(product);

    // Act
    byte[] result = csvFileParser.export(data);

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);
    String csvContent = new String(result);
    assertTrue(csvContent.contains(ProductDtoFixture.SKU_CODE));
    assertTrue(csvContent.contains(ProductDtoFixture.PRODUCT_NAME));
  }
}
