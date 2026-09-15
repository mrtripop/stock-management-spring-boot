---
name: partial-return-refund
description: Design for allowing staff to partially return items from a completed invoice, restocking to the original batch and adjusting invoice totals.
date: 2026-09-15
status: proposed
---

# Partial Return & Refund Design

## 1. Purpose

Pharmacy staff need to return some (not necessarily all) items from a completed sale — e.g. a
patient brings back 2 of 10 dispensed tablets. Today `InvoiceServiceImpl.voidInvoice()` only
supports reversing an entire invoice: it loops every `InvoiceItem`, calls
`batchService.restoreStock()` for the full original quantity of each, and marks the whole invoice
`VOIDED`. There is no way to return a subset of items or a partial quantity of one item while
keeping the rest of the sale intact.

This feature adds partial, per-line returns against a `COMPLETED` invoice: selected quantities are
restocked to the batch they were sold from, and the invoice's monetary totals are reduced
accordingly. "Refund" is bookkeeping only — there is no payment gateway anywhere in this codebase
(`Invoice` tracks `totalAmount`/`patientOwed`/`insuranceClaimAmount`, nothing more), so a refund
here means adjusting those fields, not calling out to a payment provider.

## 2. Scope

**In scope:**
- Returning any quantity ≤ remaining (unreturned) quantity of one or more lines on a `COMPLETED`
  invoice, in a single request.
- Restocking returned quantity back to the original `Batch`/`StoreStock` via the existing
  `BatchService.restoreStock(storeId, batchId, quantity)`.
- Reducing `Invoice.totalAmount`/`patientOwed`/`insuranceClaimAmount` proportionally.
- A `Return`/`ReturnItem` audit trail: who (via inherited `AuditEntity` timestamps), when, why,
  how much, against which invoice item.

**Out of scope (explicit non-goals):**
- A "damaged/do not restock" path — every returned unit always goes back to `AVAILABLE` stock on
  its original batch.
- A return time-window/eligibility policy (e.g. "within 7 days").
- Any payment gateway integration — refund is bookkeeping only.
- Editing or cancelling a `Return` once created.
- A new `InvoiceStatus` value — an invoice with returns stays `COMPLETED`; "how much has been
  returned" is always derived from `ReturnItem` rows, never stored as invoice state.

## 3. Architecture & Components

Everything lives in the existing `com.mrtripop.transaction` package, alongside `Invoice`. No
changes to `com.mrtripop.inventory` — `BatchService.restoreStock()` is reused exactly as-is, called
once per returned line instead of once per invoice.

```
transaction/
├── models/db/
│   ├── Return.java            (new)
│   ├── ReturnItem.java        (new)
│   └── ReturnReason.java      (new enum)
├── models/dto/
│   ├── CreateReturnRequest.java     (new)
│   ├── ReturnItemRequest.java       (new)
│   ├── ReturnDto.java               (new)
│   └── ReturnItemDto.java           (new)
├── repository/
│   ├── ReturnRepository.java        (new)
│   └── ReturnItemRepository.java    (new)
├── component/
│   └── ReturnMapper.java            (new, MapStruct — mirrors InvoiceMapper)
├── services/
│   ├── ReturnService.java           (new interface)
│   └── impl/ReturnServiceImpl.java  (new)
├── controllers/
│   └── ReturnController.java        (new)
└── constant/
    ├── ErrorCode.java     (add 4 entries: TXN4012-TXN4015)
    └── SuccessCode.java   (add entries for return endpoints)
```

`ReturnServiceImpl` depends on `InvoiceRepository`, `InvoiceItemRepository`, `ReturnRepository`,
`ReturnItemRepository`, `BatchService`, `AuditService`, `ReturnMapper` — the same collaborator
shape `InvoiceServiceImpl` already has for `voidInvoice()`.

## 4. Data Model

### `Return` (table `returns`, extends `AuditEntity`)

```java
@Id @GeneratedValue(strategy = SEQUENCE) Long id;

@ManyToOne(fetch = LAZY) @JoinColumn(name = "invoice_id", nullable = false)
Invoice invoice;

@Enumerated(STRING) @Column(name = "reason", nullable = false, length = 30)
ReturnReason reason;

@Column(name = "total_refund_amount", nullable = false, precision = 10, scale = 2)
BigDecimal totalRefundAmount;
```

