package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.component.BatchMapper;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.BatchDto;
import com.mrtripop.inventory.models.dto.StockEntryRequest;
import com.mrtripop.inventory.models.dto.StockEntryResponseDto;
import com.mrtripop.inventory.models.dto.StoreStockDto;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.BatchService;
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

  private final BatchRepository batchRepository;
  private final StoreStockRepository storeStockRepository;
  private final BrandRepository brandRepository;
  private final StoreRepository storeRepository;
  private final AuditService auditService;
  private final BatchMapper batchMapper;

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
        StoreStock.builder()
            .store(store)
            .batch(batch)
            .quantity(request.getQuantity())
            .build();

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
