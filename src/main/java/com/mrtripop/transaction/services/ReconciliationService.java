package com.mrtripop.transaction.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.transaction.models.dto.ReconciliationReportDto;
import com.mrtripop.transaction.models.dto.ReconciliationRequest;

public interface ReconciliationService {

  ReconciliationReportDto generateReport(ReconciliationRequest request) throws ApplicationException;
}
