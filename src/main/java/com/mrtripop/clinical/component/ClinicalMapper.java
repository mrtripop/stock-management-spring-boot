package com.mrtripop.clinical.component;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.clinical.models.dto.MoleculeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClinicalMapper {

  MoleculeDto toMoleculeDto(Molecule molecule);

  Molecule toMolecule(MoleculeDto moleculeDto);

  @Mapping(source = "molecule.id", target = "moleculeId")
  BrandDto toBrandDto(Brand brand);

  @Mapping(source = "moleculeId", target = "molecule.id")
  Brand toBrand(BrandDto brandDto);
}
