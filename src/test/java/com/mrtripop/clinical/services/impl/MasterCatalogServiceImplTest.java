package com.mrtripop.clinical.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.component.ClinicalMapper;
import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.clinical.models.dto.MoleculeDto;
import com.mrtripop.clinical.repository.AuditLedgerRepository;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class MasterCatalogServiceImplTest {

  @Mock private MoleculeRepository moleculeRepository;
  @Mock private BrandRepository brandRepository;
  @Mock private AuditLedgerRepository auditLedgerRepository;
  @Mock private ClinicalMapper clinicalMapper;

  @InjectMocks private MasterCatalogServiceImpl masterCatalogService;

  @Test
  void createMolecule_ShouldSaveAndReturnDto() {
    MoleculeDto input = MoleculeDto.builder().genericName("Paracetamol").build();
    Molecule saved = Molecule.builder().id(UUID.randomUUID()).genericName("Paracetamol").build();

    when(clinicalMapper.toMolecule(input)).thenReturn(
        Molecule.builder().genericName("Paracetamol").build());
    when(moleculeRepository.save(any(Molecule.class))).thenReturn(saved);
    when(clinicalMapper.toMoleculeDto(saved)).thenReturn(
        MoleculeDto.builder().id(saved.getId()).genericName("Paracetamol").build());

    MoleculeDto result = masterCatalogService.createMolecule(input);

    assertNotNull(result.getId());
    assertEquals("Paracetamol", result.getGenericName());
    verify(moleculeRepository).save(any(Molecule.class));
  }

  @Test
  void createMolecule_ShouldThrowDuplicate_WhenGenericNameExists() {
    MoleculeDto input = MoleculeDto.builder().genericName("Paracetamol").build();

    when(clinicalMapper.toMolecule(input)).thenReturn(
        Molecule.builder().genericName("Paracetamol").build());
    when(moleculeRepository.save(any(Molecule.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate key"));

    assertThrows(
        MasterCatalogServiceImpl.DuplicateMoleculeException.class,
        () -> masterCatalogService.createMolecule(input));
  }

  @Test
  void createBrand_ShouldLinkToMoleculeAndSave() {
    UUID molId = UUID.randomUUID();
    BrandDto input = BrandDto.builder().moleculeId(molId).brandName("Tylenol").build();
    Molecule molecule = Molecule.builder().id(molId).genericName("Paracetamol").build();
    Brand saved = Brand.builder().id(UUID.randomUUID()).brandName("Tylenol").molecule(molecule).build();

    when(moleculeRepository.findById(molId)).thenReturn(Optional.of(molecule));
    when(clinicalMapper.toBrand(input)).thenReturn(Brand.builder().brandName("Tylenol").build());
    when(brandRepository.save(any(Brand.class))).thenReturn(saved);
    when(clinicalMapper.toBrandDto(saved)).thenReturn(
        BrandDto.builder().id(saved.getId()).moleculeId(molId).brandName("Tylenol").build());

    BrandDto result = masterCatalogService.createBrand(input);

    assertNotNull(result.getId());
    assertEquals("Tylenol", result.getBrandName());
    assertEquals(molId, result.getMoleculeId());
    verify(brandRepository).save(any(Brand.class));
  }

  @Test
  void createBrand_ShouldThrowException_WhenMoleculeNotFound() {
    UUID molId = UUID.randomUUID();
    BrandDto input = BrandDto.builder().moleculeId(molId).brandName("Tylenol").build();

    when(moleculeRepository.findById(molId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> masterCatalogService.createBrand(input));
    verify(brandRepository, never()).save(any(Brand.class));
  }
}
