package com.mrtripop.inventory.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.component.BatchMapper;
import com.mrtripop.inventory.fixture.BatchFixture;
import com.mrtripop.inventory.fixture.StoreStockFixture;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.BatchDto;
import com.mrtripop.inventory.models.dto.StockEntryRequest;
import com.mrtripop.inventory.models.dto.StockEntryResponseDto;
import com.mrtripop.inventory.models.dto.StoreStockDto;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BatchServiceImpl")
class BatchServiceImplTest {

  @Mock private BatchRepository batchRepository;
  @Mock private StoreStockRepository storeStockRepository;
  @Mock private BrandRepository brandRepository;
  @Mock private StoreRepository storeRepository;
  @Mock private AuditService auditService;
  @Mock private BatchMapper batchMapper;
  @InjectMocks private BatchServiceImpl batchService;

  @Nested
  @DisplayName("createBatchFromBarcode")
  class CreateBatchFromBarcode {

    @Test
    @DisplayName("should create batch and store stock when barcode is recognized")
    void shouldCreateBatchAndStoreStockWhenBarcodeIsRecognized() throws Throwable {
      // Arrange
      StockEntryRequest request = BatchFixture.validStockEntryRequest();
      Brand brand =
          Brand.builder().id(UUID.randomUUID()).barcode(request.getBarcode()).build();
      Store store =
          Store.builder()
              .id(request.getStoreId())
              .name("Main Store")
              .type(StoreType.PHYSICAL)
              .build();

      when(batchRepository.save(any())).thenAnswer(inv -> {
        Batch b = inv.getArgument(0);
        if (b.getId() == null) {
          b = Batch.builder()
              .id(1L)
              .brand(b.getBrand())
              .batchNumber(b.getBatchNumber())
              .expiryDate(b.getExpiryDate())
              .quantity(b.getQuantity())
              .supplierReference(b.getSupplierReference())
              .manufacturerLotNumber(b.getManufacturerLotNumber())
              .storageConditions(b.getStorageConditions())
              .status(b.getStatus())
              .build();
        }
        return b;
      });
      when(storeStockRepository.save(any())).thenAnswer(inv -> {
        StoreStock s = inv.getArgument(0);
        if (s.getId() == null) {
          s = StoreStock.builder()
              .id(1L)
              .store(s.getStore())
              .batch(s.getBatch())
              .quantity(s.getQuantity())
              .build();
        }
        return s;
      });
      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(brand));
      when(batchRepository.findByBrandIdAndBatchNumber(brand.getId(), request.getBatchNumber()))
          .thenReturn(Optional.empty());
      when(storeRepository.findById(request.getStoreId())).thenReturn(Optional.of(store));
      when(batchMapper.toBatchDto(any())).thenReturn(BatchFixture.validBatchDto());
      when(batchMapper.toStoreStockDto(any())).thenReturn(StoreStockFixture.validStoreStockDto());

      // Act
      StockEntryResponseDto result = batchService.createBatchFromBarcode(request);

      // Assert
      assertNotNull(result);
      assertNotNull(result.getBatch());
      assertNotNull(result.getStoreStock());
      verify(batchRepository).save(any());
      verify(storeStockRepository).save(any());
      verify(auditService)
          .recordAudit(eq("INVENTORY_IN"), eq("Batch"), anyString(), isNull(), anyString());
    }

