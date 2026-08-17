# Backend Spec: Stock Reconciliation Monitoring Mechanic

## Context
The Stock Reconciliation Engine currently provides a trigger endpoint that returns immediately while the process runs asynchronously in the background. To provide a good UX, the frontend needs a way to monitor this process.

## Requirements

### 1. Status Tracking
The system must track the current state of the reconciliation process globally.
- **Possible States:** `IDLE`, `PROCESSING`, `COMPLETED`, `FAILED`.
- **Persistence:** Use a shared state (e.g., Redis key or a dedicated `system_status` table) to ensure the status is consistent across multiple application instances.

### 2. Status API Endpoint
Implement a new endpoint to allow the frontend to poll the current status.

- **URL:** `GET /api/v1/inventory/admin/reconcile/status`
- **Authentication:** Requires `ADMIN` role.
- **Response Body:**
  ```json
  {
    "code": "INV2005",
    "message": "Current reconciliation status",
    "data": {
      "status": "PROCESSING", 
      "progress": 45,         // Optional: current percentage of batches processed
      "startTime": "2026-05-26T10:00:00Z"
    }
  }
  ```

### 3. Lifecycle Management
- **Trigger:** When `POST /api/v1/inventory/admin/reconcile` is called, the status must immediately transition to `PROCESSING`.
- **Completion:** Upon successful completion of the reconciliation loop, transition status to `COMPLETED`.
- **Failure:** If the process crashes or throws an unhandled exception, transition status to `FAILED`.
- **Reset:** The status should return to `IDLE` after:
  - A manual reset.
  - A timeout (e.g., 1 hour after completion).
  - A new reconciliation trigger.

---
**Status:** Proposed for Backend Implementation
**Created:** 2026-05-26
