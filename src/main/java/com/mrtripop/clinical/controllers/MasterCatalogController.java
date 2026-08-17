package com.mrtripop.clinical.controllers;

import com.mrtripop.clinical.constant.SuccessCode;
import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.clinical.models.dto.MoleculeDto;
import com.mrtripop.clinical.services.MasterCatalogService;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/clinical/catalog")
@RequiredArgsConstructor
@Validated
public class MasterCatalogController {

  private final MasterCatalogService masterCatalogService;

  @PostMapping("/molecules")
  public ResponseEntity<Object> createMolecule(@Valid @RequestBody MoleculeDto moleculeDto) {
    MoleculeDto created = masterCatalogService.createMolecule(moleculeDto);
    return ResponseBody.builder()
        .code(SuccessCode.CL2011_CREATE_MOLECULE_SUCCESS.getCode())
        .message(SuccessCode.CL2011_CREATE_MOLECULE_SUCCESS.getMessage())
        .data(created)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @PostMapping("/brands")
  public ResponseEntity<Object> createBrand(@Valid @RequestBody BrandDto brandDto) {
    BrandDto created = masterCatalogService.createBrand(brandDto);
    return ResponseBody.builder()
        .code(SuccessCode.CL2012_CREATE_BRAND_SUCCESS.getCode())
        .message(SuccessCode.CL2012_CREATE_BRAND_SUCCESS.getMessage())
        .data(created)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @GetMapping("/molecules/{id}")
  public ResponseEntity<Object> getMolecule(@PathVariable UUID id) {
    MoleculeDto molecule = masterCatalogService.getMolecule(id);
    return ResponseBody.builder()
        .code(SuccessCode.CL2013_GET_MOLECULE_SUCCESS.getCode())
        .message(SuccessCode.CL2013_GET_MOLECULE_SUCCESS.getMessage())
        .data(molecule)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("/molecules/{id}/metadata")
  public ResponseEntity<Object> updateMoleculeMetadata(
      @PathVariable UUID id, @RequestBody MoleculeDto moleculeDto) {
    MoleculeDto updated = masterCatalogService.updateMoleculeMetadata(id, moleculeDto);
    return ResponseBody.builder()
        .code(SuccessCode.CL2014_UPDATE_MOLECULE_METADATA_SUCCESS.getCode())
        .message(SuccessCode.CL2014_UPDATE_MOLECULE_METADATA_SUCCESS.getMessage())
        .data(updated)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/molecules/search")
  public ResponseEntity<Object> searchMolecules(@RequestParam(required = true) String query) {
    List<MoleculeDto> results = masterCatalogService.searchMolecules(query);
    return ResponseBody.builder()
        .code(SuccessCode.CL2015_SEARCH_MOLECULES_SUCCESS.getCode())
        .message(SuccessCode.CL2015_SEARCH_MOLECULES_SUCCESS.getMessage())
        .data(results)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/molecules/{moleculeId}/brands")
  public ResponseEntity<Object> getBrandsByMolecule(@PathVariable UUID moleculeId) {
    List<BrandDto> brands = masterCatalogService.getBrandsByMolecule(moleculeId);
    return ResponseBody.builder()
        .code(SuccessCode.CL2016_GET_BRANDS_BY_MOLECULE_SUCCESS.getCode())
        .message(SuccessCode.CL2016_GET_BRANDS_BY_MOLECULE_SUCCESS.getMessage())
        .data(brands)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}