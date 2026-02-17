package com.mrtripop.product.util;

import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.product.models.db.ProductHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public class ProductUtil {

  private ProductUtil() {}

  public static Specification<ProductHistory> productsHaveCode(String code) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("code").as(String.class), code);
  }

  public static PageRequest initSortOrder(PageRequest pageRequest, Sort.Direction orderBy) {
    return pageRequest.withSort(orderBy, "id");
  }

  public static PageRequest initPageable(Integer page, Integer size) {
    // page is start by 0 but page number should start from 1
    return PageRequest.of(page - 1, size);
  }

  public static Pageable initPageableWithSort(BaseQueryParams queryParams) {
    PageRequest pageRequest = initPageable(queryParams.getPage(), queryParams.getSize());
    return initSortOrder(pageRequest, queryParams.getOrderBy());
  }
}
