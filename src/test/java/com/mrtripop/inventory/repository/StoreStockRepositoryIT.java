package com.mrtripop.inventory.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.StoreStock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("StoreStockRepository")
class StoreStockRepositoryIT {

  @Autowired private StoreStockRepository storeStockRepository;
  @Autowired private BatchRepository batchRepository;
  @Autowired private BrandRepository brandRepository;
  @Autowired private MoleculeRepository moleculeRepository;
  @Autowired private StoreRepository storeRepository;

  private Store store;
  private Brand brand;
  private Batch batch;

  @BeforeEach
  void setUp() {
    storeStockRepository.deleteAll();
    batchRepository.deleteAll();
    brandRepository.deleteAll();
    moleculeRepository.deleteAll();
    storeRepository.deleteAll();

    Molecule molecule = Molecule.builder().genericName("Paracetamol").build();
    molecule = moleculeRepository.save(molecule);

    brand =
        Brand.builder()
            .molecule(molecule)
            .brandName("Tylenol")
            .barcode("1234567890123")
            .build();
    brand = brandRepository.save(brand);

    batch =
        Batch.builder()
            .brand(brand)
            .batchNumber("BATCH-001")
            .expiryDate(LocalDate.now().plusYears(1))
            .quantity(100L)
            .status(BatchStatus.AVAILABLE)
            .build();
    batch = batchRepository.save(batch);

    store = Store.builder().name("Main Store").type(StoreType.PHYSICAL).build();
    store = storeRepository.save(store);
  }

  @Nested
  @DisplayName("findByStoreIdAndBatchId")
  class FindByStoreIdAndBatchId {

    @Test
    @DisplayName("should find store stock when store ID and batch ID match")
    void shouldFindStoreStockWhenStoreIdAndBatchIdMatch() {
      StoreStock storeStock =
          StoreStock.builder().store(store).batch(batch).quantity(50L).build();
      storeStockRepository.save(storeStock);

      Optional<StoreStock> result =
          storeStockRepository.findByStoreIdAndBatchId(store.getId(), batch.getId());

      assertTrue(result.isPresent());
      assertEquals(storeStock.getId(), result.get().getId());
      assertEquals(50L, result.get().getQuantity());
    }

