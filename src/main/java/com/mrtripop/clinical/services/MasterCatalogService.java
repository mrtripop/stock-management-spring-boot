package com.mrtripop.clinical.services;

import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.clinical.models.dto.MoleculeDto;
import java.util.List;
import java.util.UUID;

public interface MasterCatalogService {
  MoleculeDto createMolecule(MoleculeDto moleculeDto);

  BrandDto createBrand(BrandDto brandDto);

  MoleculeDto getMolecule(UUID id);

  MoleculeDto updateMoleculeMetadata(UUID id, MoleculeDto moleculeDto);

  List<MoleculeDto> searchMolecules(String query);

  List<BrandDto> getBrandsByMolecule(UUID moleculeId);
}
