package com.mrtripop.inventory.repository;

import com.mrtripop.inventory.models.db.Batch;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
  Optional<Batch> findByBrandIdAndBatchNumber(UUID brandId, String batchNumber);
  Page<Batch> findByBrandId(UUID brandId, Pageable pageable);
}