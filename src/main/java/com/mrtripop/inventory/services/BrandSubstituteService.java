package com.mrtripop.inventory.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.BrandSubstituteDto;
import java.util.List;
import java.util.UUID;

public interface BrandSubstituteService {

  List<BrandSubstituteDto> findSubstitutes(UUID storeId, UUID brandId)
      throws ApplicationException;
}