No `status` field: a `Return` is an immutable financial record once created (matches the "no
editing/cancelling" non-goal). `AuditEntity` already provides `createdAt`/`updatedAt`, so no
separate "processed by" field is needed for a first cut — if a "who processed this return" reporting
need shows up later, it can be added then (YAGNI).

Index: `idx_returns_invoice_id` on `invoice_id` (mirrors `idx_invoice_items_invoice_id`).

### `ReturnItem` (table `return_items`)

```java
@Id @GeneratedValue(strategy = SEQUENCE) Long id;

@ManyToOne(fetch = LAZY) @JoinColumn(name = "return_id", nullable = false)
Return parentReturn;   // `return` is a reserved word — see naming note below

@ManyToOne(fetch = LAZY) @JoinColumn(name = "invoice_item_id", nullable = false)
InvoiceItem invoiceItem;

@Column(name = "quantity", nullable = false)
Long quantity;

@Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
BigDecimal refundAmount;
```

Index: `idx_return_items_return_id` on `return_id`, `idx_return_items_invoice_item_id` on
`invoice_item_id` (the second is queried directly by the "already returned" check below).

**Naming note:** `return` is a Java reserved word, so the field/getter can't be named `return`;
use `parentReturn` (getter `getParentReturn()`) for the `Return` back-reference on `ReturnItem`.

### `ReturnReason` (new enum)

```java
public enum ReturnReason { DAMAGED, EXPIRED, WRONG_ITEM, CUSTOMER_CHANGED_MIND, OTHER }
```

## 5. Business Logic (`ReturnServiceImpl.createReturn`)

```java
@Transactional(rollbackFor = ApplicationException.class)
public ReturnDto createReturn(Long invoiceId, CreateReturnRequest request)
```

1. Load `Invoice` by id (else `INVOICE_NOT_FOUND`, reusing the existing shared error code).
2. If `invoice.getStatus() != InvoiceStatus.COMPLETED` → throw `INVOICE_NOT_COMPLETED`
   (`HttpStatus.CONFLICT`, matching how `INVOICE_ALREADY_COMPLETED`/`INVOICE_ALREADY_VOIDED` are
   surfaced today).
3. For each line in `request.getItems()`:
   a. Load `InvoiceItem` by id, verify `invoiceItem.getInvoice().getId().equals(invoiceId)` (else
      `INVOICE_ITEM_NOT_FOUND`).
   b. `alreadyReturned = returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())`
      (new `@Query`, same `COALESCE(SUM(...), 0)` pattern as
      `InvoiceItemRepository.sumQuantityByInvoiceIds`).
   c. If `request line quantity > invoiceItem.getQuantity() - alreadyReturned` → throw
      `RETURN_QUANTITY_EXCEEDS_AVAILABLE`.
   d. Compute this line's refund split, proportional to the returned fraction of the item. To avoid
      compounding rounding error from computing an intermediate fraction, each amount is derived
      directly as `(itemAmount * quantity) / originalQuantity`, done as a single `BigDecimal`
      `multiply().divide(..., 2, HALF_UP)` (matching `InvoiceServiceImpl.create()`'s existing
      rounding pattern) rather than pre-computing a separate fraction value:
      - `refundAmount = invoiceItem.getUnitPrice() * quantity` (exact — `unitPrice` is a per-unit
        amount already, no division needed)
      - `refundPatientOwed = invoiceItem.getPatientOwed() * quantity / invoiceItem.getQuantity()`
      - `refundInsuranceClaim = invoiceItem.getInsuranceClaimAmount() * quantity / invoiceItem.getQuantity()`
   e. `batchService.restoreStock(invoice.getStore().getId(), invoiceItem.getBatch().getId(), quantity)`.
4. Sum all lines' `refundAmount`/`refundPatientOwed`/`refundInsuranceClaim`. Build and save `Return`
   + `ReturnItem`s.
5. Reduce and save `Invoice`: `totalAmount -= sum(refundAmount)`, `patientOwed -=
   sum(refundPatientOwed)`, `insuranceClaimAmount -= sum(refundInsuranceClaim)`. Saving triggers the
   existing `@Version` optimistic-lock check on `Invoice`, which is what protects two concurrent
   returns on the same invoice from racing each other's totals — no new locking mechanism needed.
6. `auditService.recordAudit("RETURN", "Invoice", invoiceId, oldTotalAmount, newTotalAmount)` —
   same call shape `recordAudit` already uses for `CREATE`/`COMPLETE`/`VOID`.
7. Whole method is one `@Transactional` block: if any line fails validation or `restoreStock` throws,
   nothing commits — same atomicity `voidInvoice()`'s loop already relies on.

All money math uses `BigDecimal`, scale 2, `RoundingMode.HALF_UP` — matching every existing
monetary calculation in `InvoiceServiceImpl`.

## 6. API

Following `api-design.md`'s nesting rule (max 2 levels) and this domain's existing prefix
`/api/v1/transaction/invoices`:

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/transaction/invoices/{invoiceId}/returns` | Create a return (one or more lines) |
| `GET` | `/api/v1/transaction/invoices/{invoiceId}/returns` | List returns for an invoice (paginated, `BaseQueryParams`) |
| `GET` | `/api/v1/transaction/returns/{returnId}` | Get a single return |

`CreateReturnRequest`:
```java
@NotNull ReturnReason reason;
@NotEmpty @Valid List<ReturnItemRequest> items;
```
`ReturnItemRequest`:
```java
@NotNull @Min(1) Long invoiceItemId;
@NotNull @Min(1) Long quantity;
```

Controller follows `InvoiceController`'s exact shape: `@Slf4j @RestController
@RequiredArgsConstructor @RequestMapping(...) @Validated`, `@PathVariable @Min(1) Long invoiceId`,
`@Valid @RequestBody`, responses wrapped in `ResponseBody.builder()...toResponseEntity(...)` with
codes from a new `SuccessCode` block (`TXN20xx` continuing the existing numbering), `201 CREATED`
for the `POST`, `200 OK` for both `GET`s. No business logic in the controller — it only calls
`ReturnService`.

## 7. Error Handling

New entries appended to `transaction/constant/ErrorCode.java` (continuing the existing `TXN40xx`
sequence, which currently ends at `TXN4011`):

```java
INVOICE_NOT_COMPLETED("TXN4012", "Invoice must be completed before it can be returned"),
RETURN_QUANTITY_EXCEEDS_AVAILABLE("TXN4013", "Return quantity exceeds the remaining returnable quantity"),
INVOICE_ITEM_NOT_FOUND("TXN4014", "Invoice item not found"),
RETURN_NOT_FOUND("TXN4015", "Return not found"),
```

`INVOICE_NOT_COMPLETED` → `HttpStatus.CONFLICT` (same status class as the other invoice-state
errors). `RETURN_QUANTITY_EXCEEDS_AVAILABLE` → `HttpStatus.BAD_REQUEST`. `INVOICE_ITEM_NOT_FOUND` /
`RETURN_NOT_FOUND` → `HttpStatus.NOT_FOUND`. Existing `INVOICE_NOT_FOUND` is reused, not
duplicated.

## 8. Testing Plan

Unit tests for `ReturnServiceImpl` (`@Nested` per scenario group, AAA, fixtures per
`testing-style.md`, mirroring `InvoiceServiceImplTest`'s structure):

- **Happy path — single line partial return:** quantity < invoice item quantity → `restoreStock`
  called once with the right batch/quantity, `Invoice` totals reduced by the expected amount,
  `Return`/`ReturnItem` persisted with correct `refundAmount`.
- **Happy path — multi-line return in one request:** two lines returned together → both restocked,
  invoice totals reduced by the sum.
- **Cumulative partial returns:** two separate `createReturn` calls against the same invoice item,
  second one's "already returned" check correctly accounts for the first.
- **Rejects return on non-`COMPLETED` invoice** (`PENDING` and `VOIDED` cases) →
  `INVOICE_NOT_COMPLETED`.
- **Rejects over-quantity return** (quantity > remaining) → `RETURN_QUANTITY_EXCEEDS_AVAILABLE`.
- **Rejects unknown invoice item / invoice item belonging to a different invoice** →
  `INVOICE_ITEM_NOT_FOUND`.
- **Refund math / rounding correctness** for a line with insurance coverage split (non-zero
  `insuranceCoveragePercent`).
- **`restoreStock` failure rolls back the whole return** (no `Return`/`ReturnItem` persisted, no
  invoice total change) — verifies the `@Transactional` boundary.

Controller test (`ReturnControllerTest` or `@WebMvcTest`, mirroring existing controller test
conventions): request validation (`@NotEmpty items`, `@Min(1)` fields), successful `201`/`200`
responses shaped by `ResponseBody`.

## 9. Impact Analysis

- **Touches:** `transaction` package only (new files) + two additive entries in the domain's
  existing `ErrorCode`/`SuccessCode` enums. No changes to `Invoice`, `InvoiceItem`,
  `InvoiceServiceImpl`, `BatchService`, or anything in `inventory`/`clinical`.
- **New tables:** `returns`, `return_items` — additive migration, no existing table altered.
- **Risk:** LOW-MEDIUM. The only shared mutable state touched is `Invoice` (via its existing
  `@Version` field) and `StoreStock`/`Batch` quantities via the already-tested
  `BatchService.restoreStock()` — no new concurrency primitive introduced. Gitnexus `impact`
  analysis should still be run on `InvoiceRepository`, `InvoiceItemRepository`, and
  `BatchService.restoreStock` before implementation, since this is new code calling existing
  symbols upstream.
