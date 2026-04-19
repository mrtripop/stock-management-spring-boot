package com.mrtripop.clinical.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.models.dto.StoreProductDto;
import com.mrtripop.clinical.models.dto.UpdateOverrideRequest;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.NotFoundException;
import com.mrtripop.clinical.fixture.StoreFixture;
import com.mrtripop.clinical.fixture.StoreProductFixture;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreProductServiceImpl")
class StoreProductServiceImplTest {

  @Mock private StoreRepository storeRepository;
  @Mock private BrandRepository brandRepository;
  @Mock private StoreProductRepository storeProductRepository;
  @Mock private AuditService auditService;

  @InjectMocks private StoreProductServiceImpl storeProductService;

  private final UUID storeId = UUID.randomUUID();
  private final UUID brandId = UUID.randomUUID();

  @Nested
  @DisplayName("activateProduct")
  class ActivateProduct {

    @Test
    @DisplayName("should activate product and record audit")
    void shouldActivateAndRecordAudit() {
      Store store = StoreFixture.defaultEntity();
      Molecule molecule = Molecule.builder().id(UUID.randomUUID()).genericName("Paracetamol").build();
      Brand brand = Brand.builder().id(brandId).molecule(molecule).brandName("Tylenol").build();
      StoreProduct saved =
          StoreProductFixture.activeEntity(storeId, brandId, UUID.randomUUID());

      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
      when(storeProductRepository.existsByStoreIdAndBrandId(storeId, brandId)).thenReturn(false);
      when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(saved);

      StoreProductDto result = storeProductService.activateProduct(storeId, brandId);

      assertNotNull(result.getId());
      assertEquals(storeId, result.getStoreId());
      assertEquals(brandId, result.getBrandId());
      assertTrue(result.getIsActive());
      verify(auditService).recordAudit(eq("ACTIVATE_PRODUCT"), eq("StoreProduct"),
          eq(saved.getId().toString()), isNull(), eq(brandId.toString()));
    }

    @Test
    @DisplayName("should throw DuplicateStoreProductException when brand already activated")
    void whenDuplicate_ShouldThrowConflict() {
      Store store = StoreFixture.defaultEntity();
      Brand brand = Brand.builder().id(brandId).build();

      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
      when(storeProductRepository.existsByStoreIdAndBrandId(storeId, brandId)).thenReturn(true);

      assertThrows(
          StoreProductServiceImpl.DuplicateStoreProductException.class,
          () -> storeProductService.activateProduct(storeId, brandId));
      verify(auditService, never()).recordAudit(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("should throw NotFoundException when brand does not exist")
    void whenBrandNotFound_ShouldThrowNotFound() {
      Store store = StoreFixture.defaultEntity();

      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> storeProductService.activateProduct(storeId, brandId));
    }

    @Test
    @DisplayName("should throw NotFoundException when store does not exist")
    void whenStoreNotFound_ShouldThrowNotFound() {
      when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> storeProductService.activateProduct(storeId, brandId));
    }
  }

  @Nested
  @DisplayName("getActiveProducts")
  class GetActiveProducts {

    @Test
    @DisplayName("should return paginated active products enriched with catalog data")
    void shouldReturnPaginatedActiveProducts() {
      Store store = StoreFixture.defaultEntity();
      UUID spId = UUID.randomUUID();
      StoreProduct sp = StoreProductFixture.activeEntity(storeId, brandId, spId);
      Pageable pageable = PageRequest.of(0, 10);
      Page<StoreProduct> page = new PageImpl<>(java.util.List.of(sp));

      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeProductRepository.findByStoreIdAndIsActiveTrue(storeId, pageable)).thenReturn(page);

      Page<StoreProductDto> result = storeProductService.getActiveProducts(storeId, pageable);

      assertEquals(1, result.getContent().size());
      StoreProductDto dto = result.getContent().get(0);
      assertEquals(spId, dto.getId());
      assertEquals("Ibuprofen", dto.getMoleculeGenericName());
      assertEquals("Advil", dto.getBrandName());
    }
  }

  @Nested
  @DisplayName("getStoreProduct")
  class GetStoreProduct {

    @Test
    @DisplayName("should return enriched store product")
    void shouldReturnEnrichedProduct() {
      UUID spId = UUID.randomUUID();
      StoreProduct sp = StoreProductFixture.activeEntity(storeId, brandId, spId);

      when(storeProductRepository.findByIdAndStoreId(spId, storeId)).thenReturn(Optional.of(sp));

      StoreProductDto result = storeProductService.getStoreProduct(storeId, spId);

      assertNotNull(result);
      assertEquals(spId, result.getId());
      assertEquals(brandId, result.getBrandId());
      assertEquals("Ibuprofen", result.getMoleculeGenericName());
    }

    @Test
    @DisplayName("should throw NotFoundException when product not found")
    void whenNotFound_ShouldThrowNotFound() {
      UUID spId = UUID.randomUUID();

      when(storeProductRepository.findByIdAndStoreId(spId, storeId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> storeProductService.getStoreProduct(storeId, spId));
    }
  }

  @Nested
  @DisplayName("updateOverride")
  class UpdateOverride {

    @Test
    @DisplayName("should update price and record audit")
    void shouldUpdatePriceAndRecordAudit() {
      UUID spId = UUID.randomUUID();
      StoreProduct sp = StoreProductFixture.activeEntity(storeId, brandId, spId);
      sp.setPrice(null);
      sp.setShelfLocation(null);

      UpdateOverrideRequest request =
          UpdateOverrideRequest.builder().price(new BigDecimal("19.99")).shelfLocation("B3").build();

      when(storeProductRepository.findByIdAndStoreId(spId, storeId)).thenReturn(Optional.of(sp));
      when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

      StoreProductDto result = storeProductService.updateOverride(storeId, spId, request);

      verify(auditService).recordAudit(eq("UPDATE_OVERRIDE"), eq("StoreProduct"),
          eq(spId.toString()), anyString(), anyString());
    }

    @Test
    @DisplayName("should throw NotFoundException when product not found")
    void whenNotFound_ShouldThrowNotFound() {
      UUID spId = UUID.randomUUID();
      UpdateOverrideRequest request = UpdateOverrideRequest.builder().price(new BigDecimal("10.00")).build();

      when(storeProductRepository.findByIdAndStoreId(spId, storeId)).thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class, () -> storeProductService.updateOverride(storeId, spId, request));
    }
  }

  @Nested
  @DisplayName("deactivateProduct")
  class DeactivateProduct {

    @Test
    @DisplayName("should soft delete and record audit")
    void shouldSoftDeleteAndRecordAudit() {
      UUID spId = UUID.randomUUID();
      StoreProduct sp = StoreProductFixture.activeEntity(storeId, brandId, spId);

      when(storeProductRepository.findByIdAndStoreId(spId, storeId)).thenReturn(Optional.of(sp));
      when(storeProductRepository.save(any(StoreProduct.class))).thenReturn(sp);

      storeProductService.deactivateProduct(storeId, spId);

      assertFalse(sp.getIsActive());
      verify(auditService).recordAudit(eq("DEACTIVATE_PRODUCT"), eq("StoreProduct"),
          eq(spId.toString()), eq("active"), eq("inactive"));
    }

    @Test
    @DisplayName("should throw NotFoundException when product not found")
    void whenNotFound_ShouldThrowNotFound() {
      UUID spId = UUID.randomUUID();

      when(storeProductRepository.findByIdAndStoreId(spId, storeId)).thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class, () -> storeProductService.deactivateProduct(storeId, spId));
    }
  }
}
