package com.mrtripop.inventory.fixture;

import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.StoreStockDto;
import java.util.UUID;

public final class StoreStockFixture {

  private StoreStockFixture() {}

  public static StoreStockDto validStoreStockDto() {
    return StoreStockDto.builder()
        .id(1L)
        .storeId(UUID.randomUUID())
        .batchId(1L)
        .quantity(100L)
        .build();
  }

  public static StoreStock defaultStoreStock() {
    Store store =
        Store.builder().id(UUID.randomUUID()).name("Main Store").type(StoreType.PHYSICAL).build();
    Batch batch = BatchFixture.defaultBatch();
    return StoreStock.builder().id(1L).store(store).batch(batch).quantity(100L).build();
  }
}