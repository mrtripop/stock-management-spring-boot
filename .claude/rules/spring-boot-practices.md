# Spring Boot Practices

Framework-level rules for using Spring Boot correctly. Each rule has a correct and incorrect example.

**Note:** API endpoint design (URLs, HTTP methods, response wrapping, DTO naming) is in `api-design.md`. Security rules (SQL injection, CORS, secrets) are in `security.md`. Performance rules (N+1, caching, lazy loading) are in `performance.md`.

## Dependency Injection

- Use `@RequiredArgsConstructor` for constructor injection. Never use `@Autowired` on fields or constructors.

```java
// Wrong
@Autowired
private ProductService productService;

// Right
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;
}
```

- Inject only what the class directly uses. No "just in case" dependencies.

## Stereotype Annotations

- Use `@RestController` for API controllers. Never use `@Controller` for REST endpoints.
- Use `@Service` for service classes. Never `@Component` for services.
- Use `@Repository` for repository interfaces (Spring Data provides this automatically).

## Configuration Properties

- Use `@ConfigurationProperties` for grouped config values. Use `@Value` only for single, simple values.

```java
// Wrong
@Value("${app.cache.ttl}")
private long cacheTtl;
@Value("${app.cache.max-size}")
private int cacheMaxSize;

// Right
@ConfigurationProperties(prefix = "app.cache")
@Data
public class CacheProperties {
  private Duration ttl;
  private int maxSize;
  private boolean enabled;
}
```

- Never hardcode configuration values (timeouts, URLs, thresholds, batch sizes) in service or controller code. Always externalize to `application.yml` or environment variables.
- Feature flags must be external configuration only. Never `boolean featureEnabled = true` in source code.
- Use Spring `Resource` abstraction for file paths. Never hardcode file system paths.

## Transaction Management

- Put `@Transactional` on service methods, never on controllers or repositories.
- Always specify `rollbackFor` on write transactions. Default rollback only on unchecked exceptions.

```java
// Wrong
@Transactional
public void createOrder(OrderDto dto) { ... }

// Right
@Transactional(rollbackFor = ApplicationException.class)
public OrderDto createOrder(OrderDto dto) { ... }

@Transactional(readOnly = true)
public OrderDto findById(Long id) { ... }
```

- Keep transactions short. Never make external API calls or I/O operations inside `@Transactional`.

```java
// Wrong
@Transactional
public OrderDto createOrder(OrderDto dto) {
  Order order = orderRepository.save(entity);
  emailService.sendConfirmation(order); // external I/O inside transaction
  return mapper.toDto(order);
}

// Right
public OrderDto createOrder(OrderDto dto) {
  Order order = saveOrderInternal(dto);
  emailService.sendConfirmation(order.getId());
  return mapper.toDto(order);
}

@Transactional(rollbackFor = ApplicationException.class)
private Order saveOrderInternal(OrderDto dto) {
  return orderRepository.save(mapper.toEntity(dto));
}
```

## Exception Handling

- Use `ApplicationException` with per-domain error code enums. Never throw raw `RuntimeException` with string messages or inline error code strings.

```java
// Wrong
throw new RuntimeException("Product not found with id: " + id);
throw new ApplicationException("PRO1001", "Product not found");

// Right
throw new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND);
```

- Never catch and swallow exceptions silently. At minimum, log the error.

```java
// Wrong
try {
  orderService.process(order);
} catch (Exception e) {
  // silently ignored
}

// Right
try {
  orderService.process(order);
} catch (ApplicationException e) {
  log.warn("Failed to process order {}: {}", order.getId(), e.getMessage());
  throw e;
}
```

- Do not use `@ResponseStatus` on exceptions. Let the global `ControllerAdvice` handle all HTTP status mapping.

## Service Layer

- Define service interfaces. Implementations use the `Impl` suffix.

```java
// Wrong
public class ProductService { ... }

// Right
public interface ProductService { ... }
public class ProductServiceImpl implements ProductService { ... }
```

- Services should orchestrate business logic. Never embed business logic in controllers or repositories.

## Repository Patterns

- Use Spring Data derived queries for simple operations. Use `@Query` only for complex queries.

