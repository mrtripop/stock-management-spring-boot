package com.mrtripop.component.fileparser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mrtripop.product.models.dto.ProductDTO;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvFileParserTest {

  private final CsvFileParser csvFileParser = new CsvFileParser();

  @Test
  void testExport() {
    ProductDTO product = ProductDTO.builder()
        .id(1L)
        .code("SKU001")
        .barcode("123456789")
        .name("Test Product")
        .description("Description")
        .category("Category")
        .reorderQuantity(10)
        .packedWeight(1.0)
        .packedHeight(1.0)
        .packedWidth(1.0)
        .packedDepth(1.0)
        .isActive(true)
        .build();

    List<ProductDTO> data = Arrays.asList(product);
    byte[] result = csvFileParser.export(data);

    assertNotNull(result);
    assertTrue(result.length > 0);
    String csvContent = new String(result);
    assertTrue(csvContent.contains("SKU001"));
    assertTrue(csvContent.contains("Test Product"));
  }
}
