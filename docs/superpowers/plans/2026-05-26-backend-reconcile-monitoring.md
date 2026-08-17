# Stock Reconciliation Monitoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement asynchronous stock reconciliation with a Redis-backed status monitoring system and API endpoint.

**Architecture:**
- `ReconciliationStatusService` manages a Redis Hash (`inventory:reconcile:status`) containing `status`, `progress`, `start_time`, and `updated_time`.
- `StockReconciliationServiceImpl.reconcileAll()` is moved to an asynchronous thread using `@Async`.
- `InventoryAdminController` provides a non-blocking trigger and a polling endpoint for status.

**Tech Stack:** Java 17, Spring Boot 3.4.2, Redis, PostgreSQL.

---

## File Mapping

### New Files
- `src/main/java/com/mrtripop/inventory/config/AsyncConfig.java`: Enables `@Async` processing.
- `src/main/java/com/mrtripop/inventory/models/dto/ReconciliationStatusDto.java`: Response DTO for status polling.
- `src/main/java/com/mrtripop/inventory/services/ReconciliationStatusService.java`: Interface for status management.
- `src/main/java/com/mrtripop/inventory/services/impl/ReconciliationStatusServiceImpl.java`: Redis-backed implementation of status management.
- `src/test/java/com/mrtripop/inventory/services/ReconciliationStatusServiceTest.java`: Unit tests for status tracking.

### Modified Files
- `src/main/java/com/mrtripop/inventory/constant/SuccessCode.java`: Add `INV2005_GET_RECONCILE_STATUS_SUCCESS`.
- `src/main/java/com/mrtripop/inventory/services/impl/StockReconciliationServiceImpl.java`: Add `@Async`, integrate `ReconciliationStatusService`, and add error handling.
- `src/main/java/com/mrtripop/inventory/controllers/InventoryAdminController.java`: Add the status polling endpoint.

---

## Implementation Tasks

### Task 1: Infrastructure and Constants

- [ ] **Step 1: Add SuccessCode for status endpoint**
Modify `src/main/java/com/mrtripop/inventory/constant/SuccessCode.java` to include:
```java
INV2005_GET_RECONCILE_STATUS_SUCCESS("INV2005", "Reconciliation status retrieved successfully")
```

- [ ] **Step 2: Enable Async Processing**
Create `src/main/java/com/mrtripop/inventory/config/AsyncConfig.java`:
```java
package com.mrtripop.inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
}
```

- [ ] **Step 3: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/constant/SuccessCode.java src/main/java/com/mrtripop/inventory/config/AsyncConfig.java
git commit -m "feat(inventory): enable async and add success code for reconcile monitoring"
```

### Task 2: Status Data Model

- [ ] **Step 1: Create ReconciliationStatusDto**
Create `src/main/java/com/mrtripop/inventory/models/dto/ReconciliationStatusDto.java`:
```java
package com.mrtripop.inventory.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationStatusDto {
    private String status;
    private int progress;
    private Instant startTime;
    private Instant updatedTime;
}
```

- [ ] **Step 2: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/models/dto/ReconciliationStatusDto.java
git commit -m "feat(inventory): add reconciliation status DTO"
```

### Task 3: Reconciliation Status Service

- [ ] **Step 1: Create ReconciliationStatusService interface**
Create `src/main/java/com/mrtripop/inventory/services/ReconciliationStatusService.java`:
```java
package com.mrtripop.inventory.services;

import com.mrtripop.inventory.models.dto.ReconciliationStatusDto;

public interface ReconciliationStatusService {
    void startProcess();
    void updateProgress(int percent);
    void updateStatus(String status);
    ReconciliationStatusDto getStatus();
}
```

- [ ] **Step 2: Implement ReconciliationStatusServiceImpl**
Create `src/main/java/com/mrtripop/inventory/services/impl/ReconciliationStatusServiceImpl.java`. 
Use `RedisTemplate<String, Object>` to manage a Hash at key `inventory:reconcile:status`.
Fields: `status`, `progress`, `start_time`, `updated_time`.
Implement `startProcess()`, `updateProgress(int percent)`, `updateStatus(String status)`, and `getStatus()`.
Ensure all Redis operations are wrapped in try-catch to prevent reconciliation failure if Redis is down.

- [ ] **Step 3: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/services/ReconciliationStatusService.java src/main/java/com/mrtripop/inventory/services/impl/ReconciliationStatusServiceImpl.java
git commit -m "feat(inventory): implement redis-backed reconciliation status service"
```

### Task 4: Status Service Unit Tests

- [ ] **Step 1: Write failing tests for ReconciliationStatusService**
Create `src/test/java/com/mrtripop/inventory/services/ReconciliationStatusServiceTest.java`.
Test cases:
- `startProcess` sets status to PROCESSING, progress to 0, and timestamps.
- `updateProgress` updates progress and `updated_time`.
- `updateStatus` updates status and `updated_time`.
- `getStatus` returns the correct DTO from Redis.

- [ ] **Step 2: Run tests to verify they pass**
Run: `./mvnw test -Dtest=ReconciliationStatusServiceTest`

- [ ] **Step 3: Commit**
```bash
git add src/test/java/com/mrtripop/inventory/services/ReconciliationStatusServiceTest.java
git commit -m "test(inventory): add unit tests for reconciliation status service"
```

### Task 5: Asynchronous Reconciliation Logic

- [ ] **Step 1: Modify StockReconciliationServiceImpl for Async and Status**
Modify `src/main/java/com/mrtripop/inventory/services/impl/StockReconciliationServiceImpl.java`:
- Inject `ReconciliationStatusService`.
- Annotate `reconcileAll()` with `@Async`.
- At start of `reconcileAll()`: call `statusService.startProcess()`.
- In the `do-while` loop:
    - Calculate total batches (if possible) or use an estimate to update progress.
    - Call `statusService.updateProgress(percent)` after processing each page.
- Wrap the loop in a `try-catch (Exception e)`:
    - On success: call `statusService.updateStatus("COMPLETED")`.
    - On failure: call `statusService.updateStatus("FAILED")`.

- [ ] **Step 2: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/services/impl/StockReconciliationServiceImpl.java
git commit -m "feat(inventory): make reconciliation async and add status tracking"
```

### Task 6: Status Polling Endpoint

- [ ] **Step 1: Add status endpoint to InventoryAdminController**
Modify `src/main/java/com/mrtripop/inventory/controllers/InventoryAdminController.java`:
- Inject `ReconciliationStatusService`.
- Add `@GetMapping("/reconcile/status")` method.
- Call `statusService.getStatus()`.
- Wrap in `ResponseBody` using `SuccessCode.INV2005_GET_RECONCILE_STATUS_SUCCESS`.

- [ ] **Step 2: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/controllers/InventoryAdminController.java
git commit -m "feat(inventory): add endpoint to poll reconciliation status"
```

### Task 7: End-to-End Integration Tests

- [ ] **Step 1: Write Integration Test for Monitoring Flow**
Create or modify a test to verify:
1. Call `POST /api/v1/inventory/admin/reconcile`.
2. Immediately call `GET /api/v1/inventory/admin/reconcile/status` $\rightarrow$ expect `PROCESSING`.
3. Poll status until `COMPLETED` or `FAILED`.
4. Verify `progress` transitions from 0 toward 100.

- [ ] **Step 2: Run and verify**
Run: `./mvnw test`

- [ ] **Step 3: Final Commit**
```bash
git add .
git commit -m "test(inventory): add integration tests for reconcile monitoring"
```
