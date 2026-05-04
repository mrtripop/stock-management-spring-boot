package com.mrtripop.inventory.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.DigitalSignatureDto;
import com.mrtripop.inventory.models.dto.SyncSealResult;
import java.util.Optional;

public interface DigitalSignatureService {

  void saveSignature(Long storeStockId, String licenseNumber, SyncSealResult result)
      throws ApplicationException;

  Optional<DigitalSignatureDto> getSignatureByStoreStockId(Long storeStockId);
}
