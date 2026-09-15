package com.mrtripop.inventory.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.inventory.fixture.DigitalSignatureFixture;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.DigitalSignature;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.db.VerificationStatus;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName(
    "Pharmacists' digital signatures on controlled-substance stock are recorded and retrievable")
class DigitalSignatureRepositoryIT {

  @Autowired private DigitalSignatureRepository digitalSignatureRepository;
  @Autowired private StoreStockRepository storeStockRepository;
  @Autowired private StoreRepository storeRepository;
  @Autowired private MoleculeRepository moleculeRepository;
  @Autowired private BrandRepository brandRepository;
  @Autowired private com.mrtripop.inventory.repository.BatchRepository batchRepository;

  private StoreStock storeStock;

  @BeforeEach
  void setUp() {
    Store store =
        storeRepository.save(
            Store.builder().name("Test Store").type(StoreType.PHYSICAL).build());

    Molecule molecule =
        moleculeRepository.save(Molecule.builder().genericName("Morphine").build());

    Brand brand =
        brandRepository.save(
            Brand.builder().molecule(molecule).brandName("Morphine-Inj").baseUnit("VIAL").build());

    Batch batch =
        batchRepository.save(
            Batch.builder()
                .brand(brand)
                .batchNumber("BATCH-001")
                .expiryDate(LocalDate.now().plusDays(60))
                .quantity(100L)
                .status(BatchStatus.AVAILABLE)
                .build());

    storeStock =
        storeStockRepository.save(
            StoreStock.builder().store(store).batch(batch).quantity(100L).build());
  }

  @Nested
  @DisplayName("A pharmacist signs a controlled-substance stock movement")
  class SaveAndRetrieve {

    @Test
    @DisplayName(
        "the signature is saved and can be retrieved with the pharmacist's license and "
            + "verification result intact")
    void shouldSaveAndRetrieveDigitalSignature() {
      // Arrange
      DigitalSignature signature = DigitalSignatureFixture.validDigitalSignatureEntity(storeStock);
      signature.setId(null); // fixture's id is for mocked saves; a real save must start unsaved

      // Act
      signature = digitalSignatureRepository.save(signature);
      Optional<DigitalSignature> found = digitalSignatureRepository.findById(signature.getId());

      // Assert
      assertTrue(found.isPresent());
      assertEquals(DigitalSignatureFixture.VALID_LICENSE, found.get().getPharmacistLicenseNumber());
      assertEquals(VerificationStatus.VERIFIED, found.get().getVerificationStatus());
      assertNotNull(found.get().getCreatedAt());
    }
  }

  @Nested
  @DisplayName(
      "Staff look up whether a specific stock item already carries a pharmacist's signature")
  class FindByStoreStockId {

    @Test
    @DisplayName("a stock item that was signed returns its recorded signature")
    void shouldReturnSignatureForMatchingStoreStockId() {
      // Arrange
      DigitalSignature signature = DigitalSignatureFixture.validDigitalSignatureEntity(storeStock);
      signature.setId(null); // fixture's id is for mocked saves; a real save must start unsaved
      digitalSignatureRepository.save(signature);

      // Act
      Optional<DigitalSignature> found =
          digitalSignatureRepository.findByStoreStockId(storeStock.getId());

      // Assert
      assertTrue(found.isPresent());
      assertEquals(DigitalSignatureFixture.VALID_LICENSE, found.get().getPharmacistLicenseNumber());
    }

    @Test
    @DisplayName("a stock item that was never signed returns no signature")
    void shouldReturnEmptyForNonExistentStoreStockId() {
      // Act
      Optional<DigitalSignature> found =
          digitalSignatureRepository.findByStoreStockId(99999L);

      // Assert
      assertTrue(found.isEmpty());
    }
  }

  @Nested
  @DisplayName(
      "A failed signature verification must be recorded just as reliably as a successful one")
  class EnumPersistence {

    @Test
    @DisplayName("a failed verification outcome is saved and read back as failed")
    void shouldPersistVerificationStatusAsString() {
      // Arrange
      DigitalSignature signature =
          DigitalSignature.builder()
              .storeStock(storeStock)
              .pharmacistLicenseNumber(DigitalSignatureFixture.VALID_LICENSE)
              .verificationStatus(VerificationStatus.FAILED)
              .verifiedAt(System.currentTimeMillis())
              .build();

      // Act
      signature = digitalSignatureRepository.save(signature);
      DigitalSignature found = digitalSignatureRepository.findById(signature.getId()).orElseThrow();

      // Assert
      assertEquals(VerificationStatus.FAILED, found.getVerificationStatus());
    }
  }
}
