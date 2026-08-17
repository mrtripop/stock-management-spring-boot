package com.mrtripop.inventory.services;

import static com.mrtripop.inventory.fixture.StockReconciliationFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.ReconciliationStatusService;
import com.mrtripop.inventory.services.impl.StockReconciliationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("Stock Reconciliation Service")
class StockReconciliationServiceTest {

  @Mock private BatchRepository batchRepository;
  @Mock private StoreStockRepository storeStockRepository;
  @Mock private AuditService auditService;
  @Mock private ReconciliationStatusService statusService;

  @InjectMocks private StockReconciliationServiceImpl service;

  @BeforeEach
  void injectSelfReference() {
    ReflectionTestUtils.setField(service, "self", service);
  }

  @Nested
  @DisplayName("Reconcile single batch")
  class ReconcileBatch {

    @Test
    @DisplayName("should correct drift and record audit when quantity differs")
    void shouldCorrectDriftAndRecordAudit() {
      // Arrange
      Batch batch = defaultBatch();
      when(batchRepository.findById(VALID_BATCH_ID)).thenReturn(Optional.of(batch));
      when(storeStockRepository.sumQuantityByBatchId(VALID_BATCH_ID)).thenReturn(CORRECT_SUM_QTY);

      // Act
      service.reconcileBatch(VALID_BATCH_ID);

      // Assert
      assertEquals(CORRECT_SUM_QTY, batch.getQuantity());
      verify(batchRepository).save(batch);
      verify(auditService).recordAudit(
          ACTION_RECONCILIATION,
          ENTITY_BATCH,
          VALID_BATCH_ID.toString(),
          INITIAL_BATCH_QTY.toString(),
          CORRECT_SUM_QTY.toString()
      );
    }
  }

  @Nested
  @DisplayName("Edge cases")
  class EdgeCases {

    @Test
    @DisplayName("should do nothing when synchronized")
    void shouldDoNothingWhenSynced() {
      // Arrange
      Batch batch = defaultBatch();
      when(batchRepository.findById(VALID_BATCH_ID)).thenReturn(Optional.of(batch));
      when(storeStockRepository.sumQuantityByBatchId(VALID_BATCH_ID)).thenReturn(INITIAL_BATCH_QTY);

      // Act
      service.reconcileBatch(VALID_BATCH_ID);

      // Assert
      verify(batchRepository, never()).save(any());
      verify(auditService, never()).recordAudit(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("should set quantity to zero when batch is an orphan")
    void shouldSetToZeroWhenOrphan() {
      // Arrange
      Batch batch = defaultBatch();
      batch.setQuantity(INITIAL_BATCH_QTY);
      when(batchRepository.findById(VALID_BATCH_ID)).thenReturn(Optional.of(batch));
      when(storeStockRepository.sumQuantityByBatchId(VALID_BATCH_ID)).thenReturn(0L);

      // Act
      service.reconcileBatch(VALID_BATCH_ID);

      // Assert
      assertEquals(0L, batch.getQuantity());
      verify(batchRepository).save(batch);
    }
  }

  @Nested
  @DisplayName("Resilience")
  class Resilience {

    @Test
    @DisplayName("should handle optimistic lock failure and not record audit")
    void shouldHandleOptimisticLockFailure() {
      // Arrange
      Batch batch = defaultBatch();
      when(batchRepository.findById(VALID_BATCH_ID)).thenReturn(Optional.of(batch));
      when(storeStockRepository.sumQuantityByBatchId(VALID_BATCH_ID)).thenReturn(CORRECT_SUM_QTY);
      when(batchRepository.save(any())).thenThrow(new ObjectOptimisticLockingFailureException(Batch.class, VALID_BATCH_ID));

      // Act & Assert
      assertDoesNotThrow(() -> service.reconcileBatch(VALID_BATCH_ID));
      verify(auditService, never()).recordAudit(any(), any(), any(), any(), any());
    }
  }

  @Nested
  @DisplayName("Reconcile all batches")
  class ReconcileAll {

    @Test
    @DisplayName("should start process, update progress and complete on success")
    void shouldStartProcessUpdateProgressAndCompleteOnSuccess() {
      // Arrange
      long totalBatches = 2L;
      when(batchRepository.count()).thenReturn(totalBatches);

      Batch b1 = defaultBatch();
      Batch b2 = defaultBatch();
      Page<Batch> page1 = new PageImpl<Batch>(List.of(b1), PageRequest.of(0, 1), 2);
      Page<Batch> page2 = new PageImpl<Batch>(List.of(b2), PageRequest.of(1, 1), 2);

      // Two pages, each with one batch
      when(batchRepository.findAll(any(PageRequest.class)))
          .thenReturn(page1)
          .thenReturn(page2);

      when(batchRepository.findById(any())).thenReturn(Optional.of(b1));
      when(storeStockRepository.sumQuantityByBatchId(any())).thenReturn(INITIAL_BATCH_QTY);

      // Act
      service.reconcileAll();

      // Assert
      verify(statusService).startProcess();
      verify(statusService).updateProgress(50);
      verify(statusService).updateProgress(100);
      verify(statusService).updateStatus("COMPLETED");
    }

    @Test
    @DisplayName("should update status to failed when exception occurs")
    void shouldUpdateStatusToFailedOnException() {
      // Arrange
      when(batchRepository.count()).thenThrow(new RuntimeException("DB Error"));

      // Act
      service.reconcileAll();

      // Assert
      verify(statusService).startProcess();
      verify(statusService).updateStatus("FAILED");
      verify(statusService, never()).updateStatus("COMPLETED");
    }
  }
}
