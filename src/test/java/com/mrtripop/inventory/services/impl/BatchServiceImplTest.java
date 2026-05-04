package com.mrtripop.inventory.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.RegulatorySchedule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.component.BatchMapper;
import com.mrtripop.inventory.fixture.BatchFixture;
import com.mrtripop.inventory.fixture.DigitalSignatureFixture;
import com.mrtripop.inventory.fixture.StoreStockFixture;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.db.VerificationStatus;
import com.mrtripop.inventory.models.dto.BatchDto;
import com.mrtripop.inventory.models.dto.DeductedBatchDto;
import com.mrtripop.inventory.models.dto.DigitalSignatureRequest;
import com.mrtripop.inventory.models.dto.SignatureVerificationDto;
import com.mrtripop.inventory.models.dto.StockDeductionRequest;
import com.mrtripop.inventory.models.dto.StockDeductionResponseDto;
import com.mrtripop.inventory.models.dto.StockEntryRequest;
import com.mrtripop.inventory.models.dto.StockEntryResponseDto;
import com.mrtripop.inventory.models.dto.StoreStockDto;
import com.mrtripop.inventory.models.dto.SyncSealResult;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.DigitalSignatureService;
import com.mrtripop.inventory.services.SyncSealService;
import com.mrtripop.inventory.services.UnitConversionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BatchServiceImpl")
class BatchServiceImplTest {

  @Mock private BatchRepository batchRepository;
  @Mock private StoreStockRepository storeStockRepository;
  @Mock private BrandRepository brandRepository;
  @Mock private StoreRepository storeRepository;
  @Mock private StoreProductRepository storeProductRepository;
  @Mock private AuditService auditService;
  @Mock private BatchMapper batchMapper;
  @Mock private UnitConversionService unitConversionService;
  @Mock private SyncSealService syncSealService;
  @Mock private DigitalSignatureService digitalSignatureService;
  @InjectMocks private BatchServiceImpl batchService;

  @BeforeEach
  void setUp() {
    when(storeProductRepository.findByStoreIdAndBrandId(any(), any()))
        .thenReturn(Optional.empty());
  }

  @Nested
  @DisplayName("createBatchFromBarcode")
  class CreateBatchFromBarcode {

    @Test
    @DisplayName("should create batch and store stock when barcode is recognized")
    void shouldCreateBatchAndStoreStockWhenBarcodeIsRecognized() throws Throwable {
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

      StockEntryResponseDto result = batchService.createBatchFromBarcode(request);

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
      StockEntryRequest request = BatchFixture.validStockEntryRequest();

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.empty());

      assertThrows(
          ApplicationException.class, () -> batchService.createBatchFromBarcode(request));

      verify(batchRepository, never()).save(any());
      verify(storeStockRepository, never()).save(any());
      verify(auditService, never()).recordAudit(anyString(), anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("should throw BATCH_ALREADY_EXISTS when batch number already exists for brand")
    void shouldThrowBatchAlreadyExistsWhenBatchNumberAlreadyExistsForBrand() {
      StockEntryRequest request = BatchFixture.validStockEntryRequest();
      Brand brand =
          Brand.builder().id(UUID.randomUUID()).barcode(request.getBarcode()).build();

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(brand));
      when(batchRepository.findByBrandIdAndBatchNumber(brand.getId(), request.getBatchNumber()))
          .thenReturn(Optional.of(BatchFixture.defaultBatch()));

      assertThrows(
          ApplicationException.class, () -> batchService.createBatchFromBarcode(request));

      verify(batchRepository, never()).save(any());
      verify(storeStockRepository, never()).save(any());
      verify(auditService, never()).recordAudit(anyString(), anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("should throw STORE_NOT_FOUND when store ID does not exist")
    void shouldThrowStoreNotFoundWhenStoreIdDoesNotExist() {
      StockEntryRequest request = BatchFixture.validStockEntryRequest();
      Brand brand =
          Brand.builder().id(UUID.randomUUID()).barcode(request.getBarcode()).build();

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(brand));
      when(batchRepository.findByBrandIdAndBatchNumber(brand.getId(), request.getBatchNumber()))
          .thenReturn(Optional.empty());
      when(storeRepository.findById(request.getStoreId())).thenReturn(Optional.empty());

      assertThrows(
          ApplicationException.class, () -> batchService.createBatchFromBarcode(request));

      verify(batchRepository, never()).save(any());
      verify(storeStockRepository, never()).save(any());
      verify(auditService, never()).recordAudit(anyString(), anyString(), any(), any(), any());
    }
  }

  @Nested
  @DisplayName("deductStock")
  class DeductStock {

