package com.mrtripop.clinical.repository;

import com.mrtripop.clinical.models.db.Molecule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoleculeRepository extends JpaRepository<Molecule, UUID> {
  List<Molecule> findByGenericNameContainingIgnoreCase(String genericName);
}
