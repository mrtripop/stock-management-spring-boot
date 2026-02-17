package com.mrtripop.product.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.product.models.dto.ProductDTO;
import java.util.List;

public interface ProductHistoryService {

  List<ProductDTO> getProductsHistories(BaseQueryParams queryParams) throws ApplicationException;

  List<ProductDTO> getProductHistoriesByCode(String code, BaseQueryParams queryParams)
      throws ApplicationException;
}
