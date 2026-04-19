package com.mrtripop.component.fileparser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.List;

@Slf4j
@Service(FileType.XML)
public class XmlFileParser implements FileParser {
  private final XmlMapper xmlMapper = new XmlMapper();

  @Override
  public void parse(String file) {
    log.info("Parsing XML file: {}", file);
  }

  @Override
  public <T> byte[] export(List<T> data) {
    log.info("Exporting data to XML");
    try {
      return xmlMapper.writeValueAsBytes(data);
    } catch (JsonProcessingException e) {
      log.error("Error exporting to XML: {}", e.getMessage());
      return new byte[0];
    }
  }
}
