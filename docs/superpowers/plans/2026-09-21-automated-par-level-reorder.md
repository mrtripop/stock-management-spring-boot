# Automated Par-Level Reorder Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an automated par-level stock replenishment system that consolidates low-stock items into draft reorders during the daily scan and provides manager approval, receiving, and automatic action task resolution.

**Architecture:** Enhances `StoreProduct` with par-level target stock, introduces `Reorder` and `ReorderItem` domain entities in `com.mrtripop.inventory`, extends `ActionQueueServiceImpl.runReorderScan()` to consolidate draft requisitions, and exposes REST endpoints via `ReorderController` for manager approval and shipment receiving.

**Tech Stack:** Java 17, Spring Boot 3.4.2, Spring Data JPA, Hibernate, PostgreSQL, MapStruct 1.6.0, Lombok, JUnit 5, Mockito, Google Java Format.

**Mandatory Engineering Skills Reference:**
- [`code-style-standards`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/code-style-standards/SKILL.md) & [`java-spring.md`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/code-style-standards/references/java-spring.md)
- [`testing-standards`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/testing-standards/SKILL.md)
- [`testing-principles`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/testing-principles/SKILL.md)
- [`security-standards`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/security-standards/SKILL.md)
- [`performance-standards`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/performance-standards/SKILL.md)
- [`foundation-principles`](file:///Users/mrtripop/.gemini/config/plugins/software-engineer/skills/foundation-principles/SKILL.md)

---

### Task 1: Add `targetStock` to `StoreProduct`

**Files:**
- Modify: `src/main/java/com/mrtripop/clinical/models/db/StoreProduct.java`
- Modify: `src/main/java/com/mrtripop/clinical/models/dto/StoreProductDto.java`
- Modify: `src/main/java/com/mrtripop/clinical/models/dto/ActivateProductRequest.java`
- Modify: `src/main/java/com/mrtripop/clinical/models/dto/UpdateOverrideRequest.java`
- Modify: `src/main/java/com/mrtripop/clinical/component/StoreProductMapper.java`
- Test: `src/test/java/com/mrtripop/clinical/services/StoreProductServiceTest.java`

- [ ] **Step 1: Write unit test verifying `targetStock` mapping in `StoreProductServiceTest`**
```java
@Test
void shouldMapTargetStock_whenActivatingStoreProduct() {
    // Verify targetStock is mapped from ActivateProductRequest to StoreProduct and StoreProductDto
}
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./mvnw test -Dtest=StoreProductServiceTest`

- [ ] **Step 3: Implement `targetStock` column in `StoreProduct` with Hibernate `@Comment`**
```java
@Comment("Target inventory par-level to restock up to; if null, defaults to 2x reorderThreshold")
@Column(name = "target_stock")
private Long targetStock;
```
Update DTOs and MapStruct mapper accordingly.

- [ ] **Step 4: Run test to verify it passes**
Run: `./mvnw test -Dtest=StoreProductServiceTest`

- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/mrtripop/clinical/
git commit -m "feat(clinical): add targetStock par level column to StoreProduct"
```

---

### Task 2: Create `Reorder` & `ReorderItem` Entities and `ReorderStatus` Enum

**Files:**
- Create: `src/main/java/com/mrtripop/inventory/models/db/ReorderStatus.java`
- Create: `src/main/java/com/mrtripop/inventory/models/db/Reorder.java`
- Create: `src/main/java/com/mrtripop/inventory/models/db/ReorderItem.java`

- [ ] **Step 1: Create `ReorderStatus` Enum**
```java
package com.mrtripop.inventory.models.db;

public enum ReorderStatus {
    DRAFT,
    APPROVED,
    RECEIVED,
    CANCELLED
}
```

- [ ] **Step 2: Create `Reorder` Entity with strict `@Comment` on every column**
```java
package com.mrtripop.inventory.models.db;

import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.product.models.db.AuditEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

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
    @Builder.Default
    private List<ReorderItem> items = new ArrayList<>();

    @Comment("Optimistic locking version counter to prevent concurrent modification conflicts")
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
```

- [ ] **Step 3: Create `ReorderItem` Entity with strict `@Comment` on every column**
```java
package com.mrtripop.inventory.models.db;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.product.models.db.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

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

- [ ] **Step 4: Verify project compiles**
Run: `./mvnw clean compile`

- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/models/db/
git commit -m "feat(inventory): add Reorder, ReorderItem entities and ReorderStatus enum"
```

---

### Task 3: Create Repositories for `Reorder` and `ReorderItem`

**Files:**
- Create: `src/main/java/com/mrtripop/inventory/repository/ReorderRepository.java`
- Create: `src/main/java/com/mrtripop/inventory/repository/ReorderItemRepository.java`

- [ ] **Step 1: Create `ReorderRepository`**
```java
package com.mrtripop.inventory.repository;

import com.mrtripop.inventory.models.db.Reorder;
import com.mrtripop.inventory.models.db.ReorderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReorderRepository extends JpaRepository<Reorder, Long> {

    @Query("""
        SELECT r FROM Reorder r
        LEFT JOIN FETCH r.items i
        LEFT JOIN FETCH i.brand
        WHERE r.id = :id
        """)
    Optional<Reorder> findWithDetailsById(@Param("id") Long id);

    Optional<Reorder> findByStore_IdAndStatus(UUID storeId, ReorderStatus status);

    Page<Reorder> findByStore_Id(UUID storeId, Pageable pageable);

    Page<Reorder> findByStore_IdAndStatus(UUID storeId, ReorderStatus status, Pageable pageable);

    boolean existsByStore_IdAndStatusAndItems_Brand_Id(UUID storeId, ReorderStatus status, UUID brandId);
}
```

- [ ] **Step 2: Create `ReorderItemRepository`**
```java
package com.mrtripop.inventory.repository;

import com.mrtripop.inventory.models.db.ReorderItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReorderItemRepository extends JpaRepository<ReorderItem, Long> {

    @Query("""
        SELECT ri FROM ReorderItem ri
        JOIN FETCH ri.reorder r
        JOIN FETCH ri.brand b
        WHERE ri.id = :id AND r.id = :reorderId
        """)
    Optional<ReorderItem> findByIdAndReorderId(@Param("id") Long id, @Param("reorderId") Long reorderId);
}
```

- [ ] **Step 3: Verify compilation**
Run: `./mvnw compile`

- [ ] **Step 4: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/repository/
git commit -m "feat(inventory): add ReorderRepository and ReorderItemRepository"
```

---

### Task 4: DTOs, MapStruct Mapper & Status/Error Codes

**Files:**
- Create: `src/main/java/com/mrtripop/inventory/models/dto/ReorderDto.java`
- Create: `src/main/java/com/mrtripop/inventory/models/dto/ReorderItemDto.java`
- Create: `src/main/java/com/mrtripop/inventory/models/dto/ApproveReorderRequest.java`
- Create: `src/main/java/com/mrtripop/inventory/models/dto/ApproveReorderItemRequest.java`
- Create: `src/main/java/com/mrtripop/inventory/models/dto/ReceiveReorderRequest.java`
- Create: `src/main/java/com/mrtripop/inventory/models/dto/ReceiveReorderItemRequest.java`
- Create: `src/main/java/com/mrtripop/inventory/models/dto/CancelReorderRequest.java`
- Create: `src/main/java/com/mrtripop/inventory/component/ReorderMapper.java`
- Modify: `src/main/java/com/mrtripop/inventory/constant/ErrorCode.java`
- Modify: `src/main/java/com/mrtripop/inventory/constant/SuccessCode.java`

- [ ] **Step 1: Add error and success codes in `ErrorCode` and `SuccessCode`**
Add:
- `INV4020_REORDER_NOT_FOUND`
- `INV4021_INVALID_REORDER_STATUS`
- `INV4022_REORDER_ITEM_NOT_FOUND`
- `INV4023_REORDER_ALREADY_RECEIVED`
- `INV4024_REORDER_CONCURRENT_MODIFICATION`
And success codes:
- `INV2020_GET_REORDERS_SUCCESS`
- `INV2021_GET_REORDER_BY_ID_SUCCESS`
- `INV2022_APPROVE_REORDER_SUCCESS`
- `INV2023_RECEIVE_REORDER_SUCCESS`
- `INV2024_CANCEL_REORDER_SUCCESS`

- [ ] **Step 2: Create DTOs with Bean Validation annotations (`@NotNull`, `@Min`, `@Future`, `@NotBlank`)**

- [ ] **Step 3: Create `ReorderMapper` using MapStruct**
```java
package com.mrtripop.inventory.component;

import com.mrtripop.inventory.models.db.Reorder;
import com.mrtripop.inventory.models.db.ReorderItem;
import com.mrtripop.inventory.models.dto.ReorderDto;
import com.mrtripop.inventory.models.dto.ReorderItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReorderMapper {

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.storeName", target = "storeName")
    ReorderDto toDto(Reorder reorder);

    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.brandName", target = "brandName")
    @Mapping(source = "brand.barcode", target = "barcode")
    @Mapping(source = "task.id", target = "linkedTaskId")
    ReorderItemDto toDto(ReorderItem item);
}
```

- [ ] **Step 4: Verify compilation & MapStruct generation**
Run: `./mvnw compile`

- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/models/dto/ src/main/java/com/mrtripop/inventory/component/ src/main/java/com/mrtripop/inventory/constant/
git commit -m "feat(inventory): add reorder DTOs, MapStruct mapper, and status codes"
```

---

### Task 5: Enhance `runReorderScan()` in `ActionQueueServiceImpl`

**Files:**
- Modify: `src/main/java/com/mrtripop/inventory/services/impl/ActionQueueServiceImpl.java`
- Test: `src/test/java/com/mrtripop/inventory/services/ActionQueueServiceImplTest.java`

- [ ] **Step 1: Write unit tests in `ActionQueueServiceImplTest` for reorder consolidation**
Write `@Nested` tests for `RunReorderScan`:
- `shouldCreateConsolidatedDraftReorder_whenProductsBelowThreshold`
- `shouldAppendToExistingDraftReorder_whenDraftAlreadyOpenForStore`
- `shouldSkipAddingItem_whenApprovedOrderAlreadyInFlightForBrand`

- [ ] **Step 2: Run test to verify failure**
Run: `./mvnw test -Dtest=ActionQueueServiceImplTest`

- [ ] **Step 3: Update `ActionQueueServiceImpl.runReorderScan()`**
- Calculate `targetStock = sp.getTargetStock() != null ? sp.getTargetStock() : (sp.getReorderThreshold() * 2)`.
- Calculate `suggestedQty = Math.max(1L, targetStock - available)`.
- Check if an `APPROVED` reorder already includes `brandId` for `storeId`.
- Find or create `DRAFT` `Reorder` for `storeId`.
- Create or update `ReorderItem` linked to the `Task`.

- [ ] **Step 4: Run tests to verify they pass**
Run: `./mvnw test -Dtest=ActionQueueServiceImplTest`

- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/services/impl/ActionQueueServiceImpl.java src/test/java/com/mrtripop/inventory/services/ActionQueueServiceImplTest.java
git commit -m "feat(inventory): consolidate low-stock products into draft reorders during scan"
```

---

### Task 6: Implement `ReorderService` (Approval, Receiving, Cancellation & Query)

**Files:**
- Create: `src/main/java/com/mrtripop/inventory/services/ReorderService.java`
- Create: `src/main/java/com/mrtripop/inventory/services/impl/ReorderServiceImpl.java`
- Test: `src/test/java/com/mrtripop/inventory/services/ReorderServiceImplTest.java`

- [ ] **Step 1: Define `ReorderService` interface**
```java
package com.mrtripop.inventory.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.db.ReorderStatus;
import com.mrtripop.inventory.models.dto.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReorderService {
    Page<ReorderDto> getReorders(UUID storeId, ReorderStatus status, Pageable pageable);
    ReorderDto getReorderById(Long id) throws ApplicationException;
    ReorderDto approveReorder(Long id, ApproveReorderRequest request) throws ApplicationException;
    ReorderDto receiveReorder(Long id, ReceiveReorderRequest request) throws ApplicationException;
    ReorderDto cancelReorder(Long id, CancelReorderRequest request) throws ApplicationException;
}
```

- [ ] **Step 2: Write unit tests in `ReorderServiceImplTest`**
Group scenarios using `@Nested`:
- `@Nested class ApproveReorder`:
  - `shouldApproveReorder_whenOrderIsDraft`
  - `shouldAcknowledgeLinkedTasks_whenOrderIsApproved`
  - `shouldThrowConflict_whenOrderIsNotDraft`
- `@Nested class ReceiveReorder`:
  - `shouldReceiveAllItemsAndUpdateStock_whenOrderIsApproved`
  - `shouldAutoResolveLinkedTasks_whenOrderIsReceived`
  - `shouldThrowConflict_whenOrderIsNotApproved`
  - `shouldThrowBadRequest_whenItemDoesNotBelongToOrder`
- `@Nested class CancelReorder`:
  - `shouldCancelReorder_whenOrderIsDraftOrApproved`
  - `shouldThrowConflict_whenOrderIsAlreadyReceived`

- [ ] **Step 3: Run test to verify it fails**
Run: `./mvnw test -Dtest=ReorderServiceImplTest`

- [ ] **Step 4: Implement `ReorderServiceImpl`**
- `approveReorder()`: Updates status to `APPROVED`, sets `approvedQuantity` (defaults to `suggestedQuantity`), transitions linked `Task` to `ACKNOWLEDGED`, logs `REORDER_APPROVED` audit.
- `receiveReorder()`: Atomically calls `batchService.stockEntry()` / creates batch, increments `StoreStock`, sets `receivedQuantity`, sets linked `Task` to `RESOLVED`, updates status to `RECEIVED`, logs `REORDER_RECEIVED` audit.
- `cancelReorder()`: Sets status to `CANCELLED`, reverts `ACKNOWLEDGED` tasks to `PENDING`, logs `REORDER_CANCELLED` audit.
- Handles `OptimisticLockException` translating to `INV4024_REORDER_CONCURRENT_MODIFICATION`.

- [ ] **Step 5: Run tests to verify they pass**
Run: `./mvnw test -Dtest=ReorderServiceImplTest`

- [ ] **Step 6: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/services/ src/test/java/com/mrtripop/inventory/services/ReorderServiceImplTest.java
git commit -m "feat(inventory): implement ReorderService approval, receiving and cancellation"
```

---

### Task 7: Implement `ReorderController` with `@WebMvcTest` Slice Tests

**Files:**
- Create: `src/main/java/com/mrtripop/inventory/controllers/ReorderController.java`
- Test: `src/test/java/com/mrtripop/inventory/controllers/ReorderControllerTest.java`

- [ ] **Step 1: Write `@WebMvcTest` controller slice tests in `ReorderControllerTest`**
Test:
- `GET /api/v1/inventory/reorders`: returns `200 OK` with page list
- `GET /api/v1/inventory/reorders/{id}`: returns `200 OK`
- `PATCH /api/v1/inventory/reorders/{id}/approve`: returns `200 OK`
- `POST /api/v1/inventory/reorders/{id}/receive`: returns `200 OK`
- `POST /api/v1/inventory/reorders/{id}/receive`: returns `400 BAD_REQUEST` when missing batch number or past expiry date
- `PATCH /api/v1/inventory/reorders/{id}/cancel`: returns `200 OK`

- [ ] **Step 2: Run test to verify failure**
Run: `./mvnw test -Dtest=ReorderControllerTest`

- [ ] **Step 3: Implement `ReorderController`**
```java
package com.mrtripop.inventory.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.SuccessCode;
import com.mrtripop.inventory.models.db.ReorderStatus;
import com.mrtripop.inventory.models.dto.*;
import com.mrtripop.inventory.services.ReorderService;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory/reorders")
@Validated
public class ReorderController {

    private final ReorderService reorderService;

    @GetMapping
    public ResponseEntity<Object> getReorders(
            @RequestParam UUID storeId,
            @RequestParam(required = false) ReorderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReorderDto> result = reorderService.getReorders(storeId, status, pageable);
        BaseStatusCode success = SuccessCode.INV2020_GET_REORDERS_SUCCESS;
        return ResponseBody.builder()
                .code(success.getCode())
                .message(success.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getReorderById(@PathVariable @Min(1) Long id) throws ApplicationException {
        ReorderDto result = reorderService.getReorderById(id);
        BaseStatusCode success = SuccessCode.INV2021_GET_REORDER_BY_ID_SUCCESS;
        return ResponseBody.builder()
                .code(success.getCode())
                .message(success.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Object> approveReorder(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody(required = false) ApproveReorderRequest request) throws ApplicationException {
        ReorderDto result = reorderService.approveReorder(id, request);
        BaseStatusCode success = SuccessCode.INV2022_APPROVE_REORDER_SUCCESS;
        return ResponseBody.builder()
                .code(success.getCode())
                .message(success.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<Object> receiveReorder(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody ReceiveReorderRequest request) throws ApplicationException {
        ReorderDto result = reorderService.receiveReorder(id, request);
        BaseStatusCode success = SuccessCode.INV2023_RECEIVE_REORDER_SUCCESS;
        return ResponseBody.builder()
                .code(success.getCode())
                .message(success.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Object> cancelReorder(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody(required = false) CancelReorderRequest request) throws ApplicationException {
        ReorderDto result = reorderService.cancelReorder(id, request);
        BaseStatusCode success = SuccessCode.INV2024_CANCEL_REORDER_SUCCESS;
        return ResponseBody.builder()
                .code(success.getCode())
                .message(success.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }
}
```

- [ ] **Step 4: Run controller slice tests to verify they pass**
Run: `./mvnw test -Dtest=ReorderControllerTest`

- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/mrtripop/inventory/controllers/ src/test/java/com/mrtripop/inventory/controllers/
git commit -m "feat(inventory): implement ReorderController endpoints with WebMvcTest slice tests"
```

---

### Task 8: Full Test Suite Verification

- [ ] **Step 1: Run all unit and slice tests**
Run: `./mvnw test -Dtest=*Test`

- [ ] **Step 2: Verify zero compiler warnings, zero lint errors, and all tests pass**

- [ ] **Step 3: Commit any formatting or polish changes**
```bash
git status
```
