package com.mrtripop.clinical.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mrtripop.clinical.component.StoreProductMapper;
import com.mrtripop.clinical.fixture.StoreFixture;
import com.mrtripop.clinical.fixture.StoreProductFixture;
import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.models.dto.ActivateProductRequest;
import com.mrtripop.clinical.models.dto.StoreProductDto;
import com.mrtripop.clinical.models.dto.UpdateOverrideRequest;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.impl.StoreProductServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pharmacy staff configure store catalog products and par-level replenishment targets")
class StoreProductServiceTest {

  @Mock private StoreRepository storeRepository;
  @Mock private BrandRepository brandRepository;
  @Mock private StoreProductRepository storeProductRepository;
  @Mock private AuditService auditService;

  @InjectMocks private StoreProductServiceImpl storeProductService;

  private final UUID storeId = UUID.randomUUID();
  private final UUID brandId = UUID.randomUUID();

  @Nested
  @DisplayName("When activating a product with target stock par level")
  class ActivateProductWithTargetStock {

    @Test
    @DisplayName("should map and persist target stock when activating store product with par level")
    void shouldMapAndPersistTargetStock_whenActivatingStoreProduct() {
      // Arrange
      Store store = StoreFixture.defaultEntity();
      Molecule molecule = Molecule.builder().id(UUID.randomUUID()).genericName("Paracetamol").build();
      Brand brand = Brand.builder().id(brandId).molecule(molecule).brandName("Tylenol").build();
      UUID spId = UUID.randomUUID();
      Long targetStock = StoreProductFixture.DEFAULT_TARGET_STOCK;

      ActivateProductRequest request =
          ActivateProductRequest.builder().brandId(brandId).targetStock(targetStock).build();

      StoreProduct saved = StoreProductFixture.activeEntity(storeId, brandId, spId);
      saved.setTargetStock(targetStock);

      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
      when(storeProductRepository.existsByStoreIdAndBrandId(storeId, brandId)).thenReturn(false);
      when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(saved);

      // Act
      StoreProductDto result = storeProductService.activateProduct(storeId, request);

      // Assert
      ArgumentCaptor<StoreProduct> captor = ArgumentCaptor.forClass(StoreProduct.class);
      verify(storeProductRepository).save(captor.capture());
      assertEquals(targetStock, captor.getValue().getTargetStock());
      assertNotNull(result);
      assertEquals(targetStock, result.getTargetStock());
    }
  }

  @Nested
  @DisplayName("When updating store product override values")
  class UpdateOverrideWithTargetStock {

    @Test
    @DisplayName("should update and persist target stock when par level override is supplied")
    void shouldUpdateAndPersistTargetStock_whenOverrideIsProvided() {
      // Arrange
      UUID spId = UUID.randomUUID();
      StoreProduct sp = StoreProductFixture.activeEntity(storeId, brandId, spId);
      sp.setTargetStock(StoreProductFixture.DEFAULT_TARGET_STOCK);
      Long updatedTargetStock = StoreProductFixture.UPDATED_TARGET_STOCK;

      UpdateOverrideRequest request =
          UpdateOverrideRequest.builder().targetStock(updatedTargetStock).build();

      when(storeProductRepository.findByIdAndStoreId(spId, storeId)).thenReturn(Optional.of(sp));
      when(storeProductRepository.save(any(StoreProduct.class))).thenAnswer(inv -> inv.getArgument(0));

      // Act
      StoreProductDto result = storeProductService.updateOverride(storeId, spId, request);

      // Assert
      ArgumentCaptor<StoreProduct> captor = ArgumentCaptor.forClass(StoreProduct.class);
      verify(storeProductRepository).save(captor.capture());
      assertEquals(updatedTargetStock, captor.getValue().getTargetStock());
      assertEquals(updatedTargetStock, result.getTargetStock());
    }
  }

  @Nested
  @DisplayName("When mapping store product via MapStruct")
  class StoreProductMapping {

    @Test
    @DisplayName("should map target stock from entity to dto")
    void shouldMapTargetStockFromEntityToDto() {
      // Arrange
      StoreProduct sp = StoreProductFixture.activeEntity(storeId, brandId, UUID.randomUUID());
      sp.setTargetStock(StoreProductFixture.DEFAULT_TARGET_STOCK);
      StoreProductMapper mapper = Mappers.getMapper(StoreProductMapper.class);

      // Act
      StoreProductDto dto = mapper.toStoreProductDto(sp);

      // Assert
      assertNotNull(dto);
      assertEquals(StoreProductFixture.DEFAULT_TARGET_STOCK, dto.getTargetStock());
    }
  }
}
