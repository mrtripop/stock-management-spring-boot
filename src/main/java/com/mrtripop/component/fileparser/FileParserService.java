package com.mrtripop.component.fileparser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileParserService {
  private final FileParserFactory fileParserFactory;

  public FileParserService(FileParserFactory fileParserFactory) {
    this.fileParserFactory = fileParserFactory;
  }

  public void parse(String file, String fileType) {
    FileParser fileParser = fileParserFactory.get(fileType);
    fileParser.parse(file);
  }
}
