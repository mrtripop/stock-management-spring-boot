package com.mrtripop.inventory.component;

import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.BatchDto;
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
}