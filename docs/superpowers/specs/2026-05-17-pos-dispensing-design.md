# POS-Style Dispensing Feature Design

**Date**: 2026-05-17
**Status**: Approved
**Scope**: Connect invoice completion to inventory deduction, add quick dispense, daily summary, and void reversal

## Problem

Pharmacy staff need a POS-style dispensing workflow: record a sale, automatically deduct stock using FEFO, and get a receipt. Currently, invoice completion does not deduct inventory — the two domains operate independently. Staff must manually deduct stock after completing invoices, which is error-prone and slow.

## Design Decisions

- **Walk-in only**: No customer/patient accounts. Transactions are anonymous.
- **Reuse existing Invoice + Batch services**: Wire them together rather than building a new orchestration layer.
- **Approach**: Complete the Invoice → Inventory loop (Approach A from brainstorming).

## 1. Invoice → Inventory Integration

When `complete()` is called, iterate through invoice items and deduct stock from the specific batch each item references.

**Challenge**: The existing `BatchService.deductStock()` takes a barcode and auto-selects batches via FEFO. Invoice items already have a specific `batchId` picked during creation.

**Solution**: Add a new method `deductStockByBatch(storeId, batchId, quantity)` to `BatchService` that deducts from a specific batch — reusing the same locking and audit logic from the existing FEFO deduction, but skipping the batch selection step.

**Flow**:
```
complete(invoiceId)
  → validate status is PENDING
  → re-check stock still available for each item
  → for each InvoiceItem: deductStockByBatch(storeId, batchId, quantity)
  → set status to COMPLETED
  → audit record
```

**Modified files**:
- `BatchService` — add `deductStockByBatch(UUID storeId, Long batchId, Long quantity)`
- `BatchServiceImpl` — implement with pessimistic write lock and audit logging
- `InvoiceServiceImpl.complete()` — wire to call `deductStockByBatch` per item

## 2. Quick Dispense Endpoint

A single-call endpoint for POS speed — creates the invoice, completes it, and deducts inventory in one transaction.

**Endpoint**: `POST /api/v1/transaction/invoices/dispense`

**Flow**:
```
dispense(request)
  → validate store, brands, batches exist
  → validate stock available for each item
  → create invoice (PENDING)
  → for each item: deductStockByBatch(storeId, batchId, quantity)
  → set status to COMPLETED
  → generate receipt
  → return invoice + receipt
```

**Request**: Same `CreateInvoiceRequest` DTO — the data is identical, the difference is behavior (create + complete in one call).

**Response**: Completed `InvoiceDto` with items and receipt info.

**Error handling**: If any item fails stock deduction, the entire transaction rolls back via `@Transactional`. No partial dispensing.

**Modified files**:
- `InvoiceService` — add `dispense(CreateInvoiceRequest)`
- `InvoiceServiceImpl` — implement as combined create + complete + deduct
- `InvoiceController` — add `POST /dispense` endpoint
- `SuccessCode` — add `TXN2006_DISPENSE_IS_SUCCESS`
- `ErrorCode` — add any dispensing-specific error codes if needed

## 3. Daily Sales Summary

A single endpoint for pharmacy staff to see their day-at-a-glance numbers.

**Endpoint**: `GET /api/v1/transaction/invoices/daily-summary?storeId={storeId}&date={date}`

**Response DTO** (`DailySalesSummaryDto`):
- `date` (LocalDate)
- `totalInvoices` (int) — completed invoices count
- `totalRevenue` (BigDecimal) — sum of totalAmount
- `totalPatientPaid` (BigDecimal) — sum of patientOwed
- `totalInsuranceClaims` (BigDecimal) — sum of insuranceClaimAmount
- `totalItemsDispensed` (int) — sum of all invoice item quantities
- `voidedCount` (int) — voided invoices count

**Date parameter**: Optional, defaults to today. `@RequestParam(required = false)` with `LocalDate` parsing.

**Query**: Single JPQL aggregation query on `invoice` + `invoice_item` tables filtered by store, date range (start of day to end of day), and status. No N+1.

**Modified files**:
- `DailySalesSummaryDto` — new DTO
- `InvoiceRepository` — add aggregation query
- `InvoiceItemRepository` — add item quantity aggregation query
- `InvoiceService` — add `getDailySummary(UUID storeId, LocalDate date)`
- `InvoiceServiceImpl` — implement
- `InvoiceController` — add `GET /daily-summary` endpoint
- `SuccessCode` — add `TXN2007_GET_DAILY_SUMMARY_IS_SUCCESS`

## 4. Void Reversal (Stock Restoration)

Currently, voiding a COMPLETED invoice only changes status — stock stays deducted. Add stock restoration when voiding a completed invoice.

**Flow**:
```
voidInvoice(invoiceId)
  → if PENDING: void as-is (no stock deducted, nothing to restore)
  → if COMPLETED:
      → for each InvoiceItem: restoreStock(storeId, batchId, quantity)
      → set status to VOIDED
      → audit record
  → if VOIDED: throw already voided error (existing)
```

**New method**: `restoreStock(UUID storeId, Long batchId, Long quantity)` on `BatchService`. Increments `StoreStock.quantity` and `Batch.quantity` back, using the same pessimistic write lock pattern as deduction.

**Guard**: Only COMPLETED invoices trigger stock restoration. Once voided, stays voided.

**Audit**: Records both the void action and each stock restoration.

**Modified files**:
- `BatchService` — add `restoreStock(UUID storeId, Long batchId, Long quantity)`
- `BatchServiceImpl` — implement with locking and audit
- `InvoiceServiceImpl.voidInvoice()` — add restoration logic for COMPLETED invoices
- `ErrorCode` — add `TXN4012_STOCK_RESTORE_FAILED` if needed

## Impact Analysis

All changes are LOW risk:
- `InvoiceServiceImpl` — 0 upstream callers outside the controller
- `BatchService` — 3 direct dependents (impl + controller), adding methods only
- `InvoiceService` — 3 direct dependents (impl + controller), adding methods only

No existing method signatures change. All changes are additive (new methods) or behavioral (wiring in `complete()` and `voidInvoice()`).
