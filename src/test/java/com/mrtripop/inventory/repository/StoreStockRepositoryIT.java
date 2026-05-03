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
import com.mrtripop.inventory.models.db.StoreStock;
import java.time.LocalDate;
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
import org.springframework.test.context.transaction.TestTransaction;
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

    Brand brand =
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
      // Arrange
      StoreStock storeStock =
          StoreStock.builder().store(store).batch(batch).quantity(50L).build();
      storeStockRepository.save(storeStock);

      // Act
      Optional<StoreStock> result =
          storeStockRepository.findByStoreIdAndBatchId(store.getId(), batch.getId());

      // Assert
      assertTrue(result.isPresent());
      assertEquals(storeStock.getId(), result.get().getId());
      assertEquals(50L, result.get().getQuantity());
    }

    @Test
    @DisplayName("should return empty when batch ID does not match")
    void shouldReturnEmptyWhenBatchIdDoesNotMatch() {
      // Arrange
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
              .build();
      anotherBatch = batchRepository.save(anotherBatch);

      // Act
      Optional<StoreStock> result =
          storeStockRepository.findByStoreIdAndBatchId(store.getId(), anotherBatch.getId());

      // Assert
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("findByStoreId")
  class FindByStoreId {

    @Test
    @DisplayName("should return paged store stocks for a specific store")
    void shouldReturnPagedStoreStocksForASpecificStore() {
      // Arrange
      for (long i = 1; i <= 3; i++) {
        StoreStock stock =
            StoreStock.builder().store(store).batch(batch).quantity(i * 10L).build();
        storeStockRepository.save(stock);
      }

      // Act
      Page<StoreStock> result = storeStockRepository.findByStoreId(store.getId(), PageRequest.of(0, 10));

      // Assert
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
      // Arrange
      StoreStock storeStock =
          StoreStock.builder().store(store).batch(batch).quantity(50L).build();
      storeStockRepository.save(storeStock);

      StoreStock duplicate =
          StoreStock.builder().store(store).batch(batch).quantity(100L).build();

      // Act & Assert
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
      // Arrange — use a transient Batch with a non-existent ID
      Batch nonExistentBatch = Batch.builder().id(99999L).build();

      StoreStock invalidStock =
          StoreStock.builder().store(store).batch(nonExistentBatch).quantity(10L).build();

      // Act & Assert
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
      // Arrange
      StoreStock storeStock =
          StoreStock.builder().store(store).batch(batch).quantity(50L).build();

      // Act
      StoreStock result = storeStockRepository.save(storeStock);

      // Assert
      assertNotNull(result.getId());
      assertEquals(store.getId(), result.getStore().getId());
      assertEquals(batch.getId(), result.getBatch().getId());
      assertEquals(50L, result.getQuantity());
    }
  }
}
