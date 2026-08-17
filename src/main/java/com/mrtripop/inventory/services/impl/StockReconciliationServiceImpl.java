package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.NotFoundException;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.ReconciliationStatusService;
import com.mrtripop.inventory.services.StockReconciliationService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockReconciliationServiceImpl implements StockReconciliationService {

  private final BatchRepository batchRepository;
  private final StoreStockRepository storeStockRepository;
  private final AuditService auditService;
  private final ReconciliationStatusService statusService;

  // Self-injection to ensure Spring proxy is used for @Transactional self-invocation
  @Autowired
  @Lazy
  private StockReconciliationService self;

  private static final String STATUS_COMPLETED = "COMPLETED";
  private static final String STATUS_FAILED = "FAILED";

  @Override
  @Async
  public void reconcileAll() {
    statusService.startProcess();
    try {
      long totalBatches = batchRepository.count();
      int pageNumber = 0;
      long processedBatches = 0;
      Page<Batch> batchPage;
      do {
        batchPage = batchRepository.findAll(PageRequest.of(pageNumber++, 100, Sort.by("id")));
        for (Batch batch : batchPage) {
          try {
            // Use self-reference to ensure @Transactional proxy is invoked
            self.reconcileBatch(batch.getId());
          } catch (Exception e) {
            log.error("Failed to reconcile batch {}: ", batch.getId(), e);
          }
          processedBatches++;
        }
        int progress = (int) ((processedBatches * 100) / (totalBatches == 0 ? 1 : totalBatches));
        statusService.updateProgress(progress);
      } while (batchPage.hasNext());
      statusService.updateStatus(STATUS_COMPLETED);
    } catch (Exception e) {
      log.error("Stock reconciliation process failed", e);
      statusService.updateStatus(STATUS_FAILED);
    }
  }

  @Override
  @Transactional
  public void reconcileBatch(Long batchId) {
    Batch batch = batchRepository.findById(batchId)
        .orElseThrow(() -> new NotFoundException("Batch not found"));

    long actualSum = Optional.ofNullable(storeStockRepository.sumQuantityByBatchId(batchId)).orElse(0L);
    long oldQuantity = batch.getQuantity();

    if (oldQuantity != actualSum) {
      log.info("Corrected stock drift for batch {}: {} -> {}", batchId, oldQuantity, actualSum);
      batch.setQuantity(actualSum);
      try {
        batchRepository.save(batch);
      } catch (ObjectOptimisticLockingFailureException e) {
        log.warn("Optimistic lock failure reconciling batch {}: skipping", batchId);
        return;
      }

      auditService.recordAudit(
          StockReconciliationService.ACTION_RECONCILIATION,
          StockReconciliationService.ENTITY_BATCH,
          batch.getId().toString(),
          String.valueOf(oldQuantity),
          String.valueOf(actualSum)
      );
    }
  }
}
