package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.inventory.models.dto.MeshStockDto;
import com.mrtripop.inventory.models.dto.MeshStockResponseDto;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.MeshWarehouseService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeshWarehouseServiceImpl implements MeshWarehouseService {

  private final StoreStockRepository storeStockRepository;
  private final MoleculeRepository moleculeRepository;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "mesh-stock", key = "#moleculeId + '_' + #requestingStoreId", unless = "#result == null")
  public MeshStockResponseDto searchByMolecule(UUID moleculeId, UUID requestingStoreId) {
    List<MeshStockDto> allStocks = storeStockRepository.aggregateStockByMolecule(moleculeId);
    return buildMeshResponse(allStocks, requestingStoreId);
  }

  @Override
  @Transactional(readOnly = true)
  public MeshStockResponseDto searchByGenericName(String genericName, UUID requestingStoreId) {
    List<Molecule> molecules =
        moleculeRepository.findByGenericNameContainingIgnoreCase(genericName);
    if (molecules.isEmpty()) {
      return emptyResponse();
    }
    List<MeshStockDto> allStocks =
        molecules.stream()
            .map(m -> searchByMolecule(m.getId(), requestingStoreId))
            .flatMap(
                response ->
                    Stream.concat(
                        response.getLocalStoreStocks().stream(),
                        response.getMeshStoreStocks().stream()))
            .toList();
    return buildMeshResponse(allStocks, requestingStoreId);
  }

  private MeshStockResponseDto buildMeshResponse(
      List<MeshStockDto> stocks, UUID requestingStoreId) {
    List<MeshStockDto> local =
        stocks.stream().filter(s -> s.getStoreId().equals(requestingStoreId)).toList();
    List<MeshStockDto> mesh =
        stocks.stream().filter(s -> !s.getStoreId().equals(requestingStoreId)).toList();
    long totalMesh = mesh.stream().mapToLong(MeshStockDto::getTotalQuantity).sum();
    return MeshStockResponseDto.builder()
        .localStoreStocks(local)
        .meshStoreStocks(mesh)
        .totalMeshQuantity(totalMesh)
        .build();
  }

  private MeshStockResponseDto emptyResponse() {
    return MeshStockResponseDto.builder()
        .localStoreStocks(Collections.emptyList())
        .meshStoreStocks(Collections.emptyList())
        .totalMeshQuantity(0L)
        .build();
  }
}
