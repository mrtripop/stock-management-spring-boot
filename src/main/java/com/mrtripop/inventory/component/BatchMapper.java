package com.mrtripop.inventory.component;

import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.BatchDto;
import com.mrtripop.inventory.models.dto.DeductedBatchDto;
import com.mrtripop.inventory.models.dto.StoreStockDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BatchMapper {

  @Mapping(source = "brand.id", target = "brandId")
  BatchDto toBatchDto(Batch batch);

  @Mapping(source = "store.id", target = "storeId")
  @Mapping(source = "batch.id", target = "batchId")
  StoreStockDto toStoreStockDto(StoreStock storeStock);

  @Mapping(source = "storeStock.batch.id", target = "batchId")
  @Mapping(source = "storeStock.batch.batchNumber", target = "batchNumber")
  @Mapping(source = "storeStock.batch.expiryDate", target = "expiryDate")
  @Mapping(source = "storeStock.batch.brand.baseUnit", target = "baseUnit")
  @Mapping(target = "remainingQuantity", expression = "java(storeStock.getQuantity() - deductedQuantity)")
  DeductedBatchDto toDeductedBatchDto(StoreStock storeStock, Long deductedQuantity);
}
