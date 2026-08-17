package com.mrtripop.inventory.services;

public interface StockReconciliationService {
    String ACTION_RECONCILIATION = "STOCK_RECONCILIATION";
    String ENTITY_BATCH = "Batch";

    void reconcileAll();
    void reconcileBatch(Long batchId);
}
