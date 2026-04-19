# Performance Standards

Concise rules for AI agents. Each rule has a correct and incorrect example.

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

## Pagination

- Always paginate list endpoints. Never return unbounded result sets.

```java
// Wrong
@GetMapping("/products")
public List<ProductDto> getAll() { ... } // could return millions of rows

// Right
@GetMapping("/products")
public Page<ProductDto> getAll(@Valid BaseQueryParams params) {
  return productService.findAll(params.toPageable()).map(mapper::toDto);
}
```

## Database Indexes

- Index columns used in `WHERE`, `JOIN`, `ORDER BY`, and `GROUP BY` clauses.
- Use the existing `@Index` pattern in entity definitions.

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

- Use JPQL projections or DTO projections for partial data. Avoid fetching full entities when only a few fields are needed.

```java
// Wrong — fetches full Product entity with all columns and relationships
@Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
List<Product> findByCategory(@Param("categoryId") Long categoryId);

// Right — fetches only needed fields
@Query("SELECT new com.mrtripop.product.models.dto.ProductSummary(p.id, p.name, p.price) "
    + "FROM Product p WHERE p.category.id = :categoryId")
List<ProductSummary> findSummariesByCategory(@Param("categoryId") Long categoryId);
```

## Caching

- Use `@Cacheable` for frequently read, rarely updated data.
- Set appropriate TTLs. Never cache data that changes frequently.
- Always use `@CacheEvict` or `@CachePut` on write operations.

```java
// Wrong
@Cacheable("products") // no TTL, caches forever
public ProductDto findById(Long id) { ... }

// Right
@Cacheable(value = "products", key = "#id", unless = "#result == null")
public ProductDto findById(Long id) { ... }

@CacheEvict(value = "products", key = "#id")
public ProductDto update(Long id, ProductDto dto) { ... }
```

## Stream API Usage

- Prefer Java Streams for in-memory collection processing.
- Use `flatMap` instead of nested loops.
- For small collections (< 10 elements), plain loops may be more readable — use judgment.

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
- Use `@Transactional` boundaries to control connection lifecycle.
- Keep transactions short — do not make external API calls inside `@Transactional`.

```java
// Wrong — long transaction with external call
@Transactional
public OrderDto createOrder(OrderDto dto) {
  Order order = orderRepository.save(mapper.toEntity(dto));
  paymentService.callExternalPaymentGateway(order); // slow external call
  return mapper.toDto(order);
}

// Right — external call outside transaction
public OrderDto createOrder(OrderDto dto) {
  Order order = saveOrderInternal(dto);
  paymentService.processPayment(order.getId()); // not in transaction
  return mapper.toDto(order);
}

@Transactional
private Order saveOrderInternal(OrderDto dto) {
  return orderRepository.save(mapper.toEntity(dto));
}
```

## Lazy Loading

- Default to `FetchType.LAZY` for all relationships.
- Use `EAGER` only when justified — document the reason.

```java
// Wrong
@ManyToOne(fetch = FetchType.EAGER) // default, loads category for every product
private Category category;

// Right
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
private Category category;
```

## Batch Operations

- Use `saveAll()` instead of individual `save()` in loops.
- For large batches (> 1000 rows), use `@Modifying` bulk queries or native batch inserts.

```java
// Wrong
for (ProductDto dto : dtos) {
  productRepository.save(mapper.toEntity(dto)); // N individual INSERT statements
}

// Right
productRepository.saveAll(dtos.stream().map(mapper::toEntity).toList()); // single batch
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

## DTO Mapping at Service Layer

- Map entities to DTOs in the service layer, not the controller.
- This prevents accidental serialization of lazy-loaded relationships.

```java
// Wrong
@GetMapping("/products/{id}")
public ProductDto getById(@PathVariable Long id) {
  Product product = productRepository.findById(id).orElseThrow();
  return productMapper.toDto(product); // mapping in controller
}

// Right
@GetMapping("/products/{id}")
public ProductDto getById(@PathVariable Long id) {
  return productService.findById(id); // mapping inside service
}
```
