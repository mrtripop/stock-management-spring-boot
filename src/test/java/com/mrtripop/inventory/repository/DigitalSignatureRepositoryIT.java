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
import com.mrtripop.inventory.models.db.DigitalSignature;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.db.VerificationStatus;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
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
  class SaveAndRetrieve {

    @Test
    void shouldSaveAndRetrieveDigitalSignature() {
      DigitalSignature signature =
          DigitalSignature.builder()
              .storeStock(storeStock)
              .pharmacistLicenseNumber("PHARM-12345")
              .signaturePayload("test-payload")
              .signatureHash("abc123hash")
              .verificationStatus(VerificationStatus.VERIFIED)
              .verifiedAt(System.currentTimeMillis())
              .build();
      signature = digitalSignatureRepository.save(signature);

      Optional<DigitalSignature> found = digitalSignatureRepository.findById(signature.getId());
      assertTrue(found.isPresent());
      assertEquals("PHARM-12345", found.get().getPharmacistLicenseNumber());
      assertEquals(VerificationStatus.VERIFIED, found.get().getVerificationStatus());
      assertNotNull(found.get().getCreatedAt());
    }
  }

  @Nested
  class FindByStoreStockId {

    @Test
    void shouldReturnSignatureForMatchingStoreStockId() {
      DigitalSignature signature =
          DigitalSignature.builder()
              .storeStock(storeStock)
              .pharmacistLicenseNumber("PHARM-67890")
              .signaturePayload("payload")
              .signatureHash("hash456")
              .verificationStatus(VerificationStatus.VERIFIED)
              .verifiedAt(System.currentTimeMillis())
              .build();
      digitalSignatureRepository.save(signature);

      Optional<DigitalSignature> found =
          digitalSignatureRepository.findByStoreStockId(storeStock.getId());

      assertTrue(found.isPresent());
      assertEquals("PHARM-67890", found.get().getPharmacistLicenseNumber());
    }

    @Test
    void shouldReturnEmptyForNonExistentStoreStockId() {
      Optional<DigitalSignature> found =
          digitalSignatureRepository.findByStoreStockId(99999L);

      assertTrue(found.isEmpty());
    }
  }

  @Nested
  class EnumPersistence {

    @Test
    void shouldPersistVerificationStatusAsString() {
      DigitalSignature signature =
          DigitalSignature.builder()
              .storeStock(storeStock)
              .pharmacistLicenseNumber("PHARM-11111")
              .verificationStatus(VerificationStatus.FAILED)
              .verifiedAt(System.currentTimeMillis())
              .build();
      signature = digitalSignatureRepository.save(signature);

      DigitalSignature found = digitalSignatureRepository.findById(signature.getId()).orElseThrow();

      assertEquals(VerificationStatus.FAILED, found.getVerificationStatus());
    }
  }
}
