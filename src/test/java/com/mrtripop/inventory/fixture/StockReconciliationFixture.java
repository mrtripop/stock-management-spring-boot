package com.mrtripop.inventory.fixture;

import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;

public final class StockReconciliationFixture {
    private StockReconciliationFixture() {}

    public static final Long VALID_BATCH_ID = 1L;
    public static final Long INITIAL_BATCH_QTY = 100L;
    public static final Long CORRECT_SUM_QTY = 80L;
    public static final String ACTION_RECONCILIATION = "STOCK_RECONCILIATION";
    public static final String ENTITY_BATCH = "Batch";

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
