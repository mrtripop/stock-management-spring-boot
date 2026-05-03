package com.mrtripop.inventory.repository;

import com.mrtripop.inventory.models.db.StoreStock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreStockRepository extends JpaRepository<StoreStock, Long> {
  Optional<StoreStock> findByStoreIdAndBatchId(UUID storeId, Long batchId);
  Page<StoreStock> findByStoreId(UUID storeId, Pageable pageable);
}