package com.mrtripop.inventory.services.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.VerificationStatus;
import com.mrtripop.inventory.models.dto.SyncSealResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LocalSyncSealService")
class LocalSyncSealServiceImplTest {

  private final LocalSyncSealServiceImpl syncSealService = new LocalSyncSealServiceImpl();

  @Nested
  @DisplayName("verifyPharmacist")
  class VerifyPharmacist {

    @Test
    @DisplayName("should return VERIFIED result for valid license and payload")
    void shouldReturnVerifiedForValidInputs() throws ApplicationException {
      SyncSealResult result = syncSealService.verifyPharmacist("PHARM-12345", "payload-data");

      assertNotNull(result);
      assertEquals(VerificationStatus.VERIFIED, result.verificationStatus());
      assertNotNull(result.signatureHash());
      assertTrue(result.signatureHash().length() == 64);
      assertNotNull(result.verifiedAt());
    }

    @Test
    @DisplayName("should throw INVALID_DIGITAL_SIGNATURE when license is null")
    void shouldThrowWhenLicenseIsNull() {
      ApplicationException ex =
          assertThrows(
              ApplicationException.class,
              () -> syncSealService.verifyPharmacist(null, "payload"));

      assertEquals(ErrorCode.INVALID_DIGITAL_SIGNATURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw INVALID_DIGITAL_SIGNATURE when license is blank")
    void shouldThrowWhenLicenseIsBlank() {
      ApplicationException ex =
          assertThrows(
              ApplicationException.class,
              () -> syncSealService.verifyPharmacist("   ", "payload"));

      assertEquals(ErrorCode.INVALID_DIGITAL_SIGNATURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw INVALID_DIGITAL_SIGNATURE when payload is null")
    void shouldThrowWhenPayloadIsNull() {
      ApplicationException ex =
          assertThrows(
              ApplicationException.class,
              () -> syncSealService.verifyPharmacist("PHARM-12345", null));

      assertEquals(ErrorCode.INVALID_DIGITAL_SIGNATURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw INVALID_DIGITAL_SIGNATURE when payload is blank")
    void shouldThrowWhenPayloadIsBlank() {
      ApplicationException ex =
          assertThrows(
              ApplicationException.class,
              () -> syncSealService.verifyPharmacist("PHARM-12345", "  "));

      assertEquals(ErrorCode.INVALID_DIGITAL_SIGNATURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw INVALID_DIGITAL_SIGNATURE when license is too short")
    void shouldThrowWhenLicenseTooShort() {
      ApplicationException ex =
          assertThrows(
              ApplicationException.class,
              () -> syncSealService.verifyPharmacist("AB", "payload"));

      assertEquals(ErrorCode.INVALID_DIGITAL_SIGNATURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw INVALID_DIGITAL_SIGNATURE when license is too long")
    void shouldThrowWhenLicenseTooLong() {
      String longLicense = "A".repeat(31);
      ApplicationException ex =
          assertThrows(
              ApplicationException.class,
              () -> syncSealService.verifyPharmacist(longLicense, "payload"));

      assertEquals(ErrorCode.INVALID_DIGITAL_SIGNATURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw INVALID_DIGITAL_SIGNATURE when license has special characters")
    void shouldThrowWhenLicenseHasSpecialChars() {
      ApplicationException ex =
          assertThrows(
              ApplicationException.class,
              () -> syncSealService.verifyPharmacist("PHARM!@#$", "payload"));

      assertEquals(ErrorCode.INVALID_DIGITAL_SIGNATURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should accept license with hyphens")
    void shouldAcceptLicenseWithHyphens() throws ApplicationException {
      SyncSealResult result =
          syncSealService.verifyPharmacist("PH-123-456", "payload-data");

      assertEquals(VerificationStatus.VERIFIED, result.verificationStatus());
    }
  }
}
