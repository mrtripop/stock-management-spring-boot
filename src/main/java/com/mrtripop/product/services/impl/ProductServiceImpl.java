package com.mrtripop.product.services.impl;

import com.mrtripop.exception.GlobalThrowable;
import com.mrtripop.model.QueryParams;
import com.mrtripop.product.component.ProductMapper;
import com.mrtripop.product.constant.ErrorCode;
import com.mrtripop.product.services.ProductService;
import com.mrtripop.product.models.db.Product;
import com.mrtripop.product.models.db.ProductHistory;
import com.mrtripop.product.models.dto.ProductDTO;
import com.mrtripop.product.repository.ProductHistoryRepository;
import com.mrtripop.product.repository.ProductRepository;
import com.mrtripop.product.util.ProductUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductHistoryRepository productHistoryRepository;
  private static final ProductMapper productMapper = ProductMapper.INSTANCE;

  public ProductServiceImpl(
      ProductRepository productRepository, ProductHistoryRepository productHistoryRepository) {
    this.productRepository = productRepository;
    this.productHistoryRepository = productHistoryRepository;
  }

  @Override
  public List<ProductDTO> getProducts(QueryParams queryParams) {
    Pageable pageSize = ProductUtil.initPageableWithSort(queryParams);
    Page<Product> productPages = productRepository.findAll(pageSize);
    List<Product> products = productPages.getContent();
    return products.stream().map(productMapper::toProductDTO).toList();
  }

  @Override
  @Cacheable(value = "product", key = "#id", unless = "#result==null")
  public ProductDTO getProductById(Long id) throws GlobalThrowable {
    Optional<Product> haveProduct = productRepository.findById(id);
    Product product =
        haveProduct.orElseThrow(
            () ->
                new GlobalThrowable(
                    ErrorCode.PRO1003_CANNOT_GET_PRODUCT_BY_ID, HttpStatus.NOT_FOUND));
    return productMapper.toProductDTO(product);
  }

  @Override
  @Transactional(rollbackOn = {GlobalThrowable.class})
  public ProductDTO createProduct(ProductDTO productDTO) throws GlobalThrowable {
    try {
      Product product = productMapper.toProduct(productDTO);
      ProductHistory productHistory = productMapper.toProductHistory(productDTO);
      productHistoryRepository.save(productHistory);
      Product productSaved = productRepository.save(product);
      return productMapper.toProductDTO(productSaved);
    } catch (Exception e) {
      log.error("Cannot create a new product: {}", e.getMessage());
      throw new GlobalThrowable(
          ErrorCode.PRO1002_CANNOT_CREATE_NEW_PRODUCT, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  @Transactional(rollbackOn = {GlobalThrowable.class})
  @CachePut(value = "product", key = "#id")
  public ProductDTO updateProduct(Long id, ProductDTO updateProduct) throws GlobalThrowable {
    try {
      ProductDTO existingProduct = getProductById(id);
      updateProduct(existingProduct, updateProduct);
      return createProduct(existingProduct);
    } catch (Exception e) {
      log.error("Cannot update product ID='{}': {}", id, e.getMessage());
      throw new GlobalThrowable(
          ErrorCode.PRO1004_CANNOT_UPDATE_EXISTING_PRODUCT, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  @Transactional(rollbackOn = {GlobalThrowable.class})
  @CacheEvict(value = "product", allEntries = true)
  public void deleteProduct(Long id) throws GlobalThrowable {
    try {
      ProductDTO existingProduct = getProductById(id);
      productRepository.deleteById(id);
      ProductHistory producthistory = productMapper.toProductHistory(existingProduct);
      producthistory.setIsActive(false);
      productHistoryRepository.save(producthistory);
    } catch (Exception e) {
      log.error("Cannot delete product ID='{}': {}", id, e.getMessage());
      throw new GlobalThrowable(
          ErrorCode.PRO1005_CANNOT_DELETE_EXISTING_PRODUCT, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private void updateProduct(ProductDTO oldProduct, ProductDTO newProduct) {
    oldProduct.setCode(newProduct.getCode());
    oldProduct.setBarcode(newProduct.getBarcode());
    oldProduct.setName(newProduct.getName());
    oldProduct.setDescription(newProduct.getDescription());
    oldProduct.setCategory(newProduct.getCategory());
    oldProduct.setReorderQuantity(newProduct.getReorderQuantity());
    oldProduct.setPackedWeight(newProduct.getPackedWeight());
    oldProduct.setPackedHeight(newProduct.getPackedHeight());
    oldProduct.setPackedWidth(newProduct.getPackedWidth());
    oldProduct.setPackedDepth(newProduct.getPackedDepth());
    oldProduct.setIsActive(newProduct.getIsActive());
  }
}
