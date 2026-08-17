package com.mrtripop.inventory.services.impl;

import com.mrtripop.inventory.models.dto.ReconciliationStatusDto;
import com.mrtripop.inventory.services.ReconciliationStatusService;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationStatusServiceImpl implements ReconciliationStatusService {

  private final RedisTemplate<String, Object> redisTemplate;

  private static final String REDIS_KEY = "inventory:reconcile:status";
  private static final String FIELD_STATUS = "status";
  private static final String FIELD_PROGRESS = "progress";
  private static final String FIELD_START_TIME = "start_time";
  private static final String FIELD_UPDATED_TIME = "updated_time";

  @Override
  public void startProcess() {
    try {
      Instant now = Instant.now();
      redisTemplate.opsForHash().put(REDIS_KEY, FIELD_STATUS, "IN_PROGRESS");
      redisTemplate.opsForHash().put(REDIS_KEY, FIELD_PROGRESS, 0);
      redisTemplate.opsForHash().put(REDIS_KEY, FIELD_START_TIME, now);
      redisTemplate.opsForHash().put(REDIS_KEY, FIELD_UPDATED_TIME, now);
    } catch (Exception e) {
      log.error("Failed to initialize reconciliation status in Redis", e);
    }
  }

  @Override
  public void updateProgress(int percent) {
    try {
      redisTemplate.opsForHash().put(REDIS_KEY, FIELD_PROGRESS, percent);
      redisTemplate.opsForHash().put(REDIS_KEY, FIELD_UPDATED_TIME, Instant.now());
    } catch (Exception e) {
      log.error("Failed to update reconciliation progress in Redis", e);
    }
  }

  @Override
  public void updateStatus(String status) {
    try {
      redisTemplate.opsForHash().put(REDIS_KEY, FIELD_STATUS, status);
      redisTemplate.opsForHash().put(REDIS_KEY, FIELD_UPDATED_TIME, Instant.now());
    } catch (Exception e) {
      log.error("Failed to update reconciliation status in Redis", e);
    }
  }

  @Override
  public ReconciliationStatusDto getStatus() {
    try {
      Map<Object, Object> entries = redisTemplate.opsForHash().entries(REDIS_KEY);
      if (entries == null || entries.isEmpty()) {
        return null;
      }

      return ReconciliationStatusDto.builder()
          .status(castToString(entries.get(FIELD_STATUS)))
          .progress(castToInt(entries.get(FIELD_PROGRESS)))
          .startTime(castToInstant(entries.get(FIELD_START_TIME)))
          .updatedTime(castToInstant(entries.get(FIELD_UPDATED_TIME)))
          .build();
    } catch (Exception e) {
      log.error("Failed to retrieve reconciliation status from Redis", e);
      return null;
    }
  }

  private String castToString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private int castToInt(Object value) {
    if (value instanceof Integer) {
      return (Integer) value;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    try {
      return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private Instant castToInstant(Object value) {
    if (value instanceof Instant) {
      return (Instant) value;
    }
    try {
      return value == null ? null : Instant.parse(String.valueOf(value));
    } catch (Exception e) {
      log.debug("Could not parse value as Instant: {}", value);
      return null;
    }
  }
}
