package com.mrtripop.inventory.services;

import com.mrtripop.inventory.models.dto.ReconciliationStatusDto;

public interface ReconciliationStatusService {
  void startProcess();

  void updateProgress(int percent);

  void updateStatus(String status);

  ReconciliationStatusDto getStatus();
}
