# POS Dispensing — Frontend Product Brief

**Date**: 2026-05-17
**Audience**: Frontend implementation agent
**Goal**: Build a POS-style dispensing UI for pharmacy staff to quickly record sales, auto-deduct inventory, and view daily summaries.

---

## Context

The backend POS dispensing feature is complete. The existing demo-ui (React 19 + Vite + Tailwind + React Query) has a Transactions page that shows a basic transaction log. There is no invoice/dispensing workflow yet. This brief covers what the frontend agent needs to build.

## Tech Stack

- React 19, Vite, Tailwind CSS 4
- React Query (TanStack Query) for data fetching
- Existing custom hooks in `demo-ui/src/lib/hooks.js`
- Existing API client in `demo-ui/src/lib/api.js` (auto snake_case ↔ camelCase)
- Existing atoms/molecules/organisms components
- Follow existing patterns from Products.jsx, Inventory.jsx, Clinical.jsx

## Pages to Build

### 1. Dispensing Page (NEW)

**Route**: `/dispensing`
**Sidebar label**: "Dispensing"

The main POS screen for pharmacy staff. Single-page workflow:

**Layout**: Two-column on desktop, stacked on mobile.
- **Left column**: Invoice item list (what's being dispensed)
- **Right column**: Item search/scan + payment summary

**Workflow**:
1. Staff searches for a product by name or brand
2. Selects a batch (shows batch number, expiry date, available quantity)
3. Sets quantity and optional insurance coverage %
4. Item is added to the invoice item list
5. Staff repeats for more items
6. Staff clicks "Dispense" to submit

**On submit**: Calls `POST /api/v1/transaction/invoices/dispense` with the assembled items. On success, shows a success confirmation with invoice ID and receipt option. On failure (e.g., insufficient stock), shows the error per item.

**Key behaviors**:
- Show running totals: total amount, patient owed, insurance claims
- Insurance coverage is a per-item slider/input (0-100%), defaulting to 0%
- Quantity must not exceed available stock for that batch
- After successful dispense, clear the form and show confirmation
- Option to void the invoice from the confirmation screen

### 2. Daily Summary (Section on Dashboard or Dispensing Page)

**API**: `GET /api/v1/transaction/invoices/daily-summary?storeId={storeId}`

A summary card/section showing today's numbers:
- Total invoices completed
- Total revenue
- Patient paid vs insurance claims
- Total items dispensed
- Voided count

Display as stat cards (reuse existing `StatCard` molecule). Date defaults to today, optional date picker for past days.

### 3. Invoice Detail View (Enhancement to Transactions Page)

**API**: `GET /api/v1/transaction/invoices/{invoiceId}`

When clicking a transaction/invoice in the Transactions page, show a detail view or drawer with:
- Invoice status (PENDING, COMPLETED, VOIDED) with badge
- Store name
- Item list: brand name, batch number, quantity, unit price, line total, insurance %
- Totals: total amount, patient owed, insurance claims
- Action buttons based on status:
  - PENDING: "Complete" and "Void" buttons
  - COMPLETED: "Void" button, "View Receipt" button
  - VOIDED: no actions (show "Voided" badge)

### 4. Void Flow

**API**: `POST /api/v1/transaction/invoices/{invoiceId}/void`

A confirmation dialog before voiding. The dialog should warn:
- For PENDING invoices: "This invoice was not completed. Void to cancel."
- For COMPLETED invoices: "Voiding will restore stock to inventory. This cannot be undone."

On success, refresh the invoice detail. On failure, show error.

---

## API Reference

### Dispense (create + complete + deduct in one call)

```
POST /api/v1/transaction/invoices/dispense
Content-Type: application/json

{
  "storeId": "uuid",
  "items": [
    {
      "brandId": "uuid",
      "batchId": 1,
      "quantity": 5,
      "insuranceCoveragePercent": 0
    }
  ]
}
```

Response: `InvoiceDto` with `status: "COMPLETED"`

### List Invoices

```
GET /api/v1/transaction/invoices?storeId={storeId}&page=1&size=20&orderBy=desc
```

Response: `Page<InvoiceDto>`

### Get Invoice Detail

```
GET /api/v1/transaction/invoices/{invoiceId}
```

Response: `InvoiceDto` with `items` array

### Complete Invoice (if created separately)

```
POST /api/v1/transaction/invoices/{invoiceId}/complete
```

### Void Invoice

```
POST /api/v1/transaction/invoices/{invoiceId}/void
```

### Daily Summary

```
GET /api/v1/transaction/invoices/daily-summary?storeId={storeId}&date=2026-05-17
```

Response:
```json
{
  "date": "2026-05-17",
  "totalInvoices": 12,
  "totalRevenue": 1500.00,
  "totalPatientPaid": 1050.00,
  "totalInsuranceClaims": 450.00,
  "totalItemsDispensed": 47,
  "voidedCount": 1
}
```

### Get Available Batches for a Brand

For the item search/selection UI, use the existing inventory API:

```
GET /api/v1/inventory/batches?brandId={brandId}&page=1&size=20&orderBy=asc
```

Returns batches with batch number, expiry date, quantity. Filter by AVAILABLE status and non-expired.

### Get Store Stock

```
GET /api/v1/inventory/stores/{storeId}/stock?page=1&size=20&orderBy=desc
```

Returns stock levels per batch per store.

---

## Data Models

### InvoiceDto
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Invoice ID |
| storeId | UUID | Store reference |
| storeName | String | Store display name |
| status | String | PENDING, COMPLETED, VOIDED |
| totalAmount | BigDecimal | Sum of all line totals |
| patientOwed | BigDecimal | Amount patient pays after insurance |
| insuranceClaimAmount | BigDecimal | Amount covered by insurance |
| items | List<InvoiceItemDto> | Invoice line items |
| createdAt | Long | Epoch millis |
| updatedAt | Long | Epoch millis |

### InvoiceItemDto
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Item ID |
| brandName | String | Brand display name |
| batchNumber | String | Batch identifier |
| quantity | Long | Quantity dispensed |
| unitPrice | BigDecimal | Price per unit |
| lineTotal | BigDecimal | unitPrice × quantity |
| patientOwed | BigDecimal | Line-level patient amount |
| insuranceClaimAmount | BigDecimal | Line-level insurance amount |
| insuranceCoveragePercent | int | 0-100 |

### DailySalesSummaryDto
| Field | Type | Notes |
|-------|------|-------|
| date | LocalDate | Summary date |
| totalInvoices | int | Completed count |
| totalRevenue | BigDecimal | Sum of totalAmount |
| totalPatientPaid | BigDecimal | Sum of patientOwed |
| totalInsuranceClaims | BigDecimal | Sum of insuranceClaimAmount |
| totalItemsDispensed | long | Sum of all item quantities |
| voidedCount | int | Voided count |

---

## Implementation Notes

1. **Follow existing patterns**: Look at `Products.jsx` and `Inventory.jsx` for page structure, `useCreateMutation` for API calls, `DataTable` for lists, `FormDrawer` for forms.
2. **API client**: All calls go through `demo-ui/src/lib/api.js` which handles snake_case conversion. Use the `usePostMutation`, `useQueryList`, `useQueryDetail` hooks from `demo-ui/src/lib/hooks.js`.
3. **Store ID**: The current UI doesn't have store selection. For now, use a hardcoded store ID or add a store selector. Check `Clinical.jsx` for how stores are fetched.
4. **Product search**: For finding brands to add to the invoice, search brands via the clinical catalog API. Check how the Clinical page fetches brands.
5. **Error handling**: Follow existing pattern — mutations show error toasts/notifications on failure.
6. **Sidebar**: Add "Dispensing" to the sidebar navigation in `demo-ui/src/organisms/Sidebar.jsx`.
7. **Routing**: Add `/dispensing` route to the router configuration.
