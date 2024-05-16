package com.mrtripop.product.services;

import com.mrtripop.exception.GlobalThrowable;
import com.mrtripop.product.interfaces.ProductService;
import com.mrtripop.product.models.ProductDTO;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

  private final ProductService databaseManager;

  public ProductServiceImpl(DatabaseManagementImpl databaseManager) {
    this.databaseManager = databaseManager;
  }

  @Override
  public List<ProductDTO> getProducts(Integer page, Integer size, Sort.Direction orderBy)
      throws GlobalThrowable {
    return databaseManager.getProducts(page, size, orderBy);
  }

  @Override
  @Cacheable(value = "product", key = "#id", unless = "#result==null")
  public ProductDTO getProductById(Long id) throws GlobalThrowable {
    return databaseManager.getProductById(id);
  }

  @Override
  public ProductDTO createProduct(ProductDTO newProduct) throws GlobalThrowable {
    // send email or produce queue or something here
    return databaseManager.createProduct(newProduct);
  }

  @Override
  @CachePut(value = "product", key = "#id")
  public ProductDTO updateProduct(Long id, ProductDTO updateProduct) throws GlobalThrowable {
    // send email or produce queue or something here
    return databaseManager.updateProduct(id, updateProduct);
  }

  @Override
  @CacheEvict(value = "product", allEntries = true)
  public void deleteProduct(Long id) throws GlobalThrowable {
    // send email or produce queue or something here
    databaseManager.deleteProduct(id);
  }
}