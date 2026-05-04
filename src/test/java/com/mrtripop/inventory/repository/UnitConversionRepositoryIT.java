package com.mrtripop.inventory.repository;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.inventory.models.db.UnitConversion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UnitConversionRepository")
class UnitConversionRepositoryIT {

  @Autowired private UnitConversionRepository unitConversionRepository;
  @Autowired private BrandRepository brandRepository;
  @Autowired private MoleculeRepository moleculeRepository;

  // Helper to create and persist a Brand
  private Brand createBrand(String baseUnit) {
    Molecule molecule = moleculeRepository.save(
        Molecule.builder().id(UUID.randomUUID()).genericName("Test Molecule").build());
    Brand brand = Brand.builder()
        .id(UUID.randomUUID())
        .brandName("Test Brand")
        .baseUnit(baseUnit)
        .barcode("1234567890123")
        .molecule(molecule)
        .build();
    return brandRepository.save(brand);
  }

  @Test
  @DisplayName("should save and retrieve unit conversion")
  void shouldSaveAndRetrieveUnitConversion() {
    Brand brand = createBrand("TABLET");
    UnitConversion conversion = UnitConversion.builder()
        .brand(brand)
        .fromUnit("BOX")
        .toUnit("TABLET")
        .ratio(30)
        .build();

    UnitConversion saved = unitConversionRepository.save(conversion);

    assertNotNull(saved.getId());
    assertEquals("BOX", saved.getFromUnit());
    assertEquals("TABLET", saved.getToUnit());
    assertEquals(30, saved.getRatio());
    assertEquals(brand.getId(), saved.getBrand().getId());
  }

  @Test
  @DisplayName("should enforce unique constraint on brand_id and from_unit")
  void shouldEnforceUniqueConstraintOnBrandAndFromUnit() {
    Brand brand = createBrand("TABLET");

    UnitConversion conv1 = UnitConversion.builder()
        .brand(brand).fromUnit("BOX").toUnit("TABLET").ratio(30).build();
    unitConversionRepository.save(conv1);

    UnitConversion conv2 = UnitConversion.builder()
        .brand(brand).fromUnit("BOX").toUnit("TABLET").ratio(10).build();

    // Duplicate should throw DataIntegrityViolationException or similar
    assertThrows(Exception.class, () -> unitConversionRepository.save(conv2));
  }

  @Test
  @DisplayName("should find conversion by brandId and fromUnit")
  void shouldFindConversionByBrandIdAndFromUnit() {
    Brand brand = createBrand("TABLET");

    UnitConversion conversion = UnitConversion.builder()
        .brand(brand).fromUnit("BOX").toUnit("TABLET").ratio(30).build();
    unitConversionRepository.save(conversion);

    Optional<UnitConversion> found = unitConversionRepository
        .findByBrandIdAndFromUnit(brand.getId(), "BOX");

    assertTrue(found.isPresent());
    assertEquals(30, found.get().getRatio());
  }

  @Test
  @DisplayName("should return empty when no conversion found for brand and unit")
  void shouldReturnEmptyWhenNoConversionFound() {
    Brand brand = createBrand("TABLET");

    Optional<UnitConversion> found = unitConversionRepository
        .findByBrandIdAndFromUnit(brand.getId(), "UNKNOWN_UNIT");

    assertTrue(found.isEmpty());
  }

  @Test
  @DisplayName("should find all conversions for a brand")
  void shouldFindAllConversionsForABrand() {
    Brand brand = createBrand("TABLET");

    UnitConversion boxConv = UnitConversion.builder()
        .brand(brand).fromUnit("BOX").toUnit("TABLET").ratio(30).build();
    UnitConversion stripConv = UnitConversion.builder()
        .brand(brand).fromUnit("STRIP").toUnit("TABLET").ratio(10).build();
    unitConversionRepository.saveAll(List.of(boxConv, stripConv));

    List<UnitConversion> conversions = unitConversionRepository.findByBrandId(brand.getId());

    assertEquals(2, conversions.size());
  }

  @Test
  @DisplayName("should check existence by brandId and fromUnit")
  void shouldCheckExistenceByBrandIdAndFromUnit() {
    Brand brand = createBrand("TABLET");

    assertFalse(unitConversionRepository.existsByBrandIdAndFromUnit(brand.getId(), "BOX"));

    UnitConversion conversion = UnitConversion.builder()
        .brand(brand).fromUnit("BOX").toUnit("TABLET").ratio(30).build();
    unitConversionRepository.save(conversion);

    assertTrue(unitConversionRepository.existsByBrandIdAndFromUnit(brand.getId(), "BOX"));
  }
}