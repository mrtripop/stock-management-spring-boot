package com.mrtripop.clinical.fixture;

import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.clinical.models.dto.CreateStoreRequest;
import com.mrtripop.clinical.models.dto.UpdateStoreRequest;
import java.util.UUID;

public final class StoreFixture {

  private StoreFixture() {}

  public static final String STORE_NAME = "Main Pharmacy";
  public static final String STORE_NAME_UPDATED = "Updated Pharmacy";
  public static final String LOGICAL_STORE_NAME = "Cloud Store";
  public static final String TEST_STORE_NAME = "Test Store";
  public static final String CLOUD_NODE_NAME = "Cloud Node";
  public static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  public static Store defaultEntity() {
    return Store.builder().id(STORE_ID).name(STORE_NAME).type(StoreType.PHYSICAL).active(true).build();
  }

  public static Store logicalEntity() {
    return Store.builder().id(UUID.randomUUID()).name(LOGICAL_STORE_NAME).type(StoreType.LOGICAL).active(true).build();
  }

  public static Store entityWithId(UUID id) {
    return Store.builder().id(id).name(TEST_STORE_NAME).type(StoreType.PHYSICAL).active(true).build();
  }

  public static CreateStoreRequest validCreateRequest() {
    return CreateStoreRequest.builder().name(STORE_NAME).type(StoreType.PHYSICAL).build();
  }

  public static CreateStoreRequest logicalCreateRequest() {
    return CreateStoreRequest.builder().name(CLOUD_NODE_NAME).type(StoreType.LOGICAL).build();
  }

  public static UpdateStoreRequest updateNameRequest() {
    return UpdateStoreRequest.builder().name(STORE_NAME_UPDATED).build();
  }

  public static UpdateStoreRequest updateTypeRequest() {
    return UpdateStoreRequest.builder().type(StoreType.LOGICAL).build();
  }

  public static UpdateStoreRequest updateBothRequest() {
    return UpdateStoreRequest.builder().name(STORE_NAME_UPDATED).type(StoreType.LOGICAL).build();
  }
}
