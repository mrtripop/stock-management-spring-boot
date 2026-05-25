# Performance Standards

Performance-specific rules for database queries, data access, and resource management. Each rule has a correct and incorrect example.

**Note:** Pagination in endpoints is in `api-design.md`.

## N+1 Query Prevention

- Use `@EntityGraph`, `JOIN FETCH`, or `@BatchSize` for relationships accessed in loops.
- Detect N+1: if a loop body triggers a query, it's an N+1 problem.

```java
// Wrong — N+1: one query for orders, N queries for items
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
  List<OrderItem> items = order.getItems(); // triggers query per order
}

// Right — single query with JOIN FETCH
@Query("SELECT o FROM Order o JOIN FETCH o.items")
List<Order> findAllWithItems();

// Also right — @EntityGraph
@EntityGraph(attributePaths = {"items"})
List<Order> findAll();
```

## Database Indexes

- Index columns used in `WHERE`, `JOIN`, `ORDER BY`, and `GROUP BY` clauses.

```java
// Wrong
@Table(name = "products")

// Right
@Table(
    name = "products",
    indexes = {
      @Index(name = "products_code", columnList = "code"),
      @Index(name = "products_category_id", columnList = "category_id")
    }
)
```

## Select Only Needed Columns

- Use native SQL projections for partial data. Avoid fetching full entities when only a few fields are needed.
- Use text blocks (`"""`) for multi-line SQL queries.

```java
// Wrong — fetches full Product entity with all columns and relationships
@Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
List<Product> findByCategory(@Param("categoryId") Long categoryId);

// Right — native SQL fetches only needed fields
@Query(
    value = """
        SELECT p.id, p.name, p.price
        FROM products p
        WHERE p.category_id = :categoryId
        """,
    nativeQuery = true)
List<ProductSummary> findSummariesByCategory(@Param("categoryId") Long categoryId);
```

## Batch Operations

- Use `saveAll()` instead of individual `save()` in loops.
- For large batches (> 1000 rows), use `@Modifying` bulk queries or native batch inserts.

```java
// Wrong
for (ProductDto dto : dtos) {
  productRepository.save(mapper.toEntity(dto));
}

// Right
productRepository.saveAll(dtos.stream().map(mapper::toEntity).toList());
```

## Stream API Usage

- Prefer Java Streams for in-memory collection processing.
- Use `flatMap` instead of nested loops.

```java
// Wrong — nested loops
List<OrderItem> allItems = new ArrayList<>();
for (Order order : orders) {
  for (OrderItem item : order.getItems()) {
    allItems.add(item);
  }
}

// Right — flatMap
List<OrderItem> allItems = orders.stream()
    .flatMap(order -> order.getItems().stream())
    .toList();
```

## Connection Management

- Rely on Spring's HikariCP connection pool. Never manage connections manually.
- Keep transactions short — do not make external API calls inside `@Transactional`.

## DTO Mapping at Service Layer

- Map entities to DTOs in the service layer, not the controller.
- This prevents accidental serialization of lazy-loaded relationships.

```java
// Wrong — mapping in controller, risks lazy-loaded relationship serialization
@GetMapping("/products/{id}")
public ResponseEntity<Object> getById(@PathVariable Long id) {
  Product product = productRepository.findById(id).orElseThrow();
  return ResponseBody.builder()
      .code(success.getCode())
      .message(success.getMessage())
      .data(productMapper.toDto(product))
      .build()
      .toResponseEntity(HttpStatus.OK);
}

// Right — mapping inside service
@GetMapping("/products/{id}")
public ResponseEntity<Object> getById(@PathVariable Long id) {
  ProductDto result = productService.findById(id);
  return ResponseBody.builder()
      .code(success.getCode())
      .message(success.getMessage())
      .data(result)
      .build()
      .toResponseEntity(HttpStatus.OK);
}
```

## Async for I/O

- Use `@Async` or `CompletableFuture` for external API calls, file operations, or email sending.
- Do not make the calling thread wait for non-critical I/O.

```java
// Right
@Async
public CompletableFuture<Void> sendOrderConfirmationEmail(Long orderId) {
  emailService.send(orderId);
  return CompletableFuture.completedFuture(null);
}
```

## Do NOT

- Never access lazy relationships in a loop without `JOIN FETCH` or `@EntityGraph`
- Never fetch full entities when only a few fields are needed
- Never call `repository.save()` in a loop — use `saveAll()`
- Never make external API calls inside `@Transactional`
- Never map entities to DTOs in the controller — do it in the service layer
