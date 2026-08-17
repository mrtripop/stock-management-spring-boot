# Stock Reconciliation Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement an automated system to reconcile the redundant `Batch.quantity` field with the actual sum of `StoreStock` quantities to prevent data drift.

**Architecture:** A background scheduler and admin-triggered service that calculates the real stock sum per batch and corrects the batch total if discrepancies are found, utilizing optimistic locking for concurrency safety.

**Tech Stack:** Java 17, Spring Boot 3.4.2, Spring Data JPA, PostgreSQL.

---

### Task 1: Repository Extension

**Files:**
- Modify: `src/main/java/com/mrtripop/inventory/repository/StoreStockRepository.java`

- [ ] **Step 1: Add summation query**

Add the following method to `StoreStockRepository`:

```java
@Query("SELECT COALESCE(SUM(ss.quantity), 0L) FROM StoreStock ss WHERE ss.batch.id = :batchId")
Long sumQuantityByBatchId(@Param("batchId") Long batchId);
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/mrtripop/inventory/repository/StoreStockRepository.java
git commit -m "feat(inventory): add sumQuantityByBatchId to StoreStockRepository"
```

---

### Task 2: Test Fixtures

**Files:**
- Create: `src/test/java/com/mrtripop/inventory/fixture/StockReconciliationFixture.java`

- [ ] **Step 1: Create Fixture class with shared constants**

```java
package com.mrtripop.inventory.fixture;

import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import java.util.UUID;

public final class StockReconciliationFixture {
    private StockReconciliationFixture() {}

    public static final Long VALID_BATCH_ID = 1L;
    public static final Long INITIAL_BATCH_QTY = 100L;
    public static final Long CORRECT_SUM_QTY = 80L;
    public static final String ACTION_RECONCILIATION = "STOCK_RECONCILIATION";
    public static final String ENTITY_BATCH = "Batch";

    public static Batch defaultBatch() {
        return Batch.builder()
            .id(VALID_BATCH_ID)
            .quantity(INITIAL_BATCH_QTY)
            .build();
    }

    public static StoreStock storeStockWithQty(Long qty) {
        return StoreStock.builder()
            .quantity(qty)
            .build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/test/java/com/mrtripop/inventory/fixture/StockReconciliationFixture.java
git commit -m "test(inventory): add StockReconciliationFixture"
```

---

### Task 3: Core Logic - Business Success (Drift Correction)

**Files:**
- Create: `src/main/java/com/mrtripop/inventory/services/StockReconciliationService.java`
- Create: `src/main/java/com/mrtripop/inventory/services/impl/StockReconciliationServiceImpl.java`
- Create: `src/test/java/com/mrtripop/inventory/services/StockReconciliationServiceTest.java`

- [ ] **Step 1: Define the Service Interface**

```java
package com.mrtripop.inventory.services;

public interface StockReconciliationService {
    void reconcileAll();
    void reconcileBatch(Long batchId);
}
```

- [ ] **Step 2: Write the failing test for Business Success**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Stock Reconciliation Service Tests")
class StockReconciliationServiceTest {
    @Mock private BatchRepository batchRepository;
    @Mock private StoreStockRepository storeStockRepository;
    @Mock private AuditService auditService;
    @InjectMocks private StockReconciliationServiceImpl reconciliationService;

