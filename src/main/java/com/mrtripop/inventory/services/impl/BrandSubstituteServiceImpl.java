package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.BrandSubstituteDto;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.BrandSubstituteService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandSubstituteServiceImpl implements BrandSubstituteService {

  private final StoreRepository storeRepository;
  private final BrandRepository brandRepository;
  private final StoreStockRepository storeStockRepository;
  private final StoreProductRepository storeProductRepository;

  @Override
  @Transactional(readOnly = true)
  public List<BrandSubstituteDto> findSubstitutes(UUID storeId, UUID brandId)
      throws ApplicationException {
    if (!storeRepository.existsById(storeId)) {
      throw new ApplicationException(ErrorCode.STORE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    Brand requestedBrand =
        brandRepository
            .findById(brandId)
            .orElseThrow(
                () -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND, HttpStatus.NOT_FOUND));

    // Without strength and form on record, equivalence cannot be confirmed; suggesting a brand
    // anyway risks handing the patient a different dose.
    if (requestedBrand.getStrength() == null || requestedBrand.getForm() == null) {
      return Collections.emptyList();
    }

    List<StoreStock> equivalentStock =
        storeStockRepository.findAvailableSubstituteStock(
            storeId,
            requestedBrand.getMolecule().getId(),
            requestedBrand.getStrength(),
            requestedBrand.getForm(),
            brandId);
    if (equivalentStock.isEmpty()) {
      return Collections.emptyList();
    }

    // Stock arrives ordered by expiry, so grouping in encounter order keeps the soonest-expiring
    // brand first and makes each brand's first row its nearest-expiry batch.
    Map<UUID, List<StoreStock>> stockByBrandId =
        equivalentStock.stream()
            .collect(
                Collectors.groupingBy(
                    stock -> stock.getBatch().getBrand().getId(),
                    LinkedHashMap::new,
                    Collectors.toList()));

    Map<UUID, BigDecimal> priceByBrandId =
        storeProductRepository
            .findByStoreIdAndBrandIdInAndIsActiveTrueAndPriceIsNotNull(
                storeId, stockByBrandId.keySet())
            .stream()
            .collect(
                Collectors.toMap(
                    storeProduct -> storeProduct.getBrand().getId(), StoreProduct::getPrice));

    return stockByBrandId.entrySet().stream()
        .filter(entry -> priceByBrandId.containsKey(entry.getKey()))
        .map(entry -> toSubstituteDto(entry.getValue(), priceByBrandId.get(entry.getKey())))
        .toList();
  }

  private BrandSubstituteDto toSubstituteDto(List<StoreStock> brandStock, BigDecimal price) {
    StoreStock nearestExpiryStock = brandStock.get(0);
    Brand brand = nearestExpiryStock.getBatch().getBrand();
    long availableQuantity = brandStock.stream().mapToLong(StoreStock::getQuantity).sum();

    return BrandSubstituteDto.builder()
        .brandId(brand.getId())
        .brandName(brand.getBrandName())
        .strength(brand.getStrength())
        .form(brand.getForm())
        .price(price)
        .availableQuantity(availableQuantity)
        .nearestExpiryDate(nearestExpiryStock.getBatch().getExpiryDate())
        .nearestExpiryBatchId(nearestExpiryStock.getBatch().getId())
        .build();
  }
}
