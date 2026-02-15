package com.mrtripop.product.services;

import com.mrtripop.exception.GlobalThrowable;
import com.mrtripop.model.QueryParams;
import com.mrtripop.product.models.dto.ProductDTO;
import java.util.List;

public interface ProductHistoryService {

  List<ProductDTO> getProductsHistories(QueryParams queryParams) throws GlobalThrowable;

  List<ProductDTO> getProductHistoriesByCode(String code, QueryParams queryParams)
      throws GlobalThrowable;
}
