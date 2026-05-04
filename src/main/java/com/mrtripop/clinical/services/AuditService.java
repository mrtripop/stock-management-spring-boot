package com.mrtripop.clinical.services;

import com.mrtripop.clinical.models.db.AuditLedger;
import java.util.Map;

public interface AuditService {

  AuditLedger recordAudit(
      String actionType, String entityName, String entityId, String oldValue, String newValue);
}
