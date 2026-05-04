package com.mrtripop.inventory.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.db.Task;
import com.mrtripop.inventory.models.db.TaskType;
import com.mrtripop.inventory.models.dto.RecallBatchResponse;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.repository.TaskRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("ComplianceServiceImpl")
class ComplianceServiceImplTest {

  @Mock private BatchRepository batchRepository;
  @Mock private StoreStockRepository storeStockRepository;
  @Mock private TaskRepository taskRepository;
  @Mock private AuditService auditService;

  @InjectMocks private ComplianceServiceImpl complianceService;

  private static final Long BATCH_ID = 1L;
  private static final UUID STORE_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID STORE_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final String BATCH_NUMBER = "BATCH-001";
  private static final String BRAND_NAME = "Paracetamol 500mg";

  private Batch buildAvailableBatch() {
    Brand brand = Brand.builder().id(UUID.randomUUID()).brandName(BRAND_NAME).build();
    return Batch.builder()
        .id(BATCH_ID).batchNumber(BATCH_NUMBER).brand(brand).status(BatchStatus.AVAILABLE).build();
  }

  private StoreStock buildStoreStock(UUID storeId, Long quantity) {
    Store store = Store.builder().id(storeId).name("Store " + storeId).build();
    Brand brand = Brand.builder().id(UUID.randomUUID()).brandName(BRAND_NAME).build();
    Batch batch = Batch.builder().id(BATCH_ID).batchNumber(BATCH_NUMBER).brand(brand).status(BatchStatus.AVAILABLE).build();
    return StoreStock.builder().id(10L).store(store).batch(batch).quantity(quantity).build();
  }

  @Nested
  @DisplayName("recallBatch")
  class RecallBatch {

    @Test
    @DisplayName("should recall available batch and create alert tasks for all affected stores")
    void shouldRecallBatchAndCreateAlerts() throws ApplicationException {
      // Arrange
      Batch batch = buildAvailableBatch();
      StoreStock stock1 = buildStoreStock(STORE_ID_1, 50L);
      StoreStock stock2 = buildStoreStock(STORE_ID_2, 30L);
      when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
      when(batchRepository.recallBatch(BATCH_ID)).thenReturn(1);
      when(storeStockRepository.findByBatchIdAndQuantityGreaterThan(BATCH_ID, 0L))
          .thenReturn(List.of(stock1, stock2));
      AtomicLong taskIdCounter = new AtomicLong(100L);
      when(taskRepository.saveAll(anyList())).thenAnswer(inv -> {
        List<Task> tasks = inv.getArgument(0);
        for (Task t : tasks) {
          if (t.getId() == null) {
            t.setId(taskIdCounter.getAndIncrement());
          }
        }
        return tasks;
      });

      // Act
      RecallBatchResponse result = complianceService.recallBatch(BATCH_ID);

      // Assert
      assertEquals(BATCH_ID, result.getBatchId());
      assertEquals(BATCH_NUMBER, result.getBatchNumber());
      assertEquals(BRAND_NAME, result.getBrandName());
      assertEquals(2, result.getAffectedStores());
      assertEquals(BatchStatus.RECALLED.name(), result.getRecallStatus());
      verify(taskRepository).saveAll(anyList());
      verify(auditService).recordAudit(eq("COMPLIANCE_BATCH_RECALLED"), eq("Batch"), eq(BATCH_ID.toString()), eq("AVAILABLE"), eq("RECALLED"));
      verify(auditService, times(2)).recordAudit(eq("COMPLIANCE_RECALL_ALERT_CREATED"), eq("Task"), any(), isNull(), contains("RECALL:"));
    }

    @Test
    @DisplayName("should throw INV4020 when batch not found")
    void shouldThrowWhenBatchNotFound() {
      // Arrange
      when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex = assertThrows(ApplicationException.class, () -> complianceService.recallBatch(BATCH_ID));
      assertEquals(ErrorCode.BATCH_NOT_RECALLABLE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw INV4021 when batch is already recalled")
    void shouldThrowWhenAlreadyRecalled() {
      // Arrange
      Brand brand = Brand.builder().id(UUID.randomUUID()).brandName(BRAND_NAME).build();
      Batch batch = Batch.builder().id(BATCH_ID).batchNumber(BATCH_NUMBER).brand(brand).status(BatchStatus.RECALLED).build();
      when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
      when(batchRepository.recallBatch(BATCH_ID)).thenReturn(0);

      // Act & Assert
      ApplicationException ex = assertThrows(ApplicationException.class, () -> complianceService.recallBatch(BATCH_ID));
      assertEquals(ErrorCode.BATCH_ALREADY_RECALLED, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw INV4022 when batch is already quarantined")
    void shouldThrowWhenAlreadyQuarantined() {
      // Arrange
      Brand brand = Brand.builder().id(UUID.randomUUID()).brandName(BRAND_NAME).build();
      Batch batch = Batch.builder().id(BATCH_ID).batchNumber(BATCH_NUMBER).brand(brand).status(BatchStatus.QUARANTINED).build();
      when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
      when(batchRepository.recallBatch(BATCH_ID)).thenReturn(0);

      // Act & Assert
      ApplicationException ex = assertThrows(ApplicationException.class, () -> complianceService.recallBatch(BATCH_ID));
      assertEquals(ErrorCode.BATCH_ALREADY_QUARANTINED, ex.getErrorCode());
    }

    @Test
    @DisplayName("should recall batch with zero affected stores and create no tasks")
    void shouldRecallWithZeroAffectedStores() throws ApplicationException {
      // Arrange
      Batch batch = buildAvailableBatch();
      when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
      when(batchRepository.recallBatch(BATCH_ID)).thenReturn(1);
      when(storeStockRepository.findByBatchIdAndQuantityGreaterThan(BATCH_ID, 0L))
          .thenReturn(Collections.emptyList());

      // Act
      RecallBatchResponse result = complianceService.recallBatch(BATCH_ID);

      // Assert
      assertEquals(0, result.getAffectedStores());
      verify(taskRepository, never()).saveAll(anyList());
    }
  }
}
