package com.mrtripop.component.fileparser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mrtripop.product.models.dto.ProductDTO;
import com.mrtripop.util.CsvHelper;
import java.util.List;

@Slf4j
@Service(FileType.CSV)
public class CsvFileParser implements FileParser {
  @Override
  public void parse(String file) {
    log.info("Parsing CSV file: {}", file);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> byte[] export(List<T> data) {
    log.info("Exporting data to CSV");
    if (data == null || data.isEmpty()) {
      return new byte[0];
    }
    // For now, assuming T is ProductDTO
    if (data.get(0) instanceof ProductDTO) {
      return CsvHelper.productsToCsv((List<ProductDTO>) data);
    }
    throw new UnsupportedOperationException("CSV export only supports ProductDTO for now");
  }
}
