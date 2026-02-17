package com.mrtripop.util;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.product.constant.ErrorCode;
import com.mrtripop.product.models.dto.ProductDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

@UtilityClass
public class CsvHelper {
  public static final String TYPE = "text/csv";

  public static boolean hasCsvFormat(MultipartFile file) {
    return TYPE.equals(file.getContentType());
  }

  public static List<ProductDTO> readProductDTO(MultipartFile csvFile) throws ApplicationException {
    try (BufferedReader fileReader =
        new BufferedReader(
            new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
      CSVFormat format =
          CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim();
      CSVParser csvParser = new CSVParser(fileReader, format);
      return csvParser.getRecords().stream().map(toProductDTO()).toList();
    } catch (IOException e) {
      throw new ApplicationException(
          ErrorCode.PRO5001_READ_PRODUCTS_FROM_CSV_FAILED, HttpStatus.BAD_REQUEST);
    }
  }

  private static Function<CSVRecord, ProductDTO> toProductDTO() {
    return csvRecord ->
        ProductDTO.builder()
            .id(Long.parseLong(csvRecord.get("id")))
            .code(csvRecord.get("code"))
            .barcode(csvRecord.get("barcode"))
            .name(csvRecord.get("name"))
            .description(csvRecord.get("description"))
            .category(csvRecord.get("category"))
            .reorderQuantity(Integer.parseInt(csvRecord.get("reorder_quantity")))
            .packedWeight(Double.parseDouble(csvRecord.get("packed_weight")))
            .packedHeight(Double.parseDouble(csvRecord.get("packed_height")))
            .packedWidth(Double.parseDouble(csvRecord.get("packed_width")))
            .packedDepth(Double.parseDouble(csvRecord.get("packed_depth")))
            .isActive(Boolean.parseBoolean(csvRecord.get("is_active")))
            .build();
  }
}
