package com.mrtripop.inventory.fixture;

import com.mrtripop.inventory.models.dto.ReconciliationStatusDto;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class ReconciliationStatusFixture {
  private ReconciliationStatusFixture() {}

  public static final String REDIS_KEY = "inventory:reconcile:status";
  public static final String FIELD_STATUS = "status";
  public static final String FIELD_PROGRESS = "progress";
  public static final String FIELD_START_TIME = "start_time";
  public static final String FIELD_UPDATED_TIME = "updated_time";

  public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
  public static final String STATUS_COMPLETED = "COMPLETED";

  public static final int DEFAULT_PROGRESS = 0;
  public static final int MOCK_PROGRESS = 25;

  public static ReconciliationStatusDto defaultDto() {
    return ReconciliationStatusDto.builder()
        .status(STATUS_IN_PROGRESS)
        .progress(MOCK_PROGRESS)
        .build();
  }

  public static Map<Object, Object> createRedisData(Instant now) {
    Map<Object, Object> data = new HashMap<>();
    data.put(FIELD_STATUS, STATUS_IN_PROGRESS);
    data.put(FIELD_PROGRESS, MOCK_PROGRESS);
    data.put(FIELD_START_TIME, now);
    data.put(FIELD_UPDATED_TIME, now);
    return data;
  }
}
