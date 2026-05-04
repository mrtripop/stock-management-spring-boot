package com.mrtripop.inventory.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.component.UnitConversionMapper;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.fixture.UnitConversionFixture;
import com.mrtripop.inventory.models.db.UnitConversion;
import com.mrtripop.inventory.models.dto.CreateUnitConversionRequest;
import com.mrtripop.inventory.models.dto.UnitConversionDto;
import com.mrtripop.inventory.repository.UnitConversionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UnitConversionService")
class UnitConversionServiceImplTest {

  @Mock private UnitConversionRepository unitConversionRepository;
  @Mock private BrandRepository brandRepository;
  @Mock private UnitConversionMapper unitConversionMapper;
  @InjectMocks private UnitConversionServiceImpl unitConversionService;

  @Nested
  @DisplayName("createConversion")
  class CreateConversion {

    @Test
    @DisplayName("should save and return conversion DTO when request is valid")
    void shouldSaveAndReturnConversionDtoWhenRequestIsValid() throws ApplicationException {
      CreateUnitConversionRequest request = UnitConversionFixture.validCreateRequest();
      Brand brand = Brand.builder().id(request.getBrandId()).baseUnit("TABLET").build();
      UnitConversion saved = UnitConversionFixture.defaultEntity();
      UnitConversionDto dto = UnitConversionDto.builder()
          .id(1L)
          .brandId(request.getBrandId())
          .fromUnit("BOX")
          .toUnit("TABLET")
          .ratio(30)
          .build();

      when(brandRepository.findById(request.getBrandId())).thenReturn(Optional.of(brand));
      when(unitConversionMapper.toEntity(eq(request), eq(brand))).thenReturn(saved);
      when(unitConversionRepository.save(saved)).thenReturn(saved);
      when(unitConversionMapper.toDto(saved)).thenReturn(dto);

      UnitConversionDto result = unitConversionService.createConversion(request);

      assertNotNull(result);
      assertEquals("BOX", result.getFromUnit());
      assertEquals(30, result.getRatio());
      verify(unitConversionRepository).save(saved);
    }

