package com.mrtripop.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/learning/strategies")
public class FileParserController {

  private final FileParserService fileParserService;

  public FileParserController(FileParserService fileParserService) {
    this.fileParserService = fileParserService;
  }

  @GetMapping("/file-parser")
  public void parse(
      @RequestParam(name = "file", defaultValue = "FILE") String file,
      @RequestParam(name = "fileType", defaultValue = "FILETYPE") String fileType) {
    log.info("Parsing file: {} with type: {}", file, fileType);
    fileParserService.parse(file, fileType);
  }
}
