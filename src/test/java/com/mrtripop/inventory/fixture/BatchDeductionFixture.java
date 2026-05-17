package com.mrtripop.inventory.fixture;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.StoreStock;
import java.time.LocalDate;
import java.util.UUID;

public final class BatchDeductionFixture {

  private BatchDeductionFixture() {}

  public static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final String STORE_NAME = "Main Pharmacy";
  public static final UUID BRAND_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
  public static final String BRAND_NAME = "Tylenol";
  public static final Long BATCH_ID = 1L;
  public static final String BATCH_NUMBER = "BATCH-001";
  public static final Long STORE_STOCK_ID = 1L;
  public static final Long STORE_STOCK_QUANTITY = 100L;
  public static final Long DEDUCT_QUANTITY = 5L;

  public static Store validStore() {
    return Store.builder()
        .id(STORE_ID)
        .name(STORE_NAME)
        .type(StoreType.PHYSICAL)
        .active(true)
        .build();
  }

  public static Brand validBrand() {
    Molecule molecule =
        Molecule.builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000020"))
            .genericName("Paracetamol")
            .build();
    return Brand.builder().id(BRAND_ID).brandName(BRAND_NAME).molecule(molecule).build();
  }

  public static Batch validBatch() {
    return Batch.builder()
        .id(BATCH_ID)
        .brand(validBrand())
        .batchNumber(BATCH_NUMBER)
        .expiryDate(LocalDate.now().plusYears(1))
        .quantity(STORE_STOCK_QUANTITY)
        .status(BatchStatus.AVAILABLE)
        .build();
  }

  public static StoreStock validStoreStock() {
    return StoreStock.builder()
        .id(STORE_STOCK_ID)
        .store(validStore())
        .batch(validBatch())
        .quantity(STORE_STOCK_QUANTITY)
        .build();
  }
}
