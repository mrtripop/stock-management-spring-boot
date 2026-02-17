package com.mrtripop.product.services.manager;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.product.constant.ErrorCode;
import com.mrtripop.product.models.db.Product;
import com.mrtripop.product.models.db.ProductHistory;
import com.mrtripop.product.repository.ProductHistoryRepository;
import com.mrtripop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductManager {
  private final ProductRepository productRepository;
  private final ProductHistoryRepository productHistoryRepository;

  /// Product Table

  public Page<Product> getProducts(Pageable pageSize) {
    return productRepository.findAll(pageSize);
  }

  public Optional<Product> getProductById(Long id) {
    return productRepository.findById(id);
  }

  public List<Product> getProductsByIds(List<Long> ids) {
    return productRepository.findAllById(ids);
  }

  public Product createProduct(Product product, ProductHistory productHistory) {
    productHistoryRepository.save(productHistory);
    return productRepository.save(product);
  }

  public void updateProduct(Product product, ProductHistory productHistory) {
    productHistoryRepository.save(productHistory);
    productRepository.deleteById(product.getId());
  }

  public List<Product> updateProducts(List<Product> products, ProductHistory productHistory) {
    productHistoryRepository.save(productHistory);
    return productRepository.saveAll(products);
  }

  /// Product History Table

  public Page<ProductHistory> getProductHistories(Pageable pageSize) {
    return productHistoryRepository.findAll(pageSize);
  }
}
