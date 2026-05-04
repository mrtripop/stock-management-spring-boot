package com.mrtripop.clinical.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.component.ClinicalMapper;
import com.mrtripop.clinical.fixture.BrandFixture;
import com.mrtripop.clinical.fixture.MoleculeFixture;
import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.RegulatorySchedule;
import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.clinical.models.dto.MoleculeDto;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("MasterCatalogServiceImpl")
class MasterCatalogServiceImplTest {

  @Mock private MoleculeRepository moleculeRepository;
  @Mock private BrandRepository brandRepository;
  @Mock private AuditService auditService;
  @Mock private ClinicalMapper clinicalMapper;

  @InjectMocks private MasterCatalogServiceImpl masterCatalogService;

  @Nested
  @DisplayName("createMolecule")
  class CreateMolecule {

    @Test
    @DisplayName("should save and return molecule DTO")
    void shouldSaveAndReturnDto() {
      MoleculeDto input = MoleculeFixture.validDto();
      Molecule saved = MoleculeFixture.defaultEntity();

      when(clinicalMapper.toMolecule(input)).thenReturn(
          Molecule.builder().genericName("Paracetamol").build());
      when(moleculeRepository.save(any(Molecule.class))).thenReturn(saved);
      when(clinicalMapper.toMoleculeDto(saved)).thenReturn(
          MoleculeDto.builder().id(saved.getId()).genericName("Paracetamol").build());

      // Act
      MoleculeDto result = masterCatalogService.createMolecule(input);

      // Assert
      assertNotNull(result.getId());
      assertEquals("Paracetamol", result.getGenericName());
      verify(moleculeRepository).save(any(Molecule.class));
      verify(auditService).recordAudit(eq("CREATE_MOLECULE"), eq("Molecule"),
          eq(saved.getId().toString()), isNull(), eq("Paracetamol"));
    }

    @Test
    @DisplayName("should throw DuplicateMoleculeException when generic name already exists")
    void whenGenericNameExists_ShouldThrowDuplicate() {
      MoleculeDto input = MoleculeFixture.validDto();

      when(clinicalMapper.toMolecule(input)).thenReturn(
          Molecule.builder().genericName("Paracetamol").build());
      when(moleculeRepository.save(any(Molecule.class)))
          .thenThrow(new DataIntegrityViolationException("duplicate key"));

      assertThrows(
          MasterCatalogServiceImpl.DuplicateMoleculeException.class,
          () -> masterCatalogService.createMolecule(input));
      verify(auditService, never()).recordAudit(any(), any(), any(), any(), any());
    }
  }

  @Nested
  @DisplayName("createBrand")
  class CreateBrand {

    @Test
    @DisplayName("should link to molecule and save")
    void shouldLinkToMoleculeAndSave() {
      UUID molId = UUID.randomUUID();
      BrandDto input = BrandFixture.validDto(molId);
      Molecule molecule = Molecule.builder().id(molId).genericName("Paracetamol").build();
      Brand saved = BrandFixture.defaultEntity(molId);

      when(moleculeRepository.findById(molId)).thenReturn(Optional.of(molecule));
      when(clinicalMapper.toBrand(input)).thenReturn(Brand.builder().brandName("Tylenol").build());
      when(brandRepository.save(any(Brand.class))).thenReturn(saved);
      when(clinicalMapper.toBrandDto(saved)).thenReturn(
          BrandDto.builder().id(saved.getId()).moleculeId(molId).brandName("Tylenol").build());

      // Act
      BrandDto result = masterCatalogService.createBrand(input);

      // Assert
      assertNotNull(result.getId());
      assertEquals("Tylenol", result.getBrandName());
      verify(auditService).recordAudit(eq("CREATE_BRAND"), eq("Brand"),
          eq(saved.getId().toString()), isNull(), eq("Tylenol"));
    }

    @Test
    @DisplayName("should throw NotFoundException when molecule not found")
    void whenMoleculeNotFound_ShouldThrowException() {
      UUID molId = UUID.randomUUID();
      BrandDto input = BrandFixture.validDto(molId);

      when(moleculeRepository.findById(molId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> masterCatalogService.createBrand(input));
      verify(brandRepository, never()).save(any(Brand.class));
    }
  }

  @Nested
  @DisplayName("updateMoleculeMetadata")
  class UpdateMoleculeMetadata {

    @Test
    @DisplayName("should update fields and record audit when values change")
    void shouldUpdateAndRecordAudit_WhenValuesChange() {
      UUID id = UUID.randomUUID();
      MoleculeDto input = MoleculeFixture.validDtoWithSchedule();
      Molecule existing = Molecule.builder().id(id).genericName("Diazepam")
          .therapeuticClass("Sedative").build();
      Molecule updated = Molecule.builder().id(id).genericName("Diazepam")
          .therapeuticClass("Anxiolytic").regulatorySchedule(RegulatorySchedule.RX).build();

      when(moleculeRepository.findById(id)).thenReturn(Optional.of(existing));
      when(moleculeRepository.save(any(Molecule.class))).thenReturn(updated);
      when(clinicalMapper.toMoleculeDto(updated)).thenReturn(
          MoleculeDto.builder().id(id).genericName("Diazepam")
              .therapeuticClass("Anxiolytic").regulatorySchedule("RX").build());

      // Act
      MoleculeDto result = masterCatalogService.updateMoleculeMetadata(id, input);

      // Assert
      assertEquals("Anxiolytic", result.getTherapeuticClass());
      assertEquals("RX", result.getRegulatorySchedule());
      verify(auditService).recordAudit(eq("UPDATE_METADATA"), eq("Molecule"), eq(id.toString()),
          anyString(), anyString());
    }

    @Test
    @DisplayName("should not record audit when values do not change")
    void shouldNotRecordAudit_WhenValuesUnchanged() {
      UUID id = UUID.randomUUID();
      MoleculeDto input = MoleculeFixture.validDtoWithTherapeuticClass();
      Molecule existing = Molecule.builder().id(id).genericName("Amoxicillin")
          .therapeuticClass("Antibiotic").build();
      Molecule updated = Molecule.builder().id(id).genericName("Amoxicillin")
          .therapeuticClass("Antibiotic").build();

      when(moleculeRepository.findById(id)).thenReturn(Optional.of(existing));
      when(moleculeRepository.save(any(Molecule.class))).thenReturn(updated);
      when(clinicalMapper.toMoleculeDto(updated)).thenReturn(
          MoleculeDto.builder().id(id).genericName("Amoxicillin")
              .therapeuticClass("Antibiotic").build());

      // Act
      masterCatalogService.updateMoleculeMetadata(id, input);

      // Assert
      verify(auditService, never()).recordAudit(any(), any(), any(), any(), any());
    }
  }
}
