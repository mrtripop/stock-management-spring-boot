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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import org.apache.commons.csv.CSVPrinter;

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

  public static byte[] productsToCsv(List<ProductDTO> products) {
    CSVFormat format = CSVFormat.DEFAULT.withHeader("id", "code", "barcode", "name", "description", "category", "reorder_quantity", "packed_weight", "packed_height", "packed_width", "packed_depth", "is_active");

    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {
      for (ProductDTO product : products) {
        List<String> data = Arrays.asList(
            String.valueOf(product.getId()),
            product.getCode(),
            product.getBarcode(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            String.valueOf(product.getReorderQuantity()),
            String.valueOf(product.getPackedWeight()),
            String.valueOf(product.getPackedHeight()),
            String.valueOf(product.getPackedWidth()),
            String.valueOf(product.getPackedDepth()),
            String.valueOf(product.getIsActive())
        );
        csvPrinter.printRecord(data);
      }

      csvPrinter.flush();
      return out.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("fail to import data to CSV file: " + e.getMessage());
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
