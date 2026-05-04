package com.mrtripop.inventory.component;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.inventory.models.db.UnitConversion;
import com.mrtripop.inventory.models.dto.CreateUnitConversionRequest;
import com.mrtripop.inventory.models.dto.UnitConversionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UnitConversionMapper {

  UnitConversionDto toDto(UnitConversion unitConversion);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "brand", source = "brand")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  UnitConversion toEntity(CreateUnitConversionRequest request, Brand brand);
}
