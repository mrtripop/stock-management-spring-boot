package com.mrtripop.inventory.controllers;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.CreateUnitConversionRequest;
import com.mrtripop.inventory.models.dto.UnitConversionDto;
import com.mrtripop.inventory.services.UnitConversionService;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory/conversions")
public class UnitConversionController {

  private final UnitConversionService unitConversionService;

  @PostMapping
  public ResponseEntity<Object> createConversion(
      @Valid @RequestBody CreateUnitConversionRequest request) throws ApplicationException {
    UnitConversionDto result = unitConversionService.createConversion(request);
    return ResponseBody.builder()
        .code("CONVERSION_CREATED")
        .message("Unit conversion created successfully")
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<Object> getConversionsByBrand(@RequestParam UUID brandId) {
    List<UnitConversionDto> result = unitConversionService.getConversionsByBrandId(brandId);
    return ResponseBody.builder()
        .code("CONVERSIONS_FOUND")
        .message("Unit conversions retrieved successfully")
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteConversion(@PathVariable Long id)
      throws ApplicationException {
    unitConversionService.deleteConversion(id);
    return ResponseBody.builder()
        .code("CONVERSION_DELETED")
        .message("Unit conversion deleted successfully")
        .data(null)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}
