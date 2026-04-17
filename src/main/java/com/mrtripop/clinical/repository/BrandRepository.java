package com.mrtripop.clinical.repository;

import com.mrtripop.clinical.models.db.Brand;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {
  List<Brand> findByMoleculeId(UUID moleculeId);
}
