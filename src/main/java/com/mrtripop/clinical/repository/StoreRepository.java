package com.mrtripop.clinical.repository;

import com.mrtripop.clinical.models.db.Store;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
  Page<Store> findByActiveTrue(Pageable pageable);

  Optional<Store> findByIdAndActiveTrue(UUID id);

  boolean existsByName(String name);

  boolean existsByNameAndActiveTrue(String name);
}
