package com.mrtripop.inventory.fixture;

import com.mrtripop.inventory.models.db.DigitalSignature;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.db.VerificationStatus;
import com.mrtripop.inventory.models.dto.DigitalSignatureRequest;
import com.mrtripop.inventory.models.dto.SyncSealResult;
import java.time.Instant;

public final class DigitalSignatureFixture {

  private DigitalSignatureFixture() {}

  public static final String VALID_LICENSE = "PHARM-12345";
  public static final String VALID_PAYLOAD = "signature-data-payload";

  public static DigitalSignatureRequest validSignatureRequest() {
    return DigitalSignatureRequest.builder()
        .licenseNumber(VALID_LICENSE)
        .signaturePayload(VALID_PAYLOAD)
        .build();
  }

  public static SyncSealResult defaultSyncSealResult() {
    return new SyncSealResult(VerificationStatus.VERIFIED, Instant.now().toEpochMilli(), "abc123");
  }

  public static DigitalSignature validDigitalSignatureEntity(StoreStock storeStock) {
    SyncSealResult result = defaultSyncSealResult();
    return DigitalSignature.builder()
        .id(1L)
        .storeStock(storeStock)
        .pharmacistLicenseNumber(VALID_LICENSE)
        .signaturePayload(VALID_PAYLOAD)
        .signatureHash(result.signatureHash())
        .verificationStatus(result.verificationStatus())
        .verifiedAt(result.verifiedAt())
        .build();
  }
}
