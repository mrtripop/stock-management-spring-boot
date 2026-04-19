package com.mrtripop.component.fileparser;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FileParserFactory {

  private final Map<String, FileParser> fileParsers;

  public FileParserFactory(Map<String, FileParser> fileParsers) {
    this.fileParsers = fileParsers;
  }

  public FileParser get(String fileType) {
    log.info("Getting file parser for file type: {}", fileType);
    log.debug("All file parsers: {}", fileParsers);
    return fileParsers.get(fileType);
  }
}
