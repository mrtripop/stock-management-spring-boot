package com.mrtripop.inventory.services.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.inventory.fixture.MeshStockFixture;
import com.mrtripop.inventory.models.dto.MeshStockDto;
import com.mrtripop.inventory.models.dto.MeshStockResponseDto;
import com.mrtripop.inventory.repository.StoreStockRepository;
import java.util.Collections;
import java.util.List;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MeshWarehouseServiceImpl")
class MeshWarehouseServiceImplTest {

  @Mock private StoreStockRepository storeStockRepository;
  @Mock private MoleculeRepository moleculeRepository;

  @InjectMocks private MeshWarehouseServiceImpl meshWarehouseService;

  private final UUID moleculeId = UUID.fromString("00000000-0000-0000-0000-000000000100");
  private final UUID requestingStoreId = MeshStockFixture.STORE_ID_LOCAL;

  @Nested
  @DisplayName("searchByMolecule")
  class SearchByMolecule {

    @Test
    @DisplayName("should split results into local and mesh stocks by requestingStoreId")
    void shouldSplitResultsIntoLocalAndMesh() {
      // Arrange
      MeshStockDto local = MeshStockFixture.localStoreStock();
      MeshStockDto remote = MeshStockFixture.remoteStoreStock();
      when(storeStockRepository.aggregateStockByMolecule(moleculeId))
          .thenReturn(List.of(local, remote));

      // Act
      MeshStockResponseDto result =
          meshWarehouseService.searchByMolecule(moleculeId, requestingStoreId);

      // Assert
      assertEquals(1, result.getLocalStoreStocks().size());
      assertEquals(MeshStockFixture.STORE_NAME_LOCAL, result.getLocalStoreStocks().get(0).getStoreName());
      assertEquals(1, result.getMeshStoreStocks().size());
      assertEquals(MeshStockFixture.STORE_NAME_REMOTE, result.getMeshStoreStocks().get(0).getStoreName());
      assertEquals(50L, result.getTotalMeshQuantity());
    }

    @Test
    @DisplayName("should return empty response when no stock found")
    void shouldReturnEmptyResponseWhenNoStock() {
      // Arrange
      when(storeStockRepository.aggregateStockByMolecule(moleculeId))
          .thenReturn(Collections.emptyList());

      // Act
      MeshStockResponseDto result =
          meshWarehouseService.searchByMolecule(moleculeId, requestingStoreId);

      // Assert
      assertTrue(result.getLocalStoreStocks().isEmpty());
      assertTrue(result.getMeshStoreStocks().isEmpty());
      assertEquals(0L, result.getTotalMeshQuantity());
    }

    @Test
    @DisplayName("should return empty response when molecule has no matching stock")
    void shouldReturnEmptyResponseWhenMoleculeHasNoStock() {
      // Arrange
      UUID unknownMoleculeId = UUID.randomUUID();
      when(storeStockRepository.aggregateStockByMolecule(unknownMoleculeId))
          .thenReturn(Collections.emptyList());

      // Act
      MeshStockResponseDto result =
          meshWarehouseService.searchByMolecule(unknownMoleculeId, requestingStoreId);

      // Assert
      assertTrue(result.getLocalStoreStocks().isEmpty());
      assertTrue(result.getMeshStoreStocks().isEmpty());
      assertEquals(0L, result.getTotalMeshQuantity());
    }

    @Test
    @DisplayName("should aggregate totalMeshQuantity from all mesh stores")
    void shouldAggregateTotalMeshQuantity() {
      // Arrange
      MeshStockDto remote1 = MeshStockFixture.remoteStoreStock();
      MeshStockDto remote2 =
          MeshStockDto.builder()
              .storeId(UUID.fromString("00000000-0000-0000-0000-000000000003"))
              .storeName("Another Branch")
              .brandId(MeshStockFixture.BRAND_ID_TYLENOL)
              .brandName(MeshStockFixture.BRAND_NAME_TYLENOL)
              .genericName(MeshStockFixture.GENERIC_NAME_PARACETAMOL)
              .totalQuantity(75L)
              .batchCount(1L)
              .build();
      when(storeStockRepository.aggregateStockByMolecule(moleculeId))
          .thenReturn(List.of(remote1, remote2));

      // Act
      MeshStockResponseDto result =
          meshWarehouseService.searchByMolecule(moleculeId, requestingStoreId);

      // Assert
      assertEquals(2, result.getMeshStoreStocks().size());
      assertEquals(125L, result.getTotalMeshQuantity());
    }
  }

  @Nested
  @DisplayName("searchByGenericName")
  class SearchByGenericName {

    @Test
    @DisplayName("should match case-insensitively and return results")
    void shouldMatchCaseInsensitive() {
      // Arrange
      Molecule molecule = Molecule.builder().id(moleculeId).genericName("Paracetamol").build();
      when(moleculeRepository.findByGenericNameContainingIgnoreCase("paracetamol"))
          .thenReturn(List.of(molecule));
      when(storeStockRepository.aggregateStockByMolecule(moleculeId))
          .thenReturn(List.of(MeshStockFixture.localStoreStock()));

      // Act
      MeshStockResponseDto result =
          meshWarehouseService.searchByGenericName("paracetamol", requestingStoreId);

      // Assert
      assertEquals(1, result.getLocalStoreStocks().size());
      verify(moleculeRepository).findByGenericNameContainingIgnoreCase("paracetamol");
    }

    @Test
    @DisplayName("should aggregate results from multiple matching molecules")
    void shouldAggregateMultipleMatchingMolecules() {
      // Arrange
      UUID moleculeId2 = UUID.fromString("00000000-0000-0000-0000-000000000101");
      Molecule molecule1 =
          Molecule.builder().id(moleculeId).genericName("Cetirizine").build();
      Molecule molecule2 =
          Molecule.builder().id(moleculeId2).genericName("Acetaminophen").build();
      when(moleculeRepository.findByGenericNameContainingIgnoreCase("ceta"))
          .thenReturn(List.of(molecule1, molecule2));
      when(storeStockRepository.aggregateStockByMolecule(moleculeId))
          .thenReturn(List.of(MeshStockFixture.localStoreStock()));
      when(storeStockRepository.aggregateStockByMolecule(moleculeId2))
          .thenReturn(List.of(MeshStockFixture.remoteStoreStock()));

      // Act
      MeshStockResponseDto result =
          meshWarehouseService.searchByGenericName("ceta", requestingStoreId);

      // Assert
      assertEquals(1, result.getLocalStoreStocks().size());
      assertEquals(1, result.getMeshStoreStocks().size());
      verify(storeStockRepository).aggregateStockByMolecule(moleculeId);
      verify(storeStockRepository).aggregateStockByMolecule(moleculeId2);
    }

    @Test
    @DisplayName("should return empty response when no molecules match")
    void shouldReturnEmptyWhenNoMoleculesMatch() {
      // Arrange
      when(moleculeRepository.findByGenericNameContainingIgnoreCase("unknown"))
          .thenReturn(Collections.emptyList());

      // Act
      MeshStockResponseDto result =
          meshWarehouseService.searchByGenericName("unknown", requestingStoreId);

      // Assert
      assertTrue(result.getLocalStoreStocks().isEmpty());
      assertTrue(result.getMeshStoreStocks().isEmpty());
      assertEquals(0L, result.getTotalMeshQuantity());
      verify(storeStockRepository, never()).aggregateStockByMolecule(any());
    }
  }
}
