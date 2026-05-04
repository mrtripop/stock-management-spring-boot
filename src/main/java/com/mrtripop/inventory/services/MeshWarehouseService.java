package com.mrtripop.inventory.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.MeshStockResponseDto;
import java.util.UUID;

public interface MeshWarehouseService {

  MeshStockResponseDto searchByMolecule(UUID moleculeId, UUID requestingStoreId);

  MeshStockResponseDto searchByGenericName(String genericName, UUID requestingStoreId)
      throws ApplicationException;
}
