package com.mrtripop.inventory.repository;

import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.MeshStockDto;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreStockRepository extends JpaRepository<StoreStock, Long> {
  Optional<StoreStock> findByStoreIdAndBatchId(UUID storeId, Long batchId);

  Page<StoreStock> findByStoreId(UUID storeId, Pageable pageable);

  @Query(
      "SELECT ss FROM StoreStock ss JOIN FETCH ss.batch b "
          + "JOIN b.brand br "
          + "WHERE ss.store.id = :storeId AND br.id = :brandId "
          + "AND b.status = 'AVAILABLE' "
          + "AND b.expiryDate > CURRENT_DATE "
          + "AND ss.quantity > 0 "
          + "ORDER BY b.expiryDate ASC")
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<StoreStock> findAvailableStockByStoreIdAndBrandIdOrderByExpiryDate(
      @Param("storeId") UUID storeId, @Param("brandId") UUID brandId);

  @Modifying
  @Query("UPDATE StoreStock ss SET ss.quantity = ss.quantity - :amount "
      + "WHERE ss.id = :id AND ss.quantity >= :amount")
  int deductQuantity(@Param("id") Long id, @Param("amount") Long amount);

  @Modifying
  @Query("UPDATE StoreStock ss SET ss.quantity = ss.quantity + :amount WHERE ss.id = :id")
  int restoreQuantity(@Param("id") Long id, @Param("amount") Long amount);

  @Query("SELECT ss FROM StoreStock ss JOIN FETCH ss.batch b JOIN FETCH b.brand br "
      + "WHERE ss.store.id = :storeId "
      + "AND b.status = 'AVAILABLE' "
      + "AND b.expiryDate > CURRENT_DATE "
      + "AND b.expiryDate <= :thresholdDate "
      + "AND ss.quantity > 0")
  List<StoreStock> findExpiringSoonByStore(
      @Param("storeId") UUID storeId,
      @Param("thresholdDate") LocalDate thresholdDate);

  @Query("SELECT COALESCE(SUM(ss.quantity), 0) FROM StoreStock ss JOIN ss.batch b "
      + "WHERE ss.store.id = :storeId AND b.brand.id = :brandId "
      + "AND b.status = 'AVAILABLE' AND b.expiryDate > CURRENT_DATE AND ss.quantity > 0")
  Long sumAvailableQuantityByStoreAndBrand(
      @Param("storeId") UUID storeId,
      @Param("brandId") UUID brandId);

  @Query("SELECT DISTINCT ss.store.id FROM StoreStock ss")
  List<UUID> findDistinctStoreIds();

  @Query(
      "SELECT new com.mrtripop.inventory.models.dto.MeshStockDto("
          + "s.id, s.name, br.id, br.brandName, m.genericName, "
          + "COALESCE(SUM(ss.quantity), 0), COUNT(ss.id)) "
          + "FROM StoreStock ss "
          + "JOIN ss.store s "
          + "JOIN ss.batch b "
          + "JOIN b.brand br "
          + "JOIN br.molecule m "
          + "WHERE m.id = :moleculeId "
          + "AND b.status = 'AVAILABLE' "
          + "AND b.expiryDate > CURRENT_DATE "
          + "AND ss.quantity > 0 "
          + "GROUP BY s.id, s.name, br.id, br.brandName, m.genericName")
  List<MeshStockDto> aggregateStockByMolecule(@Param("moleculeId") UUID moleculeId);

  @EntityGraph(attributePaths = {"store", "batch", "batch.brand"})
  List<StoreStock> findByBatchIdAndQuantityGreaterThan(Long batchId, Long quantity);
}
