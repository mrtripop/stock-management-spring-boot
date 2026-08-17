# POS-Style Dispensing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect invoice completion to automatic inventory deduction, add a quick-dispense endpoint, daily sales summary, and void-with-restock.

**Architecture:** Wire `InvoiceServiceImpl.complete()` to call a new `BatchService.deductStockByBatch()` method that deducts from a specific batch (not barcode-based FEFO). Add a `dispense()` method that combines create + complete in one transactional call. Add a daily summary aggregation query. Add stock restoration on void of completed invoices.

**Tech Stack:** Java 17, Spring Boot 3.4.2, JPA/Hibernate, Mockito (unit tests), H2 (integration tests)

---

## File Structure

### New files
- `src/main/java/com/mrtripop/inventory/models/dto/StockDeductionByBatchRequest.java` — request DTO for batch-specific deduction
- `src/main/java/com/mrtripop/transaction/models/dto/DailySalesSummaryDto.java` — daily summary response
- `src/test/java/com/mrtripop/inventory/fixture/BatchDeductionFixture.java` — fixtures for batch deduction tests

### Modified files
- `src/main/java/com/mrtripop/inventory/services/BatchService.java` — add `deductStockByBatch()`, `restoreStock()`
- `src/main/java/com/mrtripop/inventory/services/impl/BatchServiceImpl.java` — implement new methods
- `src/main/java/com/mrtripop/inventory/repository/StoreStockRepository.java` — add `restoreQuantity()` modifying query
- `src/main/java/com/mrtripop/inventory/constant/ErrorCode.java` — add new error codes
- `src/main/java/com/mrtripop/transaction/services/InvoiceService.java` — add `dispense()`, `getDailySummary()`
- `src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java` — wire deduction to `complete()`, implement `dispense()`, `getDailySummary()`, restock on void
- `src/main/java/com/mrtripop/transaction/repository/InvoiceRepository.java` — add aggregation query
- `src/main/java/com/mrtripop/transaction/repository/InvoiceItemRepository.java` — add item quantity aggregation
- `src/main/java/com/mrtripop/transaction/controllers/InvoiceController.java` — add `POST /dispense`, `GET /daily-summary`
- `src/main/java/com/mrtripop/transaction/constant/SuccessCode.java` — add new success codes
- `src/main/java/com/mrtripop/transaction/constant/ErrorCode.java` — add new error code
- `src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java` — add tests for new methods
- `src/test/java/com/mrtripop/transaction/fixture/InvoiceFixture.java` — add fixture methods
- `src/test/java/com/mrtripop/inventory/services/impl/BatchServiceImplTest.java` — add tests for new deduction/restock

---

### Task 1: Add `deductStockByBatch` to BatchService interface and DTO

**Files:**
- Create: `src/main/java/com/mrtripop/inventory/models/dto/StockDeductionByBatchRequest.java`
- Modify: `src/main/java/com/mrtripop/inventory/services/BatchService.java`
- Modify: `src/main/java/com/mrtripop/inventory/constant/ErrorCode.java`

- [ ] **Step 1: Create the request DTO**

```java
// src/main/java/com/mrtripop/inventory/models/dto/StockDeductionByBatchRequest.java
package com.mrtripop.inventory.models.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductionByBatchRequest {

  @NotNull(message = "Store ID is required")
  private UUID storeId;

  @NotNull(message = "Batch ID is required")
  private Long batchId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Long quantity;
}
```

- [ ] **Step 2: Add error codes to inventory ErrorCode**

Add to `src/main/java/com/mrtripop/inventory/constant/ErrorCode.java`:

```java
STOCK_NOT_FOUND_FOR_BATCH("INV4012", "Store stock not found for the specified batch"),
INSUFFICIENT_BATCH_QUANTITY("INV4013", "Insufficient quantity in batch for deduction");
```

- [ ] **Step 3: Add method to BatchService interface**

Add to `src/main/java/com/mrtripop/inventory/services/BatchService.java`:

```java
void deductStockByBatch(UUID storeId, Long batchId, Long quantity) throws ApplicationException;

void restoreStock(UUID storeId, Long batchId, Long quantity) throws ApplicationException;
```

Also add the import: `import java.util.UUID;` (already present).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/mrtripop/inventory/models/dto/StockDeductionByBatchRequest.java \
        src/main/java/com/mrtripop/inventory/services/BatchService.java \
        src/main/java/com/mrtripop/inventory/constant/ErrorCode.java
git commit -m "feat(inventory): add deductStockByBatch and restoreStock interface methods

Add BatchService methods for direct batch-specific stock deduction
and restoration, needed for POS dispensing invoice completion."
```

---

### Task 2: Implement `deductStockByBatch` in BatchServiceImpl

**Files:**
- Modify: `src/main/java/com/mrtripop/inventory/services/impl/BatchServiceImpl.java`

- [ ] **Step 1: Write the failing test**

Add to `src/test/java/com/mrtripop/inventory/services/impl/BatchServiceImplTest.java` (append new `@Nested` class inside the existing test class, or create the test class if it doesn't exist):

```java
// Add these imports at the top if not already present:
// import com.mrtripop.inventory.fixture.BatchDeductionFixture;
// import com.mrtripop.inventory.models.dto.StockDeductionByBatchRequest;

