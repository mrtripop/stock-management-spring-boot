# Partial Return & Refund Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let pharmacy staff partially return items from a `COMPLETED` invoice — restocking the
returned quantity to its original batch and reducing the invoice's monetary totals — without
touching the rest of the sale.

**Architecture:** New `Return`/`ReturnItem` entities, repository, MapStruct mapper, service, and
controller, all inside the existing `com.mrtripop.transaction` package (same home as `Invoice`).
No changes to `com.mrtripop.inventory` — the existing `BatchService.restoreStock(storeId, batchId,
quantity)` is reused unchanged, called once per returned line. Invoice status never changes; "how
much of an item has been returned so far" is always derived by summing `ReturnItem.quantity`.

**Tech Stack:** Java 17, Spring Boot 3.4.2, Spring Data JPA (Hibernate `ddl-auto: update` — no
migration scripts needed, entities are DDL'd automatically), Lombok, MapStruct 1.6.0, JUnit 5 +
Mockito, Google Java Format.

**Spec:** `docs/superpowers/specs/2026-09-15-partial-return-refund-design.md`

## Global Constraints

- All money fields: `BigDecimal`, `precision = 10, scale = 2`, rounding `RoundingMode.HALF_UP`,
  computed as one `multiply().divide(..., 2, HALF_UP)` expression — never pre-compute a separate
  fraction value (avoids compounding rounding error).
- `ReturnReason` values: `DAMAGED, EXPIRED, WRONG_ITEM, CUSTOMER_CHANGED_MIND, OTHER`.
- Everything lives under `com.mrtripop.transaction` — no new top-level package.
- Every returned unit always restocks to `AVAILABLE` on its original batch — no
  damaged/discard-instead-of-restock path in this feature.
- Invoice stays `COMPLETED` after a return — no new `InvoiceStatus` value.
- A `Return` is immutable once created — no update/cancel endpoint.
- New error codes continue the existing `TXN40xx` sequence starting at `TXN4012` (current highest
  is `TXN4011`). New success codes continue `TXN20xx` starting at `TXN2010` (current highest is
  `TXN2009`).
- Follow `.claude/rules/coding-style.md`, `.claude/rules/api-design.md`, and
  `.claude/rules/testing-style.md` exactly — these were read in full while writing this plan; code
  samples below already conform to them.
- Per this project's `CLAUDE.md`: this repo is indexed by GitNexus. Editing an **existing** symbol
  (as opposed to adding a new file) requires running `impact({target: "<symbolName>",
  direction: "upstream"})` first and reporting the blast radius before editing — flagged explicitly
  on the one task that touches existing files (Task 4). The final task runs `detect_changes()`
  against `develop` as the pre-merge regression gate for the whole branch.
- Commit messages: Conventional Commits, scope `transaction`, body in imperative mood, footer
  `Ticket: N/A` (no ticket for this feature), followed by the attribution lines shown in every
  commit step below — copy them verbatim.

---

## File Structure

```
src/main/java/com/mrtripop/transaction/
├── models/db/
│   ├── ReturnReason.java        (new — Task 1)
│   ├── Return.java              (new — Task 2)
│   └── ReturnItem.java          (new — Task 3)
├── constant/
│   ├── ErrorCode.java           (modified — Task 4)
│   └── SuccessCode.java         (modified — Task 4)
├── repository/
│   ├── ReturnRepository.java        (new — Task 5)
│   └── ReturnItemRepository.java    (new — Task 5)
├── models/dto/
│   ├── ReturnItemRequest.java   (new — Task 6)
│   ├── CreateReturnRequest.java (new — Task 6)
│   ├── ReturnItemDto.java       (new — Task 6)
│   └── ReturnDto.java           (new — Task 6)
├── component/
│   └── ReturnMapper.java        (new — Task 7)
├── services/
│   ├── ReturnService.java           (new — Task 9)
│   └── impl/ReturnServiceImpl.java  (new — Tasks 9-11)
└── controllers/
    └── ReturnController.java    (new — Task 12)

src/test/java/com/mrtripop/transaction/
├── fixture/ReturnFixture.java              (new — Task 8)
└── services/impl/ReturnServiceImplTest.java (new — Tasks 9-11)
```

---

### Task 1: `ReturnReason` enum

**Files:**
- Create: `src/main/java/com/mrtripop/transaction/models/db/ReturnReason.java`

**Interfaces:**
- Produces: `enum ReturnReason { DAMAGED, EXPIRED, WRONG_ITEM, CUSTOMER_CHANGED_MIND, OTHER }` —
  used by `Return` (Task 2), `CreateReturnRequest` (Task 6), `ReturnDto` (Task 6).

This is a plain enum with no service logic, so there's nothing to TDD — write it directly.

- [ ] **Step 1: Create the enum**

```java
package com.mrtripop.transaction.models.db;

public enum ReturnReason {
  DAMAGED,
  EXPIRED,
  WRONG_ITEM,
  CUSTOMER_CHANGED_MIND,
  OTHER
}
```

- [ ] **Step 2: Compile**

Run: `./mvnw -q compile`
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/models/db/ReturnReason.java
git commit -m "$(cat <<'EOF'
feat(transaction): add ReturnReason enum

Details:
- Introduce the fixed set of reasons a partial return can be filed
under, ahead of the Return entity that references it.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 2: `Return` entity

**Files:**
- Create: `src/main/java/com/mrtripop/transaction/models/db/Return.java`

**Interfaces:**
- Consumes: `ReturnReason` (Task 1), `Invoice` (existing,
  `com.mrtripop.transaction.models.db.Invoice`), `AuditEntity` (existing,
  `com.mrtripop.product.models.db.AuditEntity`, gives `createdAt`/`updatedAt` as `Long`).
- Produces: `Return` entity with `getId()/getInvoice()/getReason()/getTotalRefundAmount()`
  (Lombok `@Getter`/`@Setter`/`@SuperBuilder`) — consumed by `ReturnItem` (Task 3),
  `ReturnRepository` (Task 5), `ReturnMapper` (Task 7), `ReturnServiceImpl` (Task 9).

No business logic here either — a plain JPA entity, matching `Invoice`'s exact annotation style.

- [ ] **Step 1: Create the entity**

```java
package com.mrtripop.transaction.models.db;

import com.mrtripop.product.models.db.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "returns",
    indexes = {@Index(name = "idx_returns_invoice_id", columnList = "invoice_id")})
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Return extends AuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "returns_sequence")
  @SequenceGenerator(name = "returns_sequence", sequenceName = "returns_sequence", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private Invoice invoice;

  @Enumerated(EnumType.STRING)
  @Column(name = "reason", nullable = false, length = 30)
  private ReturnReason reason;

  @Column(name = "total_refund_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalRefundAmount;
}
```

- [ ] **Step 2: Compile**

Run: `./mvnw -q compile`
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/models/db/Return.java
git commit -m "$(cat <<'EOF'
feat(transaction): add Return entity

Details:
- Add the Return entity that records a partial-return event against
a completed invoice, ahead of its line-item child entity.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 3: `ReturnItem` entity

**Files:**
- Create: `src/main/java/com/mrtripop/transaction/models/db/ReturnItem.java`

**Interfaces:**
- Consumes: `Return` (Task 2), `InvoiceItem` (existing,
  `com.mrtripop.transaction.models.db.InvoiceItem`).
- Produces: `ReturnItem` entity with `getId()/getParentReturn()/getInvoiceItem()/getQuantity()/
  getRefundAmount()` and `setParentReturn(Return)` — consumed by `ReturnItemRepository` (Task 5),
  `ReturnMapper` (Task 7), `ReturnServiceImpl` (Task 9).

**Naming note:** `return` is a reserved word, so the back-reference field is `parentReturn` (not
`return`), matching the note in the spec's Data Model section.

- [ ] **Step 1: Create the entity**

```java
package com.mrtripop.transaction.models.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "return_items",
    indexes = {
      @Index(name = "idx_return_items_return_id", columnList = "return_id"),
      @Index(name = "idx_return_items_invoice_item_id", columnList = "invoice_item_id")
    })
@SuperBuilder
@Getter
@Setter
@ToString(exclude = {"parentReturn"})
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItem {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "return_items_sequence")
  @SequenceGenerator(
      name = "return_items_sequence",
      sequenceName = "return_items_sequence",
      allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "return_id", nullable = false)
  private Return parentReturn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_item_id", nullable = false)
  private InvoiceItem invoiceItem;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal refundAmount;
}
```

- [ ] **Step 2: Compile**

Run: `./mvnw -q compile`
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/models/db/ReturnItem.java
git commit -m "$(cat <<'EOF'
feat(transaction): add ReturnItem entity

Details:
- Add the line-item entity for a Return, tying a returned quantity
back to the specific InvoiceItem/batch it was sold from.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 4: Extend `ErrorCode` and `SuccessCode`

**Files:**
- Modify: `src/main/java/com/mrtripop/transaction/constant/ErrorCode.java`
- Modify: `src/main/java/com/mrtripop/transaction/constant/SuccessCode.java`

**Interfaces:**
- Produces: `ErrorCode.INVOICE_NOT_COMPLETED`, `ErrorCode.RETURN_QUANTITY_EXCEEDS_AVAILABLE`,
  `ErrorCode.INVOICE_ITEM_NOT_FOUND`, `ErrorCode.RETURN_NOT_FOUND` — thrown by `ReturnServiceImpl`
  (Tasks 9-11). `SuccessCode.TXN2010_CREATE_RETURN_IS_SUCCESS`,
  `SuccessCode.TXN2011_GET_RETURNS_BY_INVOICE_IS_SUCCESS`,
  `SuccessCode.TXN2012_GET_RETURN_BY_ID_IS_SUCCESS` — used by `ReturnController` (Task 12).

These are **existing** files/symbols, so per this project's GitNexus mandate run impact analysis
before editing them.

- [ ] **Step 1: Run impact analysis on both enums before editing**

Run the GitNexus `impact` tool (`mcp__gitnexus__impact`) with `{target: "ErrorCode",
direction: "upstream"}` and again with `{target: "SuccessCode", direction: "upstream"}` (or scope
to the `transaction` module's `ErrorCode`/`SuccessCode` if the tool asks to disambiguate from other
domains' enums of the same name). Report the blast radius. Expected: LOW risk — this is a purely
additive change (new enum constants), not a modification of any existing constant's code or
message, so no existing caller (`InvoiceController`, `InvoiceServiceImpl`, etc.) is affected. If the
tool reports HIGH/CRITICAL risk, stop and report to the user before proceeding — that would mean
something unexpected is depending on the enum's exact constant set.

- [ ] **Step 2: Add the four new error codes**

Edit `src/main/java/com/mrtripop/transaction/constant/ErrorCode.java` — add after
`RECEIPT_NOT_AVAILABLE`:

```java
  RECEIPT_NOT_AVAILABLE("TXN4011", "Receipt is not available for this invoice status"),
  INVOICE_NOT_COMPLETED("TXN4012", "Invoice must be completed before it can be returned"),
  RETURN_QUANTITY_EXCEEDS_AVAILABLE(
      "TXN4013", "Return quantity exceeds the remaining returnable quantity"),
  INVOICE_ITEM_NOT_FOUND("TXN4014", "Invoice item not found"),
  RETURN_NOT_FOUND("TXN4015", "Return not found");
```

(Change the trailing `;` on the old last line to `,` and move it to the new last line, as shown.)

- [ ] **Step 3: Add the three new success codes**

Edit `src/main/java/com/mrtripop/transaction/constant/SuccessCode.java` — add after
`TXN2009_GET_DAILY_SUMMARY_IS_SUCCESS`:

```java
  TXN2009_GET_DAILY_SUMMARY_IS_SUCCESS("TXN2009", "Get daily summary is success"),
  TXN2010_CREATE_RETURN_IS_SUCCESS("TXN2010", "Create return is success"),
  TXN2011_GET_RETURNS_BY_INVOICE_IS_SUCCESS("TXN2011", "Get returns by invoice is success"),
  TXN2012_GET_RETURN_BY_ID_IS_SUCCESS("TXN2012", "Get return by ID is success");
```

(Same trailing-`;`-to-`,` adjustment.)

- [ ] **Step 4: Compile**

Run: `./mvnw -q compile`
Expected: no errors.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/constant/ErrorCode.java \
        src/main/java/com/mrtripop/transaction/constant/SuccessCode.java
git commit -m "$(cat <<'EOF'
feat(transaction): add error and success codes for returns

Details:
- Extend the TXN40xx/TXN20xx enums with the four error cases and
three success responses the partial-return feature needs.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 5: `ReturnRepository` and `ReturnItemRepository`

**Files:**
- Create: `src/main/java/com/mrtripop/transaction/repository/ReturnRepository.java`
- Create: `src/main/java/com/mrtripop/transaction/repository/ReturnItemRepository.java`

**Interfaces:**
- Consumes: `Return` (Task 2), `ReturnItem` (Task 3).
- Produces: `ReturnRepository.findByInvoiceId(Long invoiceId, Pageable pageable): Page<Return>`;
  `ReturnItemRepository.findByParentReturnId(Long returnId): List<ReturnItem>`;
  `ReturnItemRepository.sumQuantityByInvoiceItemId(Long invoiceItemId): Long` — all consumed by
  `ReturnServiceImpl` (Tasks 9-11).

This project has no repository-level tests anywhere (verified: `find src/test -iname
"*RepositoryTest*"` returns nothing) — Spring Data query methods are exercised indirectly through
service tests with mocks, matching `InvoiceItemRepository`'s own `sumQuantityByInvoiceIds`. Follow
that convention; no dedicated repository test here.

- [ ] **Step 1: Create `ReturnRepository`**

```java
package com.mrtripop.transaction.repository;

import com.mrtripop.transaction.models.db.Return;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {

  Page<Return> findByInvoiceId(Long invoiceId, Pageable pageable);
}
```

- [ ] **Step 2: Create `ReturnItemRepository`**

```java
package com.mrtripop.transaction.repository;

import com.mrtripop.transaction.models.db.ReturnItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {

  List<ReturnItem> findByParentReturnId(Long returnId);

  @Query("SELECT COALESCE(SUM(ri.quantity), 0) FROM ReturnItem ri "
      + "WHERE ri.invoiceItem.id = :invoiceItemId")
  Long sumQuantityByInvoiceItemId(@Param("invoiceItemId") Long invoiceItemId);
}
```

- [ ] **Step 3: Compile**

Run: `./mvnw -q compile`
Expected: no errors.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/repository/ReturnRepository.java \
        src/main/java/com/mrtripop/transaction/repository/ReturnItemRepository.java
git commit -m "$(cat <<'EOF'
feat(transaction): add Return and ReturnItem repositories

Details:
- Add the Spring Data repositories for returns, including the
already-returned-quantity sum query the partial-return validation
depends on.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 6: Request/response DTOs

**Files:**
- Create: `src/main/java/com/mrtripop/transaction/models/dto/ReturnItemRequest.java`
- Create: `src/main/java/com/mrtripop/transaction/models/dto/CreateReturnRequest.java`
- Create: `src/main/java/com/mrtripop/transaction/models/dto/ReturnItemDto.java`
- Create: `src/main/java/com/mrtripop/transaction/models/dto/ReturnDto.java`

**Interfaces:**
- Consumes: `ReturnReason` (Task 1).
- Produces: `ReturnItemRequest{invoiceItemId, quantity}`, `CreateReturnRequest{reason, items}`,
  `ReturnItemDto{id, invoiceItemId, brandName, batchNumber, quantity, refundAmount}`,
  `ReturnDto{id, invoiceId, reason, totalRefundAmount, items, createdAt, updatedAt}` — consumed by
  `ReturnMapper` (Task 7), `ReturnServiceImpl` (Tasks 9-11), `ReturnController` (Task 12).

Plain DTOs, no logic — matches `InvoiceItemRequest`/`CreateInvoiceRequest`/`InvoiceItemDto`/
`InvoiceDto` exactly in structure and validation style.

- [ ] **Step 1: Create `ReturnItemRequest`**

```java
package com.mrtripop.transaction.models.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemRequest {

  @NotNull(message = "Invoice item ID is required")
  @Min(value = 1, message = "Invoice item ID must be a positive number")
  private Long invoiceItemId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Long quantity;
}
```

- [ ] **Step 2: Create `CreateReturnRequest`**

```java
package com.mrtripop.transaction.models.dto;

import com.mrtripop.transaction.models.db.ReturnReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnRequest {

  @NotNull(message = "Reason is required")
  private ReturnReason reason;

  @NotEmpty(message = "Return items must not be empty")
  @Valid
  private List<ReturnItemRequest> items;
}
```

- [ ] **Step 3: Create `ReturnItemDto`**

```java
package com.mrtripop.transaction.models.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemDto {

  private Long id;
  private Long invoiceItemId;
  private String brandName;
  private String batchNumber;
  private Long quantity;
  private BigDecimal refundAmount;
}
```

- [ ] **Step 4: Create `ReturnDto`**

```java
package com.mrtripop.transaction.models.dto;

import com.mrtripop.transaction.models.db.ReturnReason;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDto {

  private Long id;
  private Long invoiceId;
  private ReturnReason reason;
  private BigDecimal totalRefundAmount;
  private List<ReturnItemDto> items;
  private Long createdAt;
  private Long updatedAt;
}
```

- [ ] **Step 5: Compile**

Run: `./mvnw -q compile`
Expected: no errors.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/models/dto/ReturnItemRequest.java \
        src/main/java/com/mrtripop/transaction/models/dto/CreateReturnRequest.java \
        src/main/java/com/mrtripop/transaction/models/dto/ReturnItemDto.java \
        src/main/java/com/mrtripop/transaction/models/dto/ReturnDto.java
git commit -m "$(cat <<'EOF'
feat(transaction): add return request and response DTOs

Details:
- Add the request/response DTOs the return API surface needs,
following the same shape as the existing invoice DTOs.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 7: `ReturnMapper`

**Files:**
- Create: `src/main/java/com/mrtripop/transaction/component/ReturnMapper.java`

**Interfaces:**
- Consumes: `Return` (Task 2), `ReturnItem` (Task 3), `ReturnDto`/`ReturnItemDto` (Task 6).
- Produces: `ReturnMapper.toDto(Return): ReturnDto` (leaves `items` null — set by the caller, same
  pattern `InvoiceMapper.toDto` uses), `ReturnMapper.toItemDto(ReturnItem): ReturnItemDto`,
  `ReturnMapper.toItemDtoList(List<ReturnItem>): List<ReturnItemDto>` — consumed by
  `ReturnServiceImpl` (Tasks 9-11).

Mirrors `InvoiceMapper` exactly.

- [ ] **Step 1: Create the mapper**

```java
package com.mrtripop.transaction.component;

import com.mrtripop.transaction.models.db.Return;
import com.mrtripop.transaction.models.db.ReturnItem;
import com.mrtripop.transaction.models.dto.ReturnDto;
import com.mrtripop.transaction.models.dto.ReturnItemDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReturnMapper {

  @Mapping(target = "invoiceId", source = "invoice.id")
  @Mapping(target = "items", ignore = true)
  ReturnDto toDto(Return returnEntity);

  @Mapping(target = "invoiceItemId", source = "invoiceItem.id")
  @Mapping(target = "brandName", source = "invoiceItem.brand.brandName")
  @Mapping(target = "batchNumber", source = "invoiceItem.batch.batchNumber")
  ReturnItemDto toItemDto(ReturnItem item);

  List<ReturnItemDto> toItemDtoList(List<ReturnItem> items);
}
```

- [ ] **Step 2: Compile (generates the MapStruct impl)**

Run: `./mvnw -q compile`
Expected: no errors. Verify the generated impl exists:
`find target/generated-sources -name "ReturnMapperImpl.java"`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/component/ReturnMapper.java
git commit -m "$(cat <<'EOF'
feat(transaction): add ReturnMapper

Details:
- Add the MapStruct mapper from Return/ReturnItem entities to their
response DTOs, mirroring InvoiceMapper.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 8: `ReturnFixture` test fixture

**Files:**
- Create: `src/test/java/com/mrtripop/transaction/fixture/ReturnFixture.java`

**Interfaces:**
- Consumes: `InvoiceFixture` (existing — reuses its `STORE_ID`, `BATCH_ID`, `BRAND_ID` etc. so a
  `ReturnFixture` invoice item lines up with the same store/batch/brand data), `Return`,
  `ReturnItem`, `ReturnReason`, `CreateReturnRequest`, `ReturnItemRequest` (Tasks 1-3, 6).
- Produces: constants and factory methods consumed by `ReturnServiceImplTest` (Tasks 9-11) — see
  full list in Step 1.

Following `testing-style.md`'s Fixture pattern: constants shared between setup and assertions,
`private` constructor, static factories.

- [ ] **Step 1: Create the fixture**

```java
package com.mrtripop.transaction.fixture;

import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.Return;
import com.mrtripop.transaction.models.db.ReturnItem;
import com.mrtripop.transaction.models.db.ReturnReason;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnItemRequest;
import java.math.BigDecimal;
import java.util.List;

public final class ReturnFixture {

  private ReturnFixture() {}

  // Reuses InvoiceFixture.validInvoiceItem(invoice): quantity=5, unitPrice=10.00,
  // patientOwed=35.00, insuranceClaimAmount=15.00 — a clean 70/30 split with no rounding.
  public static final Long RETURN_QUANTITY = 2L;
  public static final BigDecimal EXPECTED_REFUND_AMOUNT = new BigDecimal("20.00");
  public static final BigDecimal EXPECTED_REFUND_PATIENT_OWED = new BigDecimal("14.00");
  public static final BigDecimal EXPECTED_REFUND_INSURANCE_CLAIM = new BigDecimal("6.00");
  public static final BigDecimal EXPECTED_NEW_TOTAL_AMOUNT = new BigDecimal("30.00");
  public static final BigDecimal EXPECTED_NEW_PATIENT_OWED = new BigDecimal("21.00");
  public static final BigDecimal EXPECTED_NEW_INSURANCE_CLAIM = new BigDecimal("9.00");

  // A line whose quantity (3) does not evenly divide its money fields, to exercise
  // HALF_UP rounding on a 1-unit return: 10.00*1/3 = 3.33(3)->3.33, 5.00*1/3 = 1.66(6)->1.67.
  public static final Long ROUNDING_ITEM_QUANTITY = 3L;
  public static final BigDecimal ROUNDING_UNIT_PRICE = new BigDecimal("5.00");
  public static final BigDecimal ROUNDING_PATIENT_OWED = new BigDecimal("10.00");
  public static final BigDecimal ROUNDING_INSURANCE_CLAIM = new BigDecimal("5.00");
  public static final Long ROUNDING_RETURN_QUANTITY = 1L;
  public static final BigDecimal EXPECTED_ROUNDED_REFUND_AMOUNT = new BigDecimal("5.00");
  public static final BigDecimal EXPECTED_ROUNDED_PATIENT_OWED = new BigDecimal("3.33");
  public static final BigDecimal EXPECTED_ROUNDED_INSURANCE_CLAIM = new BigDecimal("1.67");

  public static InvoiceItem invoiceItemForRounding(Invoice invoice) {
    return InvoiceItem.builder()
        .id(2L)
        .invoice(invoice)
        .brand(InvoiceFixture.validBrand())
        .batch(InvoiceFixture.validBatch())
        .quantity(ROUNDING_ITEM_QUANTITY)
        .unitPrice(ROUNDING_UNIT_PRICE)
        .lineTotal(new BigDecimal("15.00"))
        .patientOwed(ROUNDING_PATIENT_OWED)
        .insuranceClaimAmount(ROUNDING_INSURANCE_CLAIM)
        .insuranceCoveragePercent(33)
        .build();
  }

  public static ReturnItemRequest validItemRequest(Long invoiceItemId) {
    return ReturnItemRequest.builder().invoiceItemId(invoiceItemId).quantity(RETURN_QUANTITY).build();
  }

  public static CreateReturnRequest validCreateRequest(Long invoiceItemId) {
    return CreateReturnRequest.builder()
        .reason(ReturnReason.CUSTOMER_CHANGED_MIND)
        .items(List.of(validItemRequest(invoiceItemId)))
        .build();
  }

  public static Return validReturn(Invoice invoice) {
    return Return.builder()
        .id(1L)
        .invoice(invoice)
        .reason(ReturnReason.CUSTOMER_CHANGED_MIND)
        .totalRefundAmount(EXPECTED_REFUND_AMOUNT)
        .build();
  }

  public static ReturnItem validReturnItem(Return parentReturn, InvoiceItem invoiceItem) {
    return ReturnItem.builder()
        .id(1L)
        .parentReturn(parentReturn)
        .invoiceItem(invoiceItem)
        .quantity(RETURN_QUANTITY)
        .refundAmount(EXPECTED_REFUND_AMOUNT)
        .build();
  }
}
```

- [ ] **Step 2: Compile**

Run: `./mvnw -q test-compile`
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/mrtripop/transaction/fixture/ReturnFixture.java
git commit -m "$(cat <<'EOF'
test(transaction): add ReturnFixture

Details:
- Add the shared test fixture for return-related tests, with
constants and expected values pre-computed for both the clean-split
and rounding-required refund math cases.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 9: `ReturnService`/`ReturnServiceImpl` — `createReturn` happy path

**Files:**
- Create: `src/main/java/com/mrtripop/transaction/services/ReturnService.java`
- Create: `src/main/java/com/mrtripop/transaction/services/impl/ReturnServiceImpl.java`
- Create: `src/test/java/com/mrtripop/transaction/services/impl/ReturnServiceImplTest.java`

**Interfaces:**
- Consumes: `InvoiceRepository`, `InvoiceItemRepository`, `BatchService` (all existing);
  `ReturnRepository`, `ReturnItemRepository` (Task 5); `ReturnMapper` (Task 7); `AuditService`
  (existing, `recordAudit(String actionType, String entityName, String entityId, String oldValue,
  String newValue): AuditLedger`); `ReturnFixture`/`InvoiceFixture` (test only).
- Produces: `ReturnService.createReturn(Long invoiceId, CreateReturnRequest request):
  ReturnDto throws ApplicationException` — consumed by `ReturnController` (Task 12) and by Task 10's
  additional test scenarios (no new production code in Task 10, only more tests against this same
  method).

This is the core orchestration method from the spec's Business Logic section. Build it test-first:
first a compiling-but-unimplemented skeleton, one failing test, then the real implementation.

- [ ] **Step 1: Create the `ReturnService` interface**

```java
package com.mrtripop.transaction.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReturnService {

  ReturnDto createReturn(Long invoiceId, CreateReturnRequest request) throws ApplicationException;

  ReturnDto findById(Long id) throws ApplicationException;

  Page<ReturnDto> findByInvoiceId(Long invoiceId, Pageable pageable) throws ApplicationException;
}
```

- [ ] **Step 2: Create the `ReturnServiceImpl` skeleton (all methods unimplemented)**

```java
package com.mrtripop.transaction.services.impl;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.services.BatchService;
import com.mrtripop.transaction.component.ReturnMapper;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnDto;
import com.mrtripop.transaction.repository.InvoiceItemRepository;
import com.mrtripop.transaction.repository.InvoiceRepository;
import com.mrtripop.transaction.repository.ReturnItemRepository;
import com.mrtripop.transaction.repository.ReturnRepository;
import com.mrtripop.transaction.services.ReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

  private final InvoiceRepository invoiceRepository;
  private final InvoiceItemRepository invoiceItemRepository;
  private final ReturnRepository returnRepository;
  private final ReturnItemRepository returnItemRepository;
  private final ReturnMapper returnMapper;
  private final AuditService auditService;
  private final BatchService batchService;

  @Override
  public ReturnDto createReturn(Long invoiceId, CreateReturnRequest request)
      throws ApplicationException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public ReturnDto findById(Long id) throws ApplicationException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public Page<ReturnDto> findByInvoiceId(Long invoiceId, Pageable pageable)
      throws ApplicationException {
    throw new UnsupportedOperationException("not yet implemented");
  }
}
```

- [ ] **Step 3: Compile**

Run: `./mvnw -q compile`
Expected: no errors.

- [ ] **Step 4: Write the failing test — happy path single-line return**

```java
package com.mrtripop.transaction.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.services.BatchService;
import com.mrtripop.transaction.component.ReturnMapper;
import com.mrtripop.transaction.fixture.InvoiceFixture;
import com.mrtripop.transaction.fixture.ReturnFixture;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.Return;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnDto;
import com.mrtripop.transaction.repository.InvoiceItemRepository;
import com.mrtripop.transaction.repository.InvoiceRepository;
import com.mrtripop.transaction.repository.ReturnItemRepository;
import com.mrtripop.transaction.repository.ReturnRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReturnServiceImpl")
class ReturnServiceImplTest {

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceItemRepository invoiceItemRepository;
  @Mock private ReturnRepository returnRepository;
  @Mock private ReturnItemRepository returnItemRepository;
  @Mock private ReturnMapper returnMapper;
  @Mock private AuditService auditService;
  @Mock private BatchService batchService;
  @InjectMocks private ReturnServiceImpl returnService;

  @Nested
  @DisplayName("CreateReturn")
  class CreateReturn {

    @Test
    @DisplayName("should restock and refund a partial quantity of one invoice line")
    void shouldCreateReturnForSingleLine() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());
      Return savedReturn = ReturnFixture.validReturn(invoice);
      ReturnDto dto = ReturnDto.builder().id(1L).invoiceId(invoice.getId()).build();

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(0L);
      when(returnRepository.save(any(Return.class))).thenReturn(savedReturn);
      when(returnItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(auditService.recordAudit(anyString(), anyString(), anyString(), anyString(), anyString()))
          .thenReturn(null);
      when(returnMapper.toDto(any(Return.class))).thenReturn(dto);
      when(returnMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // Act
      ReturnDto result = returnService.createReturn(1L, request);

      // Assert
      assertNotNull(result);
      verify(batchService).restoreStock(
          InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID, ReturnFixture.RETURN_QUANTITY);
      verify(returnRepository).save(argThat(r ->
          r.getTotalRefundAmount().compareTo(ReturnFixture.EXPECTED_REFUND_AMOUNT) == 0));
      verify(invoiceRepository).save(argThat(inv ->
          inv.getTotalAmount().compareTo(ReturnFixture.EXPECTED_NEW_TOTAL_AMOUNT) == 0
              && inv.getPatientOwed().compareTo(ReturnFixture.EXPECTED_NEW_PATIENT_OWED) == 0
              && inv.getInsuranceClaimAmount()
                  .compareTo(ReturnFixture.EXPECTED_NEW_INSURANCE_CLAIM) == 0));
      verify(auditService).recordAudit(
          eq("RETURN"), eq("Invoice"), eq("1"), anyString(), anyString());
    }
  }
}
```

- [ ] **Step 5: Run the test, verify it fails**

Run: `./mvnw -q test -Dtest=ReturnServiceImplTest`
Expected: FAIL — `UnsupportedOperationException: not yet implemented`.

- [ ] **Step 6: Implement `createReturn`**

Replace the `createReturn` method body in `ReturnServiceImpl` (keep the skeleton's other two
methods and all imports already present; add the imports listed below):

```java
  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public ReturnDto createReturn(Long invoiceId, CreateReturnRequest request)
      throws ApplicationException {
    Invoice invoice = invoiceRepository.findById(invoiceId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));

    if (invoice.getStatus() != InvoiceStatus.COMPLETED) {
      throw new ApplicationException(ErrorCode.INVOICE_NOT_COMPLETED, HttpStatus.CONFLICT);
    }

    List<ReturnItem> returnItems = new ArrayList<>();
    BigDecimal totalRefundAmount = BigDecimal.ZERO;
    BigDecimal totalRefundPatientOwed = BigDecimal.ZERO;
    BigDecimal totalRefundInsuranceClaim = BigDecimal.ZERO;

    for (ReturnItemRequest itemRequest : request.getItems()) {
      InvoiceItem invoiceItem = invoiceItemRepository.findById(itemRequest.getInvoiceItemId())
          .orElseThrow(() -> new ApplicationException(
              ErrorCode.INVOICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));

      if (!invoiceItem.getInvoice().getId().equals(invoiceId)) {
        throw new ApplicationException(ErrorCode.INVOICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
      }

      Long alreadyReturned =
          returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId());
      long remaining = invoiceItem.getQuantity() - alreadyReturned;
      if (itemRequest.getQuantity() > remaining) {
        throw new ApplicationException(
            ErrorCode.RETURN_QUANTITY_EXCEEDS_AVAILABLE, HttpStatus.BAD_REQUEST);
      }

      BigDecimal lineRefundAmount =
          invoiceItem.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
      BigDecimal lineRefundPatientOwed = invoiceItem.getPatientOwed()
          .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
          .divide(BigDecimal.valueOf(invoiceItem.getQuantity()), 2, RoundingMode.HALF_UP);
      BigDecimal lineRefundInsuranceClaim = invoiceItem.getInsuranceClaimAmount()
          .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
          .divide(BigDecimal.valueOf(invoiceItem.getQuantity()), 2, RoundingMode.HALF_UP);

      batchService.restoreStock(
          invoice.getStore().getId(), invoiceItem.getBatch().getId(), itemRequest.getQuantity());

      returnItems.add(ReturnItem.builder()
          .invoiceItem(invoiceItem)
          .quantity(itemRequest.getQuantity())
          .refundAmount(lineRefundAmount)
          .build());

      totalRefundAmount = totalRefundAmount.add(lineRefundAmount);
      totalRefundPatientOwed = totalRefundPatientOwed.add(lineRefundPatientOwed);
      totalRefundInsuranceClaim = totalRefundInsuranceClaim.add(lineRefundInsuranceClaim);
    }

    Return newReturn = Return.builder()
        .invoice(invoice)
        .reason(request.getReason())
        .totalRefundAmount(totalRefundAmount)
        .build();
    Return savedReturn = returnRepository.save(newReturn);

    for (ReturnItem item : returnItems) {
      item.setParentReturn(savedReturn);
    }
    returnItemRepository.saveAll(returnItems);

    String oldTotalAmount = invoice.getTotalAmount().toPlainString();
    invoice.setTotalAmount(invoice.getTotalAmount().subtract(totalRefundAmount));
    invoice.setPatientOwed(invoice.getPatientOwed().subtract(totalRefundPatientOwed));
    invoice.setInsuranceClaimAmount(
        invoice.getInsuranceClaimAmount().subtract(totalRefundInsuranceClaim));
    Invoice savedInvoice = invoiceRepository.save(invoice);

    auditService.recordAudit("RETURN", "Invoice", String.valueOf(invoiceId), oldTotalAmount,
        savedInvoice.getTotalAmount().toPlainString());

    ReturnDto dto = returnMapper.toDto(savedReturn);
    dto.setItems(returnMapper.toItemDtoList(returnItems));
    return dto;
  }
```

Add these imports to `ReturnServiceImpl.java`:

```java
import com.mrtripop.transaction.constant.ErrorCode;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.InvoiceStatus;
import com.mrtripop.transaction.models.db.Return;
import com.mrtripop.transaction.models.db.ReturnItem;
import com.mrtripop.transaction.models.dto.ReturnItemRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
```

- [ ] **Step 7: Run the test, verify it passes**

Run: `./mvnw -q test -Dtest=ReturnServiceImplTest`
Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/services/ReturnService.java \
        src/main/java/com/mrtripop/transaction/services/impl/ReturnServiceImpl.java \
        src/test/java/com/mrtripop/transaction/services/impl/ReturnServiceImplTest.java
git commit -m "$(cat <<'EOF'
feat(transaction): implement ReturnService.createReturn

Details:
- Implement the core partial-return flow: validate the invoice is
completed, check remaining returnable quantity per line, restock via
the existing BatchService primitive, and reduce invoice totals.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 10: `createReturn` — validation, multi-line, rounding, and rollback coverage

**Files:**
- Modify: `src/test/java/com/mrtripop/transaction/services/impl/ReturnServiceImplTest.java`
  (add more `@Test` methods inside the existing `CreateReturn` nested class from Task 9 — no
  production code changes expected)

**Interfaces:**
- Consumes: everything from Task 9 — no new interfaces produced.

These scenarios lock in behavior `createReturn` (Task 9) already implements. Add each test, run it;
it should pass immediately since the Task 9 implementation already handles these cases. If any
fails, that's a real bug in Task 9's implementation — fix `ReturnServiceImpl` (not the test) to
match the spec, then re-run.

- [ ] **Step 1: Add the remaining test methods to the `CreateReturn` nested class**

Insert these methods into the `CreateReturn` class from Task 9, after the existing
`shouldCreateReturnForSingleLine` test:

```java
    @Test
    @DisplayName("should throw TXN4012 when invoice is still pending")
    void shouldThrowNotCompletedWhenPending() {
      // Arrange
      Invoice invoice = InvoiceFixture.pendingInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.INVOICE_NOT_COMPLETED, ex.getErrorCode());
      verify(returnRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw TXN4012 when invoice is voided")
    void shouldThrowNotCompletedWhenVoided() {
      // Arrange
      Invoice invoice = InvoiceFixture.voidedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.INVOICE_NOT_COMPLETED, ex.getErrorCode());
      verify(returnRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw TXN4001 when invoice not found")
    void shouldThrowInvoiceNotFound() {
      // Arrange
      CreateReturnRequest request = ReturnFixture.validCreateRequest(1L);
      when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(com.mrtripop.transaction.constant.ErrorCode.INVOICE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw TXN4014 when invoice item does not exist")
    void shouldThrowInvoiceItemNotFound() {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      CreateReturnRequest request = ReturnFixture.validCreateRequest(999L);

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(999L)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.INVOICE_ITEM_NOT_FOUND, ex.getErrorCode());
      verify(batchService, never()).restoreStock(any(), any(), any());
    }

    @Test
    @DisplayName("should throw TXN4014 when invoice item belongs to a different invoice")
    void shouldThrowInvoiceItemNotFoundForMismatchedInvoice() {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      Invoice otherInvoice = InvoiceFixture.completedInvoice();
      otherInvoice.setId(2L);
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(otherInvoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.INVOICE_ITEM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw TXN4013 when return quantity exceeds remaining quantity")
    void shouldThrowExceedsAvailableWhenOverQuantity() {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      ReturnItemRequest overRequest = ReturnItemRequest.builder()
          .invoiceItemId(invoiceItem.getId())
          .quantity(InvoiceFixture.VALID_QUANTITY + 1)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(overRequest))
          .build();

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(0L);

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.RETURN_QUANTITY_EXCEEDS_AVAILABLE, ex.getErrorCode());
      verify(batchService, never()).restoreStock(any(), any(), any());
    }

    @Test
    @DisplayName("should account for a prior return when checking remaining quantity")
    void shouldRespectAlreadyReturnedQuantity() throws ApplicationException {
      // Arrange — 5 total, 2 already returned, requesting exactly the 3 remaining
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      ReturnItemRequest boundaryRequest = ReturnItemRequest.builder()
          .invoiceItemId(invoiceItem.getId())
          .quantity(3L)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(boundaryRequest))
          .build();
      Return savedReturn = ReturnFixture.validReturn(invoice);

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(2L);
      when(returnRepository.save(any(Return.class))).thenReturn(savedReturn);
      when(returnItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(returnMapper.toDto(any(Return.class)))
          .thenReturn(ReturnDto.builder().id(1L).build());
      when(returnMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // Act
      ReturnDto result = returnService.createReturn(1L, request);

      // Assert
      assertNotNull(result);
      verify(batchService).restoreStock(InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID, 3L);
    }

    @Test
    @DisplayName("should reject a second return once the prior return already covers the item")
    void shouldRejectWhenAlreadyReturnedExceedsRequest() {
      // Arrange — 5 total, 2 already returned, requesting 4 (only 3 remain)
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      ReturnItemRequest tooMuchRequest = ReturnItemRequest.builder()
          .invoiceItemId(invoiceItem.getId())
          .quantity(4L)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(tooMuchRequest))
          .build();

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(2L);

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.RETURN_QUANTITY_EXCEEDS_AVAILABLE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should restock and refund two lines in a single request")
    void shouldCreateReturnForMultipleLines() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem firstItem = InvoiceFixture.validInvoiceItem(invoice);
      InvoiceItem secondItem = ReturnFixture.invoiceItemForRounding(invoice);
      ReturnItemRequest firstRequest = ReturnItemRequest.builder()
          .invoiceItemId(firstItem.getId())
          .quantity(ReturnFixture.RETURN_QUANTITY)
          .build();
      ReturnItemRequest secondRequest = ReturnItemRequest.builder()
          .invoiceItemId(secondItem.getId())
          .quantity(ReturnFixture.ROUNDING_RETURN_QUANTITY)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(firstRequest, secondRequest))
          .build();
      Return savedReturn = ReturnFixture.validReturn(invoice);

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(firstItem.getId())).thenReturn(Optional.of(firstItem));
      when(invoiceItemRepository.findById(secondItem.getId())).thenReturn(Optional.of(secondItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(any())).thenReturn(0L);
      when(returnRepository.save(any(Return.class))).thenReturn(savedReturn);
      when(returnItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(returnMapper.toDto(any(Return.class)))
          .thenReturn(ReturnDto.builder().id(1L).build());
      when(returnMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // Act
      ReturnDto result = returnService.createReturn(1L, request);

      // Assert
      assertNotNull(result);
      verify(batchService).restoreStock(
          InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID, ReturnFixture.RETURN_QUANTITY);
      verify(returnRepository).save(argThat(r -> r.getTotalRefundAmount().compareTo(
          ReturnFixture.EXPECTED_REFUND_AMOUNT.add(ReturnFixture.EXPECTED_ROUNDED_REFUND_AMOUNT)) == 0));
    }

    @Test
    @DisplayName("should round refund split to the nearest cent using HALF_UP")
    void shouldRoundRefundSplitCorrectly() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = ReturnFixture.invoiceItemForRounding(invoice);
      ReturnItemRequest itemRequest = ReturnItemRequest.builder()
          .invoiceItemId(invoiceItem.getId())
          .quantity(ReturnFixture.ROUNDING_RETURN_QUANTITY)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(itemRequest))
          .build();
      Return savedReturn = ReturnFixture.validReturn(invoice);

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(0L);
      when(returnRepository.save(any(Return.class))).thenReturn(savedReturn);
      when(returnItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(returnMapper.toDto(any(Return.class)))
          .thenReturn(ReturnDto.builder().id(1L).build());
      when(returnMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // Act
      returnService.createReturn(1L, request);

      // Assert
      verify(invoiceRepository).save(argThat(inv -> {
        BigDecimal expectedTotal = invoice.getTotalAmount()
            .subtract(ReturnFixture.EXPECTED_ROUNDED_REFUND_AMOUNT);
        BigDecimal expectedPatientOwed = invoice.getPatientOwed()
            .subtract(ReturnFixture.EXPECTED_ROUNDED_PATIENT_OWED);
        BigDecimal expectedInsurance = invoice.getInsuranceClaimAmount()
            .subtract(ReturnFixture.EXPECTED_ROUNDED_INSURANCE_CLAIM);
        return inv.getTotalAmount().compareTo(expectedTotal) == 0
            && inv.getPatientOwed().compareTo(expectedPatientOwed) == 0
            && inv.getInsuranceClaimAmount().compareTo(expectedInsurance) == 0;
      }));
    }

    @Test
    @DisplayName("should roll back and record nothing when restock fails")
    void shouldRollbackWhenRestoreStockFails() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(0L);
      doThrow(new ApplicationException(
              com.mrtripop.inventory.constant.ErrorCode.STOCK_NOT_FOUND_FOR_BATCH,
              HttpStatus.NOT_FOUND))
          .when(batchService).restoreStock(any(), any(), any());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(com.mrtripop.inventory.constant.ErrorCode.STOCK_NOT_FOUND_FOR_BATCH,
          ex.getErrorCode());
      verify(returnRepository, never()).save(any());
      verify(invoiceRepository, never()).save(any());
    }
```

Task 9's test file already has `import static org.mockito.Mockito.*;` (covers `doThrow`), but it
does **not** import `HttpStatus` or `BigDecimal` — the rollback and rounding tests above use both.
Add these two imports to the test file now:

```java
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
```

- [ ] **Step 2: Run the full test class, verify everything passes**

Run: `./mvnw -q test -Dtest=ReturnServiceImplTest`
Expected: PASS, all tests including Task 9's.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/mrtripop/transaction/services/impl/ReturnServiceImplTest.java
git commit -m "$(cat <<'EOF'
test(transaction): cover createReturn validation and edge cases

Details:
- Lock in the remaining createReturn behavior: non-completed-invoice
rejection, over-quantity rejection, cumulative partial returns,
multi-line requests, HALF_UP rounding, and rollback on restock
failure.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 11: `ReturnServiceImpl` — `findById` and `findByInvoiceId`

**Files:**
- Modify: `src/main/java/com/mrtripop/transaction/services/impl/ReturnServiceImpl.java`
- Modify: `src/test/java/com/mrtripop/transaction/services/impl/ReturnServiceImplTest.java`

**Interfaces:**
- Produces: `ReturnServiceImpl.findById(Long): ReturnDto` and
  `ReturnServiceImpl.findByInvoiceId(Long, Pageable): Page<ReturnDto>` — consumed by
  `ReturnController` (Task 12).

- [ ] **Step 1: Write the failing tests**

Add a new top-level `@Nested` class to `ReturnServiceImplTest`, after `CreateReturn`:

```java
  @Nested
  @DisplayName("FindReturn")
  class FindReturn {

    @Test
    @DisplayName("should return a return by ID with its items")
    void shouldReturnById() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      Return foundReturn = ReturnFixture.validReturn(invoice);
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      ReturnItem item = ReturnFixture.validReturnItem(foundReturn, invoiceItem);
      ReturnDto dto = ReturnDto.builder().id(1L).build();
      ReturnItemDto itemDto = ReturnItemDto.builder().id(1L).build();

      when(returnRepository.findById(1L)).thenReturn(Optional.of(foundReturn));
      when(returnItemRepository.findByParentReturnId(1L)).thenReturn(List.of(item));
      when(returnMapper.toDto(foundReturn)).thenReturn(dto);
      when(returnMapper.toItemDtoList(List.of(item))).thenReturn(List.of(itemDto));

      // Act
      ReturnDto result = returnService.findById(1L);

      // Assert
      assertNotNull(result);
      assertNotNull(result.getItems());
      assertEquals(1, result.getItems().size());
    }

    @Test
    @DisplayName("should throw TXN4015 when return not found")
    void shouldThrowReturnNotFound() {
      // Arrange
      when(returnRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.findById(1L));
      assertEquals(ErrorCode.RETURN_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("should return a paginated list of returns for an invoice")
    void shouldReturnPaginatedListByInvoice() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      Return foundReturn = ReturnFixture.validReturn(invoice);
      org.springframework.data.domain.Page<Return> page =
          new org.springframework.data.domain.PageImpl<>(List.of(foundReturn));
      ReturnDto dto = ReturnDto.builder().id(1L).build();

      when(returnRepository.findByInvoiceId(eq(1L), any(org.springframework.data.domain.Pageable.class)))
          .thenReturn(page);
      when(returnMapper.toDto(foundReturn)).thenReturn(dto);

      // Act
      org.springframework.data.domain.Page<ReturnDto> result =
          returnService.findByInvoiceId(1L, org.springframework.data.domain.Pageable.unpaged());

      // Assert
      assertEquals(1, result.getTotalElements());
    }
  }
```

Add `import com.mrtripop.transaction.models.dto.ReturnItemDto;` to the test file's imports.

- [ ] **Step 2: Run the tests, verify the new ones fail**

Run: `./mvnw -q test -Dtest=ReturnServiceImplTest`
Expected: FAIL — `UnsupportedOperationException` for the three new tests, prior tests still pass.

- [ ] **Step 3: Implement `findById` and `findByInvoiceId`**

Replace the two `throw new UnsupportedOperationException(...)` bodies in `ReturnServiceImpl`:

```java
  @Override
  @Transactional(readOnly = true)
  public ReturnDto findById(Long id) throws ApplicationException {
    Return foundReturn = returnRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.RETURN_NOT_FOUND, HttpStatus.NOT_FOUND));

    List<ReturnItem> items = returnItemRepository.findByParentReturnId(id);
    ReturnDto dto = returnMapper.toDto(foundReturn);
    dto.setItems(returnMapper.toItemDtoList(items));
    return dto;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ReturnDto> findByInvoiceId(Long invoiceId, Pageable pageable)
      throws ApplicationException {
    return returnRepository.findByInvoiceId(invoiceId, pageable).map(returnMapper::toDto);
  }
```

- [ ] **Step 4: Run the full test class, verify everything passes**

Run: `./mvnw -q test -Dtest=ReturnServiceImplTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/services/impl/ReturnServiceImpl.java \
        src/test/java/com/mrtripop/transaction/services/impl/ReturnServiceImplTest.java
git commit -m "$(cat <<'EOF'
feat(transaction): implement ReturnService read methods

Details:
- Implement findById and findByInvoiceId so a return and an
invoice's return history can be looked up after creation.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 12: `ReturnController`

**Files:**
- Create: `src/main/java/com/mrtripop/transaction/controllers/ReturnController.java`

**Interfaces:**
- Consumes: `ReturnService` (Tasks 9-11), `CreateReturnRequest`/`ReturnDto` (Task 6),
  `SuccessCode.TXN2010_CREATE_RETURN_IS_SUCCESS` /
  `SuccessCode.TXN2011_GET_RETURNS_BY_INVOICE_IS_SUCCESS` /
  `SuccessCode.TXN2012_GET_RETURN_BY_ID_IS_SUCCESS` (Task 4), `ResponseBody`, `BaseQueryParams`
  (existing, `com.mrtripop.model`).

This project has no `*ControllerTest` files anywhere (verified while researching this plan) — every
other controller (`InvoiceController` included) is untested directly; behavior is verified through
the service-layer tests instead. Follow that established convention: no new controller test file.
This is a thin delegator, matching `InvoiceController`'s exact shape — no business logic here.

- [ ] **Step 1: Create the controller**

```java
package com.mrtripop.transaction.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.transaction.constant.SuccessCode;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnDto;
import com.mrtripop.transaction.services.ReturnService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transaction")
@Validated
public class ReturnController {

  private final ReturnService returnService;

  @PostMapping("/invoices/{invoiceId}/returns")
  public ResponseEntity<Object> createReturn(
      @PathVariable @Min(1) Long invoiceId, @Valid @RequestBody CreateReturnRequest request)
      throws ApplicationException {
    ReturnDto result = returnService.createReturn(invoiceId, request);
    BaseStatusCode success = SuccessCode.TXN2010_CREATE_RETURN_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @GetMapping("/invoices/{invoiceId}/returns")
  public ResponseEntity<Object> findByInvoiceId(
      @PathVariable @Min(1) Long invoiceId, @Valid BaseQueryParams params)
      throws ApplicationException {
    Pageable pageable =
        PageRequest.of(params.getPage() - 1, params.getSize(), Sort.by(params.getOrderBy(), "id"));
    Page<ReturnDto> result = returnService.findByInvoiceId(invoiceId, pageable);
    BaseStatusCode success = SuccessCode.TXN2011_GET_RETURNS_BY_INVOICE_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/returns/{returnId}")
  public ResponseEntity<Object> findById(@PathVariable @Min(1) Long returnId)
      throws ApplicationException {
    ReturnDto result = returnService.findById(returnId);
    BaseStatusCode success = SuccessCode.TXN2012_GET_RETURN_BY_ID_IS_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}
```

- [ ] **Step 2: Compile**

Run: `./mvnw -q compile`
Expected: no errors.

- [ ] **Step 3: Start the app and smoke-test the endpoints manually**

This step requires Postgres + Redis (see `docker-compose.yml`) and is optional if those aren't
available in the execution environment — if so, skip to Step 4 and rely on the unit test suite plus
a manual review of the route mappings against `.claude/rules/api-design.md`.

Run: `docker compose up -d postgres redis && ./mvnw spring-boot:run`, then in another shell:

```bash
curl -s -X POST http://localhost:8080/api/v1/transaction/invoices/1/returns \
  -H "Content-Type: application/json" \
  -d '{"reason":"CUSTOMER_CHANGED_MIND","items":[{"invoiceItemId":1,"quantity":1}]}'
```

Expected: a `201` response (or a domain error like `TXN4001`/`TXN4012` if invoice `1` doesn't exist
or isn't `COMPLETED` in the running database — either response proves the route is wired correctly).
Stop the app afterward (`Ctrl+C`) and `docker compose down`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/mrtripop/transaction/controllers/ReturnController.java
git commit -m "$(cat <<'EOF'
feat(transaction): add ReturnController

Details:
- Expose the partial-return API: create a return, list an invoice's
returns, and fetch a single return by ID.

Ticket: N/A

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01Y9KLZCoNRVz5FEnisjbY3x
EOF
)"
```

---

### Task 13: Final verification

**Files:** none (verification only)

**Interfaces:** none produced — this task gates the branch, it doesn't add code.

- [ ] **Step 1: Run the full test suite**

Run: `./mvnw -q test`
Expected: all tests pass (166 pre-existing + all new `ReturnServiceImplTest` tests), 0 failures, 0
errors. If anything fails, fix it before proceeding — do not skip this step.

- [ ] **Step 2: Run GitNexus `detect_changes` against `develop`**

Run the GitNexus `detect_changes` tool (`mcp__gitnexus__detect_changes`) with
`{scope: "compare", base_ref: "develop"}` (this branch was created off `develop`, not `master` —
see this plan's Architecture section). Review the affected symbols/execution flows list: expect it
to show only new symbols (`Return`, `ReturnItem`, `ReturnReason`, `ReturnService`,
`ReturnServiceImpl`, `ReturnController`, `ReturnMapper`, `ReturnRepository`,
`ReturnItemRepository`, the four new DTOs, the new `ErrorCode`/`SuccessCode` constants) plus no
unexpected modifications to any existing execution flow (`Invoice.complete`, `Invoice.voidInvoice`,
`BatchService.restoreStock`, etc. should show as untouched). If it reports changes to anything
outside this list, stop and investigate before considering the branch done.

- [ ] **Step 3: Report to the user**

Summarize: test count, pass/fail, and the `detect_changes` result. If both are clean, the feature is
ready for review/PR — no further commit needed in this task.
