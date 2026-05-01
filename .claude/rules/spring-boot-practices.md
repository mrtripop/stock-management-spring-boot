# Spring Boot Practices

Concise rules for AI agents. Each rule has a correct and incorrect example.

## Dependency Injection

- Use `@RequiredArgsConstructor` for constructor injection. Never use `@Autowired` on fields or constructors.

```java
// Wrong
@Autowired
private ProductService productService;

@Autowired
public ProductController(ProductService productService) {
    this.productService = productService;
}

// Right
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;
}
```

- Inject only what the class directly uses. No "just in case" dependencies.

```java
// Wrong
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;
  private final ProductService productService; // unused
}

// Right
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;
}
```

## Stereotype Annotations

- Use `@RestController` for API controllers. Never use `@Controller` for REST endpoints.
- Use `@Service` for service classes. Never `@Component` for services.
- Use `@Repository` for repository interfaces (Spring Data provides this automatically).

```java
// Wrong
@Controller
public class ProductController { ... }

@Component
public class ProductService { ... }

// Right
@RestController
public class ProductController { ... }

@Service
public class ProductServiceImpl implements ProductService { ... }
```

## Configuration Properties

- Use `@ConfigurationProperties` for grouped config values. Use `@Value` only for single, simple values.

```java
// Wrong
@Value("${app.cache.ttl}")
private long cacheTtl;
@Value("${app.cache.max-size}")
private int cacheMaxSize;
@Value("${app.cache.enabled}")
private boolean cacheEnabled;

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

```java
// Wrong
RestTemplate restTemplate = new RestTemplate();
restTemplate.setReadTimeout(30_000);

// Right
@ConfigurationProperties(prefix = "app.api")
@Data
public class ApiProperties {
  private Duration readTimeout;
  private String baseUrl;
  private int maxRetries;
}
```

- Feature flags must be external configuration only. Never `boolean featureEnabled = true` in source code.

```java
// Wrong
private boolean isNewPaymentFlow = true;

// Right
@Value("${feature.new-payment-flow.enabled:false}")
private boolean isNewPaymentFlowEnabled;
```

- Use Spring `Resource` abstraction for file paths. Never hardcode file system paths.

```java
// Wrong
File file = new File("/opt/app/data/export.csv");

// Right
@Value("classpath:data/export.csv")
Resource exportTemplate;
```

## Transaction Management

- Put `@Transactional` on service methods, never on controllers or repositories.
- Always specify `rollbackFor` on write transactions. Default rollback only on unchecked exceptions.

```java
// Wrong
@Transactional
public void createOrder(OrderDto dto) { ... } // may not rollback on checked exceptions

@GetMapping("/orders/{id}")
@Transactional(readOnly = true)
public OrderDto getOrder(@PathVariable Long id) { ... } // transaction on controller

// Right
@Transactional(rollbackFor = ApplicationException.class)
public OrderDto createOrder(OrderDto dto) { ... }

@Transactional(readOnly = true)
public OrderDto findById(Long id) { ... } // service layer
```

- Keep transactions short. Never make external API calls or I/O operations inside `@Transactional`.

```java
// Wrong
@Transactional
public OrderDto createOrder(OrderDto dto) {
  Order order = orderRepository.save(entity);
  emailService.sendConfirmation(order); // external I/O inside transaction
  paymentGateway.charge(order);         // external API inside transaction
  return mapper.toDto(order);
}

// Right
public OrderDto createOrder(OrderDto dto) {
  Order order = saveOrderInternal(dto);
  emailService.sendConfirmation(order.getId());
  paymentGateway.charge(order.getId());
  return mapper.toDto(order);
}

@Transactional(rollbackFor = ApplicationException.class)
private Order saveOrderInternal(OrderDto dto) {
  return orderRepository.save(mapper.toEntity(dto));
}
```

## Exception Handling

- Use `ApplicationException` with per-domain error code enums (`constant/ErrorCode.java`). Never throw raw `RuntimeException` with string messages or inline error code strings.

```java
// Wrong
throw new RuntimeException("Product not found with id: " + id);
throw new IllegalArgumentException("Invalid quantity");
throw new ApplicationException("PRO1001", "Product not found");

// Right
throw new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND);
throw new ApplicationException(ErrorCode.INVALID_QUANTITY);
```

- User-facing messages can be defined in the error code enum or externalized via `messages.properties`.

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

## REST Controller Patterns

- Always use `ResponseBody.builder()` + `.toResponseEntity()` for all endpoint responses.

```java
// Wrong
ResponseEntity.status(HttpStatus.CREATED).body(new ResponseBody<>("201", "Created", data));
return ResponseEntity.ok(data);

// Right
return ResponseBody.builder()
    .code(SuccessCode.PRODUCT_CREATED)
    .message("Product created successfully")
    .data(data)
    .build()
    .toResponseEntity(HttpStatus.CREATED);
```

- Use specific HTTP method annotations (`@GetMapping`, `@PostMapping`, etc.). Never `@RequestMapping` without method.

```java
// Wrong
@RequestMapping("/products/{id}")
public ProductDto getProduct(@PathVariable Long id) { ... }

// Right
@GetMapping("/products/{id}")
public ResponseEntity<Object> getProduct(@PathVariable Long id) { ... }
```

- Use `@Valid @RequestBody` on POST/PUT. Omit `@Valid` on PATCH for partial updates.

## Validation

- Put validation annotations on DTOs, not on entities. Entities represent DB state; DTOs represent API contracts.

```java
// Wrong
@Entity
public class Product {
  @NotBlank(message = "Name is required")
  private String name;
}

