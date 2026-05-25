package com.mrtripop.transaction.repository;

import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceStatus;
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
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

  Page<Invoice> findByStoreId(UUID storeId, Pageable pageable);

  List<Invoice> findByStoreIdAndStatusAndCreatedAtBetween(
      UUID storeId, InvoiceStatus status, Long start, Long end);

  @Query("SELECT i FROM Invoice i WHERE i.store.id = :storeId "
      + "AND i.status = :status AND i.createdAt >= :startOfDay AND i.createdAt < :endOfDay")
  List<Invoice> findByStoreIdAndStatusAndCreatedAtRange(
      @Param("storeId") UUID storeId,
      @Param("status") InvoiceStatus status,
      @Param("startOfDay") long startOfDay,
      @Param("endOfDay") long endOfDay);

  @EntityGraph(attributePaths = {"store"})
  Optional<Invoice> findWithStoreById(Long id);
}