    @Test
    @DisplayName("should return empty when batch ID does not match")
    void shouldReturnEmptyWhenBatchIdDoesNotMatch() {
      StoreStock storeStock =
          StoreStock.builder().store(store).batch(batch).quantity(50L).build();
      storeStockRepository.save(storeStock);

      Molecule anotherMolecule = Molecule.builder().genericName("Ibuprofen").build();
      anotherMolecule = moleculeRepository.save(anotherMolecule);

      Brand anotherBrand =
          Brand.builder()
              .molecule(anotherMolecule)
              .brandName("Advil")
              .barcode("9876543210987")
              .build();
      anotherBrand = brandRepository.save(anotherBrand);

      Batch anotherBatch =
          Batch.builder()
              .brand(anotherBrand)
              .batchNumber("BATCH-002")
              .expiryDate(LocalDate.now().plusYears(1))
              .quantity(200L)
              .status(BatchStatus.AVAILABLE)
              .build();
      anotherBatch = batchRepository.save(anotherBatch);

      Optional<StoreStock> result =
          storeStockRepository.findByStoreIdAndBatchId(store.getId(), anotherBatch.getId());

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("findByStoreId")
  class FindByStoreId {

    @Test
    @DisplayName("should return paged store stocks for a specific store")
    void shouldReturnPagedStoreStocksForASpecificStore() {
      for (long i = 1; i <= 3; i++) {
        StoreStock stock =
            StoreStock.builder().store(store).batch(batch).quantity(i * 10L).build();
        storeStockRepository.save(stock);
      }

      Page<StoreStock> result = storeStockRepository.findByStoreId(store.getId(), PageRequest.of(0, 10));

      assertEquals(3, result.getTotalElements());
    }
  }

  @Nested
  @DisplayName("unique constraint on (store_id, batch_id)")
  class UniqueConstraint {

    @Test
    @DisplayName(
        "should throw DataIntegrityViolationException when inserting duplicate store_id and batch_id")
    void shouldThrowWhenInsertingDuplicateStoreIdAndBatchId() {
      StoreStock storeStock =
          StoreStock.builder().store(store).batch(batch).quantity(50L).build();
      storeStockRepository.save(storeStock);

      StoreStock duplicate =
          StoreStock.builder().store(store).batch(batch).quantity(100L).build();

      assertThrows(
          DataIntegrityViolationException.class,
          () -> {
            storeStockRepository.save(duplicate);
            storeStockRepository.flush();
          });
    }
  }

  @Nested
  @DisplayName("FK constraint to Batch")
  class FKConstraint {

    @Test
    @DisplayName("should throw DataIntegrityViolationException when batch ID does not exist")
    void shouldThrowWhenBatchIdDoesNotExist() {
      Batch nonExistentBatch = Batch.builder().id(99999L).build();

      StoreStock invalidStock =
          StoreStock.builder().store(store).batch(nonExistentBatch).quantity(10L).build();

      assertThrows(
          DataIntegrityViolationException.class,
          () -> {
            storeStockRepository.save(invalidStock);
            storeStockRepository.flush();
          });
    }

    @Test
    @DisplayName("should successfully create store stock with valid batch reference")
    void shouldSuccessfullyCreateStoreStockWithValidBatchReference() {
      StoreStock storeStock =
          StoreStock.builder().store(store).batch(batch).quantity(50L).build();

      StoreStock result = storeStockRepository.save(storeStock);

      assertNotNull(result.getId());
      assertEquals(store.getId(), result.getStore().getId());
      assertEquals(batch.getId(), result.getBatch().getId());
      assertEquals(50L, result.getQuantity());
    }
  }

  @Nested
  @DisplayName("findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate (FEFO)")
  class FefoQuery {

    @Test
    @DisplayName("should return batches ordered by expiryDate ASC (FEFO)")
    void shouldReturnBatchesOrderedByExpiryDateAsc() {
      Batch batch1 =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-FAR")
              .expiryDate(LocalDate.now().plusYears(2))
              .quantity(100L)
              .status(BatchStatus.AVAILABLE)
              .build();
      batch1 = batchRepository.save(batch1);

      Batch batch2 =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-SOON")
              .expiryDate(LocalDate.now().plusMonths(1))
              .quantity(100L)
              .status(BatchStatus.AVAILABLE)
              .build();
      batch2 = batchRepository.save(batch2);

      Batch batch3 =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-MID")
              .expiryDate(LocalDate.now().plusMonths(6))
              .quantity(100L)
              .status(BatchStatus.AVAILABLE)
              .build();
      batch3 = batchRepository.save(batch3);

      storeStockRepository.save(
          StoreStock.builder().store(store).batch(batch1).quantity(50L).build());
      storeStockRepository.save(
          StoreStock.builder().store(store).batch(batch2).quantity(30L).build());
      storeStockRepository.save(
          StoreStock.builder().store(store).batch(batch3).quantity(20L).build());

      List<StoreStock> result =
          storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              store.getId(), brand.getId());

      assertEquals(3, result.size());
      assertEquals("BATCH-SOON", result.get(0).getBatch().getBatchNumber());
      assertEquals("BATCH-MID", result.get(1).getBatch().getBatchNumber());
      assertEquals("BATCH-FAR", result.get(2).getBatch().getBatchNumber());
    }

    @Test
    @DisplayName("should exclude expired batches")
    void shouldExcludeExpiredBatches() {
      Batch expiredBatch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-EXPIRED")
              .expiryDate(LocalDate.now().minusDays(1))
              .quantity(100L)
              .status(BatchStatus.AVAILABLE)
              .build();
      expiredBatch = batchRepository.save(expiredBatch);

      Batch validBatch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-VALID")
              .expiryDate(LocalDate.now().plusMonths(6))
              .quantity(100L)
              .status(BatchStatus.AVAILABLE)
              .build();
      validBatch = batchRepository.save(validBatch);

      storeStockRepository.save(
          StoreStock.builder().store(store).batch(expiredBatch).quantity(50L).build());
      storeStockRepository.save(
          StoreStock.builder().store(store).batch(validBatch).quantity(30L).build());

      List<StoreStock> result =
          storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              store.getId(), brand.getId());

      assertEquals(1, result.size());
      assertEquals("BATCH-VALID", result.get(0).getBatch().getBatchNumber());
    }

