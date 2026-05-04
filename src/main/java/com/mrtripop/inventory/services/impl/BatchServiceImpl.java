package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.RegulatorySchedule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.component.BatchMapper;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.db.VerificationStatus;
import com.mrtripop.inventory.models.dto.BatchDto;
import com.mrtripop.inventory.models.dto.DeductedBatchDto;
import com.mrtripop.inventory.models.dto.SignatureVerificationDto;
import com.mrtripop.inventory.models.dto.StockDeductionRequest;
import com.mrtripop.inventory.models.dto.StockDeductionResponseDto;
import com.mrtripop.inventory.models.dto.StockEntryRequest;
import com.mrtripop.inventory.models.dto.StockEntryResponseDto;
import com.mrtripop.inventory.models.dto.StoreStockDto;
import com.mrtripop.inventory.models.dto.SyncSealResult;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.BatchService;
import com.mrtripop.inventory.services.DigitalSignatureService;
import com.mrtripop.inventory.services.SyncSealService;
import com.mrtripop.inventory.services.UnitConversionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

  private static final String AUDIT_SIGNATURE_FORMAT = "license=%s, status=%s";

  private final BatchRepository batchRepository;
  private final StoreStockRepository storeStockRepository;
  private final BrandRepository brandRepository;
  private final StoreRepository storeRepository;
  private final StoreProductRepository storeProductRepository;
  private final AuditService auditService;
  private final BatchMapper batchMapper;
  private final UnitConversionService unitConversionService;
  private final SyncSealService syncSealService;
  private final DigitalSignatureService digitalSignatureService;

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public StockEntryResponseDto createBatchFromBarcode(StockEntryRequest request)
      throws ApplicationException {
    Brand brand =
        brandRepository
            .findByBarcode(request.getBarcode())
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.BARCODE_NOT_RECOGNIZED, HttpStatus.NOT_FOUND));

    Store store =
        storeRepository
            .findById(request.getStoreId())
            .orElseThrow(
                () -> new ApplicationException(ErrorCode.STORE_NOT_FOUND, HttpStatus.NOT_FOUND));

    Batch batch =
        Batch.builder()
            .brand(brand)
            .batchNumber(request.getBatchNumber())
            .expiryDate(request.getExpiryDate())
            .quantity(request.getQuantity())
            .supplierReference(request.getSupplierReference())
            .manufacturerLotNumber(request.getManufacturerLotNumber())
            .storageConditions(request.getStorageConditions())
            .status(BatchStatus.AVAILABLE)
            .build();

    try {
      batch = batchRepository.save(batch);
    } catch (DataIntegrityViolationException e) {
      throw new ApplicationException(ErrorCode.BATCH_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }

    StoreStock storeStock =
        StoreStock.builder().store(store).batch(batch).quantity(request.getQuantity()).build();

    storeStock = storeStockRepository.save(storeStock);

    BatchDto batchDto = batchMapper.toBatchDto(batch);
    StoreStockDto storeStockDto = batchMapper.toStoreStockDto(storeStock);

    auditService.recordAudit(
        "INVENTORY_IN", "Batch", batch.getId().toString(), null, batchDto.toString());

    return StockEntryResponseDto.builder()
        .batch(batchDto)
        .storeStock(storeStockDto)
        .build();
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public StockDeductionResponseDto deductStock(StockDeductionRequest request)
      throws ApplicationException {
    Brand brand =
        brandRepository
            .findByBarcode(request.getBarcode())
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.BARCODE_NOT_RECOGNIZED, HttpStatus.NOT_FOUND));

    storeRepository
        .findById(request.getStoreId())
        .orElseThrow(
            () -> new ApplicationException(ErrorCode.STORE_NOT_FOUND, HttpStatus.NOT_FOUND));

    boolean isControlled = isControlledSubstance(brand);
    SyncSealResult syncSealResult = null;

    if (isControlled) {
      if (request.getSignature() == null) {
        throw new ApplicationException(
            ErrorCode.CONTROLLED_SUBSTANCE_REQUIRES_SIGNATURE, HttpStatus.FORBIDDEN);
      }
      String licenseNumber = request.getSignature().getLicenseNumber();
      String signaturePayload = request.getSignature().getSignaturePayload();
      if (licenseNumber == null
          || licenseNumber.isBlank()
          || signaturePayload == null
          || signaturePayload.isBlank()) {
        throw new ApplicationException(ErrorCode.INVALID_DIGITAL_SIGNATURE, HttpStatus.BAD_REQUEST);
      }
      syncSealResult = syncSealService.verifyPharmacist(licenseNumber, signaturePayload);
      if (syncSealResult.verificationStatus() != VerificationStatus.VERIFIED) {
        throw new ApplicationException(
            ErrorCode.SIGNATURE_VERIFICATION_FAILED, HttpStatus.FORBIDDEN);
      }
    }

    String baseUnit = brand.getBaseUnit();
    String requestedUnit = request.getUnit();
    long baseQuantity;

    if (requestedUnit == null || requestedUnit.equalsIgnoreCase(baseUnit)) {
      baseQuantity = request.getQuantity();
    } else {
      baseQuantity =
          unitConversionService.convertToBaseUnits(
              brand.getId(), requestedUnit, request.getQuantity());
    }

    List<StoreStock> availableStock =
        storeStockRepository.findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
            request.getStoreId(), brand.getId());

    if (availableStock.isEmpty()) {
      throw new ApplicationException(ErrorCode.NO_AVAILABLE_BATCHES, HttpStatus.CONFLICT);
    }

    for (StoreStock stock : availableStock) {
      if (!stock.getBatch().getExpiryDate().isAfter(LocalDate.now())) {
        throw new ApplicationException(ErrorCode.EXPIRED_BATCH_DEDUCTION, HttpStatus.CONFLICT);
      }
    }

    List<DeductedBatchDto> deductionItems = new ArrayList<>();
    List<StoreStock> deductedStocks = new ArrayList<>();
    long remaining = baseQuantity;

    for (StoreStock stock : availableStock) {
      if (remaining <= 0) {
        break;
      }

      long oldQuantity = stock.getQuantity();
      long toDeduct = Math.min(remaining, stock.getQuantity());

      int updated = storeStockRepository.deductQuantity(stock.getId(), toDeduct);
      if (updated == 0) {
        log.warn(
            "FEFO deduction race condition: stock id={} had {} but deduction of {} failed",
            stock.getId(),
            oldQuantity,
            toDeduct);
        continue;
      }

      stock.setQuantity(oldQuantity - toDeduct);
      long newQuantity = stock.getQuantity();

      auditService.recordAudit(
          "INVENTORY_OUT",
          "StoreStock",
          stock.getId().toString(),
          String.valueOf(oldQuantity),
          String.valueOf(newQuantity));

      deductionItems.add(batchMapper.toDeductedBatchDto(stock, toDeduct));
      deductedStocks.add(stock);
      remaining -= toDeduct;
    }

    if (remaining > 0) {
      throw new ApplicationException(ErrorCode.INSUFFICIENT_QUANTITY, HttpStatus.CONFLICT);
    }

    SignatureVerificationDto signatureVerification = null;
    if (isControlled && syncSealResult != null) {
      for (StoreStock stock : deductedStocks) {
        digitalSignatureService.saveSignature(
            stock.getId(), request.getSignature().getLicenseNumber(), syncSealResult);
      }
      auditService.recordAudit(
          "CONTROLLED_SUBSTANCE_SIGNED",
          "StockDeduction",
          brand.getId().toString(),
          null,
          String.format(AUDIT_SIGNATURE_FORMAT,
              request.getSignature().getLicenseNumber(),
              syncSealResult.verificationStatus()));
      signatureVerification =
          SignatureVerificationDto.builder()
              .licenseNumber(request.getSignature().getLicenseNumber())
              .verifiedAt(syncSealResult.verifiedAt())
              .verificationStatus(syncSealResult.verificationStatus().name())
              .build();
    }

    BigDecimal unitPrice = null;
    BigDecimal totalAmount = null;
    java.util.Optional<StoreProduct> storeProductOpt =
        storeProductRepository.findByStoreIdAndBrandId(request.getStoreId(), brand.getId());
    if (storeProductOpt.isPresent()) {
      StoreProduct sp = storeProductOpt.get();
      unitPrice = sp.getPrice();
      totalAmount = sp.getPrice().multiply(BigDecimal.valueOf(baseQuantity));
    }

    return StockDeductionResponseDto.builder()
        .barcode(request.getBarcode())
        .brandId(brand.getId())
        .brandName(brand.getBrandName())
        .requestedUnit(requestedUnit)
        .requestedQuantity(request.getQuantity())
        .baseUnit(baseUnit)
        .deductedQuantity(baseQuantity - remaining)
        .unitPrice(unitPrice)
        .totalAmount(totalAmount)
        .items(deductionItems)
        .signatureVerification(signatureVerification)
        .build();
  }

  private boolean isControlledSubstance(Brand brand) {
    return brand.getMolecule() != null
        && brand.getMolecule().getRegulatorySchedule() != null
        && brand.getMolecule().getRegulatorySchedule().isControlled();
  }

  @Override
  @Transactional(readOnly = true)
  public BatchDto getBatchById(Long id) throws ApplicationException {
    Batch batch =
        batchRepository
            .findById(id)
            .orElseThrow(
                () -> new ApplicationException(ErrorCode.BATCH_NOT_FOUND, HttpStatus.NOT_FOUND));
    return batchMapper.toBatchDto(batch);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BatchDto> getBatchesByBrandId(UUID brandId, Pageable pageable) {
    return batchRepository.findByBrandId(brandId, pageable).map(batchMapper::toBatchDto);
  }

  @Override
  @Transactional(readOnly = true)
  public StoreStockDto getStoreStock(UUID storeId, Long batchId) throws ApplicationException {
    StoreStock storeStock =
        storeStockRepository
            .findByStoreIdAndBatchId(storeId, batchId)
            .orElseThrow(
                () ->
                    new ApplicationException(ErrorCode.STOCK_NOT_FOUND, HttpStatus.NOT_FOUND));
    return batchMapper.toStoreStockDto(storeStock);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<StoreStockDto> getStoreStocksByStoreId(UUID storeId, Pageable pageable) {
    return storeStockRepository.findByStoreId(storeId, pageable).map(batchMapper::toStoreStockDto);
  }
}
