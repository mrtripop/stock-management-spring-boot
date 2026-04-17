package com.mrtripop.clinical.controllers;

import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.clinical.models.dto.MoleculeDto;
import com.mrtripop.clinical.services.MasterCatalogService;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clinical/catalog")
@RequiredArgsConstructor
public class MasterCatalogController {
  private final MasterCatalogService masterCatalogService;

  @PostMapping("/molecules")
  public ResponseEntity<ResponseBody<MoleculeDto>> createMolecule(
      @Valid @RequestBody MoleculeDto moleculeDto) {
    MoleculeDto created = masterCatalogService.createMolecule(moleculeDto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ResponseBody<>(String.valueOf(HttpStatus.CREATED.value()), "Molecule created successfully", created));
  }

  @PostMapping("/brands")
  public ResponseEntity<ResponseBody<BrandDto>> createBrand(@Valid @RequestBody BrandDto brandDto) {
    BrandDto created = masterCatalogService.createBrand(brandDto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ResponseBody<>(String.valueOf(HttpStatus.CREATED.value()), "Brand created successfully", created));
  }

  @GetMapping("/molecules/{id}")
  public ResponseEntity<ResponseBody<MoleculeDto>> getMolecule(@PathVariable UUID id) {
    MoleculeDto molecule = masterCatalogService.getMolecule(id);
    return ResponseEntity.ok(
        new ResponseBody<>(String.valueOf(HttpStatus.OK.value()), "Molecule retrieved successfully", molecule));
  }

  @PatchMapping("/molecules/{id}/metadata")
  public ResponseEntity<ResponseBody<MoleculeDto>> updateMoleculeMetadata(
      @PathVariable UUID id, @Valid @RequestBody MoleculeDto moleculeDto) {
    MoleculeDto updated = masterCatalogService.updateMoleculeMetadata(id, moleculeDto);
    return ResponseEntity.ok(
        new ResponseBody<>(String.valueOf(HttpStatus.OK.value()), "Molecule metadata updated successfully", updated));
  }

  @GetMapping("/molecules/search")
  public ResponseEntity<ResponseBody<List<MoleculeDto>>> searchMolecules(
      @RequestParam String query) {
    List<MoleculeDto> results = masterCatalogService.searchMolecules(query);
    return ResponseEntity.ok(
        new ResponseBody<>(String.valueOf(HttpStatus.OK.value()), "Molecules retrieved successfully", results));
  }

  @GetMapping("/molecules/{moleculeId}/brands")
  public ResponseEntity<ResponseBody<List<BrandDto>>> getBrandsByMolecule(
      @PathVariable UUID moleculeId) {
    List<BrandDto> brands = masterCatalogService.getBrandsByMolecule(moleculeId);
    return ResponseEntity.ok(
        new ResponseBody<>(String.valueOf(HttpStatus.OK.value()), "Brands retrieved successfully", brands));
  }
}
