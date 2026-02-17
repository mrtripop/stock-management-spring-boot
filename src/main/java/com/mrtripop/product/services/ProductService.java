package com.mrtripop.product.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.product.models.dto.ProductDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
  List<ProductDTO> getProducts(BaseQueryParams queryParams) throws ApplicationException;

  ProductDTO getProductById(Long id) throws ApplicationException;

  ProductDTO createProduct(ProductDTO newProduct) throws ApplicationException;

  ProductDTO updateProduct(Long id, ProductDTO updateProduct) throws ApplicationException;

  void deleteProduct(Long id) throws ApplicationException;

  List<ProductDTO> updateProductByCsv(MultipartFile csvFile) throws ApplicationException;
}