    @Test
    @DisplayName("should exclude RECALLED batches")
    void shouldExcludeRecalledBatches() {
      Batch recalledBatch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-RECALLED")
              .expiryDate(LocalDate.now().plusMonths(6))
              .quantity(100L)
              .status(BatchStatus.RECALLED)
              .build();
      recalledBatch = batchRepository.save(recalledBatch);

      Batch validBatch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-VALID")
              .expiryDate(LocalDate.now().plusMonths(12))
              .quantity(100L)
              .status(BatchStatus.AVAILABLE)
              .build();
      validBatch = batchRepository.save(validBatch);

      storeStockRepository.save(
          StoreStock.builder().store(store).batch(recalledBatch).quantity(50L).build());
      storeStockRepository.save(
          StoreStock.builder().store(store).batch(validBatch).quantity(30L).build());

      List<StoreStock> result =
          storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              store.getId(), brand.getId());

      assertEquals(1, result.size());
      assertEquals("BATCH-VALID", result.get(0).getBatch().getBatchNumber());
    }

    @Test
    @DisplayName("should exclude QUARANTINED batches")
    void shouldExcludeQuarantinedBatches() {
      Batch quarantinedBatch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-QUARANTINED")
              .expiryDate(LocalDate.now().plusMonths(6))
              .quantity(100L)
              .status(BatchStatus.QUARANTINED)
              .build();
      quarantinedBatch = batchRepository.save(quarantinedBatch);

      Batch validBatch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-VALID")
              .expiryDate(LocalDate.now().plusMonths(12))
              .quantity(100L)
              .status(BatchStatus.AVAILABLE)
              .build();
      validBatch = batchRepository.save(validBatch);

      storeStockRepository.save(
          StoreStock.builder().store(store).batch(quarantinedBatch).quantity(50L).build());
      storeStockRepository.save(
          StoreStock.builder().store(store).batch(validBatch).quantity(30L).build());

      List<StoreStock> result =
          storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              store.getId(), brand.getId());

      assertEquals(1, result.size());
      assertEquals("BATCH-VALID", result.get(0).getBatch().getBatchNumber());
    }

    @Test
    @DisplayName("should exclude zero-quantity store stock")
    void shouldExcludeZeroQuantityStoreStock() {
      Batch batch1 =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-ZERO")
              .expiryDate(LocalDate.now().plusMonths(1))
              .quantity(100L)
              .status(BatchStatus.AVAILABLE)
              .build();
      batch1 = batchRepository.save(batch1);

      Batch batch2 =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-HAS-STOCK")
              .expiryDate(LocalDate.now().plusMonths(6))
              .quantity(100L)
              .status(BatchStatus.AVAILABLE)
              .build();
      batch2 = batchRepository.save(batch2);

      storeStockRepository.save(
          StoreStock.builder().store(store).batch(batch1).quantity(0L).build());
      storeStockRepository.save(
          StoreStock.builder().store(store).batch(batch2).quantity(30L).build());

      List<StoreStock> result =
          storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              store.getId(), brand.getId());

      assertEquals(1, result.size());
      assertEquals("BATCH-HAS-STOCK", result.get(0).getBatch().getBatchNumber());
    }

    @Test
    @DisplayName("should return empty list when no available batches exist")
    void shouldReturnEmptyListWhenNoAvailableBatchesExist() {
      List<StoreStock> result =
          storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
              store.getId(), brand.getId());

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("deductQuantity")
  class DeductQuantity {

    @Test
    @DisplayName("should deduct quantity and return 1 when sufficient stock")
    void shouldDeductQuantityAndReturn1WhenSufficientStock() {
      StoreStock stock =
          StoreStock.builder().store(store).batch(batch).quantity(100L).build();
      stock = storeStockRepository.save(stock);
      storeStockRepository.flush();

      int updated = storeStockRepository.deductQuantity(stock.getId(), 30L);
      storeStockRepository.flush();

      assertEquals(1, updated);
      StoreStock updatedStock =
          storeStockRepository.findById(stock.getId()).orElseThrow();
      assertEquals(70L, updatedStock.getQuantity());
    }

    @Test
    @DisplayName("should return 0 when attempting to deduct more than available")
    void shouldReturn0WhenAttemptingToDeductMoreThanAvailable() {
      StoreStock stock =
          StoreStock.builder().store(store).batch(batch).quantity(10L).build();
      stock = storeStockRepository.save(stock);
      storeStockRepository.flush();

      int updated = storeStockRepository.deductQuantity(stock.getId(), 50L);
      storeStockRepository.flush();

      assertEquals(0, updated);
      StoreStock unchangedStock =
          storeStockRepository.findById(stock.getId()).orElseThrow();
      assertEquals(10L, unchangedStock.getQuantity());
    }
  }
}
