package com.mrtripop.clinical.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("BrandRepository")
class BrandRepositoryIT {

  @Autowired private BrandRepository brandRepository;
  @Autowired private MoleculeRepository moleculeRepository;

  @BeforeEach
  void setUp() {
    brandRepository.deleteAll();
  }

  @Nested
  @DisplayName("findByMoleculeId")
  class FindByMoleculeId {

    @Test
  @DisplayName("should find brands by molecule ID")
    void shouldFindByMoleculeId() {
      Molecule molecule = Molecule.builder()
          .genericName("Paracetamol")
          .therapeuticClass("Analgesic")
          .build();
      molecule = moleculeRepository.save(molecule);

      Brand brand1 = Brand.builder()
          .molecule(molecule)
          .brandName("Panadol")
          .strength("500mg")
          .build();
      brandRepository.save(brand1);

      Brand brand2 = Brand.builder()
          .molecule(molecule)
          .brandName("Calpol")
          .strength("250mg")
          .build();
      brandRepository.save(brand2);

      Brand brand3 = Brand.builder()
          .molecule(molecule)
          .brandName("Dolomite")
          .strength("650mg")
          .build();
      brandRepository.save(brand3);

      List<Brand> result = brandRepository.findByMoleculeId(molecule.getId());

      assertEquals(3, result.size());
      assertTrue(result.stream().anyMatch(b -> b.getBrandName().equals("Panadol")));
      assertTrue(result.stream().anyMatch(b -> b.getBrandName().equals("Calpol")));
      assertTrue(result.stream().anyMatch(b -> b.getBrandName().equals("Dolomite")));
    }

    @Test
    @DisplayName("should return empty list when no brands for molecule")
    void shouldReturnEmptyWhenNoBrands() {
      Molecule molecule = Molecule.builder()
          .genericName("Paracetamol")
          .therapeuticClass("Analgesic")
          .build();
      molecule = moleculeRepository.save(molecule);

      List<Brand> result = brandRepository.findByMoleculeId(molecule.getId());

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty list when molecule does not exist")
    void shouldReturnEmptyWhenMoleculeNotExists() {
      List<Brand> result = brandRepository.findByMoleculeId(UUID.randomUUID());

      assertTrue(result.isEmpty());
    }
  }
}