    @Test
    @DisplayName("should deduct from single batch when stock is sufficient")
    void shouldDeductFromSingleBatchWhenStockIsSufficient() throws Throwable {
      UUID brandId = UUID.randomUUID();
      UUID storeId = UUID.randomUUID();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest().storeId(storeId).build();
      Brand brand = Brand.builder().id(brandId).brandName("Tylenol").build();
      Store store = Store.builder().id(storeId).build();

      Batch batch = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
      StoreStock stock =
          StoreStockFixture.storeStockWithQuantity(batch, store, 100L);

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, brandId))
          .thenReturn(List.of(stock));
      when(storeStockRepository.deductQuantity(eq(stock.getId()), eq(50L))).thenReturn(1);
      when(batchMapper.toDeductedBatchDto(eq(stock), eq(50L)))
          .thenReturn(
              DeductedBatchDto.builder()
                  .batchId(batch.getId())
                  .batchNumber(batch.getBatchNumber())
                  .expiryDate(batch.getExpiryDate())
                  .deductedQuantity(50L)
                  .remainingQuantity(50L)
                  .build());

      StockDeductionResponseDto result = batchService.deductStock(request);

      assertNotNull(result);
      assertEquals(50L, result.getRequestedQuantity());
      assertEquals(50L, result.getDeductedQuantity());
      assertEquals(1, result.getItems().size());
      verify(storeStockRepository).deductQuantity(stock.getId(), 50L);
      verify(auditService)
          .recordAudit(
              eq("INVENTORY_OUT"),
              eq("StoreStock"),
              eq(stock.getId().toString()),
              eq("100"),
              eq("50"));
    }