// Right
@Data
public class ProductDto {
  @NotBlank(message = "Product name is required")
  private String name;
}
```

- Use `@Null` on ID fields in creation DTOs to prevent clients from supplying IDs.

```java
// Right
@Data
public class CreateProductDto {
  @Null(message = "Product ID must be null for creation")
  private Long id;

  @NotBlank(message = "Product name is required")
  private String name;
}
```

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

```java
// Wrong — business logic in controller
@PostMapping("/orders")
public ResponseEntity<Object> createOrder(@Valid @RequestBody OrderDto dto) {
  if (dto.getItems().isEmpty()) throw new ApplicationException(ErrorCode.EMPTY_ORDER);
  Order order = mapper.toEntity(dto);
  order.setStatus(OrderStatus.PENDING);
  order.setTotal(calculateTotal(dto));
  return ResponseBody.builder().data(orderRepository.save(order)).build().toResponseEntity(HttpStatus.CREATED);
}

// Right — controller delegates to service
@PostMapping("/orders")
public ResponseEntity<Object> createOrder(@Valid @RequestBody OrderDto dto) {
  OrderDto result = orderService.createOrder(dto);
  return ResponseBody.builder().code(SuccessCode.ORDER_CREATED).data(result)
      .build().toResponseEntity(HttpStatus.CREATED);
}
```

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

// Also right: derived query
List<Product> findByNameContainingIgnoreCase(String keyword);
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

```java
// Wrong
@Entity
public class Product {
  private Long createdAt;
  private Long updatedAt;
}

// Right
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product extends BaseProduct { ... }
```

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
- Always specify a cache key. Never use default key generation for entity caches.
- Always use `unless = "#result == null"` to avoid caching empty results.

```java
// Wrong
@Cacheable("products")
public ProductDto findById(Long id) { ... }

@CachePut(value = "products")
public ProductDto update(Long id, ProductDto dto) { ... }

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

```yaml
# application.yml — defaults
spring:
  jpa:
    hibernate:
      ddl-auto: update

# application-test.yml — test overrides only
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
  cache:
    type: none
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
```

- Use `@ActiveProfiles("test")` on all test classes. Never rely on the default profile for tests.

## Lombok Usage

- Use `@Slf4j` for logging. Never create loggers manually with `LoggerFactory`.
- Use `@Data` on DTOs. Use `@Getter @Setter` on entities (to control access granularity).
- Use `@Builder` or `@SuperBuilder` on DTOs and entities. Never write builder classes manually.

```java
// Wrong
private static final Logger log = LoggerFactory.getLogger(ProductService.class);

@Getter @Setter
public class ProductDto { ... } // DTO should use @Data

// Right
@Slf4j
@Service
public class ProductServiceImpl implements ProductService { ... }

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto { ... }
```

## AOP Usage

- Use AOP for cross-cutting concerns only (logging, metrics, tracing).
- Never use AOP to implement business logic.

```java
// Wrong — business logic via AOP
@AfterReturning("execution(* com.mrtripop.order.service.*.*(..))")
public void applyDiscount(JoinPoint jp) {
  // applying discount logic in aspect
}

// Right — cross-cutting concern
@Around("execution(public * com.mrtripop..*(..))")
public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
  log.debug("Entering: {}", joinPoint.getSignature().getName());
  Object result = joinPoint.proceed();
  log.debug("Exiting: {}", joinPoint.getSignature().getName());
  return result;
}
```

## Request Mapping Conventions

- Use plural nouns for collection resources: `/api/v1/products`, `/api/v1/orders`.
- Use kebab-case for path segments: `/api/v1/order-items`, not `/api/v1/orderItems`.
- Use consistent API versioning prefix: `/api/v1/`.

```java
// Wrong
@GetMapping("/api/Product")
@GetMapping("/api/v1/orderItems")
@GetMapping("/getProductById")

// Right
@GetMapping("/api/v1/products")
@GetMapping("/api/v1/order-items")
@GetMapping("/api/v1/products/{id}")
```

## Application Properties Structure

- Group related properties under a prefix. Never use flat, ungrouped properties.

```yaml
# Wrong
app.name=StockManagement
app.version=1.0.0
cache.ttl=3600
cache.enabled=true

# Right
app:
  name: StockManagement
  version: 1.0.0

cache:
  ttl: 3600
  enabled: true
```

- Use environment variables for secrets and environment-specific values with sensible defaults.

```yaml
# Right
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/stockdb}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:}
```

## Do NOT

- Never use `@Autowired` — use `@RequiredArgsConstructor` instead.
- Never return JPA entities from controllers — always use DTOs.
- Never put `@Transactional` on controllers.
- Never catch `Exception` broadly without re-throwing or logging.
- Never use field injection (`@Autowired` on fields).
- Never create `new` instances of beans — always inject them.
- Never use `System.out.println` — use `@Slf4j` logging.
- Never use `Thread.sleep` in application code — use `@Scheduled` or async.
- Never use `@CrossOrigin(origins = "*")` — configure CORS in `SecurityConfig`.
- Never use `@Controller` for REST APIs — use `@RestController`.
- Never call `repository.save()` in a loop — use `saveAll()`.
- Never use `@EnableAutoConfiguration` directly — use `@SpringBootApplication`.
- Never start the application context in unit tests — use `@MockBean` or Mockito.
- Never mix `@SpringBootTest` with `@MockBean` for service tests — that is an integration test.
