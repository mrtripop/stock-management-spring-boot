package com.mrtripop.clinical.fixture;

import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.RegulatorySchedule;
import com.mrtripop.clinical.models.dto.MoleculeDto;
import java.util.UUID;

public final class MoleculeFixture {

  private MoleculeFixture() {}

  public static MoleculeDto validDto() {
    return MoleculeDto.builder().genericName("Paracetamol").build();
  }

  public static MoleculeDto validDtoWithTherapeuticClass() {
    return MoleculeDto.builder().genericName("Amoxicillin").therapeuticClass("Antibiotic").build();
  }

  public static MoleculeDto validDtoWithSchedule() {
    return MoleculeDto.builder()
        .genericName("Diazepam")
        .therapeuticClass("Anxiolytic")
        .regulatorySchedule("RX")
        .build();
  }

  public static Molecule defaultEntity() {
    return Molecule.builder().id(UUID.randomUUID()).genericName("Paracetamol").build();
  }

  public static Molecule controlledMolecule() {
    return Molecule.builder()
        .id(UUID.randomUUID())
        .genericName("Morphine")
        .therapeuticClass("Opioid Analgesic")
        .regulatorySchedule(RegulatorySchedule.CONTROLLED)
        .build();
  }
}
