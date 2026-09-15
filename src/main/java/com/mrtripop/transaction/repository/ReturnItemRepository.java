package com.mrtripop.transaction.repository;

import com.mrtripop.transaction.models.db.ReturnItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {

  List<ReturnItem> findByParentReturnId(Long returnId);

  @Query(
      """
      SELECT COALESCE(SUM(ri.quantity), 0) FROM ReturnItem ri
      WHERE ri.invoiceItem.id = :invoiceItemId
      """)
  Long sumQuantityByInvoiceItemId(@Param("invoiceItemId") Long invoiceItemId);
}
