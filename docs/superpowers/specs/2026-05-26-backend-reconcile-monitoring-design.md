# Design Spec: Stock Reconciliation Monitoring Mechanic

## Context
The Stock Reconciliation Engine currently performs a synchronous operation when triggered via the admin endpoint. This blocks the request and provides no feedback on progress for large datasets. To improve UX, the process must be made asynchronous and a monitoring mechanism provided to the frontend.

## Requirements

### 1. Asynchronous Execution
- The `StockReconciliationServiceImpl.reconcileAll()` method must be executed asynchronously.
- The trigger endpoint `POST /api/v1/inventory/admin/reconcile` must return a success response immediately after starting the process.

### 2. Status Tracking (Redis)
- Use a Redis Hash to store the current state of the reconciliation process.
- **Key:** `inventory:reconcile:status`
- **Fields (SNAKE_CASE):**
    - `status`: `IDLE`, `PROCESSING`, `COMPLETED`, `FAILED`
    - `progress`: Integer (0-100) representing the percentage of batches processed.
    - `start_time`: ISO-8601 timestamp of when the process began.
    - `updated_time`: ISO-8601 timestamp of the last state or progress change.

### 3. Status API Endpoint
- **URL:** `GET /api/v1/inventory/admin/reconcile/status`
- **Authentication:** `@PreAuthorize("hasRole('ADMIN')")`
- **Response Body:** `ResponseBody<ReconciliationStatusDto>`
- **Success Code:** `INV2005` (defined in `SuccessCode`)
- **DTO Fields:** `status`, `progress`, `start_time`, `updated_time`.

## Implementation Design

### Components

#### `ReconciliationStatusService` (New)
A thin wrapper around `RedisTemplate` to manage the reconciliation state.
- `updateStatus(Status status)`: Updates `status` and `updated_time`.
- `updateProgress(int percent)`: Updates `progress` and `updated_time`.
- `startProcess()`: Sets `status` to `PROCESSING`, initializes `progress` to 0, and sets `start_time` and `updated_time`.
- `getStatus()`: Retrieves all fields from the Redis hash and maps them to `ReconciliationStatusDto`.

#### `StockReconciliationServiceImpl` (Modified)
- Annotate `reconcileAll()` with `@Async`.
- Wrap the processing loop in a `try-catch` block.
- Call `statusService.startProcess()` at the beginning of `reconcileAll()`.
- Calculate progress based on `batchPage` total vs processed and call `statusService.updateProgress(percent)`.
- Call `statusService.updateStatus(COMPLETED)` on success and `statusService.updateStatus(FAILED)` in the catch block.

### Error Handling
- **Process Crashes:** Unhandled exceptions in the async thread will be caught by the global try-catch, transitioning the status to `FAILED`.
- **Redis Downtime:** Redis operations will be wrapped in try-catch blocks to ensure that a cache failure does not interrupt the actual stock reconciliation logic.
- **Stale States:** Each new trigger call to `reconcileAll()` will reset the state, clearing any previous `COMPLETED` or `FAILED` markers.

## Testing Strategy

### Unit Tests
- `ReconciliationStatusServiceTest`: Verify correct Redis Hash operations (HSET/HGETALL) and mapping to DTO.

### Integration Tests
- **Happy Path:** Trigger reconciliation $\rightarrow$ Verify status is `PROCESSING` $\rightarrow$ Poll until `COMPLETED`.
- **Progress Tracking:** Verify that the `progress` field increases as batches are processed.
- **Failure Path:** Mock a failure in `reconcileBatch` $\rightarrow$ Verify status transitions to `FAILED`.
- **Concurrency:** Verify that multiple trigger calls result in the latest process taking precedence (resetting the state).

---
**Status:** Approved for Implementation
**Created:** 2026-05-26
