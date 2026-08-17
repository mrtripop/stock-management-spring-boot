# Design Spec: Stock Reconciliation UI Trigger

## Overview
Administrators need a way to manually trigger the Stock Reconciliation Engine to ensure `Batch.quantity` matches the sum of `StoreStock` across the system.

## UI/UX Design

### Placement
- **Location:** `Inventory` page $\rightarrow$ `Tasks` tab.
- **Integration:** Added to the `PageHeader` actions alongside the "Run Scan" button.

### Component States
- **Idle (Admin):** Primary button "Reconcile Stock".
- **Idle (Non-Admin):** Disabled button with tooltip: *"Administrator privileges required to trigger stock reconciliation."*
- **Loading:** Button shows spinner during API request.
- **Active Process:** A "Reconciliation in Progress" banner is displayed at the top of the page while the backend process is running.

### User Flow
1. Admin clicks **"Reconcile Stock"**.
2. **ConfirmationDialog** appears: *"Triggering a full stock reconciliation will analyze all batches and correct any quantity drifts. This may take a moment. Do you wish to proceed?"*
3. On confirmation, call `POST /api/v1/inventory/admin/reconcile`.
4. On success (`INV2004`):
   - Show success toast.
   - Display "In Progress" banner.
   - Begin polling the status endpoint.
5. On completion/failure:
   - Remove "In Progress" banner.
   - Show final result toast.

## Technical Implementation

### API Integration
- **Trigger:** `POST /api/v1/inventory/admin/reconcile`
- **Monitoring:** `GET /api/v1/inventory/admin/reconcile/status` (polling every 10s).

### Design Tokens
Strict adherence to `theme.js`:
- **Button:** `colors.primary`
- **Banners:** `colors.warningSubtle` (bg), `colors.warning` (accent), `colors.warningText` (text).
- **Disabled:** `colors.textMuted`

---
**Status:** Finalized
**Approved:** 2026-05-26
