---
name: automated-par-level-reorder
description: Design for automated par-level stock replenishment, consolidating low-stock items into draft reorders during daily scans and closing the loop with integrated receiving and task resolution.
date: 2026-09-21
status: proposed
---

# Automated Par-Level Reorder Design

## 1. Purpose

Today, [`ActionQueueService.runReorderScan()`](file:///src/main/java/com/mrtripop/inventory/services/impl/ActionQueueServiceImpl.java#L145) detects when a brand's available stock at a store falls below `StoreProduct.reorderThreshold` and creates a `REORDER_NEEDED` alert task. However, this is purely a passive notification:
- Staff must manually calculate how much to reorder.
- There is no concept of a target par level (how much stock the store *should* hold).
- Purchasing is fragmented and manual.
- When new stock arrives, staff enter batches manually, but the open action queue tasks remain lingering until someone manually clicks resolve.

This feature implements **Automated Par-Level Replenishment**:
1. **Target Par Level:** Configurable `targetStock` on `StoreProduct` (defaulting to `2 * reorderThreshold` if null).
2. **Daily Batch Consolidation:** The scheduled 6:00 AM scan aggregates all low-stock items for a store into a single consolidated `DRAFT` [`Reorder`](#data-model) with computed suggested quantities (`targetStock - currentStock`).
3. **Manager Approval Workflow:** Store managers review, optionally adjust quantities (e.g. for pack size constraints), and approve the order (`APPROVED`).
4. **Integrated Receiving Loop:** When goods arrive, staff receive against the order lines. The system creates the batch, updates store stock, transitions the reorder to `RECEIVED`, and automatically resolves the corresponding `REORDER_NEEDED` action queue tasks.

---

## 2. Scope

### In Scope
- Adding `targetStock` to `StoreProduct`.
- New `Reorder` and `ReorderItem` entities with statuses: `DRAFT`, `APPROVED`, `RECEIVED`, `CANCELLED`.
- Scheduled daily consolidation of low-stock products into store-level draft reorders during `ActionQueueEngine` runs.
- Prevent duplicate ordering if a brand is already part of an active `APPROVED` reorder for that store.
- Manager approval endpoint (`PATCH /api/v1/inventory/reorders/{id}/approve`) with optional quantity adjustments.
- Receiving endpoint (`POST /api/v1/inventory/reorders/{id}/receive`) creating batches/stock, updating order status, and resolving linked tasks.
- Cancellation endpoint (`PATCH /api/v1/inventory/reorders/{id}/cancel`).
- Full audit ledger logging for state transitions (`AuditLedger`).
- Unit and slice tests (`@Nested`, AAA pattern) for service, engine, and controller layers.

#### Mandatory Engineering Skills & Standards Directive
> [!IMPORTANT]
> **MANDATORY FOR ALL SUBAGENTS AND IMPLEMENTERS:**
> You MUST read the **entire content** of each standard and reference file listed below using `view_file` before writing, modifying, or testing any code. Reading only summaries or skipping full-text reading is strictly prohibited.
>
> 1. **Code Style & Java Dialect:**
>    - [`code-style-standards/SKILL.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/code-style-standards/SKILL.md)
>    - [`code-style-standards/references/java-spring.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/code-style-standards/references/java-spring.md) (Notice rules on `@Comment` on every column, `@ToString(exclude = ...)`, no magic values, text blocks for SQL/JPQL)
> 2. **Testing Standards & Java Dialect:**
>    - [`testing-standards/SKILL.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/testing-standards/SKILL.md)
>    - [`testing-standards/references/java-spring.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/testing-standards/references/java-spring.md) (Notice rules on `@DisplayName` at class/nested/method levels, Fixture pattern with input/derived constants, explicit AAA structure)
>    - [`testing-principles/SKILL.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/testing-principles/SKILL.md)
> 3. **Performance Standards & Java Dialect:**
>    - [`performance-standards/SKILL.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/performance-standards/SKILL.md) (Rule 1: Never issue a query inside a loop - eliminate N+1 queries)
>    - [`performance-standards/references/java-spring.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/performance-standards/references/java-spring.md) (`JOIN FETCH`, indexing, batch operations, short transactions)
> 4. **Security & Foundation Standards:**
>    - [`security-standards/SKILL.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/security-standards/SKILL.md)
>    - [`foundation-principles/SKILL.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/foundation-principles/SKILL.md)

### Out of Scope (Explicit Non-Goals)
- **No E2E Tests:** Verification relies strictly on focused unit tests (`ReorderServiceImplTest`, `ActionQueueServiceImplTest`) and controller slice tests (`@WebMvcTest`), avoiding heavy end-to-end integration environments.
- **External Supplier EDI / API Integration:** Reorders represent internal procurement requisitions / PO drafts. Direct dispatch to third-party vendor APIs is out of scope.
- **Payment Gateway / Accounts Payable:** Financial ledger tracking is limited to quantity tracking and unit prices; vendor invoice settlement is out of scope.
- **Partial Receiving:** Goods are received in a single full delivery transaction per approved order for this iteration (YAGNI).

---

## 3. Architecture & Components

All new components reside in `com.mrtripop.inventory`, with a minor column addition to `StoreProduct` in `com.mrtripop.clinical`.

```
clinical/
└── models/db/
    └── StoreProduct.java              (add targetStock column)

inventory/
├── models/db/
│   ├── Reorder.java                   (new entity)
│   ├── ReorderItem.java               (new entity)
│   └── ReorderStatus.java             (new enum: DRAFT, APPROVED, RECEIVED, CANCELLED)
├── models/dto/
│   ├── ReorderDto.java                (new response DTO)
│   ├── ReorderItemDto.java            (new response DTO)
│   ├── ApproveReorderRequest.java     (new request DTO)
│   ├── ApproveReorderItemRequest.java (new request DTO)
│   ├── ReceiveReorderRequest.java     (new request DTO)
│   ├── ReceiveReorderItemRequest.java (new request DTO)
│   └── CancelReorderRequest.java      (new request DTO)
├── repository/
│   ├── ReorderRepository.java         (new Spring Data JPA repository)
│   └── ReorderItemRepository.java     (new Spring Data JPA repository)
├── component/
│   └── ReorderMapper.java             (new MapStruct mapper)
├── services/
│   ├── ReorderService.java            (new interface)
│   └── impl/ReorderServiceImpl.java   (new implementation)
├── controllers/
│   └── ReorderController.java         (new REST controller)
├── constant/
│   ├── ErrorCode.java                 (add INV4020 - INV4024)
│   └── SuccessCode.java               (add INV2020 - INV2024)
└── services/impl/
    └── ActionQueueServiceImpl.java    (enhance runReorderScan to generate draft reorders)
```

Collaborators:
- `ReorderServiceImpl` collaborates with `ReorderRepository`, `ReorderItemRepository`, `BatchService`, `StoreStockRepository`, `TaskRepository`, `AuditService`, and `ReorderMapper`.

---

## 4. Data Model

All entity column definitions strictly adhere to the `code-style-standards` JPA Entity Columns rule ([`references/java-spring.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/code-style-standards/references/java-spring.md#L148)):
- Every column and foreign key relationship (`@ManyToOne`) defines an explicit Hibernate `@Comment` describing its business intent and destination.
- Sequence generators follow the repository standard (`reorders_sequence`, `reorder_items_sequence`, `allocationSize = 1`).
- Lazy-loaded associations are excluded from `@ToString` to avoid recursion and `LazyInitializationException`.
- Explicit table indexes for foreign keys and filter predicates.

### `StoreProduct` Modification (Table `store_products`)
```java
@Comment("Target inventory par-level to restock up to; if null, defaults to 2x reorderThreshold")
@Column(name = "target_stock")
private Long targetStock;
```

### `ReorderStatus` (Enum)
```java
public enum ReorderStatus {
    DRAFT,
    APPROVED,
    RECEIVED,
    CANCELLED
}
```

### `Reorder` (Table `reorders`, extends `AuditEntity`)
```java
@Entity
@Table(
    name = "reorders",
    indexes = {
        @Index(name = "idx_reorders_store_status", columnList = "store_id, status"),
        @Index(name = "idx_reorders_reorder_number", columnList = "reorder_number", unique = true)
    }
)
@SuperBuilder
@Getter
@Setter
@ToString(exclude = {"store", "items"})
@NoArgsConstructor
@AllArgsConstructor
public class Reorder extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reorders_sequence")
    @SequenceGenerator(
        name = "reorders_sequence",
        sequenceName = "reorders_sequence",
        allocationSize = 1
    )
    private Long id;

    @Comment("Unique human-readable order number for tracking, e.g. RO-20260921-0001")
    @Column(name = "reorder_number", nullable = false, length = 50, unique = true)
    private String reorderNumber;

    @Comment("FK to stores; the store location this replenishment order is placed for")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Comment("Current lifecycle status of the reorder (DRAFT, APPROVED, RECEIVED, CANCELLED)")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReorderStatus status = ReorderStatus.DRAFT;

    @Comment("Manager or system notes regarding this reorder requisition")
    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "reorder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReorderItem> items = new ArrayList<>();

    @Comment("Optimistic locking version counter to prevent concurrent modification conflicts")
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
```

### `ReorderItem` (Table `reorder_items`, extends `AuditEntity`)
```java
@Entity
@Table(
    name = "reorder_items",
    indexes = {
        @Index(name = "idx_reorder_items_reorder_id", columnList = "reorder_id"),
        @Index(name = "idx_reorder_items_brand_id", columnList = "brand_id"),
        @Index(name = "idx_reorder_items_task_id", columnList = "task_id")
    }
)
@SuperBuilder
@Getter
@Setter
@ToString(exclude = {"reorder", "brand", "task"})
@NoArgsConstructor
@AllArgsConstructor
public class ReorderItem extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reorder_items_sequence")
    @SequenceGenerator(
        name = "reorder_items_sequence",
        sequenceName = "reorder_items_sequence",
        allocationSize = 1
    )
    private Long id;

    @Comment("FK to reorders; the parent replenishment order this line item belongs to")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reorder_id", nullable = false)
    private Reorder reorder;

    @Comment("FK to brands; the commercial brand / medicine being replenished")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Comment("Snapshot of available physical stock at the store when this order was drafted")
    @Column(name = "current_stock_at_order", nullable = false)
    private Long currentStockAtOrder;

    @Comment("Snapshot of store product reorder threshold at the time the order was drafted")
    @Column(name = "reorder_threshold_at_order", nullable = false)
    private Long reorderThresholdAtOrder;

    @Comment("Calculated suggested restock quantity to bring stock up to target par level")
    @Column(name = "suggested_quantity", nullable = false)
    private Long suggestedQuantity;

    @Comment("Final quantity approved by store manager for supplier order; null until approved")
    @Column(name = "approved_quantity")
    private Long approvedQuantity;

    @Comment("Actual physical quantity delivered and restocked into inventory; null until received")
    @Column(name = "received_quantity")
    private Long receivedQuantity;

    @Comment("FK to action_queue_tasks; nullable link to the REORDER_NEEDED task being fulfilled")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Comment("Optimistic locking version counter to prevent concurrent modification conflicts")
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
```

---

## 5. Workflow & Business Logic

### 1. Daily Scan & Order Consolidation (`runReorderScan()`)
During the scheduled run:
1. Scan `storeProducts` where `isActive = true` and `reorderThreshold IS NOT NULL`.
2. **N+1 Prevention (`performance-standards` §1):** Group active `storeProducts` by `storeId`. For each store, fetch available quantities for all brands in a single bulk query (`storeStockRepository.sumAvailableQuantitiesByStoreGroupedByBrand(storeId)`) and build an in-memory `Map<UUID, Long> availableStockByBrand`. Do not query the database per product in a loop.
3. For each `StoreProduct`:
   - Retrieve `availableStock = availableStockByBrand.getOrDefault(brandId, 0L)` in $O(1)$.
   - If `availableStock < reorderThreshold`:
     - Check if an `APPROVED` reorder already exists for this store that contains this brand. If so, an order is already in transit—do not create a duplicate order item.
     - Calculate target par level: `target = (sp.getTargetStock() != null) ? sp.getTargetStock() : (sp.getReorderThreshold() * 2)`.
     - Calculate `suggestedQty = Math.max(1L, target - availableStock)`.
     - Create or update the `Task` (`TaskType.REORDER_NEEDED`, `PENDING`).
     - Find existing `DRAFT` reorder for this store. If none exists, create a new `Reorder(status = DRAFT)`.
     - If line item for this brand exists in the draft, update snapshot counts and `suggestedQuantity`. If not, add new `ReorderItem` linked to the `Task`.
   - Batch save or cascade persist changes.

### 2. Approval (`approveReorder`)
`PATCH /api/v1/inventory/reorders/{id}/approve`
1. Load `Reorder` with items by ID. Throw `REORDER_NOT_FOUND` (404) if not found.
2. Verify `reorder.status == DRAFT`. Throw `INVALID_REORDER_STATUS` (409) if not `DRAFT`.
3. If `ApproveReorderRequest` provides item adjustments:
   - For each item, update `approvedQuantity = request.approvedQuantity`.
   - If no request body or line omitted, default `approvedQuantity = suggestedQuantity`.
4. Transition order status: `status = APPROVED`.
5. For each item linked to a `Task`:
   - If task status is `PENDING`, transition task to `ACKNOWLEDGED`.
6. Record audit entry: `REORDER_APPROVED`.

### 3. Receiving (`receiveReorder`)
`POST /api/v1/inventory/reorders/{id}/receive`
1. Load `Reorder` with items by ID. Throw `REORDER_NOT_FOUND` (404) if not found.
2. Verify `reorder.status == APPROVED`. Throw `INVALID_REORDER_STATUS` (409) if not `APPROVED`.
3. Validate each payload item:
   - Must belong to this reorder.
   - `receivedQuantity >= 1`.
   - `expiryDate.isAfter(LocalDate.now())`.
4. Atomically for each line:
   - Call `batchService.createBatchFromBarcode` / internal stock-in: create/find `Batch` and increment `StoreStock.quantity`.
   - Set `item.receivedQuantity = request.receivedQuantity`.
   - If item has a linked `Task`, set task status to `RESOLVED`.
5. Transition order status: `status = RECEIVED`.
6. Record audit entry: `REORDER_RECEIVED`.

### 4. Cancellation (`cancelReorder`)
`PATCH /api/v1/inventory/reorders/{id}/cancel`
1. Load `Reorder` by ID.
2. Verify `reorder.status` is either `DRAFT` or `APPROVED`. (Cannot cancel a `RECEIVED` or `CANCELLED` order).
3. Set `status = CANCELLED`.
4. If order was `APPROVED` and linked tasks were `ACKNOWLEDGED`, revert them back to `PENDING` so they can be picked up in future scans.
5. Record audit entry: `REORDER_CANCELLED`.

---

## 6. API Specifications

Base path: `/api/v1/inventory/reorders`

### 1. List Reorders
`GET /api/v1/inventory/reorders?storeId={uuid}&status={status}&page=1&size=20`
- Response: `200 OK` with `ResponseBody<Page<ReorderDto>>`

### 2. Get Reorder by ID
`GET /api/v1/inventory/reorders/{id}`
- Response: `200 OK` with `ResponseBody<ReorderDto>`

### 3. Approve Reorder
`PATCH /api/v1/inventory/reorders/{id}/approve`
- Request Body (Optional):
```json
{
  "notes": "Rounded up to full cartons",
  "items": [
    {
      "itemId": 101,
      "approvedQuantity": 50
    }
  ]
}
```
- Response: `200 OK` with `ResponseBody<ReorderDto>`

### 4. Receive Reorder
`POST /api/v1/inventory/reorders/{id}/receive`
- Request Body:
```json
{
  "items": [
    {
      "itemId": 101,
      "batchNumber": "BAT-2026-X9",
      "expiryDate": "2027-12-31",
      "receivedQuantity": 50,
      "supplierReference": "SUPP-INV-9921",
      "manufacturerLotNumber": "LOT-8812",
      "storageConditions": "Store below 25C"
    }
  ]
}
```
- Response: `200 OK` with `ResponseBody<ReorderDto>`

### 5. Cancel Reorder
`PATCH /api/v1/inventory/reorders/{id}/cancel`
- Request Body (Optional):
```json
{
  "reason": "Overstock from alternative branch transfer"
}
```
- Response: `200 OK` with `ResponseBody<ReorderDto>`

---

## 7. Error Codes

| Code | HTTP Status | Message |
|---|---|---|
| `INV4020` | `404 NOT_FOUND` | Reorder not found |
| `INV4021` | `409 CONFLICT` | Invalid reorder status for requested operation |
| `INV4022` | `400 BAD_REQUEST` | Reorder item not found or does not belong to order |
| `INV4023` | `409 CONFLICT` | Reorder has already been received |
| `INV4024` | `409 CONFLICT` | Reorder was modified concurrently by another transaction |

---

## 8. Testing Strategy (No E2E)

Strictly adhere to [`testing-standards/SKILL.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/testing-standards/SKILL.md) and [`references/java-spring.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/testing-standards/references/java-spring.md):
- **Descriptive `@DisplayName`:** Required at the root class level (business capability), on every `@Nested` class (business scenario/rule), and on every `@Test` method (stating who, what, and business outcome).
- **Centralized Test Fixtures (`ReorderFixture.java`):**
  - Define shared test constants as single source of truth for both setup and assertions (`VALID_STORE_ID`, `VALID_BRAND_ID`, `THRESHOLD_QTY`, `TARGET_PAR_QTY`, `EXPECTED_SUGGESTED_QTY`, etc.).
  - Never hardcode duplicate literals or inline magic numbers inside test assertions.
  - Factory methods: `validDraftEntity()`, `validApprovedEntity()`, `validApproveRequest()`, `validReceiveRequest()`.
- **Strict AAA Pattern:**
  - Every test must have `// Arrange`, `// Act`, `// Assert` blocks separated by blank lines. No interleaved assertions or setups.
- **Behavioral Verification:**
  - Verify `auditService.recordAudit()` only because recording compliance audit is the explicit behavioral contract.
  - Assert on observable state and return values, not internal mock calls.

### Unit Tests (`ReorderServiceImplTest`)
Class annotation: `@DisplayName("Pharmacy inventory reorder management - approval, receiving, and cancellation workflows")`

* `@Nested @DisplayName("Store manager approves draft reorder") class ApproveReorder`
  * `@Test @DisplayName("When order is in DRAFT, manager can approve it and transition status to APPROVED")` -> `shouldApproveReorder_whenOrderIsDraft`
  * `@Test @DisplayName("When manager provides adjusted quantities, approved quantities reflect the custom amounts")` -> `shouldApplyCustomApprovedQuantities_whenProvidedInRequest`
  * `@Test @DisplayName("When manager provides no adjustments, approved quantities default to suggested quantities")` -> `shouldDefaultToSuggestedQuantity_whenNoAdjustmentsProvided`
  * `@Test @DisplayName("When order is not in DRAFT status, approval is rejected with conflict")` -> `shouldThrowConflict_whenOrderIsNotDraft`
  * `@Test @DisplayName("When order is approved, linked action queue tasks are acknowledged to show order in-flight")` -> `shouldAcknowledgeLinkedTasks_whenOrderIsApproved`

* `@Nested @DisplayName("Pharmacy staff receives delivered reorder shipment") class ReceiveReorder`
  * `@Test @DisplayName("When valid shipment arrives, batch is created, store stock is incremented, and order is marked RECEIVED")` -> `shouldReceiveAllItemsAndIncrementStock_whenOrderIsApproved`
  * `@Test @DisplayName("When shipment is received, all linked action queue tasks are automatically resolved")` -> `shouldAutoResolveLinkedTasks_whenOrderIsReceived`
  * `@Test @DisplayName("When order is not in APPROVED status, receiving is rejected with conflict")` -> `shouldThrowConflict_whenOrderIsNotApproved`
  * `@Test @DisplayName("When receiving payload contains an item from another order, receiving is rejected")` -> `shouldThrowBadRequest_whenItemDoesNotBelongToOrder`
  * `@Test @DisplayName("When any single line item in delivery fails validation, entire receipt transaction rolls back")` -> `shouldRollbackEntireReceipt_whenSingleItemFailsValidation`

* `@Nested @DisplayName("Store manager cancels reorder") class CancelReorder`
  * `@Test @DisplayName("When order is DRAFT or APPROVED, order can be cancelled and acknowledged tasks revert to pending")` -> `shouldCancelReorder_whenOrderIsDraftOrApproved`
  * `@Test @DisplayName("When order is already RECEIVED, cancellation is rejected with conflict")` -> `shouldThrowConflict_whenOrderIsAlreadyReceived`

### Scheduler Scan Tests (`ActionQueueServiceImplTest`)
* `@Nested @DisplayName("Daily automated par-level reorder consolidation scan") class RunReorderScan`
  * `@Test @DisplayName("When multiple products are below threshold, a single consolidated draft reorder is created without N+1 queries")` -> `shouldConsolidateMultipleLowStockProductsIntoSingleDraftReorder_whenScanRuns`
  * `@Test @DisplayName("When a draft reorder is already open for a store, newly detected low-stock items append to it")` -> `shouldAppendToExistingDraftReorder_whenDraftAlreadyOpenForStore`
  * `@Test @DisplayName("When an approved order is already in-flight for a brand, it is skipped to prevent duplicate orders")` -> `shouldSkipAddingItem_whenApprovedOrderAlreadyInFlightForBrand`

### Controller Web Layer Tests (`ReorderControllerTest`)
* Focused `@WebMvcTest(ReorderController.class)` verifying JSON serialization, `@Valid` bean validations (`@Min`, `@Future`, `@NotBlank`), and HTTP status codes (`200`, `400`, `404`, `409`).
