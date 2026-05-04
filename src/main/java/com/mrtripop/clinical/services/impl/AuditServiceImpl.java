package com.mrtripop.clinical.services.impl;

import com.mrtripop.clinical.models.db.AuditLedger;
import com.mrtripop.clinical.repository.AuditLedgerRepository;
import com.mrtripop.clinical.services.AuditService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

  private final AuditLedgerRepository auditLedgerRepository;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public AuditLedger recordAudit(
      String actionType, String entityName, String entityId, String oldValue, String newValue) {
    String userId = resolveUserId();

    AuditLedger entry =
        AuditLedger.builder()
            .timestamp(LocalDateTime.now())
            .userId(userId)
            .actionType(actionType)
            .entityName(entityName)
            .entityId(entityId)
            .oldValue(oldValue)
            .newValue(newValue)
            .checksum(computeChecksum(userId, actionType, entityName, entityId, oldValue, newValue))
            .build();

    log.info("Audit recorded: {} {} by {}", actionType, entityName, userId);
    return auditLedgerRepository.save(entry);
  }

  String resolveUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) {
      return "ANONYMOUS";
    }
    return auth.getName();
  }

  String computeChecksum(
      String userId, String actionType, String entityName, String entityId, String oldValue,
      String newValue) {
    String content =
        String.join(
            "|", userId, actionType, entityName, entityId, oldValue != null ? oldValue : "",
            newValue != null ? newValue : "");
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to compute SHA-256 checksum", e);
      return "CHECKSUM_ERROR";
    }
  }
}
