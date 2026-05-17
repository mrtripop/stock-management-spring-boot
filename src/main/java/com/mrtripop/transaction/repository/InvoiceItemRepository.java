package com.mrtripop.transaction.repository;

import com.mrtripop.transaction.models.db.InvoiceItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

  List<InvoiceItem> findByInvoiceId(Long invoiceId);

  @EntityGraph(attributePaths = {"brand.molecule", "batch"})
  List<InvoiceItem> findWithDetailsByInvoiceId(Long invoiceId);

  @Query("SELECT COALESCE(SUM(ii.quantity), 0) FROM InvoiceItem ii "
      + "WHERE ii.invoice.id IN :invoiceIds")
  Long sumQuantityByInvoiceIds(@Param("invoiceIds") List<Long> invoiceIds);
}
