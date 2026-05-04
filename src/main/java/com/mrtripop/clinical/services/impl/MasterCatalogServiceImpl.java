package com.mrtripop.clinical.services.impl;

import com.mrtripop.clinical.component.ClinicalMapper;
import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.RegulatorySchedule;
import com.mrtripop.clinical.models.dto.BrandDto;
import com.mrtripop.clinical.models.dto.MoleculeDto;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.clinical.services.MasterCatalogService;
import com.mrtripop.exception.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasterCatalogServiceImpl implements MasterCatalogService {

  private final MoleculeRepository moleculeRepository;
  private final BrandRepository brandRepository;
  private final AuditService auditService;
  private final ClinicalMapper clinicalMapper;

  @Override
  @Transactional
  public MoleculeDto createMolecule(MoleculeDto moleculeDto) {
    log.info("Creating molecule: {}", moleculeDto.getGenericName());
    try {
      Molecule molecule = clinicalMapper.toMolecule(moleculeDto);
      Molecule saved = moleculeRepository.save(molecule);
      auditService.recordAudit("CREATE_MOLECULE", "Molecule", saved.getId().toString(),
          null, saved.getGenericName());
      return clinicalMapper.toMoleculeDto(saved);
    } catch (DataIntegrityViolationException e) {
      throw new DuplicateMoleculeException(
          "Molecule with generic name '" + moleculeDto.getGenericName() + "' already exists");
    }
  }

  @Override
  @Transactional
  public BrandDto createBrand(BrandDto brandDto) {
    log.info("Creating brand: {} for molecule: {}", brandDto.getBrandName(), brandDto.getMoleculeId());
    Molecule molecule =
        moleculeRepository
            .findById(brandDto.getMoleculeId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Molecule not found with id: " + brandDto.getMoleculeId()));

    Brand brand = clinicalMapper.toBrand(brandDto);
    brand.setMolecule(molecule);
    Brand saved = brandRepository.save(brand);
    auditService.recordAudit("CREATE_BRAND", "Brand", saved.getId().toString(),
        null, saved.getBrandName());
    return clinicalMapper.toBrandDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public MoleculeDto getMolecule(UUID id) {
    return moleculeRepository
        .findById(id)
        .map(clinicalMapper::toMoleculeDto)
        .orElseThrow(() -> new NotFoundException("Molecule not found with id: " + id));
  }

  @Override
  @Transactional
  public MoleculeDto updateMoleculeMetadata(UUID id, MoleculeDto moleculeDto) {
    log.info("Updating metadata for molecule id: {}", id);
    Molecule molecule =
        moleculeRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Molecule not found with id: " + id));

    String oldValues =
        String.format(
            "therapeuticClass=%s, regulatorySchedule=%s",
            molecule.getTherapeuticClass(), molecule.getRegulatorySchedule());

    if (moleculeDto.getTherapeuticClass() != null) {
      molecule.setTherapeuticClass(moleculeDto.getTherapeuticClass());
    }
    if (moleculeDto.getRegulatorySchedule() != null) {
      molecule.setRegulatorySchedule(
          RegulatorySchedule.valueOf(moleculeDto.getRegulatorySchedule()));
    }

    Molecule saved = moleculeRepository.save(molecule);

    String newValues =
        String.format(
            "therapeuticClass=%s, regulatorySchedule=%s",
            saved.getTherapeuticClass(), saved.getRegulatorySchedule());

    if (!Objects.equals(oldValues, newValues)) {
      auditService.recordAudit("UPDATE_METADATA", "Molecule", id.toString(), oldValues, newValues);
    }

    return clinicalMapper.toMoleculeDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MoleculeDto> searchMolecules(String query) {
    if (query == null || query.isBlank()) {
      return List.of();
    }
    log.info("Searching molecules with query: {}", query);
    return moleculeRepository.findByGenericNameContainingIgnoreCase(query.trim()).stream()
        .map(clinicalMapper::toMoleculeDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<BrandDto> getBrandsByMolecule(UUID moleculeId) {
    return brandRepository.findByMoleculeId(moleculeId).stream()
        .map(clinicalMapper::toBrandDto)
        .toList();
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  public static class DuplicateMoleculeException extends RuntimeException {
    public DuplicateMoleculeException(String message) {
      super(message);
    }
  }
}
