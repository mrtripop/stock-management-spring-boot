package learning.patterns.strategies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service(FileType.JSON)
public class JsonFileParser implements FileParser {
  @Override
  public void parse(String file) {
    log.info("Parsing JSON file: {}", file);
  }
}