```java
// Wrong — unnecessary @Query for simple find
@Query("SELECT p FROM Product p WHERE p.code = :code")
Optional<Product> findByCode(@Param("code") String code);

// Right — derived query
Optional<Product> findByCode(String code);
```

- Parameterized queries only. Never concatenate user input into SQL/JPQL strings.

```java
// Wrong
@Query(value = "SELECT * FROM products WHERE name = '" + name + "'", nativeQuery = true)
List<Product> search(String name);

// Right
@Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword%")
List<Product> search(@Param("keyword") String keyword);
```

- Return `Optional<T>` for single-entity lookups. Return `List<T>` for collections.

```java
// Wrong
Product findById(Long id);

// Right
Optional<Product> findById(Long id);
```

- Use `existsBy` for existence checks instead of fetching the full entity.

```java
// Wrong
Optional<Product> existing = productRepository.findByCode(code);
if (existing.isPresent()) { throw new ApplicationException(ErrorCode.DUPLICATE_CODE); }

// Right
if (productRepository.existsByCode(code)) {
  throw new ApplicationException(ErrorCode.DUPLICATE_CODE);
}
```

## MapStruct Mappers

- Use `componentModel = "spring"` for new mappers. Inject via `@RequiredArgsConstructor`.

```java
// Wrong — manual static instance
@Mapper
public interface ProductMapper {
  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
}

// Right — Spring-managed bean
@Mapper(componentModel = "spring")
public interface ProductMapper {
  Product toEntity(ProductDto dto);
  ProductDto toDto(Product entity);
}
```

- Never put mapping logic in service classes. Always delegate to a MapStruct mapper.

## Entity Patterns

- Extend `AuditEntity` for all auditable entities. Do not duplicate `createdAt`/`updatedAt` fields.
- Use `@SuperBuilder` on every level of the entity hierarchy. Plain `@Builder` breaks inheritance.

```java
// Wrong
@Entity
@Builder
public class Product extends BaseProduct { ... }

// Right
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Product extends BaseProduct { ... }
```

- Use `FetchType.LAZY` for all relationships. Default JPA `@ManyToOne` is `EAGER` — always override it.

```java
// Wrong — default EAGER fetch
@ManyToOne
private Category category;

// Right
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
private Category category;
```

## Caching

- Cache in the service layer only. Never cache in controllers or repositories.
- Always specify a cache key. Always use `unless = "#result == null"` to avoid caching empty results.

```java
// Wrong
@Cacheable("products")
public ProductDto findById(Long id) { ... }

// Right
@Cacheable(value = "products", key = "#id", unless = "#result == null")
public ProductDto findById(Long id) { ... }

@CachePut(value = "products", key = "#id")
public ProductDto update(Long id, ProductDto dto) { ... }

@CacheEvict(value = "products", key = "#id")
public void delete(Long id) { ... }
```

## Profile Management

- Use `application.yml` for default config. Use `application-{profile}.yml` for profile-specific overrides.
- Never duplicate config across profiles. Only override what differs.
- Use `@ActiveProfiles("test")` on all test classes. Never rely on the default profile for tests.

## Lombok Usage

- Use `@Slf4j` for logging. Never create loggers manually with `LoggerFactory`.
- Use `@Data` on DTOs. Use `@Getter @Setter` on entities (to control access granularity).
- Use `@Builder` or `@SuperBuilder` on DTOs and entities. Never write builder classes manually.

## AOP Usage

- Use AOP for cross-cutting concerns only (logging, metrics, tracing).
- Never use AOP to implement business logic.

## Do NOT

- Never use `@Autowired` — use `@RequiredArgsConstructor` instead
- Never return JPA entities from controllers — always use DTOs
- Never put `@Transactional` on controllers
- Never catch `Exception` broadly without re-throwing or logging
- Never create `new` instances of beans — always inject them
- Never use `System.out.println` — use `@Slf4j` logging
- Never use `@Controller` for REST APIs — use `@RestController`
- Never call `repository.save()` in a loop — use `saveAll()`
- Never use `@EnableAutoConfiguration` directly — use `@SpringBootApplication`
- Never start the application context in unit tests — use `@MockBean` or Mockito
- Never mix `@SpringBootTest` with `@MockBean` for service tests — that is an integration test
