package com.mrtripop.inventory.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.RecallBatchResponse;

public interface ComplianceService {
  RecallBatchResponse recallBatch(Long batchId) throws ApplicationException;
}