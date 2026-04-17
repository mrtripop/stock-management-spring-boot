package com.mrtripop.clinical.repository;

import com.mrtripop.clinical.models.db.AuditLedger;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLedgerRepository extends JpaRepository<AuditLedger, UUID> {

  List<AuditLedger> findByEntityIdOrderByTimestampDesc(String entityId);

  @Override
  default void delete(AuditLedger entity) {
    throw new UnsupportedOperationException("Audit ledger entries are immutable and cannot be deleted");
  }

  @Override
  default void deleteById(UUID id) {
    throw new UnsupportedOperationException("Audit ledger entries are immutable and cannot be deleted");
  }

  @Override
  default void deleteAll(Iterable<? extends AuditLedger> entities) {
    throw new UnsupportedOperationException("Audit ledger entries are immutable and cannot be deleted");
  }

  @Override
  default void deleteAll() {
    throw new UnsupportedOperationException("Audit ledger entries are immutable and cannot be deleted");
  }
}
