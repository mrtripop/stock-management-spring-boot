package com.mrtripop.util;

import java.util.List;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Helper {

  public static <T> List<List<T>> splitBatch(List<T> productDTOs, int batchSize) {
    return IntStream.range(0, (productDTOs.size() + batchSize - 1) / batchSize)
        .mapToObj(
            page ->
                productDTOs.subList(
                    page * batchSize, Math.min((page + 1) * batchSize, productDTOs.size())))
        .toList();
  }
}
