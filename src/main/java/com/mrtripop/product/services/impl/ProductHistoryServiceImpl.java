package com.mrtripop.product.services.impl;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.product.component.ProductMapper;
import com.mrtripop.product.constant.ErrorCode;
import com.mrtripop.product.models.db.ProductHistory;
import com.mrtripop.product.models.dto.ProductDTO;
import com.mrtripop.product.repository.ProductHistoryRepository;
import com.mrtripop.product.services.ProductHistoryService;
import com.mrtripop.product.util.ProductUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductHistoryServiceImpl implements ProductHistoryService {

  private ProductHistoryRepository productHistoryRepository;
  private static final ProductMapper productMapper = ProductMapper.INSTANCE;

  @Override
  public List<ProductDTO> getProductsHistories(BaseQueryParams queryParams) throws ApplicationException {
    try {
      Pageable pageable = ProductUtil.initPageableWithSort(queryParams);
      Page<ProductHistory> productHistoriesPages = productHistoryRepository.findAll(pageable);
      return productHistoriesPages.getContent().stream().map(productMapper::toProductDTO).toList();
    } catch (Exception e) {
      log.error("Cannot get product histories: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1007_CANNOT_GET_ALL_PRODUCT_HISTORIES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public List<ProductDTO> getProductHistoriesByCode(String code, BaseQueryParams queryParams)
      throws ApplicationException {
    try {
      // create a specification for query column
      // create a sort and pagination
      Specification<ProductHistory> specification = ProductUtil.productsHaveCode(code);
      Pageable pageable = ProductUtil.initPageableWithSort(queryParams);
      Page<ProductHistory> productHistoriesPages =
          productHistoryRepository.findAll(specification, pageable);
      return productHistoriesPages.getContent().stream().map(productMapper::toProductDTO).toList();
    } catch (Exception e) {
      log.error("Cannot get product histories by code: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1011_CANNOT_GET_PRODUCT_HISTORIES_BY_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
