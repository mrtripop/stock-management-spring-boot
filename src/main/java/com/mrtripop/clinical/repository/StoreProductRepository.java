package com.mrtripop.clinical.repository;

import com.mrtripop.clinical.models.db.StoreProduct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreProductRepository extends JpaRepository<StoreProduct, UUID> {

  @EntityGraph(attributePaths = {"brand", "brand.molecule"})
  @Query(
      "SELECT sp FROM StoreProduct sp "
          + "WHERE sp.store.id = :storeId AND sp.isActive = true "
          + "ORDER BY sp.brand.brandName ASC")
  Page<StoreProduct> findByStoreIdAndIsActiveTrue(@Param("storeId") UUID storeId, Pageable pageable);

  @EntityGraph(attributePaths = {"brand", "brand.molecule"})
  Optional<StoreProduct> findByIdAndStoreId(UUID id, UUID storeId);

  boolean existsByStoreIdAndBrandId(UUID storeId, UUID brandId);

  Optional<StoreProduct> findByStoreIdAndBrandId(UUID storeId, UUID brandId);

  @Query("SELECT sp FROM StoreProduct sp JOIN FETCH sp.brand JOIN FETCH sp.store "
      + "WHERE sp.reorderThreshold IS NOT NULL AND sp.isActive = true")
  List<StoreProduct> findWithReorderThreshold();
}