@Nested
@DisplayName("DeductStockByBatch")
class DeductStockByBatch {

  @Test
  @DisplayName("should deduct stock from specific batch")
  void shouldDeductStockFromSpecificBatch() throws ApplicationException {
    // Arrange
    StoreStock storeStock = BatchDeductionFixture.validStoreStock();
    when(storeStockRepository.findByStoreIdAndBatchId(
        BatchDeductionFixture.STORE_ID, BatchDeductionFixture.BATCH_ID))
        .thenReturn(Optional.of(storeStock));
    when(storeStockRepository.deductQuantity(
        storeStock.getId(), BatchDeductionFixture.DEDUCT_QUANTITY))
        .thenReturn(1);
    when(auditService.recordAudit(anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(null);

    // Act
    batchService.deductStockByBatch(
        BatchDeductionFixture.STORE_ID,
        BatchDeductionFixture.BATCH_ID,
        BatchDeductionFixture.DEDUCT_QUANTITY);

    // Assert
    verify(storeStockRepository).deductQuantity(
        storeStock.getId(), BatchDeductionFixture.DEDUCT_QUANTITY);
    verify(auditService).recordAudit(
        eq("INVENTORY_OUT"), eq("StoreStock"),
        eq(storeStock.getId().toString()), anyString(), anyString());
  }

  @Test
  @DisplayName("should throw INV4012 when store stock not found for batch")
  void shouldThrowWhenStockNotFound() {
    // Arrange
    when(storeStockRepository.findByStoreIdAndBatchId(
        BatchDeductionFixture.STORE_ID, BatchDeductionFixture.BATCH_ID))
        .thenReturn(Optional.empty());

    // Act & Assert
    ApplicationException ex = assertThrows(ApplicationException.class,
        () -> batchService.deductStockByBatch(
            BatchDeductionFixture.STORE_ID,
            BatchDeductionFixture.BATCH_ID,
            BatchDeductionFixture.DEDUCT_QUANTITY));
    assertEquals(ErrorCode.STOCK_NOT_FOUND_FOR_BATCH, ex.getErrorCode());
  }

  @Test
  @DisplayName("should throw INV4013 when insufficient batch quantity")
  void shouldThrowWhenInsufficientQuantity() throws ApplicationException {
    // Arrange
    StoreStock storeStock = BatchDeductionFixture.validStoreStock();
    when(storeStockRepository.findByStoreIdAndBatchId(
        BatchDeductionFixture.STORE_ID, BatchDeductionFixture.BATCH_ID))
        .thenReturn(Optional.of(storeStock));
    when(storeStockRepository.deductQuantity(
        storeStock.getId(), BatchDeductionFixture.DEDUCT_QUANTITY))
        .thenReturn(0);

    // Act & Assert
    ApplicationException ex = assertThrows(ApplicationException.class,
        () -> batchService.deductStockByBatch(
            BatchDeductionFixture.STORE_ID,
            BatchDeductionFixture.BATCH_ID,
            BatchDeductionFixture.DEDUCT_QUANTITY));
    assertEquals(ErrorCode.INSUFFICIENT_BATCH_QUANTITY, ex.getErrorCode());
  }
}
```

- [ ] **Step 2: Create the test fixture**

Create `src/test/java/com/mrtripop/inventory/fixture/BatchDeductionFixture.java`:

```java
package com.mrtripop.inventory.fixture;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.StoreStock;
import java.time.LocalDate;
import java.util.UUID;

public final class BatchDeductionFixture {

  private BatchDeductionFixture() {}

  public static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final String STORE_NAME = "Main Pharmacy";
  public static final UUID BRAND_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
  public static final String BRAND_NAME = "Tylenol";
  public static final Long BATCH_ID = 1L;
  public static final String BATCH_NUMBER = "BATCH-001";
  public static final Long STORE_STOCK_ID = 1L;
  public static final Long STORE_STOCK_QUANTITY = 100L;
  public static final Long DEDUCT_QUANTITY = 5L;

  public static Store validStore() {
    return Store.builder().id(STORE_ID).name(STORE_NAME).type(StoreType.PHYSICAL).active(true).build();
  }

  public static Brand validBrand() {
    Molecule molecule = Molecule.builder()
        .id(UUID.fromString("00000000-0000-0000-0000-000000000020"))
        .genericName("Paracetamol")
        .build();
    return Brand.builder().id(BRAND_ID).brandName(BRAND_NAME).molecule(molecule).build();
  }

  public static Batch validBatch() {
    return Batch.builder()
        .id(BATCH_ID)
        .brand(validBrand())
        .batchNumber(BATCH_NUMBER)
        .expiryDate(LocalDate.now().plusYears(1))
        .quantity(STORE_STOCK_QUANTITY)
        .status(BatchStatus.AVAILABLE)
        .build();
  }

  public static StoreStock validStoreStock() {
    return StoreStock.builder()
        .id(STORE_STOCK_ID)
        .store(validStore())
        .batch(validBatch())
        .quantity(STORE_STOCK_QUANTITY)
        .build();
  }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./mvnw test -Dtest=BatchServiceImplTest -pl .`
Expected: FAIL — `dedStockByBatch` not implemented yet.

- [ ] **Step 4: Implement `deductStockByBatch` in BatchServiceImpl**

Add to `src/main/java/com/mrtripop/inventory/services/impl/BatchServiceImpl.java`:

```java
@Override
@Transactional(rollbackFor = ApplicationException.class)
public void deductStockByBatch(UUID storeId, Long batchId, Long quantity)
    throws ApplicationException {
  StoreStock storeStock = storeStockRepository
      .findByStoreIdAndBatchId(storeId, batchId)
      .orElseThrow(() -> new ApplicationException(
          ErrorCode.STOCK_NOT_FOUND_FOR_BATCH, HttpStatus.NOT_FOUND));

  long oldQuantity = storeStock.getQuantity();
  int updated = storeStockRepository.deductQuantity(storeStock.getId(), quantity);
  if (updated == 0) {
    throw new ApplicationException(
        ErrorCode.INSUFFICIENT_BATCH_QUANTITY, HttpStatus.CONFLICT);
  }

  storeStock.setQuantity(oldQuantity - quantity);

  auditService.recordAudit(
      "INVENTORY_OUT",
      "StoreStock",
      storeStock.getId().toString(),
      String.valueOf(oldQuantity),
      String.valueOf(storeStock.getQuantity()));
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./mvnw test -Dtest=BatchServiceImplTest -pl .`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/mrtripop/inventory/services/impl/BatchServiceImpl.java \
        src/test/java/com/mrtripop/inventory/services/impl/BatchServiceImplTest.java \
        src/test/java/com/mrtripop/inventory/fixture/BatchDeductionFixture.java
git commit -m "feat(inventory): implement deductStockByBatch for direct batch deduction

Deducts from a specific StoreStock by store+batch ID with atomic
quantity update and audit logging."
```

---

### Task 3: Implement `restoreStock` in BatchServiceImpl

**Files:**
- Modify: `src/main/java/com/mrtripop/inventory/repository/StoreStockRepository.java`
- Modify: `src/main/java/com/mrtripop/inventory/services/impl/BatchServiceImpl.java`

- [ ] **Step 1: Write the failing test**

Add to `src/test/java/com/mrtripop/inventory/services/impl/BatchServiceImplTest.java` inside the test class:

```java
@Nested
@DisplayName("RestoreStock")
class RestoreStock {

  @Test
  @DisplayName("should restore stock to specific batch")
  void shouldRestoreStockToSpecificBatch() throws ApplicationException {
    // Arrange
    StoreStock storeStock = BatchDeductionFixture.validStoreStock();
    when(storeStockRepository.findByStoreIdAndBatchId(
        BatchDeductionFixture.STORE_ID, BatchDeductionFixture.BATCH_ID))
        .thenReturn(Optional.of(storeStock));
    when(storeStockRepository.restoreQuantity(
        storeStock.getId(), BatchDeductionFixture.DEDUCT_QUANTITY))
        .thenReturn(1);
    when(auditService.recordAudit(anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(null);

    // Act
    batchService.restoreStock(
        BatchDeductionFixture.STORE_ID,
        BatchDeductionFixture.BATCH_ID,
        BatchDeductionFixture.DEDUCT_QUANTITY);

    // Assert
    verify(storeStockRepository).restoreQuantity(
        storeStock.getId(), BatchDeductionFixture.DEDUCT_QUANTITY);
    verify(auditService).recordAudit(
        eq("INVENTORY_IN"), eq("StoreStock"),
        eq(storeStock.getId().toString()), anyString(), anyString());
  }

  @Test
  @DisplayName("should throw INV4012 when store stock not found for restoration")
  void shouldThrowWhenStockNotFound() {
    // Arrange
    when(storeStockRepository.findByStoreIdAndBatchId(
        BatchDeductionFixture.STORE_ID, BatchDeductionFixture.BATCH_ID))
        .thenReturn(Optional.empty());

    // Act & Assert
    ApplicationException ex = assertThrows(ApplicationException.class,
        () -> batchService.restoreStock(
            BatchDeductionFixture.STORE_ID,
            BatchDeductionFixture.BATCH_ID,
            BatchDeductionFixture.DEDUCT_QUANTITY));
    assertEquals(ErrorCode.STOCK_NOT_FOUND_FOR_BATCH, ex.getErrorCode());
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=BatchServiceImplTest -pl .`
Expected: FAIL — `restoreStock` and `restoreQuantity` not implemented.

- [ ] **Step 3: Add `restoreQuantity` to StoreStockRepository**

Add to `src/main/java/com/mrtripop/inventory/repository/StoreStockRepository.java`:

```java
@Modifying
@Query("UPDATE StoreStock ss SET ss.quantity = ss.quantity + :amount WHERE ss.id = :id")
int restoreQuantity(@Param("id") Long id, @Param("amount") Long amount);
```

- [ ] **Step 4: Implement `restoreStock` in BatchServiceImpl**

Add to `src/main/java/com/mrtripop/inventory/services/impl/BatchServiceImpl.java`:

```java
@Override
@Transactional(rollbackFor = ApplicationException.class)
public void restoreStock(UUID storeId, Long batchId, Long quantity)
    throws ApplicationException {
  StoreStock storeStock = storeStockRepository
      .findByStoreIdAndBatchId(storeId, batchId)
      .orElseThrow(() -> new ApplicationException(
          ErrorCode.STOCK_NOT_FOUND_FOR_BATCH, HttpStatus.NOT_FOUND));

  long oldQuantity = storeStock.getQuantity();
  storeStockRepository.restoreQuantity(storeStock.getId(), quantity);
  storeStock.setQuantity(oldQuantity + quantity);

  auditService.recordAudit(
      "INVENTORY_IN",
      "StoreStock",
      storeStock.getId().toString(),
      String.valueOf(oldQuantity),
      String.valueOf(storeStock.getQuantity()));
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./mvnw test -Dtest=BatchServiceImplTest -pl .`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/mrtripop/inventory/repository/StoreStockRepository.java \
        src/main/java/com/mrtripop/inventory/services/impl/BatchServiceImpl.java \
        src/test/java/com/mrtripop/inventory/services/impl/BatchServiceImplTest.java
git commit -m "feat(inventory): implement restoreStock for void reversal

Restores stock quantity to a specific StoreStock with audit logging.
Used when voiding a completed invoice."
```

---

### Task 4: Wire invoice `complete()` to `deductStockByBatch`

**Files:**
- Modify: `src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java`
- Modify: `src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java`
- Modify: `src/test/java/com/mrtripop/transaction/fixture/InvoiceFixture.java`

- [ ] **Step 1: Write the failing test**

Add to `src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java`:

Add a new mock field:
```java
@Mock private BatchService batchService;
```

Add a new `@Nested` class inside `CompleteInvoice`:

```java
@Test
@DisplayName("should deduct stock for each item when completing invoice")
void shouldDeductStockWhenCompletingInvoice() throws ApplicationException {
  // Arrange
  Invoice invoice = InvoiceFixture.pendingInvoice();
  InvoiceItem item = InvoiceFixture.validInvoiceItem(invoice);
  InvoiceDto dto = InvoiceDto.builder()
      .id(1L)
      .storeId(InvoiceFixture.STORE_ID)
      .storeName(InvoiceFixture.STORE_NAME)
      .build();
  InvoiceItemDto itemDto = InvoiceItemDto.builder()
      .id(1L)
      .brandName(InvoiceFixture.BRAND_NAME)
      .build();

  when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
  when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of(item));
  when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
  when(auditService.recordAudit(anyString(), anyString(), anyString(), anyString(), anyString()))
      .thenReturn(null);
  when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(dto);
  when(invoiceMapper.toItemDtoList(anyList())).thenReturn(List.of(itemDto));

  // Act
  InvoiceDto result = invoiceService.complete(1L);

  // Assert
  assertNotNull(result);
  verify(batchService).deductStockByBatch(
      InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID, InvoiceFixture.VALID_QUANTITY);
  verify(invoiceRepository).save(argThat(inv ->
      inv.getStatus() == InvoiceStatus.COMPLETED));
}

@Test
@DisplayName("should rollback and not complete when stock deduction fails")
void shouldRollbackWhenDeductionFails() throws ApplicationException {
  // Arrange
  Invoice invoice = InvoiceFixture.pendingInvoice();
  InvoiceItem item = InvoiceFixture.validInvoiceItem(invoice);

  when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
  when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of(item));
  doThrow(new ApplicationException(
      ErrorCode.INSUFFICIENT_BATCH_QUANTITY, HttpStatus.CONFLICT))
      .when(batchService).deductStockByBatch(any(), any(), any());

  // Act & Assert
  ApplicationException ex = assertThrows(ApplicationException.class,
      () -> invoiceService.complete(1L));
  assertEquals(ErrorCode.INSUFFICIENT_BATCH_QUANTITY, ex.getErrorCode());
}
```

Note: The import for `InvoiceStatus` must be added: `import com.mrtripop.transaction.models.db.InvoiceStatus;` — check if already present.

Also add the import for `BatchService`: `import com.mrtripop.inventory.services.BatchService;`

And for `doThrow`: `import static org.mockito.Mockito.doThrow;`

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=InvoiceServiceImplTest -pl .`
Expected: FAIL — `complete()` doesn't call `batchService.deductStockByBatch()` yet.

- [ ] **Step 3: Wire `complete()` to call `deductStockByBatch`**

In `src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java`:

Add the new dependency:
```java
private final BatchService batchService;
```

Modify the `complete()` method to fetch items and deduct stock before changing status:

```java
@Override
@Transactional(rollbackFor = ApplicationException.class)
public InvoiceDto complete(Long id) throws ApplicationException {
  Invoice invoice = invoiceRepository.findById(id)
      .orElseThrow(() -> new ApplicationException(ErrorCode.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));

  if (invoice.getStatus() != InvoiceStatus.PENDING) {
    if (invoice.getStatus() == InvoiceStatus.COMPLETED) {
      throw new ApplicationException(ErrorCode.INVOICE_ALREADY_COMPLETED, HttpStatus.CONFLICT);
    }
    throw new ApplicationException(ErrorCode.INVOICE_ALREADY_VOIDED, HttpStatus.CONFLICT);
  }

  List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
  for (InvoiceItem item : items) {
    batchService.deductStockByBatch(
        invoice.getStore().getId(), item.getBatch().getId(), item.getQuantity());
  }

  String oldValue = invoice.getStatus().name();
  invoice.setStatus(InvoiceStatus.COMPLETED);
  Invoice savedInvoice = invoiceRepository.save(invoice);

  recordAudit("COMPLETE", "Invoice", String.valueOf(id), oldValue,
      savedInvoice.getTotalAmount().toPlainString());

  InvoiceDto dto = invoiceMapper.toDto(savedInvoice);
  dto.setItems(invoiceMapper.toItemDtoList(items));
  return dto;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=InvoiceServiceImplTest -pl .`
Expected: PASS — all tests in CompleteInvoice group pass, including the two new ones.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java \
        src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java
git commit -m "feat(transaction): wire invoice completion to inventory deduction

Completing a PENDING invoice now deducts stock for each item via
BatchService.deductStockByBatch(). Rolls back if any deduction fails."
```

---

### Task 5: Implement quick `dispense()` (create + complete in one call)

**Files:**
- Modify: `src/main/java/com/mrtripop/transaction/services/InvoiceService.java`
- Modify: `src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java`
- Modify: `src/main/java/com/mrtripop/transaction/controllers/InvoiceController.java`
- Modify: `src/main/java/com/mrtripop/transaction/constant/SuccessCode.java`
- Modify: `src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java`

- [ ] **Step 1: Write the failing test**

Add to `src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java`:

```java
@Nested
@DisplayName("Dispense")
class Dispense {

  @Test
  @DisplayName("should create invoice and deduct stock in single call")
  void shouldCreateAndCompleteInSingleCall() throws ApplicationException {
    // Arrange
    CreateInvoiceRequest request = InvoiceFixture.validCreateRequest();
    Store store = InvoiceFixture.validStore();
    Batch batch = InvoiceFixture.validBatch();
    StoreProduct storeProduct = InvoiceFixture.validStoreProduct();
    StoreStock storeStock = InvoiceFixture.validStoreStock();
    InvoiceDto dto = InvoiceDto.builder()
        .id(1L)
        .storeId(InvoiceFixture.STORE_ID)
        .storeName(InvoiceFixture.STORE_NAME)
        .status("COMPLETED")
        .build();
    InvoiceItemDto itemDto = InvoiceItemDto.builder()
        .id(1L)
        .brandName(InvoiceFixture.BRAND_NAME)
        .build();

    when(storeRepository.findById(InvoiceFixture.STORE_ID)).thenReturn(Optional.of(store));
    when(brandRepository.findById(InvoiceFixture.BRAND_ID))
        .thenReturn(Optional.of(InvoiceFixture.validBrand()));
    when(batchRepository.findById(InvoiceFixture.BATCH_ID)).thenReturn(Optional.of(batch));
    when(storeStockRepository.findByStoreIdAndBatchId(InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID))
        .thenReturn(Optional.of(storeStock));
    when(storeProductRepository.findByStoreIdAndBrandId(InvoiceFixture.STORE_ID, InvoiceFixture.BRAND_ID))
        .thenReturn(Optional.of(storeProduct));
    when(invoiceRepository.save(any(Invoice.class))).thenAnswer(iom -> {
      Invoice entity = iom.getArgument(0);
      if (entity.getId() == null) {
        entity.setId(1L);
      }
      return entity;
    });
    when(invoiceItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(auditService.recordAudit(anyString(), anyString(), anyString(), any(), anyString()))
        .thenReturn(null);
    when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(dto);
    when(invoiceMapper.toItemDtoList(anyList())).thenReturn(List.of(itemDto));
    when(invoiceItemRepository.findByInvoiceId(1L)).thenAnswer(inv -> {
      List<InvoiceItem> items = new ArrayList<>();
      InvoiceItem item = InvoiceItem.builder()
          .id(1L)
          .batch(batch)
          .quantity(InvoiceFixture.VALID_QUANTITY)
          .build();
      items.add(item);
      return items;
    });

    // Act
    InvoiceDto result = invoiceService.dispense(request);

    // Assert
    assertNotNull(result);
    verify(batchService).deductStockByBatch(
        InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID, InvoiceFixture.VALID_QUANTITY);
    verify(invoiceRepository, atLeastOnce()).save(argThat(inv ->
        inv.getStatus() == InvoiceStatus.COMPLETED));
  }
}
```

Note: Add `import java.util.ArrayList;` if not present.

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=InvoiceServiceImplTest -pl .`
Expected: FAIL — `dispense()` not implemented.

- [ ] **Step 3: Add `dispense()` to InvoiceService interface**

Add to `src/main/java/com/mrtripop/transaction/services/InvoiceService.java`:

```java
InvoiceDto dispense(CreateInvoiceRequest request) throws ApplicationException;
```

- [ ] **Step 4: Implement `dispense()` in InvoiceServiceImpl**

Add to `src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java`:

```java
@Override
@Transactional(rollbackFor = ApplicationException.class)
public InvoiceDto dispense(CreateInvoiceRequest request) throws ApplicationException {
  InvoiceDto created = create(request);
  return complete(created.getId());
}
```

- [ ] **Step 5: Add success code**

Add to `src/main/java/com/mrtripop/transaction/constant/SuccessCode.java`:

```java
TXN2008_DISPENSE_IS_SUCCESS("TXN2008", "Dispense is success"),
```

- [ ] **Step 6: Add controller endpoint**

Add to `src/main/java/com/mrtripop/transaction/controllers/InvoiceController.java`:

```java
@PostMapping("/dispense")
public ResponseEntity<Object> dispense(@Valid @RequestBody CreateInvoiceRequest request)
    throws ApplicationException {
  InvoiceDto result = invoiceService.dispense(request);
  BaseStatusCode success = SuccessCode.TXN2008_DISPENSE_IS_SUCCESS;
  return ResponseBody.builder()
      .code(success.getCode())
      .message(success.getMessage())
      .data(result)
      .build()
      .toResponseEntity(HttpStatus.CREATED);
}
```

- [ ] **Step 7: Run test to verify it passes**

Run: `./mvnw test -Dtest=InvoiceServiceImplTest -pl .`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/services/InvoiceService.java \
        src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java \
        src/main/java/com/mrtripop/transaction/controllers/InvoiceController.java \
        src/main/java/com/mrtripop/transaction/constant/SuccessCode.java \
        src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java
git commit -m "feat(transaction): add quick dispense endpoint for POS

POST /api/v1/transaction/invoices/dispense creates and completes
an invoice in one transactional call, deducting stock automatically."
```

---

### Task 6: Add stock restoration on void of completed invoices

**Files:**
- Modify: `src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java`
- Modify: `src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java`

- [ ] **Step 1: Write the failing test**

Add to `src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java` inside the `VoidInvoice` nested class:

```java
@Test
@DisplayName("should restore stock when voiding a completed invoice")
void shouldRestoreStockWhenVoidingCompletedInvoice() throws ApplicationException {
  // Arrange
  Invoice invoice = InvoiceFixture.completedInvoice();
  InvoiceItem item = InvoiceFixture.validInvoiceItem(invoice);
  InvoiceDto dto = InvoiceDto.builder()
      .id(1L)
      .storeId(InvoiceFixture.STORE_ID)
      .storeName(InvoiceFixture.STORE_NAME)
      .build();

  when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
  when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of(item));
  when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
  when(auditService.recordAudit(anyString(), anyString(), anyString(), anyString(), anyString()))
      .thenReturn(null);
  when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(dto);
  when(invoiceMapper.toItemDtoList(anyList())).thenReturn(List.of());

  // Act
  InvoiceDto result = invoiceService.voidInvoice(1L);

  // Assert
  assertNotNull(result);
  verify(batchService).restoreStock(
      InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID, InvoiceFixture.VALID_QUANTITY);
  verify(invoiceRepository).save(argThat(inv ->
      inv.getStatus() == InvoiceStatus.VOIDED));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=InvoiceServiceImplTest -pl .`
Expected: FAIL — `voidInvoice()` doesn't call `restoreStock()` for completed invoices.

- [ ] **Step 3: Implement restock in `voidInvoice()`**

Replace the `voidInvoice()` method in `src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java`:

```java
@Override
@Transactional(rollbackFor = ApplicationException.class)
public InvoiceDto voidInvoice(Long id) throws ApplicationException {
  Invoice invoice = invoiceRepository.findById(id)
      .orElseThrow(() -> new ApplicationException(ErrorCode.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));

  if (invoice.getStatus() == InvoiceStatus.VOIDED) {
    throw new ApplicationException(ErrorCode.INVOICE_ALREADY_VOIDED, HttpStatus.CONFLICT);
  }

  if (invoice.getStatus() == InvoiceStatus.COMPLETED) {
    List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
    for (InvoiceItem item : items) {
      batchService.restoreStock(
          invoice.getStore().getId(), item.getBatch().getId(), item.getQuantity());
    }
  }

  String oldValue = invoice.getStatus().name();
  invoice.setStatus(InvoiceStatus.VOIDED);
  Invoice savedInvoice = invoiceRepository.save(invoice);

  recordAudit("VOID", "Invoice", String.valueOf(id), oldValue,
      savedInvoice.getTotalAmount().toPlainString());

  InvoiceDto dto = invoiceMapper.toDto(savedInvoice);
  List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
  dto.setItems(invoiceMapper.toItemDtoList(items));
  return dto;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=InvoiceServiceImplTest -pl .`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java \
        src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java
git commit -m "feat(transaction): restore stock when voiding completed invoice

Voiding a COMPLETED invoice now restores deducted stock via
BatchService.restoreStock(). PENDING invoices void without restock."
```

---

### Task 7: Daily sales summary

**Files:**
- Create: `src/main/java/com/mrtripop/transaction/models/dto/DailySalesSummaryDto.java`
- Modify: `src/main/java/com/mrtripop/transaction/repository/InvoiceRepository.java`
- Modify: `src/main/java/com/mrtripop/transaction/repository/InvoiceItemRepository.java`
- Modify: `src/main/java/com/mrtripop/transaction/services/InvoiceService.java`
- Modify: `src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java`
- Modify: `src/main/java/com/mrtripop/transaction/controllers/InvoiceController.java`
- Modify: `src/main/java/com/mrtripop/transaction/constant/SuccessCode.java`

- [ ] **Step 1: Create the response DTO**

Create `src/main/java/com/mrtripop/transaction/models/dto/DailySalesSummaryDto.java`:

```java
package com.mrtripop.transaction.models.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesSummaryDto {

  private LocalDate date;
  private int totalInvoices;
  private BigDecimal totalRevenue;
  private BigDecimal totalPatientPaid;
  private BigDecimal totalInsuranceClaims;
  private long totalItemsDispensed;
  private int voidedCount;
}
```

- [ ] **Step 2: Add aggregation queries to repositories**

Add to `src/main/java/com/mrtripop/transaction/repository/InvoiceRepository.java`:

```java
@Query("SELECT i FROM Invoice i WHERE i.store.id = :storeId "
    + "AND i.status = :status AND i.createdAt >= :startOfDay AND i.createdAt < :endOfDay")
List<Invoice> findByStoreIdAndStatusAndCreatedAtRange(
    @Param("storeId") UUID storeId,
    @Param("status") InvoiceStatus status,
    @Param("startOfDay") long startOfDay,
    @Param("endOfDay") long endOfDay);
```

Also add import for `InvoiceStatus` and `UUID` if not present:
```java
import com.mrtripop.transaction.models.db.InvoiceStatus;
import java.util.UUID;
```

Add to `src/main/java/com/mrtripop/transaction/repository/InvoiceItemRepository.java`:

```java
@Query("SELECT COALESCE(SUM(ii.quantity), 0) FROM InvoiceItem ii "
    + "WHERE ii.invoice.id IN :invoiceIds")
Long sumQuantityByInvoiceIds(@Param("invoiceIds") List<Long> invoiceIds);
```

Also add import for `List`:
```java
import java.util.List;
```

- [ ] **Step 3: Write the failing test**

Add to `src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java`:

```java
@Nested
@DisplayName("DailySummary")
class DailySummary {

  @Test
  @DisplayName("should return daily sales summary for a store")
  void shouldReturnDailySummary() throws ApplicationException {
    // Arrange
    UUID storeId = InvoiceFixture.STORE_ID;
    LocalDate date = LocalDate.now();
    Invoice completedInvoice = InvoiceFixture.completedInvoice();
    Invoice voidedInvoice = InvoiceFixture.voidedInvoice();
    InvoiceItem item = InvoiceFixture.validInvoiceItem(completedInvoice);
    List<Long> completedIds = List.of(1L);

    when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtRange(
        eq(storeId), eq(InvoiceStatus.COMPLETED), anyLong(), anyLong()))
        .thenReturn(List.of(completedInvoice));
    when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtRange(
        eq(storeId), eq(InvoiceStatus.VOIDED), anyLong(), anyLong()))
        .thenReturn(List.of(voidedInvoice));
    when(invoiceItemRepository.sumQuantityByInvoiceIds(completedIds))
        .thenReturn(InvoiceFixture.VALID_QUANTITY);

    // Act
    DailySalesSummaryDto result = invoiceService.getDailySummary(storeId, date);

    // Assert
    assertNotNull(result);
    assertEquals(date, result.getDate());
    assertEquals(1, result.getTotalInvoices());
    assertEquals(InvoiceFixture.VALID_QUANTITY.longValue(), result.getTotalItemsDispensed());
    assertEquals(1, result.getVoidedCount());
  }

  @Test
  @DisplayName("should return zero counts when no invoices exist")
  void shouldReturnZeroCountsWhenNoInvoices() throws ApplicationException {
    // Arrange
    UUID storeId = InvoiceFixture.STORE_ID;
    LocalDate date = LocalDate.now();

    when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtRange(
        eq(storeId), eq(InvoiceStatus.COMPLETED), anyLong(), anyLong()))
        .thenReturn(List.of());
    when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtRange(
        eq(storeId), eq(InvoiceStatus.VOIDED), anyLong(), anyLong()))
        .thenReturn(List.of());

    // Act
    DailySalesSummaryDto result = invoiceService.getDailySummary(storeId, date);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getTotalInvoices());
    assertEquals(0, result.getTotalItemsDispensed());
    assertEquals(0, result.getVoidedCount());
    assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
  }
}
```

Add imports:
```java
import com.mrtripop.transaction.models.dto.DailySalesSummaryDto;
import java.time.LocalDate;
```

- [ ] **Step 4: Run test to verify it fails**

Run: `./mvnw test -Dtest=InvoiceServiceImplTest -pl .`
Expected: FAIL — `getDailySummary()` not implemented.

- [ ] **Step 5: Add method to InvoiceService interface**

Add to `src/main/java/com/mrtripop/transaction/services/InvoiceService.java`:

```java
DailySalesSummaryDto getDailySummary(UUID storeId, LocalDate date) throws ApplicationException;
```

Add imports:
```java
import com.mrtripop.transaction.models.dto.DailySalesSummaryDto;
import java.time.LocalDate;
```

- [ ] **Step 6: Implement `getDailySummary` in InvoiceServiceImpl**

Add to `src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java`:

```java
@Override
@Transactional(readOnly = true)
public DailySalesSummaryDto getDailySummary(UUID storeId, LocalDate date)
    throws ApplicationException {
  LocalDate targetDate = date != null ? date : LocalDate.now();
  long startOfDay = targetDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
  long endOfDay = targetDate.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();

  List<Invoice> completedInvoices = invoiceRepository.findByStoreIdAndStatusAndCreatedAtRange(
      storeId, InvoiceStatus.COMPLETED, startOfDay, endOfDay);
  List<Invoice> voidedInvoices = invoiceRepository.findByStoreIdAndStatusAndCreatedAtRange(
      storeId, InvoiceStatus.VOIDED, startOfDay, endOfDay);

  BigDecimal totalRevenue = completedInvoices.stream()
      .map(Invoice::getTotalAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  BigDecimal totalPatientPaid = completedInvoices.stream()
      .map(Invoice::getPatientOwed)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  BigDecimal totalInsuranceClaims = completedInvoices.stream()
      .map(Invoice::getInsuranceClaimAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

  long totalItemsDispensed = 0;
  if (!completedInvoices.isEmpty()) {
    List<Long> completedIds = completedInvoices.stream()
        .map(Invoice::getId)
        .toList();
    totalItemsDispensed = invoiceItemRepository.sumQuantityByInvoiceIds(completedIds);
  }

  return DailySalesSummaryDto.builder()
      .date(targetDate)
      .totalInvoices(completedInvoices.size())
      .totalRevenue(totalRevenue)
      .totalPatientPaid(totalPatientPaid)
      .totalInsuranceClaims(totalInsuranceClaims)
      .totalItemsDispensed(totalItemsDispensed)
      .voidedCount(voidedInvoices.size())
      .build();
}
```

Add imports:
```java
import com.mrtripop.transaction.models.dto.DailySalesSummaryDto;
import java.time.LocalDate;
import java.time.ZoneId;
```

- [ ] **Step 7: Add success code**

Add to `src/main/java/com/mrtripop/transaction/constant/SuccessCode.java`:

```java
TXN2009_GET_DAILY_SUMMARY_IS_SUCCESS("TXN2009", "Get daily summary is success"),
```

- [ ] **Step 8: Add controller endpoint**

Add to `src/main/java/com/mrtripop/transaction/controllers/InvoiceController.java`:

```java
@GetMapping("/daily-summary")
public ResponseEntity<Object> getDailySummary(
    @RequestParam UUID storeId,
    @RequestParam(required = false) LocalDate date) throws ApplicationException {
  DailySalesSummaryDto result = invoiceService.getDailySummary(storeId, date);
  BaseStatusCode success = SuccessCode.TXN2009_GET_DAILY_SUMMARY_IS_SUCCESS;
  return ResponseBody.builder()
      .code(success.getCode())
      .message(success.getMessage())
      .data(result)
      .build()
      .toResponseEntity(HttpStatus.OK);
}
```

Add imports:
```java
import com.mrtripop.transaction.models.dto.DailySalesSummaryDto;
import java.time.LocalDate;
```

- [ ] **Step 9: Run test to verify it passes**

Run: `./mvnw test -Dtest=InvoiceServiceImplTest -pl .`
Expected: PASS

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/models/dto/DailySalesSummaryDto.java \
        src/main/java/com/mrtripop/transaction/repository/InvoiceRepository.java \
        src/main/java/com/mrtripop/transaction/repository/InvoiceItemRepository.java \
        src/main/java/com/mrtripop/transaction/services/InvoiceService.java \
        src/main/java/com/mrtripop/transaction/services/impl/InvoiceServiceImpl.java \
        src/main/java/com/mrtripop/transaction/controllers/InvoiceController.java \
        src/main/java/com/mrtripop/transaction/constant/SuccessCode.java \
        src/test/java/com/mrtripop/transaction/services/impl/InvoiceServiceImplTest.java
git commit -m "feat(transaction): add daily sales summary endpoint

GET /api/v1/transaction/invoices/daily-summary returns total invoices,
revenue, patient paid, insurance claims, items dispensed, and voided
count for a given store and date."
```

---

### Task 8: Run full test suite and verify compilation

- [ ] **Step 1: Run all tests**

Run: `./mvnw clean test`
Expected: All tests pass.

- [ ] **Step 2: Verify compilation**

Run: `./mvnw clean compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Run detect_changes to verify scope**

Run GitNexus `detect_changes` to confirm changes only affect expected symbols and flows.

- [ ] **Step 4: Final commit if any fixups needed**

```bash
git add -A
git commit -m "chore: fix compilation and test issues from POS dispensing implementation"
```
