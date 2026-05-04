package com.mrtripop.component.fileparser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

@Slf4j
@Service(FileType.JSON)
public class JsonFileParser implements FileParser {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void parse(String file) {
    log.info("Parsing JSON file: {}", file);
  }

  @Override
  public <T> byte[] export(List<T> data) {
    log.info("Exporting data to JSON");
    try {
      return objectMapper.writeValueAsBytes(data);
    } catch (JsonProcessingException e) {
      log.error("Error exporting to JSON: {}", e.getMessage());
      return new byte[0];
    }
  }
}
