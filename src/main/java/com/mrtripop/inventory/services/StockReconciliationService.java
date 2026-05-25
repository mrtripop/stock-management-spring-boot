package com.mrtripop.inventory.services;

public interface StockReconciliationService {
    void reconcileAll();
    void reconcileBatch(Long batchId);
}