    @Test
    @DisplayName("should throw INVALID_UNIT_CONVERSION when toUnit does not match brand baseUnit")
    void shouldThrowInvalidUnitConversionWhenToUnitDoesNotMatchBrandBaseUnit() {
      CreateUnitConversionRequest request = CreateUnitConversionRequest.builder()
          .brandId(UUID.randomUUID())
          .fromUnit("BOX")
          .toUnit("ML")
          .ratio(10)
          .build();
      Brand brand = Brand.builder().id(request.getBrandId()).baseUnit("TABLET").build();

      when(brandRepository.findById(request.getBrandId())).thenReturn(Optional.of(brand));

      ApplicationException ex = assertThrows(ApplicationException.class,
          () -> unitConversionService.createConversion(request));
      assertEquals(ErrorCode.INVALID_UNIT_CONVERSION, ex.getErrorCode());
      verify(brandRepository).findById(request.getBrandId());
      verify(unitConversionMapper, never()).toEntity(any(), any());
      verify(unitConversionRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw INVALID_UNIT_CONVERSION when ratio is less than 2")
    void shouldThrowInvalidUnitConversionWhenRatioIsLessThan2() {
      CreateUnitConversionRequest request = CreateUnitConversionRequest.builder()
          .brandId(UUID.randomUUID())
          .fromUnit("BOX")
          .toUnit("TABLET")
          .ratio(1)
          .build();
      Brand brand = Brand.builder().id(request.getBrandId()).baseUnit("TABLET").build();

      when(brandRepository.findById(request.getBrandId())).thenReturn(Optional.of(brand));

      ApplicationException ex = assertThrows(ApplicationException.class,
          () -> unitConversionService.createConversion(request));
      assertEquals(ErrorCode.INVALID_UNIT_CONVERSION, ex.getErrorCode());
      verify(brandRepository).findById(request.getBrandId());
      verify(unitConversionMapper, never()).toEntity(any(), any());
      verify(unitConversionRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BATCH_ALREADY_EXISTS on duplicate brandId+fromUnit")
    void shouldThrowBatchAlreadyExistsOnDuplicateBrandIdFromUnit() throws Exception {
      CreateUnitConversionRequest request = UnitConversionFixture.validCreateRequest();
      Brand brand = Brand.builder().id(request.getBrandId()).baseUnit("TABLET").build();
      UnitConversion conversion = UnitConversionFixture.defaultEntity();

      when(brandRepository.findById(request.getBrandId())).thenReturn(Optional.of(brand));
      when(unitConversionMapper.toEntity(eq(request), eq(brand))).thenReturn(conversion);
      when(unitConversionRepository.save(conversion))
          .thenThrow(new DataIntegrityViolationException("duplicate"));

      ApplicationException ex = assertThrows(ApplicationException.class,
          () -> unitConversionService.createConversion(request));
      assertEquals(ErrorCode.BATCH_ALREADY_EXISTS, ex.getErrorCode());
      verify(brandRepository).findById(request.getBrandId());
      verify(unitConversionMapper).toEntity(eq(request), eq(brand));
      verify(unitConversionRepository).save(conversion);
      verify(unitConversionMapper, never()).toDto(any());
    }
  }

  @Nested
  @DisplayName("getConversionsByBrandId")
  class GetConversionsByBrandId {

    @Test
    @DisplayName("should return list of conversion DTOs for a brand")
    void shouldReturnListOfConversionDtosForABrand() {
      UUID brandId = UUID.randomUUID();
      Brand brand = Brand.builder().id(brandId).baseUnit("TABLET").build();
      UnitConversion conv1 = UnitConversionFixture.defaultEntity();
      UnitConversion conv2 = UnitConversionFixture.stripToTabletConversion(brand);
      UnitConversionDto dto1 = UnitConversionDto.builder()
          .id(conv1.getId())
          .brandId(conv1.getBrand().getId())
          .fromUnit(conv1.getFromUnit())
          .toUnit(conv1.getToUnit())
          .ratio(conv1.getRatio())
          .build();
      UnitConversionDto dto2 = UnitConversionDto.builder()
          .id(conv2.getId())
          .brandId(conv2.getBrand().getId())
          .fromUnit(conv2.getFromUnit())
          .toUnit(conv2.getToUnit())
          .ratio(conv2.getRatio())
          .build();

      when(unitConversionRepository.findByBrandId(brandId)).thenReturn(List.of(conv1, conv2));
      when(unitConversionMapper.toDto(conv1)).thenReturn(dto1);
      when(unitConversionMapper.toDto(conv2)).thenReturn(dto2);

      List<UnitConversionDto> result = unitConversionService.getConversionsByBrandId(brandId);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(dto1.getId(), result.get(0).getId());
      assertEquals(dto2.getId(), result.get(1).getId());
      verify(unitConversionRepository).findByBrandId(brandId);
    }
  }

  @Nested
  @DisplayName("deleteConversion")
  class DeleteConversion {

    @Test
    @DisplayName("should delete conversion when id exists")
    void shouldDeleteConversionWhenIdExists() throws ApplicationException {
      Long id = 1L;
      when(unitConversionRepository.existsById(id)).thenReturn(true);

      unitConversionService.deleteConversion(id);

      verify(unitConversionRepository).existsById(id);
      verify(unitConversionRepository).deleteById(id);
    }

    @Test
    @DisplayName("should throw BATCH_NOT_FOUND when id does not exist")
    void shouldThrowBatchNotFoundWhenIdDoesNotExist() {
      Long id = 1L;
      when(unitConversionRepository.existsById(id)).thenReturn(false);

      ApplicationException ex = assertThrows(ApplicationException.class,
          () -> unitConversionService.deleteConversion(id));
      assertEquals(ErrorCode.BATCH_NOT_FOUND, ex.getErrorCode());
      verify(unitConversionRepository).existsById(id);
      verify(unitConversionRepository, never()).deleteById(any());
    }
  }

  @Nested
  @DisplayName("convertToBaseUnits")
  class ConvertToBaseUnits {

    @Test
    @DisplayName("should return quantity unchanged when fromUnit is null")
    void shouldReturnQuantityUnchangedWhenFromUnitIsNull() throws ApplicationException {
      UUID brandId = UUID.randomUUID();
      long quantity = 5L;

      long result = unitConversionService.convertToBaseUnits(brandId, null, quantity);

      assertEquals(quantity, result);
      verify(unitConversionRepository, never()).findByBrandIdAndFromUnit(any(), any());
    }

    @Test
    @DisplayName("should convert 2 BOX to 60 TABLET")
    void shouldConvert2BoxTo60Tablet() throws ApplicationException {
      UUID brandId = UUID.randomUUID();
      long quantity = 2L;
      String fromUnit = "BOX";
      UnitConversion conversion = UnitConversionFixture.defaultEntity();

      when(unitConversionRepository.findByBrandIdAndFromUnit(brandId, fromUnit))
          .thenReturn(Optional.of(conversion));

      long result = unitConversionService.convertToBaseUnits(brandId, fromUnit, quantity);

      assertEquals(60L, result);
      verify(unitConversionRepository).findByBrandIdAndFromUnit(brandId, fromUnit);
    }

    @Test
    @DisplayName("should convert 5 STRIP to 50 TABLET")
    void shouldConvert5StripTo50Tablet() throws ApplicationException {
      Brand brand = Brand.builder().id(UUID.randomUUID()).baseUnit("TABLET").build();
      UUID brandId = brand.getId();
      long quantity = 5L;
      String fromUnit = "STRIP";
      UnitConversion conversion = UnitConversionFixture.stripToTabletConversion(brand);

      when(unitConversionRepository.findByBrandIdAndFromUnit(brandId, fromUnit))
          .thenReturn(Optional.of(conversion));

      long result = unitConversionService.convertToBaseUnits(brandId, fromUnit, quantity);

      assertEquals(50L, result);
      verify(unitConversionRepository).findByBrandIdAndFromUnit(brandId, fromUnit);
    }

    @Test
    @DisplayName("should throw UNIT_CONVERSION_NOT_FOUND when no conversion exists")
    void shouldThrowUnitConversionNotFoundWhenNoConversionExists() {
      UUID brandId = UUID.randomUUID();
      long quantity = 5L;
      String fromUnit = "UNKNOWN";

      when(unitConversionRepository.findByBrandIdAndFromUnit(brandId, fromUnit))
          .thenReturn(Optional.empty());

      ApplicationException ex = assertThrows(ApplicationException.class,
          () -> unitConversionService.convertToBaseUnits(brandId, fromUnit, quantity));
      assertEquals(ErrorCode.UNIT_CONVERSION_NOT_FOUND, ex.getErrorCode());
      verify(unitConversionRepository).findByBrandIdAndFromUnit(brandId, fromUnit);
    }
  }
}
