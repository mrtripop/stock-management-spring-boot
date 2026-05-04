package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.db.Task;
import com.mrtripop.inventory.models.db.TaskStatus;
import com.mrtripop.inventory.models.db.TaskType;
import com.mrtripop.inventory.models.dto.RecallBatchResponse;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.repository.TaskRepository;
import com.mrtripop.inventory.services.ComplianceService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceServiceImpl implements ComplianceService {
  private static final int MAX_TASK_MESSAGE_LENGTH = 500;

  private final BatchRepository batchRepository;
  private final StoreStockRepository storeStockRepository;
  private final TaskRepository taskRepository;
  private final AuditService auditService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public RecallBatchResponse recallBatch(Long batchId) throws ApplicationException {
    Batch batch = batchRepository.findById(batchId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.BATCH_NOT_RECALLABLE, HttpStatus.NOT_FOUND));

    BatchStatus previousStatus = batch.getStatus();

    int updatedRows = batchRepository.recallBatch(batchId);
    if (updatedRows == 0) {
      throw switch (previousStatus) {
        case RECALLED -> new ApplicationException(ErrorCode.BATCH_ALREADY_RECALLED, HttpStatus.CONFLICT);
        case QUARANTINED -> new ApplicationException(ErrorCode.BATCH_ALREADY_QUARANTINED, HttpStatus.CONFLICT);
        default -> new ApplicationException(ErrorCode.BATCH_NOT_RECALLABLE, HttpStatus.CONFLICT);
      };
    }

    batch.setStatus(BatchStatus.RECALLED);

    auditService.recordAudit("COMPLIANCE_BATCH_RECALLED", "Batch", batchId.toString(), previousStatus.name(), "RECALLED");

    List<StoreStock> affectedStocks = storeStockRepository.findByBatchIdAndQuantityGreaterThan(batchId, 0L);

    List<Task> tasks = new ArrayList<>();
    for (StoreStock stock : affectedStocks) {
      Batch recalledBatch = stock.getBatch();
      Brand brand = recalledBatch.getBrand();
      String message = String.format(
          "RECALL: Batch %s of %s has been recalled. Store has %d units in stock. Remove from shelves immediately.",
          recalledBatch.getBatchNumber(), brand.getBrandName(), stock.getQuantity());
      message = message.substring(0, Math.min(message.length(), MAX_TASK_MESSAGE_LENGTH));

      tasks.add(Task.builder()
          .store(stock.getStore())
          .taskType(TaskType.RECALL_ALERT)
          .status(TaskStatus.PENDING)
          .batch(recalledBatch)
          .brand(brand)
          .message(message)
          .currentQuantity(stock.getQuantity())
          .build());
    }

    if (!tasks.isEmpty()) {
      List<Task> savedTasks = taskRepository.saveAll(tasks);
      for (Task task : savedTasks) {
        auditService.recordAudit("COMPLIANCE_RECALL_ALERT_CREATED", "Task", task.getId().toString(), null, task.getMessage());
      }
    }

    String brandName = affectedStocks.isEmpty()
        ? batch.getBrand() != null ? batch.getBrand().getBrandName() : null
        : affectedStocks.get(0).getBatch().getBrand().getBrandName();

    return RecallBatchResponse.builder()
        .batchId(batchId)
        .batchNumber(batch.getBatchNumber())
        .brandName(brandName)
        .affectedStores(affectedStocks.size())
        .recallStatus(BatchStatus.RECALLED.name())
        .build();
  }
}
