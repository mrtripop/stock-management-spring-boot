package com.mrtripop.clinical.fixture;

import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import java.util.UUID;

public final class StoreFixture {

  private StoreFixture() {}

  public static Store defaultEntity() {
    return Store.builder()
        .id(UUID.randomUUID())
        .name("Main Pharmacy")
        .type(StoreType.PHYSICAL)
        .build();
  }

  public static Store entityWithId(UUID id) {
    return Store.builder().id(id).name("Test Store").type(StoreType.PHYSICAL).build();
  }
}
