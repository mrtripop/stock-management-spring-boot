package com.mrtripop.inventory.repository;

import com.mrtripop.inventory.models.db.UnitConversion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitConversionRepository extends JpaRepository<UnitConversion, Long> {
  Optional<UnitConversion> findByBrandIdAndFromUnit(UUID brandId, String fromUnit);

  List<UnitConversion> findByBrandId(UUID brandId);

  boolean existsByBrandIdAndFromUnit(UUID brandId, String fromUnit);
}
