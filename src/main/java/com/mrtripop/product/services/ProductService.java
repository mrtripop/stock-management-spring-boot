package com.mrtripop.product.services;

import com.mrtripop.exception.GlobalThrowable;
import com.mrtripop.model.QueryParams;
import com.mrtripop.product.models.dto.ProductDTO;
import java.util.List;

public interface ProductService {
  List<ProductDTO> getProducts(QueryParams queryParams) throws GlobalThrowable;

  ProductDTO getProductById(Long id) throws GlobalThrowable;

  ProductDTO createProduct(ProductDTO newProduct) throws GlobalThrowable;

  ProductDTO updateProduct(Long id, ProductDTO updateProduct) throws GlobalThrowable;

  void deleteProduct(Long id) throws GlobalThrowable;
}
