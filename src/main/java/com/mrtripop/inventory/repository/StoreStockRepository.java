package com.mrtripop.inventory.repository;

import com.mrtripop.inventory.models.db.StoreStock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
}
