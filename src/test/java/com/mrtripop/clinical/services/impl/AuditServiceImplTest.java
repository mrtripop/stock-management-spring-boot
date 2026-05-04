package com.mrtripop.clinical.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mrtripop.clinical.models.db.AuditLedger;
import com.mrtripop.clinical.repository.AuditLedgerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditServiceImpl")
class AuditServiceImplTest {

  @Mock
  AuditLedgerRepository auditLedgerRepository;

  @InjectMocks
  AuditServiceImpl auditService;

  @BeforeEach
  void setUp() {
    when(auditLedgerRepository.save(any(AuditLedger.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Nested
  @DisplayName("recordAudit")
  class RecordAudit {

    @Test
    @DisplayName("should record audit entry with all required fields")
    void shouldRecordEntryWithAllFields() {
      AuditLedger result =
          auditService.recordAudit("CREATE_MOLECULE", "Molecule", "test-id", "old", "new");

      assertEquals("CREATE_MOLECULE", result.getActionType());
      assertEquals("Molecule", result.getEntityName());
      assertEquals("test-id", result.getEntityId());
      assertEquals("old", result.getOldValue());
      assertEquals("new", result.getNewValue());
      assertNotNull(result.getTimestamp());
      assertNotNull(result.getChecksum());
      assertEquals("ANONYMOUS", result.getUserId());
    }

    @Test
    @DisplayName("should capture userId from SecurityContext")
    void shouldCaptureUserIdFromSecurityContext() {
      Authentication auth =
          new UsernamePasswordAuthenticationToken("pharmacist", null, null);
      SecurityContextHolder.getContext().setAuthentication(auth);

      try {
        AuditLedger result =
            auditService.recordAudit("CREATE", "Molecule", "id", null, "value");

        assertEquals("pharmacist", result.getUserId());
      } finally {
        SecurityContextHolder.clearContext();
      }
    }

    @Test
    @DisplayName("should fallback to ANONYMOUS when no SecurityContext")
    void shouldFallbackToAnonymous_WhenNoSecurityContext() {
      SecurityContextHolder.clearContext();

      AuditLedger result =
          auditService.recordAudit("CREATE", "Molecule", "id", null, "value");

      assertEquals("ANONYMOUS", result.getUserId());
    }

    @Test
    @DisplayName("should persist audit entry via repository")
    void shouldPersistEntryViaRepository() {
      AuditLedger result =
          auditService.recordAudit("CREATE", "Brand", "id", null, "value");

      ArgumentCaptor<AuditLedger> captor = ArgumentCaptor.forClass(AuditLedger.class);
      verify(auditLedgerRepository).save(captor.capture());
      assertEquals("CREATE", captor.getValue().getActionType());
    }
  }

  @Nested
  @DisplayName("checksum generation")
  class ChecksumGeneration {

    @Test
    @DisplayName("should generate consistent SHA-256 checksum for same input")
    void shouldGenerateConsistentChecksum() {
      AuditLedger first =
          auditService.recordAudit("CREATE", "Molecule", "id", "old", "new");
      AuditLedger second =
          auditService.recordAudit("CREATE", "Molecule", "id", "old", "new");

      assertNotNull(first.getChecksum());
      assertNotNull(second.getChecksum());
      assertEquals(first.getChecksum(), second.getChecksum());
    }

    @Test
    @DisplayName("should generate different checksums for different inputs")
    void shouldGenerateDifferentChecksums_ForDifferentInputs() {
      AuditLedger first =
          auditService.recordAudit("CREATE", "Molecule", "id", "old", "new");
      AuditLedger second =
          auditService.recordAudit("CREATE", "Molecule", "id", "old", "CHANGED");

      assertNotEquals(first.getChecksum(), second.getChecksum());
    }
  }
}
