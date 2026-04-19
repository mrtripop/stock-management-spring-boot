package com.mrtripop.clinical.repository;

import static org.junit.jupiter.api.Assertions.*;

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
@DisplayName("MoleculeRepository")
class MoleculeRepositoryIT {

  @Autowired private MoleculeRepository moleculeRepository;

  @BeforeEach
  void setUp() {
    moleculeRepository.deleteAll();
  }

  @Nested
  @DisplayName("Search molecules by generic name with case insensitive partial matching")
  class SearchMoleculesByGenericName {

    @Test
    @DisplayName("should find molecules matching partial generic name")
    void shouldFindMoleculesWithPartialNameMatch() {
      Molecule molecule1 =
          Molecule.builder().genericName("Paracetamol").therapeuticClass("Analgesic").build();
      moleculeRepository.save(molecule1);

      Molecule molecule2 =
          Molecule.builder().genericName("Amoxicillin").therapeuticClass("Antibiotic").build();
      moleculeRepository.save(molecule2);

      Molecule molecule3 =
          Molecule.builder().genericName("Ibuprofen").therapeuticClass("NSAID").build();
      moleculeRepository.save(molecule3);

      List<Molecule> result =
          moleculeRepository.findByGenericNameContainingIgnoreCase("aracetamol");

      assertEquals(1, result.size());
      assertEquals("Paracetamol", result.get(0).getGenericName());
    }

    @Test
    @DisplayName("should find molecules ignoring case in search term")
    void shouldFindMoleculesIgnoringCase() {
      Molecule molecule1 =
          Molecule.builder().genericName("Paracetamol").therapeuticClass("Analgesic").build();
      moleculeRepository.save(molecule1);

      List<Molecule> result = moleculeRepository.findByGenericNameContainingIgnoreCase("paraceta");

      assertEquals(1, result.size());
      assertEquals("Paracetamol", result.get(0).getGenericName());
    }

    @Test
    @DisplayName("should return empty list when no molecules match search criteria")
    void shouldReturnEmptyWhenNoMoleculesMatchSearch() {
      Molecule molecule =
          Molecule.builder().genericName("Paracetamol").therapeuticClass("Analgesic").build();
      moleculeRepository.save(molecule);

      List<Molecule> result =
          moleculeRepository.findByGenericNameContainingIgnoreCase("nonexistent");

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should find multiple molecules sharing same base name prefix")
    void shouldFindMultipleMoleculesWithSameBaseName() {
      Molecule molecule1 =
          Molecule.builder().genericName("Amoxicillin").therapeuticClass("Antibiotic").build();
      moleculeRepository.save(molecule1);

      Molecule molecule2 =
          Molecule.builder()
              .genericName("Amoxicillin Clavulanate")
              .therapeuticClass("Antibiotic")
              .build();
      moleculeRepository.save(molecule2);

      List<Molecule> result =
          moleculeRepository.findByGenericNameContainingIgnoreCase("amoxicillin");

      assertEquals(2, result.size());
    }
  }
}
