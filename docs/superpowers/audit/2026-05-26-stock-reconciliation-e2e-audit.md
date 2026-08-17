# E2E Audit Guide: Stock Reconciliation Feature

This document provides the necessary context and test vectors for an audit agent to verify the end-to-end (E2E) functionality of the Stock Reconciliation feature across the backend and frontend.

## 1. Feature Overview
The Stock Reconciliation Engine ensures data integrity by reconciling the `Batch.quantity` field with the actual sum of `StoreStock` quantities for that batch. This is a high-privilege administrative action.

## 2. Technical Specifications

### Backend API
- **Trigger Endpoint:** `POST /api/v1/inventory/admin/reconcile`
  - **Auth:** `ADMIN` role required.
  - **Behavior:** Returns `INV2004` immediately; starts asynchronous reconciliation process.
- **Status Endpoint:** `GET /api/v1/inventory/admin/reconcile/status`
  - **Auth:** `ADMIN` role required.
  - **Expected Responses:**
    - `status: "PROCESSING"`: Process is running.
    - `status: "COMPLETED"`: Process finished successfully.
    - `status: "FAILED"`: Process encountered an error.
    - `status: "IDLE"`: No process active.

### Frontend UI (`Inventory` page $\rightarrow$ `Tasks` tab)
- **Trigger Button:** "Reconcile Stock" (Admin only).
- **Confirmation:** `ConfirmationDialog` must appear before execution.
- **Feedback:**
  - Success toast on trigger.
  - Persistent `AlertBanner` visible only during `PROCESSING` state.
  - Button disabled during `PROCESSING` state.
- **Monitoring:** Polls status endpoint every 10 seconds.

## 3. E2E Test Scenarios

### Scenario 1: Happy Path (Admin)
1. **Login** as user with `ADMIN` role.
2. **Navigate** to `Inventory` $\rightarrow$ `Tasks`.
3. **Action:** Click "Reconcile Stock".
4. **Verify:** `ConfirmationDialog` appears.
5. **Action:** Confirm action.
6. **Verify:** 
   - Success toast appears.
   - "In Progress" banner appears.
   - "Reconcile Stock" button becomes disabled.
7. **Verify (Backend):** Check API `/inventory/admin/reconcile/status` returns `PROCESSING`.
8. **Verify (Data):** Ensure `Batch.quantity` is updated to match $\sum StoreStock$.
9. **Verify (Completion):** Wait for process to finish $\rightarrow$ Banner disappears $\rightarrow$ Button becomes enabled.

### Scenario 2: Role-Based Access Control (Non-Admin)
1. **Login** as user with `PHARMACIST` or `EMPLOYEE` role.
2. **Navigate** to `Inventory` $\rightarrow$ `Tasks`.
3. **Verify:** "Reconcile Stock" button is disabled.
4. **Verify:** Tooltip shows *"Administrator privileges required to trigger stock reconciliation."*
5. **Verify (API):** Attempt manual `POST` to `/inventory/admin/reconcile` $\rightarrow$ Expect `403 Forbidden`.

### Scenario 3: Concurrent Trigger Prevention
1. **Login** as Admin.
2. **Trigger** reconciliation.
3. **Verify:** Button is disabled while banner is visible.
4. **Action:** Attempt to trigger another reconciliation via API while the first is `PROCESSING`.
5. **Verify:** System prevents concurrent runs or handles them gracefully (as per backend spec).

### Scenario 4: Error State Handling
1. **Simulate** a backend failure during reconciliation (e.g., database timeout).
2. **Verify:** Status endpoint eventually returns `FAILED`.
3. **Verify:** Frontend banner disappears (or shows error state).
4. **Verify:** Button returns to enabled state.

## 4. Verification Checklist
- [ ] UI matches design tokens for banner and buttons.
- [ ] No "flickering" during polling (proper use of TanStack Query).
- [ ] Reconciliation actually corrects data in the database.
- [ ] Audit log entries `ACTION_RECONCILIATION` are created for corrected batches.
- [ ] No console errors during the entire flow.
