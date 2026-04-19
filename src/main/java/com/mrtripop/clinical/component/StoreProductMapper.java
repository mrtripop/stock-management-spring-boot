package com.mrtripop.clinical.component;

import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.models.dto.StoreDto;
import com.mrtripop.clinical.models.dto.StoreProductDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StoreProductMapper {

  StoreDto toStoreDto(Store store);

  StoreProductDto toStoreProductDto(StoreProduct storeProduct);
}
