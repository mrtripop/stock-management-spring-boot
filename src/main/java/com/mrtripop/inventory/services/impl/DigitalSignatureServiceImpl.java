package com.mrtripop.inventory.services.impl;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.component.DigitalSignatureMapper;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.DigitalSignature;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.DigitalSignatureDto;
import com.mrtripop.inventory.models.dto.SyncSealResult;
import com.mrtripop.inventory.repository.DigitalSignatureRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.DigitalSignatureService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalSignatureServiceImpl implements DigitalSignatureService {

  private final DigitalSignatureRepository digitalSignatureRepository;
  private final StoreStockRepository storeStockRepository;
  private final DigitalSignatureMapper digitalSignatureMapper;

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public void saveSignature(Long storeStockId, String licenseNumber, SyncSealResult result)
      throws ApplicationException {
    StoreStock storeStock =
        storeStockRepository
            .findById(storeStockId)
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.STOCK_NOT_FOUND, HttpStatus.NOT_FOUND));

    DigitalSignature signature =
        DigitalSignature.builder()
            .storeStock(storeStock)
            .pharmacistLicenseNumber(licenseNumber)
            .signatureHash(result.signatureHash())
            .verificationStatus(result.verificationStatus())
            .verifiedAt(result.verifiedAt())
            .build();

    digitalSignatureRepository.save(signature);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<DigitalSignatureDto> getSignatureByStoreStockId(Long storeStockId) {
    return digitalSignatureRepository
        .findByStoreStockId(storeStockId)
        .map(digitalSignatureMapper::toDto);
  }
}
