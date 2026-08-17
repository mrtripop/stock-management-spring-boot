package com.mrtripop.inventory.services;

import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.BatchDto;
import com.mrtripop.inventory.models.dto.StockDeductionRequest;
import com.mrtripop.inventory.models.dto.StockDeductionResponseDto;
import com.mrtripop.inventory.models.dto.StockEntryRequest;
import com.mrtripop.inventory.models.dto.StockEntryResponseDto;
import com.mrtripop.inventory.models.dto.StoreStockDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BatchService {

  StockEntryResponseDto createBatchFromBarcode(StockEntryRequest request)
      throws ApplicationException;

  StockDeductionResponseDto deductStock(StockDeductionRequest request)
      throws ApplicationException;

  BatchDto getBatchById(Long id) throws ApplicationException;

  Page<BatchDto> getBatchesByBrandId(UUID brandId, Pageable pageable);

  Page<BatchDto> getBatches(Pageable pageable);

  BrandDto resolveBarcode(String barcode) throws ApplicationException;

  StoreStockDto getStoreStock(UUID storeId, Long batchId) throws ApplicationException;

  Page<StoreStockDto> getStoreStocksByStoreId(UUID storeId, Pageable pageable);

  void deductStockByBatch(UUID storeId, Long batchId, Long quantity) throws ApplicationException;

  void restoreStock(UUID storeId, Long batchId, Long quantity) throws ApplicationException;
}
