package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.NotFoundException;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.services.StockReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockReconciliationServiceImpl implements StockReconciliationService {

  private final BatchRepository batchRepository;
  private final StoreStockRepository storeStockRepository;
  private final AuditService auditService;

  @Override
  public void reconcileAll() {
    // To be implemented in Task 4
  }

  @Override
  @Transactional
  public void reconcileBatch(Long batchId) {
    Batch batch = batchRepository.findById(batchId)
        .orElseThrow(() -> new NotFoundException("Batch not found"));

    Long actualSum = storeStockRepository.sumQuantityByBatchId(batchId);
    long oldQuantity = batch.getQuantity();

    if (oldQuantity != actualSum) {
      batch.setQuantity(actualSum);
      batchRepository.save(batch);

      auditService.recordAudit(
          "STOCK_RECONCILIATION",
          "Batch",
          batch.getId().toString(),
          String.valueOf(oldQuantity),
          String.valueOf(actualSum)
      );
    }
  }
}
