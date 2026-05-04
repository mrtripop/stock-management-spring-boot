package com.mrtripop.inventory.models.dto;

import com.mrtripop.inventory.models.db.VerificationStatus;

public record SyncSealResult(VerificationStatus verificationStatus, Long verifiedAt, String signatureHash) {}
