package com.mrtripop.clinical.services;

import com.mrtripop.clinical.models.dto.StoreProductDto;
import com.mrtripop.clinical.models.dto.UpdateOverrideRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreProductService {

  StoreProductDto activateProduct(UUID storeId, UUID brandId);

  Page<StoreProductDto> getActiveProducts(UUID storeId, Pageable pageable);

  StoreProductDto getStoreProduct(UUID storeId, UUID productId);

  StoreProductDto updateOverride(UUID storeId, UUID productId, UpdateOverrideRequest request);

  void deactivateProduct(UUID storeId, UUID productId);
}
