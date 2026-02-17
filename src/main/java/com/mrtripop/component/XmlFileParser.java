package com.mrtripop.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service(FileType.XML)
public class XmlFileParser implements FileParser {
  @Override
  public void parse(String file) {
    log.info("Parsing XML file: {}", file);
  }
}
