package com.mrtripop.inventory.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.SyncSealResult;

/**
 * Sync-Seal verification service for pharmacist identity validation.
 *
 * <p>MVP implementation ({@code LocalSyncSealServiceImpl}) validates license format and generates
 * a SHA-256 hash. Epic 3 will replace this with a live central pharmacist registry integration
 * without modifying the deduction flow.
 */
public interface SyncSealService {

  SyncSealResult verifyPharmacist(String licenseNumber, String signaturePayload)
      throws ApplicationException;
}
