package com.mrtripop.inventory.services;

import static com.mrtripop.inventory.fixture.StockReconciliationFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.impl.StockReconciliationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("Stock Reconciliation Service")
class StockReconciliationServiceTest {

  @Mock private BatchRepository batchRepository;
  @Mock private StoreStockRepository storeStockRepository;
  @Mock private AuditService auditService;

  @InjectMocks private StockReconciliationServiceImpl service;

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
}
