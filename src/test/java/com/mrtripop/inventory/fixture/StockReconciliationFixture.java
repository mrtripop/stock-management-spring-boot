package com.mrtripop.inventory.fixture;

import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.services.StockReconciliationService;

public final class StockReconciliationFixture {
    private StockReconciliationFixture() {}

    public static final Long VALID_BATCH_ID = 1L;
    public static final Long INITIAL_BATCH_QTY = 100L;
    public static final Long CORRECT_SUM_QTY = 80L;
    public static final String ACTION_RECONCILIATION = StockReconciliationService.ACTION_RECONCILIATION;
    public static final String ENTITY_BATCH = StockReconciliationService.ENTITY_BATCH;

    public static Batch defaultBatch() {
        return Batch.builder()
            .id(VALID_BATCH_ID)
            .quantity(INITIAL_BATCH_QTY)
            .build();
    }

    public static StoreStock storeStockWithQty(Long qty) {
        return StoreStock.builder()
            .quantity(qty)
            .build();
    }
}
