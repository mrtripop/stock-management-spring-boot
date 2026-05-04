package com.mrtripop.clinical.component;

import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.dto.CreateStoreRequest;
import com.mrtripop.clinical.models.dto.StoreDto;
import com.mrtripop.clinical.models.dto.UpdateStoreRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface StoreMapper {
  Store toEntity(CreateStoreRequest request);

  StoreDto toDto(Store store);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void partialUpdate(UpdateStoreRequest request, @MappingTarget Store store);
}