    @Test
    @DisplayName("should throw BARCODE_NOT_RECOGNIZED when barcode does not match any brand")
    void shouldThrowBarcodeNotRecognizedWhenBarcodeDoesNotMatchAnyBrand() {
      // Arrange
      StockEntryRequest request = BatchFixture.validStockEntryRequest();

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(
          ApplicationException.class, () -> batchService.createBatchFromBarcode(request));

      verify(batchRepository, never()).save(any());
      verify(storeStockRepository, never()).save(any());
      verify(auditService, never()).recordAudit(anyString(), anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("should throw BATCH_ALREADY_EXISTS when batch number already exists for brand")
    void shouldThrowBatchAlreadyExistsWhenBatchNumberAlreadyExistsForBrand() {
      // Arrange
      StockEntryRequest request = BatchFixture.validStockEntryRequest();
      Brand brand =
          Brand.builder().id(UUID.randomUUID()).barcode(request.getBarcode()).build();

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(brand));
      when(batchRepository.findByBrandIdAndBatchNumber(brand.getId(), request.getBatchNumber()))
          .thenReturn(Optional.of(BatchFixture.defaultBatch()));

      // Act & Assert
      assertThrows(
          ApplicationException.class, () -> batchService.createBatchFromBarcode(request));

      verify(batchRepository, never()).save(any());
      verify(storeStockRepository, never()).save(any());
      verify(auditService, never()).recordAudit(anyString(), anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("should throw STORE_NOT_FOUND when store ID does not exist")
    void shouldThrowStoreNotFoundWhenStoreIdDoesNotExist() {
      // Arrange
      StockEntryRequest request = BatchFixture.validStockEntryRequest();
      Brand brand =
          Brand.builder().id(UUID.randomUUID()).barcode(request.getBarcode()).build();

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(brand));
      when(batchRepository.findByBrandIdAndBatchNumber(brand.getId(), request.getBatchNumber()))
          .thenReturn(Optional.empty());
      when(storeRepository.findById(request.getStoreId())).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(
          ApplicationException.class, () -> batchService.createBatchFromBarcode(request));

      verify(batchRepository, never()).save(any());
      verify(storeStockRepository, never()).save(any());
      verify(auditService, never()).recordAudit(anyString(), anyString(), any(), any(), any());
    }
  }

  @Nested
  @DisplayName("getBatchById")
  class GetBatchById {

    @Test
    @DisplayName("should return batch DTO when batch exists")
    void shouldReturnBatchDtoWhenBatchExists() throws Throwable {
      // Arrange
      Batch batch = BatchFixture.defaultBatch();
      BatchDto batchDto = BatchFixture.validBatchDto();

      when(batchRepository.findById(batch.getId())).thenReturn(Optional.of(batch));
      when(batchMapper.toBatchDto(batch)).thenReturn(batchDto);

      // Act
      BatchDto result = batchService.getBatchById(batch.getId());

      // Assert
      assertEquals(batchDto, result);
      verify(batchRepository).findById(batch.getId());
      verify(batchMapper).toBatchDto(batch);
    }

    @Test
    @DisplayName("should throw BATCH_NOT_FOUND when batch does not exist")
    void shouldThrowBatchNotFoundWhenBatchDoesNotExist() {
      // Arrange
      Long batchId = 1L;

      when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(ApplicationException.class, () -> batchService.getBatchById(batchId));

      verify(batchRepository).findById(batchId);
      verify(batchMapper, never()).toBatchDto(any());
    }
  }

  @Nested
  @DisplayName("getBatchesByBrandId")
  class GetBatchesByBrandId {

    @Test
    @DisplayName("should return page of batch DTOs when brand exists")
    void shouldReturnPageOfBatchDtosWhenBrandExists() {
      // Arrange
      UUID brandId = UUID.randomUUID();
      Batch batch = BatchFixture.defaultBatch();
      List<Batch> batches = List.of(batch);
      Page<Batch> batchPage = new PageImpl<>(batches);

      when(batchRepository.findByBrandId(eq(brandId), any(Pageable.class)))
          .thenReturn(batchPage);

      // Act
      Page<BatchDto> result = batchService.getBatchesByBrandId(brandId, Pageable.unpaged());

      // Assert
      assertEquals(1, result.getTotalElements());
      verify(batchRepository).findByBrandId(eq(brandId), any(Pageable.class));
    }

    @Test
    @DisplayName("should return empty page when brand has no batches")
    void shouldReturnEmptyPageWhenBrandHasNoBatches() {
      // Arrange
      UUID brandId = UUID.randomUUID();
      Page<Batch> emptyPage = Page.empty();

      when(batchRepository.findByBrandId(eq(brandId), any(Pageable.class)))
          .thenReturn(emptyPage);

      // Act
      Page<BatchDto> result = batchService.getBatchesByBrandId(brandId, Pageable.unpaged());

      // Assert
      assertEquals(0, result.getTotalElements());
      verify(batchRepository).findByBrandId(eq(brandId), any(Pageable.class));
    }
  }

  @Nested
  @DisplayName("getStoreStocksByStoreId")
  class GetStoreStocksByStoreId {

    @Test
    @DisplayName("should return page of store stock DTOs when store exists")
    void shouldReturnPageOfStoreStockDtosWhenStoreExists() {
      // Arrange
      UUID storeId = UUID.randomUUID();
      StoreStock storeStock = StoreStockFixture.defaultStoreStock();
      List<StoreStock> storeStocks = List.of(storeStock);
      Page<StoreStock> storeStockPage = new PageImpl<>(storeStocks);

      when(storeStockRepository.findByStoreId(eq(storeId), any(Pageable.class)))
          .thenReturn(storeStockPage);

      // Act
      Page<StoreStockDto> result =
          batchService.getStoreStocksByStoreId(storeId, Pageable.unpaged());

      // Assert
      assertEquals(1, result.getTotalElements());
      verify(storeStockRepository).findByStoreId(eq(storeId), any(Pageable.class));
    }

    @Test
    @DisplayName("should return empty page when store has no stock")
    void shouldReturnEmptyPageWhenStoreHasNoStock() {
      // Arrange
      UUID storeId = UUID.randomUUID();
      Page<StoreStock> emptyPage = Page.empty();

      when(storeStockRepository.findByStoreId(eq(storeId), any(Pageable.class)))
          .thenReturn(emptyPage);

      // Act
      Page<StoreStockDto> result =
          batchService.getStoreStocksByStoreId(storeId, Pageable.unpaged());

      // Assert
      assertEquals(0, result.getTotalElements());
      verify(storeStockRepository).findByStoreId(eq(storeId), any(Pageable.class));
    }
  }

  @Nested
  @DisplayName("getStoreStock")
  class GetStoreStock {

    @Test
    @DisplayName("should return store stock DTO when record exists")
    void shouldReturnStoreStockDtoWhenRecordExists() throws Throwable {
      // Arrange
      StoreStock storeStock = StoreStockFixture.defaultStoreStock();
      StoreStockDto storeStockDto = StoreStockFixture.validStoreStockDto();

      when(storeStockRepository.findByStoreIdAndBatchId(
              eq(storeStock.getStore().getId()), eq(storeStock.getBatch().getId())))
          .thenReturn(Optional.of(storeStock));
      when(batchMapper.toStoreStockDto(storeStock)).thenReturn(storeStockDto);

      // Act
      StoreStockDto result =
          batchService.getStoreStock(storeStock.getStore().getId(), storeStock.getBatch().getId());

      // Assert
      assertEquals(storeStockDto, result);
      verify(storeStockRepository)
          .findByStoreIdAndBatchId(
              eq(storeStock.getStore().getId()), eq(storeStock.getBatch().getId()));
      verify(batchMapper).toStoreStockDto(storeStock);
    }

    @Test
    @DisplayName("should throw STOCK_NOT_FOUND when record does not exist")
    void shouldThrowStockNotFoundWhenRecordDoesNotExist() {
      // Arrange
      UUID storeId = UUID.randomUUID();
      Long batchId = 1L;

      when(storeStockRepository.findByStoreIdAndBatchId(storeId, batchId))
          .thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(ApplicationException.class, () -> batchService.getStoreStock(storeId, batchId));

      verify(storeStockRepository).findByStoreIdAndBatchId(storeId, batchId);
      verify(batchMapper, never()).toStoreStockDto(any());
    }
  }
}
