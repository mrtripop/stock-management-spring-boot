package com.mrtripop.transaction.repository;

import com.mrtripop.transaction.models.db.Return;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {

  Page<Return> findByInvoiceId(Long invoiceId, Pageable pageable);
}
