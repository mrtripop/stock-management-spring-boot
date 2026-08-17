# Frontend Implementation Brief: Stock Reconciliation Trigger

## Overview
The backend has implemented a **Stock Reconciliation Engine** to prevent data drift by ensuring the `Batch.quantity` field matches the actual sum of `StoreStock` quantities. 

This process is automated daily at 2 AM, but administrators require a manual trigger for immediate reconciliation.

## Technical Specification

### 1. API Endpoint
- **URL:** `/api/v1/inventory/admin/reconcile`
- **Method:** `POST`
- **Authentication:** Requires `ADMIN` role.
- **Expected Response:**
    - **Success (200 OK):** 
      ```json
      {
        "code": "INV2004",
        "message": "Stock reconciliation triggered successfully",
        "data": null
      }
      ```
    - **Failure (403 Forbidden):** If the user is not an admin.
    - **Failure (500 Internal Server Error):** If the process fails to start.

### 2. Proposed UI Workflow
- **Location:** Inventory Administration panel (or a "System Tools" section within the Inventory module).
- **Component:** A "Reconcile Stock" button.
- **Interaction:**
    1. **Trigger:** Admin clicks "Reconcile Stock".
    2. **Confirmation:** Show a confirmation dialog: *"Triggering a full stock reconciliation will analyze all batches and correct any quantity drifts. This may take a moment. Do you wish to proceed?"*
    3. **Execution:** Call the `POST` endpoint.
    4. **Feedback:** Display a success toast/notification upon receiving the `INV2004` success code.

## Additional Context
- **Audit Trail:** Every correction is recorded in the audit ledger (`ACTION_RECONCILIATION`). Users can verify changes via the Audit Log UI.
- **Performance:** The backend uses pagination for the reconciliation loop, meaning the API call returns quickly while the process runs asynchronously in the background.

---
**Status:** Pending Implementation
**Related Backend Task:** Stock Reconciliation Engine (Implemented)
