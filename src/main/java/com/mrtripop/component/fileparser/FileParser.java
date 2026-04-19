package com.mrtripop.component.fileparser;

import java.util.List;

public interface FileParser {
  void parse(String file);

  <T> byte[] export(List<T> data);
}
