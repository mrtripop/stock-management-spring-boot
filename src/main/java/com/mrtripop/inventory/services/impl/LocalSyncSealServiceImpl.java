package com.mrtripop.inventory.services.impl;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.VerificationStatus;
import com.mrtripop.inventory.models.dto.SyncSealResult;
import com.mrtripop.inventory.services.SyncSealService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Local MVP implementation of Sync-Seal verification.
 *
 * <p>Validates pharmacist license format and generates a SHA-256 hash as proof-of-concept
 * signature. Epic 3 will replace this with {@code CentralRegistrySyncSealServiceImpl} that calls
 * the live pharmacist registry API.
 */
@Slf4j
@Service
public class LocalSyncSealServiceImpl implements SyncSealService {

  private static final Pattern LICENSE_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]{5,30}$");

  @Override
  public SyncSealResult verifyPharmacist(String licenseNumber, String signaturePayload)
      throws ApplicationException {
    validateInputs(licenseNumber, signaturePayload);

    long timestamp = System.currentTimeMillis();
    String signatureHash = generateHash(licenseNumber, signaturePayload, timestamp);

    log.info("Local Sync-Seal verification completed for pharmacist license={}", licenseNumber);

    return new SyncSealResult(VerificationStatus.VERIFIED, timestamp, signatureHash);
  }

  private void validateInputs(String licenseNumber, String signaturePayload)
      throws ApplicationException {
    if (licenseNumber == null || licenseNumber.isBlank()) {
      throw new ApplicationException(ErrorCode.INVALID_DIGITAL_SIGNATURE, HttpStatus.BAD_REQUEST);
    }
    if (signaturePayload == null || signaturePayload.isBlank()) {
      throw new ApplicationException(ErrorCode.INVALID_DIGITAL_SIGNATURE, HttpStatus.BAD_REQUEST);
    }
    if (!LICENSE_PATTERN.matcher(licenseNumber).matches()) {
      throw new ApplicationException(ErrorCode.INVALID_DIGITAL_SIGNATURE, HttpStatus.BAD_REQUEST);
    }
  }

  private String generateHash(String licenseNumber, String signaturePayload, long timestamp) {
    try {
      String input = licenseNumber + ":" + signaturePayload + ":" + timestamp;
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }
}
