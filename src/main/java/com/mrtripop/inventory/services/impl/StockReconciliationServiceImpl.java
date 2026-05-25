package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.NotFoundException;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.StockReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockReconciliationServiceImpl implements StockReconciliationService {

  private final BatchRepository batchRepository;
  private final StoreStockRepository storeStockRepository;
  private final AuditService auditService;

  @Override
  public void reconcileAll() {
    int pageNumber = 0;
    Page<Batch> batchPage;
    do {
      batchPage = batchRepository.findAll(PageRequest.of(pageNumber++, 100));
      for (Batch batch : batchPage) {
        try {
          reconcileBatch(batch.getId());
        } catch (Exception e) {
          log.error("Failed to reconcile batch {}: {}", batch.getId(), e.getMessage());
        }
      }
    } while (batchPage.hasNext());
  }

  @Override
  @Transactional
  public void reconcileBatch(Long batchId) {
    Batch batch = batchRepository.findById(batchId)
        .orElseThrow(() -> new NotFoundException("Batch not found"));

    Long actualSum = storeStockRepository.sumQuantityByBatchId(batchId);
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
