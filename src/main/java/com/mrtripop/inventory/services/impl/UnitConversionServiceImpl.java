package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.component.UnitConversionMapper;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.UnitConversion;
import com.mrtripop.inventory.models.dto.CreateUnitConversionRequest;
import com.mrtripop.inventory.models.dto.UnitConversionDto;
import com.mrtripop.inventory.repository.UnitConversionRepository;
import com.mrtripop.inventory.services.UnitConversionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnitConversionServiceImpl implements UnitConversionService {

  private final UnitConversionRepository unitConversionRepository;
  private final BrandRepository brandRepository;
  private final UnitConversionMapper unitConversionMapper;

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public UnitConversionDto createConversion(CreateUnitConversionRequest request)
      throws ApplicationException {
    Brand brand =
        brandRepository
            .findById(request.getBrandId())
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.BARCODE_NOT_RECOGNIZED, HttpStatus.NOT_FOUND));

    if (!brand.getBaseUnit().equalsIgnoreCase(request.getToUnit())) {
      throw new ApplicationException(ErrorCode.INVALID_UNIT_CONVERSION, HttpStatus.BAD_REQUEST);
    }

    if (request.getRatio() < 2) {
      throw new ApplicationException(ErrorCode.INVALID_UNIT_CONVERSION, HttpStatus.BAD_REQUEST);
    }

    try {
      UnitConversion conversion =
          unitConversionMapper.toEntity(request, brand);
      UnitConversion saved = unitConversionRepository.save(conversion);
      return unitConversionMapper.toDto(saved);
    } catch (DataIntegrityViolationException e) {
      log.warn("Duplicate unit conversion for brand {} and unit {}", request.getBrandId(),
          request.getFromUnit());
      throw new ApplicationException(ErrorCode.BATCH_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<UnitConversionDto> getConversionsByBrandId(UUID brandId) {
    return unitConversionRepository.findByBrandId(brandId).stream()
        .map(unitConversionMapper::toDto)
        .toList();
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public void deleteConversion(Long id) throws ApplicationException {
    if (!unitConversionRepository.existsById(id)) {
      throw new ApplicationException(ErrorCode.BATCH_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    unitConversionRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public long convertToBaseUnits(UUID brandId, String fromUnit, long quantity)
      throws ApplicationException {
    if (fromUnit == null) {
      return quantity;
    }
    UnitConversion conversion =
        unitConversionRepository
            .findByBrandIdAndFromUnit(brandId, fromUnit)
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.UNIT_CONVERSION_NOT_FOUND, HttpStatus.BAD_REQUEST));
    return quantity * conversion.getRatio();
  }
}
