package com.mrtripop.inventory.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.CreateUnitConversionRequest;
import com.mrtripop.inventory.models.dto.UnitConversionDto;
import java.util.List;
import java.util.UUID;

public interface UnitConversionService {

  UnitConversionDto createConversion(CreateUnitConversionRequest request)
      throws ApplicationException;

  List<UnitConversionDto> getConversionsByBrandId(UUID brandId);

  void deleteConversion(Long id) throws ApplicationException;

  long convertToBaseUnits(UUID brandId, String fromUnit, long quantity)
      throws ApplicationException;
}
