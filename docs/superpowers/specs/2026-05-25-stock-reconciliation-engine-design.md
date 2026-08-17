---
name: stock-reconciliation-engine
description: Design for an automated system to reconcile drift between batch totals and store stock sums.
date: 2026-05-25
status: proposed
---

# Stock Reconciliation Engine Design

## 1. Purpose
In the current implementation, `Batch.quantity` is a redundant field that can drift from the actual sum of quantities in `StoreStock`. This engine ensures that the "total quantity" of a batch is always accurate by treating the sum of store stocks as the single source of truth.

## 2. Architecture & Components

### 2.1 StockReconciliationService
The core logic for identifying and fixing stock drift.
- **`reconcileAll()`**: 
    - Iterates through all batches.
    - Calculates total stock across all stores for each batch.
    - Updates `Batch.quantity` if a discrepancy is found.
    - Records a `STOCK_RECONCILIATION` audit event.
- **`reconcileBatch(Long batchId)`**: 
    - Handles a single batch's reconciliation.
    - Uses `@Version` optimistic locking to prevent overwriting concurrent updates.

### 2.2 StockReconciliationScheduler
Automates the process.
- **Schedule**: Runs daily at 02:00 AM.
- **Trigger**: Invokes `StockReconciliationService.reconcileAll()`.

### 2.3 Admin Interface
Provides manual control for operators.
- **Endpoint**: `POST /api/inventory/reconcile`
- **Access**: Restricted to users with `ADMIN` role.
- **Action**: Immediately triggers the reconciliation process.

## 3. Data Flow & Logic

### 3.1 The Reconciliation Process
1. **Pagination**: Fetch batches in pages (e.g., 100 per page) to maintain a small memory footprint.
2. **Summation**: For each batch:
   `SELECT SUM(ss.quantity) FROM StoreStock ss WHERE ss.batch.id = :batchId`
3. **Comparison**: Compare calculated sum with `batch.getQuantity()`.
4. **Correction**: If different:
   - Set `batch.setQuantity(calculatedSum)`.
   - Save the batch (triggering `@Version` check).
5. **Audit**: Record the event via `AuditService`:
   - Event: `STOCK_RECONCILIATION`
   - Details: `{ batchId, oldQuantity, newQuantity, diff }`

### 3.2 Edge Case Handling
- **Optimistic Locking**: If a `ObjectOptimisticLockingFailureException` occurs, the engine logs the conflict and skips the specific batch, ensuring the overall loop continues.
- **Orphan Batches**: If no `StoreStock` entries exist, the sum is treated as `0` and `Batch.quantity` is corrected accordingly.
- **Performance**: Summation queries are read-only and indexed on `batch_id` for efficiency.

## 4. Verification Plan

### 4.1 Business Success (Drift Correction)
- **Setup**: `Batch(qty=100)`, `StoreStock` sum = 80.
- **Execution**: Run `reconcileAll()`.
- **Success**: `Batch.qty` becomes 80, audit record created for -20.

### 4.2 Business Neutral/Edge Cases
- **Perfect Sync**: `Batch` = 100, `StoreStock` sum = 100 $\rightarrow$ No change, no audit.
- **Orphan Batch**: `Batch` = 50, `StoreStock` sum = 0 $\rightarrow$ `Batch.qty` becomes 0.

### 4.3 Technical Resilience
- **Concurrent Update**: Trigger reconciliation $\rightarrow$ mid-process update of `Batch` $\rightarrow$ check for graceful skip via optimistic locking exception.

## 5. Impact Analysis
- **Write Load**: Only writes to `Batch` and `Audit` tables when drift is actually detected.
- **Read Load**: Read-only summation queries are lightweight due to indexing.
- **Risk**: LOW. The engine only fixes a redundant value based on existing store data.
