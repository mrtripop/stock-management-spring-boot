package com.mrtripop.clinical.fixture;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.dto.BrandDto;
import java.util.UUID;

public final class BrandFixture {

  private BrandFixture() {}

  public static BrandDto validDto(UUID moleculeId) {
    return BrandDto.builder().moleculeId(moleculeId).brandName("Tylenol").strength("500mg")
        .form("Tablet").build();
  }

  public static Brand defaultEntity(UUID moleculeId) {
    Molecule molecule = Molecule.builder().id(moleculeId).genericName("Paracetamol").build();
    return Brand.builder().id(UUID.randomUUID()).brandName("Tylenol").molecule(molecule).build();
  }
}