    @Nested
    @DisplayName("Batch reconciliation logic")
    class ReconcileBatch {
        @Test
        @DisplayName("should correct Batch quantity when drift is detected and record audit")
        void shouldCorrectDriftAndRecordAudit() {
            // Arrange
            Batch batch = StockReconciliationFixture.defaultBatch();
            when(batchRepository.findById(StockReconciliationFixture.VALID_BATCH_ID)).thenReturn(Optional.of(batch));
            when(storeStockRepository.sumQuantityByBatchId(StockReconciliationFixture.VALID_BATCH_ID))
                .thenReturn(StockReconciliationFixture.CORRECT_SUM_QTY);

            // Act
            reconciliationService.reconcileBatch(StockReconciliationFixture.VALID_BATCH_ID);

            // Assert
            assertEquals(StockReconciliationFixture.CORRECT_SUM_QTY, batch.getQuantity());
            verify(batchRepository).save(batch);
            verify(auditService).recordAudit(
                eq(StockReconciliationFixture.ACTION_RECONCILIATION),
                eq(StockReconciliationFixture.ENTITY_BATCH),
                eq(batch.getId().toString()),
                eq(String.valueOf(StockReconciliationFixture.INITIAL_BATCH_QTY)),
                eq(String.valueOf(StockReconciliationFixture.CORRECT_SUM_QTY))
            );
        }
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./mvnw test -Dtest=StockReconciliationServiceTest`
Expected: FAIL (Method not implemented)

- [ ] **Step 4: Implement `reconcileBatch` logic**

```java
@Override
@Transactional
public void reconcileBatch(Long batchId) {
    Batch batch = batchRepository.findById(batchId)
        .orElseThrow(() -> new NotFoundException("Batch not found"));
    
    Long actualSum = storeStockRepository.sumQuantityByBatchId(batchId);
    long oldQuantity = batch.getQuantity();
    
    if (oldQuantity != actualSum) {
        batch.setQuantity(actualSum);
        batchRepository.save(batch);
        
        auditService.recordAudit(
            "STOCK_RECONCILIATION",
            "Batch",
            batch.getId().toString(),
            String.valueOf(oldQuantity),
            String.valueOf(actualSum)
        );
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./mvnw test -Dtest=StockReconciliationServiceTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/mrtripop/inventory/services/StockReconciliationService.java \
     src/main/java/com/mrtripop/inventory/services/impl/StockReconciliationServiceImpl.java \
     src/test/java/com/mrtripop/inventory/services/StockReconciliationServiceTest.java
git commit -m "feat(inventory): implement basic stock reconciliation logic"
```

---

### Task 4: Core Logic - Business Neutral & Edge Cases

**Files:**
- Modify: `src/test/java/com/mrtripop/inventory/services/StockReconciliationServiceTest.java`

- [ ] **Step 1: Add tests for Perfect Sync and Orphan Batches using AAA**

```java
@Nested
@DisplayName("Edge case scenarios")
class EdgeCases {
    @Test
    @DisplayName("should do nothing when batch quantity is already in sync with stock sum")
    void shouldDoNothingWhenSynced() {
        // Arrange
        Batch batch = Batch.builder().id(StockReconciliationFixture.VALID_BATCH_ID).quantity(100L).build();
        when(batchRepository.findById(StockReconciliationFixture.VALID_BATCH_ID)).thenReturn(Optional.of(batch));
        when(storeStockRepository.sumQuantityByBatchId(StockReconciliationFixture.VALID_BATCH_ID)).thenReturn(100L);

        // Act
        reconciliationService.reconcileBatch(StockReconciliationFixture.VALID_BATCH_ID);

        // Assert
        verify(batchRepository, times(0)).save(any());
        verify(auditService, times(0)).recordAudit(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("should set quantity to zero when batch exists but has no store stock")
    void shouldSetToZeroWhenOrphan() {
        // Arrange
        Batch batch = Batch.builder().id(StockReconciliationFixture.VALID_BATCH_ID).quantity(50L).build();
        when(batchRepository.findById(StockReconciliationFixture.VALID_BATCH_ID)).thenReturn(Optional.of(batch));
        when(storeStockRepository.sumQuantityByBatchId(StockReconciliationFixture.VALID_BATCH_ID)).thenReturn(0L);

        // Act
        reconciliationService.reconcileBatch(StockReconciliationFixture.VALID_BATCH_ID);

        // Assert
        assertEquals(0L, batch.getQuantity());
        verify(batchRepository).save(batch);
    }
}
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `./mvnw test -Dtest=StockReconciliationServiceTest`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/mrtripop/inventory/services/StockReconciliationServiceTest.java
git commit -m "test(inventory): add edge case tests for reconciliation"
```

---

### Task 5: Core Logic - Technical Resilience (Optimistic Locking)

**Files:**
- Modify: `src/main/java/com/mrtripop/inventory/services/impl/StockReconciliationServiceImpl.java`
- Modify: `src/test/java/com/mrtripop/inventory/services/StockReconciliationServiceTest.java`

- [ ] **Step 1: Write test for Concurrent Update Conflict**

```java
@Nested
@DisplayName("Technical resilience")
class Resilience {
    @Test
    @DisplayName("should log warning and skip batch when optimistic lock failure occurs")
    void shouldHandleOptimisticLockFailure() {
        // Arrange
        Batch batch = StockReconciliationFixture.defaultBatch();
        when(batchRepository.findById(StockReconciliationFixture.VALID_BATCH_ID)).thenReturn(Optional.of(batch));
        when(storeStockRepository.sumQuantityByBatchId(StockReconciliationFixture.VALID_BATCH_ID))
            .thenReturn(StockReconciliationFixture.CORRECT_SUM_QTY);
        
        doThrow(new ObjectOptimisticLockingFailureException(Batch.class, StockReconciliationFixture.VALID_BATCH_ID))
            .when(batchRepository).save(any());

        // Act & Assert
        assertDoesNotThrow(() -> reconciliationService.reconcileBatch(StockReconciliationFixture.VALID_BATCH_ID));
    }
}
```

- [ ] **Step 2: Implement Try-Catch for `ObjectOptimisticLockingFailureException`**

In `StockReconciliationServiceImpl.reconcileBatch`, wrap the save call:

```java
try {
    batchRepository.save(batch);
} catch (ObjectOptimisticLockingFailureException e) {
    log.warn("Optimistic lock failure reconciling batch {}: skipping", batchId);
    return; 
}
```

- [ ] **Step 3: Run test and verify it passes**

Run: `./mvnw test -Dtest=StockReconciliationServiceTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/mrtripop/inventory/services/impl/StockReconciliationServiceImpl.java \
     src/test/java/com/mrtripop/inventory/services/StockReconciliationServiceTest.java
git commit -m "feat(inventory): handle optimistic locking in reconciliation"
```

---

### Task 6: Batch Processing Loop

**Files:**
- Modify: `src/main/java/com/mrtripop/inventory/services/impl/StockReconciliationServiceImpl.java`

- [ ] **Step 1: Implement `reconcileAll` with pagination**

```java
@Override
public void reconcileAll() {
    Pageable pageable = PageRequest.of(0, 100);
    Page<Batch> batchPage;
    int pageNumber = 0;
    
    do {
        batchPage = batchRepository.findAll(PageRequest.of(pageNumber++, 100));
        for (Batch batch : batchPage) {
            try {
                reconcileBatch(batch.getId());
            } catch (Exception e) {
                log.error("Failed to reconcile batch {}: {}", batch.getId(), e.getMessage());
            }
        }
    } while (batchPage.hasNext());
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/mrtripop/inventory/services/impl/StockReconciliationServiceImpl.java
git commit -m "feat(inventory): implement paginated reconcileAll loop"
```

---

### Task 7: Scheduling

**Files:**
- Create: `src/main/java/com/mrtripop/inventory/scheduler/StockReconciliationScheduler.java`

- [ ] **Step 1: Implement Daily Cron Job**

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class StockReconciliationScheduler {
    private final StockReconciliationService reconciliationService;

    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    public void scheduledReconciliation() {
        log.info("Starting scheduled stock reconciliation...");
        reconciliationService.reconcileAll();
        log.info("Scheduled stock reconciliation completed.");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/mrtripop/inventory/scheduler/StockReconciliationScheduler.java
git commit -m "feat(inventory): add daily stock reconciliation scheduler"
```

---

### Task 8: Admin Trigger Endpoint

**Files:**
- Create: `src/main/java/com/mrtripop/inventory/controllers/InventoryAdminController.java`

- [ ] **Step 1: Implement Trigger Endpoint**

```java
@RestController
@RequestMapping("/api/inventory/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryAdminController {
    private final StockReconciliationService reconciliationService;

    @PostMapping("/reconcile")
    public ResponseEntity<ResponseBody<?>> triggerReconciliation() {
        reconciliationService.reconcileAll();
        return ResponseBody.builder()
            .code(SuccessCode.OK.getCode())
            .message("Stock reconciliation triggered successfully")
            .build()
            .toResponseEntity(HttpStatus.OK);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/mrtripop/inventory/controllers/InventoryAdminController.java
git commit -m "feat(inventory): add admin endpoint to trigger stock reconciliation"
```