    @Test
    @DisplayName("should roll over to second batch when first batch is exhausted")
    void shouldRollOverToSecondBatchWhenFirstBatchIsExhausted() throws Throwable {
      UUID brandId = UUID.randomUUID();
      UUID storeId = UUID.randomUUID();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest().storeId(storeId).build();
      Brand brand = Brand.builder().id(brandId).brandName("Tylenol").build();
      Store store = Store.builder().id(storeId).build();

      Batch batch1 =
          BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(3), brand);
      Batch batch2 =
          BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(12), brand);
      StoreStock stock1 = StoreStockFixture.storeStockWithQuantity(batch1, store, 30L);
      StoreStock stock2 = StoreStockFixture.storeStockWithQuantity(batch2, store, 70L);

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, brandId))
          .thenReturn(List.of(stock1, stock2));
      when(storeStockRepository.deductQuantity(eq(stock1.getId()), eq(30L))).thenReturn(1);
      when(storeStockRepository.deductQuantity(eq(stock2.getId()), eq(20L))).thenReturn(1);
      when(batchMapper.toDeductedBatchDto(eq(stock1), eq(30L)))
          .thenReturn(
              DeductedBatchDto.builder()
                  .batchId(batch1.getId())
                  .batchNumber(batch1.getBatchNumber())
                  .deductedQuantity(30L)
                  .remainingQuantity(0L)
                  .build());
      when(batchMapper.toDeductedBatchDto(eq(stock2), eq(20L)))
          .thenReturn(
              DeductedBatchDto.builder()
                  .batchId(batch2.getId())
                  .batchNumber(batch2.getBatchNumber())
                  .deductedQuantity(20L)
                  .remainingQuantity(50L)
                  .build());

      StockDeductionResponseDto result = batchService.deductStock(request);

      assertNotNull(result);
      assertEquals(50L, result.getDeductedQuantity());
      assertEquals(2, result.getItems().size());
      verify(auditService, times(2))
          .recordAudit(eq("INVENTORY_OUT"), eq("StoreStock"), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("should roll over across three batches when needed")
    void shouldRollOverAcrossThreeBatchesWhenNeeded() throws Throwable {
      UUID brandId = UUID.randomUUID();
      UUID storeId = UUID.randomUUID();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest().quantity(100L).storeId(storeId).build();
      Brand brand = Brand.builder().id(brandId).brandName("Aspirin").build();
      Store store = Store.builder().id(storeId).build();

      Batch batch1 = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(1), brand);
      Batch batch2 =
          BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(3), brand);
      Batch batch3 =
          BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
      StoreStock stock1 = StoreStockFixture.storeStockWithQuantity(batch1, store, 20L);
      StoreStock stock2 = StoreStockFixture.storeStockWithQuantity(batch2, store, 30L);
      StoreStock stock3 = StoreStockFixture.storeStockWithQuantity(batch3, store, 80L);

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, brandId))
          .thenReturn(List.of(stock1, stock2, stock3));
      when(storeStockRepository.deductQuantity(eq(stock1.getId()), eq(20L))).thenReturn(1);
      when(storeStockRepository.deductQuantity(eq(stock2.getId()), eq(30L))).thenReturn(1);
      when(storeStockRepository.deductQuantity(eq(stock3.getId()), eq(50L))).thenReturn(1);
      when(batchMapper.toDeductedBatchDto(any(StoreStock.class), anyLong()))
          .thenReturn(
              DeductedBatchDto.builder().batchId(1L).batchNumber("BATCH").deductedQuantity(10L).build());

      StockDeductionResponseDto result = batchService.deductStock(request);

      assertEquals(100L, result.getDeductedQuantity());
      assertEquals(3, result.getItems().size());
      verify(storeStockRepository).deductQuantity(stock1.getId(), 20L);
      verify(storeStockRepository).deductQuantity(stock2.getId(), 30L);
      verify(storeStockRepository).deductQuantity(stock3.getId(), 50L);
    }

    @Test
    @DisplayName("should throw INSUFFICIENT_QUANTITY when total stock is insufficient")
    void shouldThrowInsufficientQuantityWhenTotalStockIsInsufficient() {
      UUID brandId = UUID.randomUUID();
      UUID storeId = UUID.randomUUID();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest().quantity(200L).storeId(storeId).build();
      Brand brand = Brand.builder().id(brandId).brandName("Tylenol").build();
      Store store = Store.builder().id(storeId).build();

      Batch batch = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
      StoreStock stock = StoreStockFixture.storeStockWithQuantity(batch, store, 50L);

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, brandId))
          .thenReturn(List.of(stock));
      when(storeStockRepository.deductQuantity(eq(stock.getId()), eq(50L))).thenReturn(1);
      when(batchMapper.toDeductedBatchDto(eq(stock), eq(50L)))
          .thenReturn(
              DeductedBatchDto.builder()
                  .batchId(batch.getId())
                  .batchNumber(batch.getBatchNumber())
                  .deductedQuantity(50L)
                  .remainingQuantity(0L)
                  .build());

      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> batchService.deductStock(request));
      assertEquals(ErrorCode.INSUFFICIENT_QUANTITY, ex.getErrorCode());
      verify(auditService).recordAudit(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("should throw NO_AVAILABLE_BATCHES when no batches are available")
    void shouldThrowNoAvailableBatchesWhenNoBatchesAreAvailable() {
      UUID storeId = UUID.randomUUID();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest().storeId(storeId).build();
      Brand brand = Brand.builder().id(UUID.randomUUID()).brandName("Tylenol").build();
      Store store = Store.builder().id(storeId).build();

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, brand.getId()))
          .thenReturn(List.of());

      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> batchService.deductStock(request));
      assertEquals(ErrorCode.NO_AVAILABLE_BATCHES, ex.getErrorCode());

      verify(storeStockRepository, never()).deductQuantity(anyLong(), anyLong());
      verify(auditService, never()).recordAudit(anyString(), anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("should throw BARCODE_NOT_RECOGNIZED when barcode does not match any brand")
    void shouldThrowBarcodeNotRecognizedForDeduction() {
      StockDeductionRequest request = BatchFixture.validStockDeductionRequest().build();

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.empty());

      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> batchService.deductStock(request));
      assertEquals(ErrorCode.BARCODE_NOT_RECOGNIZED, ex.getErrorCode());

      verify(storeStockRepository, never())
          .findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(any(), any());
    }

    @Test
    @DisplayName("should throw STORE_NOT_FOUND when store does not exist")
    void shouldThrowStoreNotFoundForDeduction() {
      UUID storeId = UUID.randomUUID();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest().storeId(storeId).build();
      Brand brand = Brand.builder().id(UUID.randomUUID()).brandName("Tylenol").build();

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> batchService.deductStock(request));
      assertEquals(ErrorCode.STORE_NOT_FOUND, ex.getErrorCode());

      verify(storeStockRepository, never())
          .findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(any(), any());
    }

    @Test
    @DisplayName("should record audit for each deducted batch")
    void shouldRecordAuditForEachDeductedBatch() throws Throwable {
      UUID brandId = UUID.randomUUID();
      UUID storeId = UUID.randomUUID();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest().storeId(storeId).build();
      Brand brand = Brand.builder().id(brandId).brandName("Tylenol").build();
      Store store = Store.builder().id(storeId).build();

      Batch batch1 = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(2), brand);
      Batch batch2 = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(8), brand);
      StoreStock stock1 = StoreStockFixture.storeStockWithQuantity(batch1, store, 20L);
      StoreStock stock2 = StoreStockFixture.storeStockWithQuantity(batch2, store, 80L);

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, brandId))
          .thenReturn(List.of(stock1, stock2));
      when(storeStockRepository.deductQuantity(eq(stock1.getId()), eq(20L))).thenReturn(1);
      when(storeStockRepository.deductQuantity(eq(stock2.getId()), eq(30L))).thenReturn(1);
      when(batchMapper.toDeductedBatchDto(any(StoreStock.class), anyLong()))
          .thenReturn(
              DeductedBatchDto.builder().batchId(1L).batchNumber("BATCH").deductedQuantity(10L).build());

      batchService.deductStock(request);

      verify(auditService, times(2))
          .recordAudit(eq("INVENTORY_OUT"), eq("StoreStock"), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("should throw EXPIRED_BATCH_DEDUCTION when defense-in-depth detects expired batch")
    void shouldThrowExpiredBatchDeductionWhenDefenseInDepthDetectsExpired() {
      UUID brandId = UUID.randomUUID();
      UUID storeId = UUID.randomUUID();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest().storeId(storeId).build();
      Brand brand = Brand.builder().id(brandId).brandName("Tylenol").build();
      Store store = Store.builder().id(storeId).build();

      Batch expiredBatch =
          BatchFixture.batchWithExpiry(LocalDate.now().minusDays(1), brand);
      StoreStock stock =
          StoreStockFixture.storeStockWithQuantity(expiredBatch, store, 100L);

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, brandId))
          .thenReturn(List.of(stock));

      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> batchService.deductStock(request));
      assertEquals(ErrorCode.EXPIRED_BATCH_DEDUCTION, ex.getErrorCode());

      verify(storeStockRepository, never()).deductQuantity(anyLong(), anyLong());
    }

    @Test
    @DisplayName("should roll over to next batch when deductQuantity returns 0 for race condition")
    void shouldRollOverToNextBatchWhenDeductQuantityReturnsZero() throws Throwable {
      UUID brandId = UUID.randomUUID();
      UUID storeId = UUID.randomUUID();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest().storeId(storeId).build();
      Brand brand = Brand.builder().id(brandId).brandName("Tylenol").build();
      Store store = Store.builder().id(storeId).build();

      Batch batch1 =
          BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(3), brand);
      Batch batch2 =
          BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
      StoreStock stock1 = StoreStockFixture.storeStockWithQuantity(batch1, store, 30L);
      StoreStock stock2 = StoreStockFixture.storeStockWithQuantity(batch2, store, 70L);

      when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, brandId))
          .thenReturn(List.of(stock1, stock2));
      when(storeStockRepository.deductQuantity(eq(stock1.getId()), eq(30L))).thenReturn(0);
      when(storeStockRepository.deductQuantity(eq(stock2.getId()), eq(50L))).thenReturn(1);
      when(batchMapper.toDeductedBatchDto(eq(stock2), eq(50L)))
          .thenReturn(
              DeductedBatchDto.builder()
                  .batchId(batch2.getId())
                  .batchNumber(batch2.getBatchNumber())
                  .deductedQuantity(50L)
                  .remainingQuantity(20L)
                  .build());

      StockDeductionResponseDto result = batchService.deductStock(request);

      assertEquals(50L, result.getDeductedQuantity());
      assertEquals(1, result.getItems().size());
      verify(auditService, times(1))
          .recordAudit(eq("INVENTORY_OUT"), eq("StoreStock"), anyString(), anyString(), anyString());
    }

    @Nested
    @DisplayName("with unit conversion")
    class DeductStockWithUnits {

      @Test
      @DisplayName("should deduct base units directly when unit is null")
      void shouldDeductBaseUnitsDirectlyWhenUnitIsNull() throws Throwable {
        UUID brandId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        StockDeductionRequest request =
            BatchFixture.validStockDeductionRequest().storeId(storeId).build();
        Brand brand =
            Brand.builder().id(brandId).brandName("Tylenol").baseUnit("TABLET").build();
        Store store = Store.builder().id(storeId).build();
        Batch batch = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
        StoreStock stock =
            StoreStockFixture.storeStockWithQuantity(batch, store, 100L);

        when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
                storeId, brandId))
            .thenReturn(List.of(stock));
        when(storeStockRepository.deductQuantity(eq(stock.getId()), eq(50L))).thenReturn(1);
        when(batchMapper.toDeductedBatchDto(eq(stock), eq(50L)))
            .thenReturn(
                DeductedBatchDto.builder()
                    .batchId(batch.getId())
                    .batchNumber(batch.getBatchNumber())
                    .deductedQuantity(50L)
                    .remainingQuantity(50L)
                    .baseUnit("TABLET")
                    .build());

        StockDeductionResponseDto result = batchService.deductStock(request);

        assertEquals(50L, result.getDeductedQuantity());
        assertEquals("TABLET", result.getBaseUnit());
        assertNull(result.getRequestedUnit());
        verify(unitConversionService, never()).convertToBaseUnits(any(), anyString(), anyLong());
      }

      @Test
      @DisplayName("should deduct base units directly when unit matches baseUnit")
      void shouldDeductBaseUnitsWhenUnitMatchesBaseUnit() throws Throwable {
        UUID brandId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        StockDeductionRequest request =
            BatchFixture.validStockDeductionRequest().storeId(storeId).unit("TABLET").build();
        Brand brand =
            Brand.builder().id(brandId).brandName("Tylenol").baseUnit("TABLET").build();
        Store store = Store.builder().id(storeId).build();
        Batch batch = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
        StoreStock stock =
            StoreStockFixture.storeStockWithQuantity(batch, store, 100L);

        when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
                storeId, brandId))
            .thenReturn(List.of(stock));
        when(storeStockRepository.deductQuantity(eq(stock.getId()), eq(50L))).thenReturn(1);
        when(batchMapper.toDeductedBatchDto(eq(stock), eq(50L)))
            .thenReturn(
                DeductedBatchDto.builder()
                    .batchId(batch.getId())
                    .deductedQuantity(50L)
                    .remainingQuantity(50L)
                    .baseUnit("TABLET")
                    .build());

        StockDeductionResponseDto result = batchService.deductStock(request);

        assertEquals(50L, result.getDeductedQuantity());
        assertEquals("TABLET", result.getRequestedUnit());
        verify(unitConversionService, never()).convertToBaseUnits(any(), anyString(), anyLong());
      }

      @Test
      @DisplayName("should convert and deduct when unit differs from baseUnit")
      void shouldConvertAndDeductWhenUnitDiffers() throws Throwable {
        UUID brandId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        StockDeductionRequest request =
            BatchFixture.validStockDeductionRequest().storeId(storeId).unit("BOX").quantity(2L)
                .build();
        Brand brand =
            Brand.builder().id(brandId).brandName("Tylenol").baseUnit("TABLET").build();
        Store store = Store.builder().id(storeId).build();
        Batch batch = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
        StoreStock stock =
            StoreStockFixture.storeStockWithQuantity(batch, store, 100L);

        when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(unitConversionService.convertToBaseUnits(brandId, "BOX", 2L)).thenReturn(60L);
        when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
                storeId, brandId))
            .thenReturn(List.of(stock));
        when(storeStockRepository.deductQuantity(eq(stock.getId()), eq(60L))).thenReturn(1);
        when(batchMapper.toDeductedBatchDto(eq(stock), eq(60L)))
            .thenReturn(
                DeductedBatchDto.builder()
                    .batchId(batch.getId())
                    .deductedQuantity(60L)
                    .remainingQuantity(40L)
                    .baseUnit("TABLET")
                    .build());

        StockDeductionResponseDto result = batchService.deductStock(request);

        assertEquals(60L, result.getDeductedQuantity());
        assertEquals("TABLET", result.getBaseUnit());
        assertEquals("BOX", result.getRequestedUnit());
        assertEquals(2L, result.getRequestedQuantity());
        verify(unitConversionService).convertToBaseUnits(brandId, "BOX", 2L);
      }

      @Test
      @DisplayName("should throw UNIT_CONVERSION_NOT_FOUND when unit is not recognized")
      void shouldThrowUnitConversionNotFoundWhenUnitNotRecognized() {
        UUID brandId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        StockDeductionRequest request =
            BatchFixture.validStockDeductionRequest().storeId(storeId).unit("UNKNOWN").build();
        Brand brand =
            Brand.builder().id(brandId).brandName("Tylenol").baseUnit("TABLET").build();
        Store store = Store.builder().id(storeId).build();

        when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        try {
          when(unitConversionService.convertToBaseUnits(brandId, "UNKNOWN", 50L))
              .thenThrow(
                  new ApplicationException(ErrorCode.UNIT_CONVERSION_NOT_FOUND, HttpStatus.BAD_REQUEST));
        } catch (ApplicationException e) {
          throw new RuntimeException(e);
        }

        ApplicationException ex =
            assertThrows(ApplicationException.class, () -> batchService.deductStock(request));
        assertEquals(ErrorCode.UNIT_CONVERSION_NOT_FOUND, ex.getErrorCode());
      }

      @Test
      @DisplayName("should include pricing when StoreProduct exists")
      void shouldIncludePricingWhenStoreProductExists() throws Throwable {
        UUID brandId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        StockDeductionRequest request =
            BatchFixture.validStockDeductionRequest().storeId(storeId).build();
        Brand brand =
            Brand.builder().id(brandId).brandName("Tylenol").baseUnit("TABLET").build();
        Store store = Store.builder().id(storeId).build();
        Batch batch = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
        StoreStock stock =
            StoreStockFixture.storeStockWithQuantity(batch, store, 100L);
        StoreProduct storeProduct = StoreProduct.builder().price(new BigDecimal("10.00")).build();

        when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
                storeId, brandId))
            .thenReturn(List.of(stock));
        when(storeProductRepository.findByStoreIdAndBrandId(storeId, brandId))
            .thenReturn(Optional.of(storeProduct));
        when(storeStockRepository.deductQuantity(eq(stock.getId()), eq(50L))).thenReturn(1);
        when(batchMapper.toDeductedBatchDto(eq(stock), eq(50L)))
            .thenReturn(
                DeductedBatchDto.builder()
                    .batchId(batch.getId())
                    .deductedQuantity(50L)
                    .remainingQuantity(50L)
                    .baseUnit("TABLET")
                    .build());

        StockDeductionResponseDto result = batchService.deductStock(request);

        assertEquals(0, new BigDecimal("10.00").compareTo(result.getUnitPrice()));
        assertEquals(0, new BigDecimal("500.00").compareTo(result.getTotalAmount()));
      }

      @Test
      @DisplayName("should have null pricing when StoreProduct does not exist")
      void shouldHaveNullPricingWhenStoreProductNotFound() throws Throwable {
        UUID brandId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        StockDeductionRequest request =
            BatchFixture.validStockDeductionRequest().storeId(storeId).build();
        Brand brand =
            Brand.builder().id(brandId).brandName("Tylenol").baseUnit("TABLET").build();
        Store store = Store.builder().id(storeId).build();
        Batch batch = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
        StoreStock stock =
            StoreStockFixture.storeStockWithQuantity(batch, store, 100L);

        when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
                storeId, brandId))
            .thenReturn(List.of(stock));
        when(storeStockRepository.deductQuantity(eq(stock.getId()), eq(50L))).thenReturn(1);
        when(batchMapper.toDeductedBatchDto(eq(stock), eq(50L)))
            .thenReturn(
                DeductedBatchDto.builder()
                    .batchId(batch.getId())
                    .deductedQuantity(50L)
                    .remainingQuantity(50L)
                    .baseUnit("TABLET")
                    .build());

        StockDeductionResponseDto result = batchService.deductStock(request);

        assertNull(result.getUnitPrice());
        assertNull(result.getTotalAmount());
      }

      @Test
      @DisplayName("should support multi-batch rollover with unit conversion")
      void shouldSupportMultiBatchRolloverWithConversion() throws Throwable {
        UUID brandId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        StockDeductionRequest request =
            BatchFixture.validStockDeductionRequest().storeId(storeId).unit("BOX").quantity(1L)
                .build();
        Brand brand =
            Brand.builder().id(brandId).brandName("Tylenol").baseUnit("TABLET").build();
        Store store = Store.builder().id(storeId).build();
        Batch batch1 = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(3), brand);
        Batch batch2 = BatchFixture.batchWithExpiry(LocalDate.now().plusMonths(6), brand);
        StoreStock stock1 = StoreStockFixture.storeStockWithQuantity(batch1, store, 20L);
        StoreStock stock2 = StoreStockFixture.storeStockWithQuantity(batch2, store, 30L);

        when(brandRepository.findByBarcode(request.getBarcode())).thenReturn(Optional.of(brand));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(unitConversionService.convertToBaseUnits(brandId, "BOX", 1L)).thenReturn(30L);
        when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
                storeId, brandId))
            .thenReturn(List.of(stock1, stock2));
        when(storeStockRepository.deductQuantity(eq(stock1.getId()), eq(20L))).thenReturn(1);
        when(storeStockRepository.deductQuantity(eq(stock2.getId()), eq(10L))).thenReturn(1);
        when(batchMapper.toDeductedBatchDto(eq(stock1), eq(20L)))
            .thenReturn(
                DeductedBatchDto.builder()
                    .batchId(batch1.getId())
                    .deductedQuantity(20L)
                    .remainingQuantity(0L)
                    .baseUnit("TABLET")
                    .build());
        when(batchMapper.toDeductedBatchDto(eq(stock2), eq(10L)))
            .thenReturn(
                DeductedBatchDto.builder()
                    .batchId(batch2.getId())
                    .deductedQuantity(10L)
                    .remainingQuantity(20L)
                    .baseUnit("TABLET")
                    .build());

        StockDeductionResponseDto result = batchService.deductStock(request);

        assertEquals(30L, result.getDeductedQuantity());
        assertEquals(2, result.getItems().size());
        verify(unitConversionService).convertToBaseUnits(brandId, "BOX", 1L);
      }
    }
  }

  @Nested
  @DisplayName("deductStock with digital signature")
  class DeductStockWithSignature {

    private Brand controlledBrand;
    private Brand otcBrand;
    private Brand rxBrand;
    private Store store;
    private StoreStock stock;
    private SyncSealResult syncSealResult;
    private UUID storeId;

    @BeforeEach
    void setUp() {
      storeId = UUID.randomUUID();
      store = Store.builder().id(storeId).build();

      Molecule controlledMolecule =
          Molecule.builder()
              .id(UUID.randomUUID())
              .genericName("Morphine")
              .regulatorySchedule(RegulatorySchedule.CONTROLLED)
              .build();
      Molecule otcMolecule =
          Molecule.builder()
              .id(UUID.randomUUID())
              .genericName("Paracetamol")
              .regulatorySchedule(RegulatorySchedule.OTC)
              .build();
      Molecule rxMolecule =
          Molecule.builder()
              .id(UUID.randomUUID())
              .genericName("Amoxicillin")
              .regulatorySchedule(RegulatorySchedule.RX)
              .build();

      controlledBrand =
          Brand.builder()
              .id(UUID.randomUUID())
              .brandName("Morphine-Inj")
              .molecule(controlledMolecule)
              .build();
      otcBrand =
          Brand.builder()
              .id(UUID.randomUUID())
              .brandName("Tylenol")
              .molecule(otcMolecule)
              .build();
      rxBrand =
          Brand.builder()
              .id(UUID.randomUUID())
              .brandName("Amoxil")
              .molecule(rxMolecule)
              .build();

      Batch batch =
          Batch.builder()
              .id(1L)
              .brand(controlledBrand)
              .expiryDate(LocalDate.now().plusMonths(6))
              .build();
      stock = StoreStockFixture.storeStockWithQuantity(batch, store, 100L);

      syncSealResult =
          new SyncSealResult(VerificationStatus.VERIFIED, System.currentTimeMillis(), "abc123");
    }

    @Test
    @DisplayName("should succeed with signature for controlled substance")
    void shouldSucceedWithSignatureForControlled() throws Throwable {
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest()
              .storeId(storeId)
              .barcode("1234567890123")
              .signature(DigitalSignatureFixture.validSignatureRequest())
              .build();

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(controlledBrand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(syncSealService.verifyPharmacist(
              DigitalSignatureFixture.VALID_LICENSE, DigitalSignatureFixture.VALID_PAYLOAD))
          .thenReturn(syncSealResult);
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, controlledBrand.getId()))
          .thenReturn(List.of(stock));
      when(storeStockRepository.deductQuantity(eq(stock.getId()), eq(50L))).thenReturn(1);
      when(batchMapper.toDeductedBatchDto(eq(stock), eq(50L)))
          .thenReturn(
              DeductedBatchDto.builder()
                  .batchId(stock.getId())
                  .batchNumber("BATCH")
                  .deductedQuantity(50L)
                  .remainingQuantity(50L)
                  .build());

      StockDeductionResponseDto result = batchService.deductStock(request);

      assertNotNull(result.getSignatureVerification());
      assertEquals(
          DigitalSignatureFixture.VALID_LICENSE,
          result.getSignatureVerification().getLicenseNumber());
      assertEquals("VERIFIED", result.getSignatureVerification().getVerificationStatus());
      verify(digitalSignatureService).saveSignature(eq(stock.getId()), anyString(), any());
      verify(auditService)
          .recordAudit(
              eq("CONTROLLED_SUBSTANCE_SIGNED"),
              eq("StockDeduction"),
              anyString(),
              isNull(),
              anyString());
    }

    @Test
    @DisplayName("should throw CONTROLLED_SUBSTANCE_REQUIRES_SIGNATURE when no signature provided")
    void shouldThrowWhenNoSignatureForControlled() {
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest()
              .storeId(storeId)
              .barcode("1234567890123")
              .build();

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(controlledBrand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> batchService.deductStock(request));
      assertEquals(ErrorCode.CONTROLLED_SUBSTANCE_REQUIRES_SIGNATURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw INVALID_DIGITAL_SIGNATURE when license is blank for controlled")
    void shouldThrowWhenLicenseBlankForControlled() {
      DigitalSignatureRequest badRequest =
          DigitalSignatureRequest.builder().licenseNumber("").signaturePayload("payload").build();
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest()
              .storeId(storeId)
              .barcode("1234567890123")
              .signature(badRequest)
              .build();

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(controlledBrand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> batchService.deductStock(request));
      assertEquals(ErrorCode.INVALID_DIGITAL_SIGNATURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should succeed without signature for OTC substance")
    void shouldSucceedWithoutSignatureForOtc() throws Throwable {
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest()
              .storeId(storeId)
              .barcode("1234567890123")
              .build();

      Batch otcBatch =
          Batch.builder()
              .id(2L)
              .brand(otcBrand)
              .expiryDate(LocalDate.now().plusMonths(6))
              .build();
      StoreStock otcStock = StoreStockFixture.storeStockWithQuantity(otcBatch, store, 100L);

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(otcBrand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, otcBrand.getId()))
          .thenReturn(List.of(otcStock));
      when(storeStockRepository.deductQuantity(eq(otcStock.getId()), eq(50L))).thenReturn(1);
      when(batchMapper.toDeductedBatchDto(eq(otcStock), eq(50L)))
          .thenReturn(
              DeductedBatchDto.builder()
                  .batchId(2L)
                  .deductedQuantity(50L)
                  .remainingQuantity(50L)
                  .build());

      StockDeductionResponseDto result = batchService.deductStock(request);

      assertNull(result.getSignatureVerification());
      verify(syncSealService, never()).verifyPharmacist(anyString(), anyString());
      verify(digitalSignatureService, never()).saveSignature(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("should succeed without signature for RX substance")
    void shouldSucceedWithoutSignatureForRx() throws Throwable {
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest()
              .storeId(storeId)
              .barcode("1234567890123")
              .build();

      Batch rxBatch =
          Batch.builder()
              .id(3L)
              .brand(rxBrand)
              .expiryDate(LocalDate.now().plusMonths(6))
              .build();
      StoreStock rxStock = StoreStockFixture.storeStockWithQuantity(rxBatch, store, 100L);

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(rxBrand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      when(storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              storeId, rxBrand.getId()))
          .thenReturn(List.of(rxStock));
      when(storeStockRepository.deductQuantity(eq(rxStock.getId()), eq(50L))).thenReturn(1);
      when(batchMapper.toDeductedBatchDto(eq(rxStock), eq(50L)))
          .thenReturn(
              DeductedBatchDto.builder()
                  .batchId(3L)
                  .deductedQuantity(50L)
                  .remainingQuantity(50L)
                  .build());

      StockDeductionResponseDto result = batchService.deductStock(request);

      assertNull(result.getSignatureVerification());
      verify(syncSealService, never()).verifyPharmacist(anyString(), anyString());
    }

    @Test
    @DisplayName("should throw SIGNATURE_VERIFICATION_FAILED when SyncSeal returns FAILED")
    void shouldThrowWhenSyncSealFails() {
      StockDeductionRequest request =
          BatchFixture.validStockDeductionRequest()
              .storeId(storeId)
              .barcode("1234567890123")
              .signature(DigitalSignatureFixture.validSignatureRequest())
              .build();

      SyncSealResult failedResult =
          new SyncSealResult(VerificationStatus.FAILED, System.currentTimeMillis(), "fail");

      when(brandRepository.findByBarcode(request.getBarcode()))
          .thenReturn(Optional.of(controlledBrand));
      when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
      try {
        when(syncSealService.verifyPharmacist(
                DigitalSignatureFixture.VALID_LICENSE, DigitalSignatureFixture.VALID_PAYLOAD))
            .thenReturn(failedResult);
      } catch (ApplicationException e) {
        throw new RuntimeException(e);
      }

      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> batchService.deductStock(request));
      assertEquals(ErrorCode.SIGNATURE_VERIFICATION_FAILED, ex.getErrorCode());
    }
  }

  @Nested
  @DisplayName("getBatchById")
  class GetBatchById {

    @Test
    @DisplayName("should return batch DTO when batch exists")
    void shouldReturnBatchDtoWhenBatchExists() throws Throwable {
      Batch batch = BatchFixture.defaultBatch();
      BatchDto batchDto = BatchFixture.validBatchDto();

      when(batchRepository.findById(batch.getId())).thenReturn(Optional.of(batch));
      when(batchMapper.toBatchDto(batch)).thenReturn(batchDto);

      BatchDto result = batchService.getBatchById(batch.getId());

      assertEquals(batchDto, result);
      verify(batchRepository).findById(batch.getId());
      verify(batchMapper).toBatchDto(batch);
    }

    @Test
    @DisplayName("should throw BATCH_NOT_FOUND when batch does not exist")
    void shouldThrowBatchNotFoundWhenBatchDoesNotExist() {
      Long batchId = 1L;

      when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

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
      UUID brandId = UUID.randomUUID();
      Batch batch = BatchFixture.defaultBatch();
      List<Batch> batches = List.of(batch);
      Page<Batch> batchPage = new PageImpl<>(batches);

      when(batchRepository.findByBrandId(eq(brandId), any(Pageable.class)))
          .thenReturn(batchPage);

      Page<BatchDto> result = batchService.getBatchesByBrandId(brandId, Pageable.unpaged());

      assertEquals(1, result.getTotalElements());
      verify(batchRepository).findByBrandId(eq(brandId), any(Pageable.class));
    }

    @Test
    @DisplayName("should return empty page when brand has no batches")
    void shouldReturnEmptyPageWhenBrandHasNoBatches() {
      UUID brandId = UUID.randomUUID();
      Page<Batch> emptyPage = Page.empty();

      when(batchRepository.findByBrandId(eq(brandId), any(Pageable.class)))
          .thenReturn(emptyPage);

      Page<BatchDto> result = batchService.getBatchesByBrandId(brandId, Pageable.unpaged());

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
      UUID storeId = UUID.randomUUID();
      StoreStock storeStock = StoreStockFixture.defaultStoreStock();
      List<StoreStock> storeStocks = List.of(storeStock);
      Page<StoreStock> storeStockPage = new PageImpl<>(storeStocks);

      when(storeStockRepository.findByStoreId(eq(storeId), any(Pageable.class)))
          .thenReturn(storeStockPage);

      Page<StoreStockDto> result =
          batchService.getStoreStocksByStoreId(storeId, Pageable.unpaged());

      assertEquals(1, result.getTotalElements());
      verify(storeStockRepository).findByStoreId(eq(storeId), any(Pageable.class));
    }

    @Test
    @DisplayName("should return empty page when store has no stock")
    void shouldReturnEmptyPageWhenStoreHasNoStock() {
      UUID storeId = UUID.randomUUID();
      Page<StoreStock> emptyPage = Page.empty();

      when(storeStockRepository.findByStoreId(eq(storeId), any(Pageable.class)))
          .thenReturn(emptyPage);

      Page<StoreStockDto> result =
          batchService.getStoreStocksByStoreId(storeId, Pageable.unpaged());

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
      StoreStock storeStock = StoreStockFixture.defaultStoreStock();
      StoreStockDto storeStockDto = StoreStockFixture.validStoreStockDto();

      when(storeStockRepository.findByStoreIdAndBatchId(
              eq(storeStock.getStore().getId()), eq(storeStock.getBatch().getId())))
          .thenReturn(Optional.of(storeStock));
      when(batchMapper.toStoreStockDto(storeStock)).thenReturn(storeStockDto);

      StoreStockDto result =
          batchService.getStoreStock(storeStock.getStore().getId(), storeStock.getBatch().getId());

      assertEquals(storeStockDto, result);
      verify(storeStockRepository)
          .findByStoreIdAndBatchId(
              eq(storeStock.getStore().getId()), eq(storeStock.getBatch().getId()));
      verify(batchMapper).toStoreStockDto(storeStock);
    }

    @Test
    @DisplayName("should throw STOCK_NOT_FOUND when record does not exist")
    void shouldThrowStockNotFoundWhenRecordDoesNotExist() {
      UUID storeId = UUID.randomUUID();
      Long batchId = 1L;

      when(storeStockRepository.findByStoreIdAndBatchId(storeId, batchId))
          .thenReturn(Optional.empty());

      assertThrows(ApplicationException.class, () -> batchService.getStoreStock(storeId, batchId));

      verify(storeStockRepository).findByStoreIdAndBatchId(storeId, batchId);
      verify(batchMapper, never()).toStoreStockDto(any());
    }
  }
}
