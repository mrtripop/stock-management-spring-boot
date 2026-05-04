package com.mrtripop.inventory.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.inventory.models.db.Batch;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
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
@DisplayName("BatchRepository")
class BatchRepositoryIT {

  @Autowired private BatchRepository batchRepository;
  @Autowired private BrandRepository brandRepository;
  @Autowired private MoleculeRepository moleculeRepository;

  @BeforeEach
  void setUp() {
    batchRepository.deleteAll();
    brandRepository.deleteAll();
    moleculeRepository.deleteAll();
  }

  @Nested
  @DisplayName("findByBrandIdAndBatchNumber")
  class FindByBrandIdAndBatchNumber {

    @Test
    @DisplayName("should find batch when brand ID and batch number match")
    void shouldFindBatchWhenBrandIdAndBatchNumberMatch() {
      // Arrange
      Molecule molecule = Molecule.builder().genericName("Paracetamol").build();
      molecule = moleculeRepository.save(molecule);

      Brand brand =
          Brand.builder()
              .molecule(molecule)
              .brandName("Tylenol")
              .barcode("1234567890123")
              .build();
      brand = brandRepository.save(brand);

      Batch batch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-001")
              .expiryDate(LocalDate.now().plusYears(1))
              .quantity(100L)
              .build();
      batchRepository.save(batch);

      // Act
      Optional<Batch> result =
          batchRepository.findByBrandIdAndBatchNumber(brand.getId(), "BATCH-001");

      // Assert
      assertTrue(result.isPresent());
      assertEquals(batch.getId(), result.get().getId());
      assertEquals("BATCH-001", result.get().getBatchNumber());
    }

    @Test
    @DisplayName("should return empty when batch number does not match")
    void shouldReturnEmptyWhenBatchNumberDoesNotMatch() {
      // Arrange
      Molecule molecule = Molecule.builder().genericName("Paracetamol").build();
      molecule = moleculeRepository.save(molecule);

      Brand brand =
          Brand.builder()
              .molecule(molecule)
              .brandName("Tylenol")
              .barcode("1234567890123")
              .build();
      brand = brandRepository.save(brand);

      Batch batch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-001")
              .expiryDate(LocalDate.now().plusYears(1))
              .quantity(100L)
              .build();
      batchRepository.save(batch);

      // Act
      Optional<Batch> result =
          batchRepository.findByBrandIdAndBatchNumber(brand.getId(), "DIFFERENT-BATCH");

      // Assert
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty when brand ID does not match")
    void shouldReturnEmptyWhenBrandIdDoesNotMatch() {
      // Arrange
      Molecule molecule = Molecule.builder().genericName("Paracetamol").build();
      molecule = moleculeRepository.save(molecule);

      Brand brand =
          Brand.builder()
              .molecule(molecule)
              .brandName("Tylenol")
              .barcode("1234567890123")
              .build();
      brand = brandRepository.save(brand);

      Batch batch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-001")
              .expiryDate(LocalDate.now().plusYears(1))
              .quantity(100L)
              .build();
      batchRepository.save(batch);

      // Act
      Optional<Batch> result =
          batchRepository.findByBrandIdAndBatchNumber(UUID.randomUUID(), "BATCH-001");

      // Assert
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("findByBrandId")
  class FindByBrandId {

    @Test
    @DisplayName("should return paged batches for a specific brand")
    void shouldReturnPagedBatchesForASpecificBrand() {
      // Arrange
      Molecule molecule = Molecule.builder().genericName("Paracetamol").build();
      molecule = moleculeRepository.save(molecule);

      Brand brand =
          Brand.builder()
              .molecule(molecule)
              .brandName("Tylenol")
              .barcode("1234567890123")
              .build();
      brand = brandRepository.save(brand);

      for (int i = 1; i <= 3; i++) {
        Batch batch =
            Batch.builder()
                .brand(brand)
                .batchNumber("BATCH-00" + i)
                .expiryDate(LocalDate.now().plusYears(1))
                .quantity(100L * i)
                .build();
        batchRepository.save(batch);
      }

      // Act
      Page<Batch> result = batchRepository.findByBrandId(brand.getId(), PageRequest.of(0, 10));

      // Assert
      assertEquals(3, result.getTotalElements());
      assertTrue(
          result.getContent().stream().anyMatch(b -> b.getBatchNumber().equals("BATCH-001")));
      assertTrue(
          result.getContent().stream().anyMatch(b -> b.getBatchNumber().equals("BATCH-002")));
      assertTrue(
          result.getContent().stream().anyMatch(b -> b.getBatchNumber().equals("BATCH-003")));
    }
  }

  @Nested
  @DisplayName("unique constraint on (brand_id, batch_number)")
  class UniqueConstraint {

    @Test
    @DisplayName(
        "should throw DataIntegrityViolationException when inserting duplicate brand_id and batch_number")
    void shouldThrowWhenInsertingDuplicateBrandIdAndBatchNumber() {
      // Arrange
      Molecule molecule = Molecule.builder().genericName("Paracetamol").build();
      molecule = moleculeRepository.save(molecule);

      Brand brand =
          Brand.builder()
              .molecule(molecule)
              .brandName("Tylenol")
              .barcode("1234567890123")
              .build();
      brand = brandRepository.save(brand);

      Batch batch =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-001")
              .expiryDate(LocalDate.now().plusYears(1))
              .quantity(100L)
              .build();
      batchRepository.save(batch);

      Batch duplicate =
          Batch.builder()
              .brand(brand)
              .batchNumber("BATCH-001")
              .expiryDate(LocalDate.now().plusYears(2))
              .quantity(200L)
              .build();

      // Act & Assert
      assertThrows(
          DataIntegrityViolationException.class,
          () -> {
            batchRepository.save(duplicate);
            TestTransaction.flagForCommit();
            TestTransaction.end();
          });
    }
  }

  @Nested
  @DisplayName("barcode resolution")
  class BarcodeResolution {

    @Test
    @DisplayName("should resolve brand by barcode")
    void shouldResolveBrandByBarcode() {
      // Arrange
      Molecule molecule = Molecule.builder().genericName("Paracetamol").build();
      molecule = moleculeRepository.save(molecule);

      Brand brand =
          Brand.builder()
              .molecule(molecule)
              .brandName("Tylenol")
              .barcode("1234567890123")
              .build();
      brand = brandRepository.save(brand);

      // Act
      Optional<Brand> result = brandRepository.findByBarcode("1234567890123");

      // Assert
      assertTrue(result.isPresent());
      assertEquals(brand.getId(), result.get().getId());
      assertEquals("Tylenol", result.get().getBrandName());
    }

    @Test
    @DisplayName("should return empty when barcode not found")
    void shouldReturnEmptyWhenBarcodeNotFound() {
      // Arrange
      Molecule molecule = Molecule.builder().genericName("Paracetamol").build();
      molecule = moleculeRepository.save(molecule);

      Brand brand =
          Brand.builder()
              .molecule(molecule)
              .brandName("Tylenol")
              .barcode("1234567890123")
              .build();
      brandRepository.save(brand);

      // Act
      Optional<Brand> result = brandRepository.findByBarcode("NONEXISTENT");

      // Assert
      assertTrue(result.isEmpty());
    }
  }
}
