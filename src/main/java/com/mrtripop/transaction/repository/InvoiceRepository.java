package com.mrtripop.transaction.repository;

import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

  Page<Invoice> findByStoreId(UUID storeId, Pageable pageable);

  List<Invoice> findByStoreIdAndStatusAndCreatedAtBetween(
      UUID storeId, InvoiceStatus status, Long start, Long end);
}
