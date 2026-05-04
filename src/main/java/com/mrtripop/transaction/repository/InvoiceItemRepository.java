package com.mrtripop.transaction.repository;

import com.mrtripop.transaction.models.db.InvoiceItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

  List<InvoiceItem> findByInvoiceId(Long invoiceId);

  @EntityGraph(attributePaths = {"brand.molecule", "batch"})
  List<InvoiceItem> findWithDetailsByInvoiceId(Long invoiceId);
}
