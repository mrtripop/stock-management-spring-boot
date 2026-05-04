package com.mrtripop.component.fileparser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mrtripop.product.models.dto.ProductDTO;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonFileParserTest {

  private final JsonFileParser jsonFileParser = new JsonFileParser();

  @Test
  void testExport() {
    ProductDTO product = ProductDTO.builder()
        .id(1L)
        .code("SKU001")
        .name("Test Product")
        .isActive(true)
        .build();

    List<ProductDTO> data = Arrays.asList(product);
    byte[] result = jsonFileParser.export(data);

    assertNotNull(result);
    assertTrue(result.length > 0);
    String content = new String(result);
    assertTrue(content.contains("SKU001"));
    assertTrue(content.contains("Test Product"));
  }
}
