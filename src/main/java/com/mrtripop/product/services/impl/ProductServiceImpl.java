package com.mrtripop.product.services.impl;

import com.mrtripop.component.fileparser.FileParser;
import com.mrtripop.component.fileparser.FileParserFactory;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.product.component.ProductMapper;
import com.mrtripop.product.constant.ErrorCode;
import com.mrtripop.product.models.db.Product;
import com.mrtripop.product.models.db.ProductHistory;
import com.mrtripop.product.models.dto.ProductDTO;
import com.mrtripop.product.repository.ProductHistoryRepository;
import com.mrtripop.product.repository.ProductRepository;
import com.mrtripop.product.services.ProductService;
import com.mrtripop.product.services.manager.ProductManager;
import com.mrtripop.product.util.ProductUtil;
import com.mrtripop.util.CsvHelper;
import com.mrtripop.util.Helper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  @Value("${stock.products.update.batch-size:1000}")
  private int batchSize;

  private final ProductRepository productRepository;
  private final ProductHistoryRepository productHistoryRepository;
  private final ProductManager productManager;
  private final FileParserFactory fileParserFactory;
  private static final ProductMapper productMapper = ProductMapper.INSTANCE;

  @Override
  public List<ProductDTO> getProducts(BaseQueryParams queryParams) {
    Pageable pageSize = ProductUtil.initPageableWithSort(queryParams);
    Page<Product> productPages = productManager.getProducts(pageSize);
    List<Product> products = productPages.getContent();
    return products.stream().map(productMapper::toProductDTO).toList();
  }

  @Override
  @Cacheable(value = "product", key = "#id", unless = "#result==null")
  public ProductDTO getProductById(Long id) throws ApplicationException {
    Optional<Product> haveProduct = productRepository.findById(id);
    Product product =
        haveProduct.orElseThrow(
            () ->
                new ApplicationException(
                    ErrorCode.PRO1003_CANNOT_GET_PRODUCT_BY_ID, HttpStatus.NOT_FOUND));
    return productMapper.toProductDTO(product);
  }

  @Override
  @Transactional(rollbackOn = {ApplicationException.class})
  public ProductDTO createProduct(ProductDTO productDTO) throws ApplicationException {
    try {
      Product product = productMapper.toProduct(productDTO);
      ProductHistory productHistory = productMapper.toProductHistory(productDTO);
      productHistory.setId(null);
      productHistoryRepository.save(productHistory);
      Product productSaved = productRepository.save(product);
      return productMapper.toProductDTO(productSaved);
    } catch (Exception e) {
      log.error("Cannot create a new product: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1002_CANNOT_CREATE_NEW_PRODUCT, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  @CachePut(value = "product", key = "#id")
  @Transactional(rollbackOn = {ApplicationException.class})
  public ProductDTO updateProduct(Long id, ProductDTO updateProduct) throws ApplicationException {
    try {
      ProductDTO existingProduct = getProductById(id);
      updateProduct(existingProduct, updateProduct);
      return createProduct(existingProduct);
    } catch (Exception e) {
      log.error("Cannot update product ID='{}': {}", id, e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1004_CANNOT_UPDATE_EXISTING_PRODUCT, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  @CacheEvict(value = "product", allEntries = true)
  @Transactional(rollbackOn = {ApplicationException.class})
  public void deleteProduct(Long id) throws ApplicationException {
    try {
      ProductDTO existingProduct = getProductById(id);
      productRepository.deleteById(id);
      ProductHistory producthistory = productMapper.toProductHistory(existingProduct);
      producthistory.setId(null);
      producthistory.setIsActive(false);
      productHistoryRepository.save(producthistory);
    } catch (Exception e) {
      log.error("Cannot delete product ID='{}': {}", id, e.getMessage());
      throw new ApplicationException(
          ErrorCode.PRO1005_CANNOT_DELETE_EXISTING_PRODUCT, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public List<ProductDTO> uploadProductByCsv(MultipartFile csvFile) throws ApplicationException {
    if (!CsvHelper.hasCsvFormat(csvFile)) {
      throw new ApplicationException(
          ErrorCode.PRO4001_USER_NEED_TO_UPLOAD_PRODUCT_CSV_FILE, HttpStatus.BAD_REQUEST);
    }
    return Helper.splitBatch(CsvHelper.readProductDTO(csvFile), batchSize).stream()
        .parallel()
        .flatMap(
            productDTOs -> {
              // find products by list of ID
              List<Long> productIds = productDTOs.stream().map(ProductDTO::getId).toList();
              List<Product> products = productRepository.findAllById(productIds);
              Map<Long, Product> productsMap =
                  products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
              // update products
              List<Product> updateProducts =
                  productDTOs.stream().map(addOrUpdateProduct(productsMap)).toList();
              // insert all
              productHistoryRepository.saveAll(
                  updateProducts.stream().map(addProductHistory()).toList());
              return productRepository.saveAll(updateProducts).stream()
                  .map(productMapper::toProductDTO);
            })
        .toList();
  }

  @Override
  public byte[] exportProducts(String fileType) throws ApplicationException {
    log.info("Exporting products with file type: {}", fileType);
    List<Product> products = productRepository.findAll();
    List<ProductDTO> productDTOs = products.stream().map(productMapper::toProductDTO).toList();

    FileParser fileParser = fileParserFactory.get(fileType);
    if (fileParser == null) {
      throw new ApplicationException(ErrorCode.PRO1001_CANNOT_GET_ALL_PRODUCTS, HttpStatus.BAD_REQUEST);
    }

    return fileParser.export(productDTOs);
  }

  private Function<Product, ProductHistory> addProductHistory() {
    return product -> {
      ProductHistory history = productMapper.toProductHistory(product);
      history.setId(null); // Force insert only, not update (for audit)
      return history;
    };
  }

  private Function<ProductDTO, Product> addOrUpdateProduct(Map<Long, Product> productsMap) {
    return productDTO -> {
      Product existingProduct = productsMap.get(productDTO.getId());
      if (existingProduct != null) {
        Product updateProduct = productMapper.toProduct(productDTO);
        updateProduct.setId(existingProduct.getId());
        return updateProduct;
      }
      return productMapper.toProduct(productDTO);
    };
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
    // Other fields
  }
}
