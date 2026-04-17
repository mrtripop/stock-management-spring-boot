package com.mrtripop.clinical.repository;

import com.mrtripop.clinical.models.db.AuditLedger;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLedgerRepository extends JpaRepository<AuditLedger, UUID> {}
