package com.mrtripop.inventory.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.inventory.component.DigitalSignatureMapper;
import com.mrtripop.inventory.fixture.DigitalSignatureFixture;
import com.mrtripop.inventory.fixture.StoreStockFixture;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.DigitalSignatureDto;
import com.mrtripop.inventory.models.dto.SyncSealResult;
import com.mrtripop.inventory.repository.DigitalSignatureRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("DigitalSignatureServiceImpl")
class DigitalSignatureServiceImplTest {

  @Mock private DigitalSignatureRepository digitalSignatureRepository;
  @Mock private StoreStockRepository storeStockRepository;
  @Mock private DigitalSignatureMapper digitalSignatureMapper;

  @InjectMocks private DigitalSignatureServiceImpl digitalSignatureService;

  private StoreStock storeStock;
  private SyncSealResult syncSealResult;

  @BeforeEach
  void setUp() {
    UUID storeId = UUID.randomUUID();
    UUID brandId = UUID.randomUUID();
    Store store = Store.builder().id(storeId).build();
    Brand brand = Brand.builder().id(brandId).build();
    Batch batch =
        Batch.builder()
            .id(1L)
            .brand(brand)
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();
    storeStock = StoreStockFixture.storeStockWithQuantity(batch, store, 100L);
    syncSealResult = DigitalSignatureFixture.defaultSyncSealResult();
  }

  @Nested
  @DisplayName("saveSignature")
  class SaveSignature {

    @Test
    @DisplayName("should save digital signature linked to store stock")
    void shouldSaveSignatureLinkedToStoreStock() throws Throwable {
      when(storeStockRepository.findById(storeStock.getId())).thenReturn(Optional.of(storeStock));
      when(digitalSignatureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

      digitalSignatureService.saveSignature(
          storeStock.getId(), DigitalSignatureFixture.VALID_LICENSE, syncSealResult);

      verify(digitalSignatureRepository).save(argThat(sig -> {
        assertNotNull(sig.getStoreStock());
        assertEquals(DigitalSignatureFixture.VALID_LICENSE, sig.getPharmacistLicenseNumber());
        assertEquals(syncSealResult.signatureHash(), sig.getSignatureHash());
        assertEquals(syncSealResult.verificationStatus(), sig.getVerificationStatus());
        return true;
      }));
    }
  }

  @Nested
  @DisplayName("getSignatureByStoreStockId")
  class GetSignatureByStoreStockId {

    @Test
    @DisplayName("should return DTO when signature exists")
    void shouldReturnDtoWhenExists() {
      var signature = DigitalSignatureFixture.validDigitalSignatureEntity(storeStock);
      DigitalSignatureDto dto =
          DigitalSignatureDto.builder()
              .id(1L)
              .storeStockId(storeStock.getId())
              .pharmacistLicenseNumber(DigitalSignatureFixture.VALID_LICENSE)
              .build();

      when(digitalSignatureRepository.findByStoreStockId(storeStock.getId()))
          .thenReturn(Optional.of(signature));
      when(digitalSignatureMapper.toDto(signature)).thenReturn(dto);

      Optional<DigitalSignatureDto> result =
          digitalSignatureService.getSignatureByStoreStockId(storeStock.getId());

      assertTrue(result.isPresent());
      assertEquals(dto, result.get());
    }

    @Test
    @DisplayName("should return empty when signature does not exist")
    void shouldReturnEmptyWhenNotFound() {
      when(digitalSignatureRepository.findByStoreStockId(999L)).thenReturn(Optional.empty());

      Optional<DigitalSignatureDto> result =
          digitalSignatureService.getSignatureByStoreStockId(999L);

      assertTrue(result.isEmpty());
    }
  }
}
