# No-Hardcoding Standards

Concise rules for AI agents. Each rule has a correct and incorrect example.

## No Magic Numbers

- Every numeric literal (except `0`, `1`, `-1`) must be a named constant or enum.
- Magic numbers in business logic must reference domain constants.

```java
// Wrong
if (order.getStatus() == 3) { ... }
if (retryCount > 5) { throw new RetryExceededException(); }
if (price < 0) { ... }

// Right
if (order.getStatus() == OrderStatus.SHIPPED.getCode()) { ... }
if (retryCount > MAX_RETRY_COUNT) { throw new RetryExceededException(); }
// 0 is allowed as a literal
if (price < 0) { throw new InvalidPriceException(); }
```

## No Magic Strings

- Every string literal (except `""`, `""` for empty, `"UTC"`, content strings) must be a named constant or enum.
- Role names, status names, type identifiers — all must be enums or constants.

```java
// Wrong
if (user.getRole().equals("ADMIN")) { ... }
if (fileType.equals("csv")) { ... }
throw new ApplicationException("ERR001", "User not found");

// Right
if (user.getRole().equals(Role.ADMIN.getValue())) { ... }
if (fileType.equals(FileType.CSV.getExtension())) { ... }
throw new ApplicationException(ErrorCode.USER_NOT_FOUND);
```

## Configuration Values

- Timeouts, URLs, thresholds, batch sizes — all in `application.yml` via `@Value` or `@ConfigurationProperties`.
- Never hardcoded in service or controller code.

```java
// Wrong
RestTemplate restTemplate = new RestTemplate();
restTemplate.setReadTimeout(30_000);

@Value("${api.timeout}")
private int timeout; // This is fine

// Right
@ConfigurationProperties(prefix = "app.api")
@Data
public class ApiProperties {
  private Duration readTimeout;
  private String baseUrl;
  private int maxRetries;
}
```

## Error Messages

- Use error code enums (per-domain `constant/ErrorCode.java`), not inline strings.
- User-facing messages can be in the enum or externalized via `messages.properties`.

```java
// Wrong
throw new ApplicationException("PRO1001", "Product not found");

// Right
throw new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND);
```

## Database Queries

- Query parameters only. Never concatenate user input into SQL/JPQL strings.

```java
// Wrong
@Query("SELECT p FROM Product p WHERE p.name = '" + name + "'")
List<Product> findByName(String name);

// Right
@Query("SELECT p FROM Product p WHERE p.name = :name")
List<Product> findByName(@Param("name") String name);
```

## No Hardcoded File Paths

- Use Spring `Resource` abstraction or externalized config for file paths.

```java
// Wrong
File file = new File("/opt/app/data/export.csv");

// Right
@Value("classpath:data/export.csv")
Resource exportTemplate;
```

## Feature Flags / Toggles

- External configuration only. Never `boolean featureEnabled = true` in source code.

```java
// Wrong
private boolean isNewPaymentFlow = true;

// Right
@Value("${feature.new-payment-flow.enabled:false}")
private boolean isNewPaymentFlowEnabled;
```
