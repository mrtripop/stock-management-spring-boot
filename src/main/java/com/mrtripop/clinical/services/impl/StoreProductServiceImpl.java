package com.mrtripop.clinical.services.impl;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.models.dto.StoreProductDto;
import com.mrtripop.clinical.models.dto.UpdateOverrideRequest;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.clinical.services.StoreProductService;
import com.mrtripop.exception.NotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreProductServiceImpl implements StoreProductService {

  private final StoreRepository storeRepository;
  private final BrandRepository brandRepository;
  private final StoreProductRepository storeProductRepository;
  private final AuditService auditService;

  @Override
  @Transactional
  public StoreProductDto activateProduct(UUID storeId, UUID brandId) {
    log.info("Activating product brand {} for store {}", brandId, storeId);
    Store store = findStoreOrThrow(storeId);
    Brand brand = findBrandOrThrow(brandId);

    if (storeProductRepository.existsByStoreIdAndBrandId(storeId, brandId)) {
      throw new DuplicateStoreProductException(
          "Brand " + brandId + " is already activated for store " + storeId);
    }

    StoreProduct storeProduct =
        StoreProduct.builder()
            .store(store)
            .brand(brand)
            .isActive(true)
            .build();

    try {
      StoreProduct saved = storeProductRepository.save(storeProduct);
      auditService.recordAudit(
          "ACTIVATE_PRODUCT", "StoreProduct", saved.getId().toString(), null, brandId.toString());
      return enrichDto(saved);
    } catch (DataIntegrityViolationException e) {
      throw new DuplicateStoreProductException(
          "Brand " + brandId + " is already activated for store " + storeId);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Page<StoreProductDto> getActiveProducts(UUID storeId, Pageable pageable) {
    findStoreOrThrow(storeId);
    return storeProductRepository
        .findByStoreIdAndIsActiveTrue(storeId, pageable)
        .map(this::enrichDto);
  }

  @Override
  @Transactional(readOnly = true)
  public StoreProductDto getStoreProduct(UUID storeId, UUID productId) {
    StoreProduct storeProduct =
        storeProductRepository
            .findByIdAndStoreId(productId, storeId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "StoreProduct not found with id: " + productId + " for store: " + storeId));
    return enrichDto(storeProduct);
  }

  @Override
  @Transactional
  public StoreProductDto updateOverride(UUID storeId, UUID productId, UpdateOverrideRequest request) {
    log.info("Updating override for store product {} in store {}", productId, storeId);
    StoreProduct storeProduct =
        storeProductRepository
            .findByIdAndStoreId(productId, storeId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "StoreProduct not found with id: " + productId + " for store: " + storeId));

    String oldValue = buildOverrideValue(storeProduct);

    if (request.getPrice() != null) {
      storeProduct.setPrice(request.getPrice());
    }
    if (request.getShelfLocation() != null) {
      storeProduct.setShelfLocation(request.getShelfLocation());
    }

    StoreProduct saved = storeProductRepository.save(storeProduct);

    String newValue = buildOverrideValue(saved);
    if (!oldValue.equals(newValue)) {
      auditService.recordAudit(
          "UPDATE_OVERRIDE", "StoreProduct", productId.toString(), oldValue, newValue);
    }

    return enrichDto(saved);
  }

  @Override
  @Transactional
  public void deactivateProduct(UUID storeId, UUID productId) {
    log.info("Deactivating store product {} in store {}", productId, storeId);
    StoreProduct storeProduct =
        storeProductRepository
            .findByIdAndStoreId(productId, storeId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "StoreProduct not found with id: " + productId + " for store: " + storeId));

    if (!storeProduct.getIsActive()) {
      return;
    }

    storeProduct.setIsActive(false);
    storeProductRepository.save(storeProduct);

    auditService.recordAudit(
        "DEACTIVATE_PRODUCT",
        "StoreProduct",
        productId.toString(),
        "active",
        "inactive");
  }

  private Store findStoreOrThrow(UUID storeId) {
    return storeRepository
        .findById(storeId)
        .orElseThrow(() -> new NotFoundException("Store not found with id: " + storeId));
  }

  private Brand findBrandOrThrow(UUID brandId) {
    return brandRepository
        .findById(brandId)
        .orElseThrow(() -> new NotFoundException("Brand not found with id: " + brandId));
  }

  private StoreProductDto enrichDto(StoreProduct sp) {
    StoreProductDto dto = StoreProductDto.builder().build();
    dto.setId(sp.getId());
    dto.setStoreId(sp.getStore().getId());
    dto.setBrandId(sp.getBrand().getId());
    dto.setPrice(sp.getPrice());
    dto.setShelfLocation(sp.getShelfLocation());
    dto.setIsActive(sp.getIsActive());
    dto.setCreatedAt(sp.getCreatedAt());
    dto.setUpdatedAt(sp.getUpdatedAt());

    Brand brand = sp.getBrand();
    dto.setBrandName(brand.getBrandName());
    dto.setStrength(brand.getStrength());
    dto.setForm(brand.getForm());
    dto.setBaseUnit(brand.getBaseUnit());

    if (brand.getMolecule() != null) {
      dto.setMoleculeId(brand.getMolecule().getId());
      dto.setMoleculeGenericName(brand.getMolecule().getGenericName());
      dto.setTherapeuticClass(brand.getMolecule().getTherapeuticClass());
      dto.setRegulatorySchedule(brand.getMolecule().getRegulatorySchedule());
    }

    return dto;
  }

  private String buildOverrideValue(StoreProduct sp) {
    return String.format("price=%s, shelfLocation=%s", sp.getPrice(), sp.getShelfLocation());
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  public static class DuplicateStoreProductException extends RuntimeException {
    public DuplicateStoreProductException(String message) {
      super(message);
    }
  }
}
