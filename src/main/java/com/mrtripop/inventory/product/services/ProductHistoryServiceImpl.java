package com.mrtripop.inventory.product.services;

import com.mrtripop.inventory.exception.GlobalThrowable;
import com.mrtripop.inventory.product.component.ProductProcessor;
import com.mrtripop.inventory.product.constant.ErrorCode;
import com.mrtripop.inventory.product.models.ProductDTO;
import com.mrtripop.inventory.product.models.db.ProductHistory;
import com.mrtripop.inventory.product.repository.ProductHistoryRepository;
import com.mrtripop.inventory.product.util.ProductUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductHistoryServiceImpl {

  private final ProductHistoryRepository productHistoryRepository;

  public ProductHistoryServiceImpl(ProductHistoryRepository productHistoryRepository) {
    this.productHistoryRepository = productHistoryRepository;
  }

  public List<ProductDTO> getProductsHistories(Integer page, Integer size, Sort.Direction orderBy)
      throws GlobalThrowable {
    try {
      Pageable pageable = ProductUtil.initPageableWithSort(page, size, orderBy);
      Page<ProductHistory> productHistoriesPages = productHistoryRepository.findAll(pageable);
      return productHistoriesPages.getContent().stream()
          .map(ProductProcessor::mapToProductDTO)
          .toList();
    } catch (Exception e) {
      log.error("Cannot get product histories: {}", e.getMessage());
      throw new GlobalThrowable(
          ErrorCode.PRO1007_CANNOT_GET_ALL_PRODUCT_HISTORIES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public List<ProductDTO> getProductHistoriesByCode(
      String code, Integer page, Integer size, Sort.Direction orderBy) throws GlobalThrowable {
    try {
      // create a specification for query column
      // create a sort and pagination
      Specification<ProductHistory> specification = ProductUtil.productsHaveCode(code);
      Pageable pageable = ProductUtil.initPageableWithSort(page, size, orderBy);
      Page<ProductHistory> productHistoriesPages =
          productHistoryRepository.findAll(specification, pageable);
      return productHistoriesPages.getContent().stream()
          .map(ProductProcessor::mapToProductDTO)
          .toList();
    } catch (Exception e) {
      log.error("Cannot get product histories by code: {}", e.getMessage());
      throw new GlobalThrowable(
          ErrorCode.PRO1011_CANNOT_GET_PRODUCT_HISTORIES_BY_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
