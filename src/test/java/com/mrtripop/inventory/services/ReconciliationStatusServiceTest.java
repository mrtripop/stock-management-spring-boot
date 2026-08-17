package com.mrtripop.inventory.services;

import com.mrtripop.inventory.models.dto.ReconciliationStatusDto;
import com.mrtripop.inventory.services.impl.ReconciliationStatusServiceImpl;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Reconciliation Status Service Unit Tests")
class ReconciliationStatusServiceTest {

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private HashOperations<String, Object, Object> hashOperations;

  @InjectMocks
  private ReconciliationStatusServiceImpl service;

  private static final String REDIS_KEY = "inventory:reconcile:status";

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);
  }

  @Nested
  @DisplayName("Start Process")
  class StartProcess {

    @Test
    @DisplayName("should initialize status to IN_PROGRESS and progress to 0")
    void shouldInitializeStatusAndProgress() {
      // Arrange
      // (RedisTemplate is already mocked in setUp)

      // Act
      service.startProcess();

      // Assert
      verify(hashOperations).put(REDIS_KEY, "status", "IN_PROGRESS");
      verify(hashOperations).put(REDIS_KEY, "progress", 0);
      verify(hashOperations, times(2)).put(eq(REDIS_KEY), anyString(), any(Instant.class));
    }
  }

  @Nested
  @DisplayName("Update Progress")
  class UpdateProgress {

    @Test
    @DisplayName("should update progress percentage and updated time")
    void shouldUpdateProgress() {
      // Arrange
      int progress = 50;

      // Act
      service.updateProgress(progress);

      // Assert
      verify(hashOperations).put(REDIS_KEY, "progress", progress);
      verify(hashOperations).put(eq(REDIS_KEY), eq("updated_time"), any(Instant.class));
    }
  }

  @Nested
  @DisplayName("Update Status")
  class UpdateStatus {

    @Test
    @DisplayName("should update status and updated time")
    void shouldUpdateStatus() {
      // Arrange
      String newStatus = "COMPLETED";

      // Act
      service.updateStatus(newStatus);

      // Assert
      verify(hashOperations).put(REDIS_KEY, "status", newStatus);
      verify(hashOperations).put(eq(REDIS_KEY), eq("updated_time"), any(Instant.class));
    }
  }

  @Nested
  @DisplayName("Get Status")
  class GetStatus {

    @Test
    @DisplayName("should return populated DTO when data exists in Redis")
    void shouldReturnDtoWhenDataExists() {
      // Arrange
      Instant now = Instant.now();
      Map<Object, Object> redisData = new HashMap<>();
      redisData.put("status", "IN_PROGRESS");
      redisData.put("progress", 25);
      redisData.put("start_time", now);
      redisData.put("updated_time", now);

      when(hashOperations.entries(REDIS_KEY)).thenReturn(redisData);

      // Act
      ReconciliationStatusDto result = service.getStatus();

      // Assert
      assertNotNull(result);
      assertEquals("IN_PROGRESS", result.getStatus());
      assertEquals(25, result.getProgress());
      assertEquals(now, result.getStartTime());
      assertEquals(now, result.getUpdatedTime());
    }

    @Test
    @DisplayName("should return null when Redis is empty")
    void shouldReturnNullWhenRedisEmpty() {
      // Arrange
      when(hashOperations.entries(REDIS_KEY)).thenReturn(Collections.emptyMap());

      // Act
      ReconciliationStatusDto result = service.getStatus();

      // Assert
      assertNull(result);
    }

    @Test
    @DisplayName("should return null when Redis throws exception")
    void shouldReturnNullWhenRedisThrows() {
      // Arrange
      when(hashOperations.entries(REDIS_KEY)).thenThrow(new RuntimeException("Redis connection failed"));

      // Act
      ReconciliationStatusDto result = service.getStatus();

      // Assert
      assertNull(result);
    }
  }
}
