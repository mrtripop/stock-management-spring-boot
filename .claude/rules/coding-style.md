# Coding Style Standards

Concise rules for AI agents. Each rule has a correct and incorrect example.

## Variable Naming

- Use descriptive names that reveal intent. Never single letters except loop indices `i`, `j`.
- Boolean variables/methods: prefix with `is`, `has`, `should`, `can`.

```java
// Wrong
int d; // elapsed time in days
String msg;
List cl;
boolean flag;
User usr;

// Right
int elapsedTimeInDays;
String message;
List<Customer> customerList;
boolean isEmailVerified;
User user;
```

## Method Naming

- Verb phrases describing the action. Never single-word methods like `process()` or `handle()`.
- For boolean-returning methods, use `is`/`has`/`can`/`should` prefix.

```java
// Wrong
void process() {}
User get(int id) {}
boolean check() {}

// Right
void processPendingOrders() {}
User findUserById(Long id) {}
boolean isUserActive(Long userId) {}
boolean hasPermissionToEdit(User user, Document doc) {}
```

## Class Naming

- Singular nouns for entities: `Product`, `Order`, `Customer`.
- Suffixes for roles: `ProductService`, `ProductController`, `ProductRepository`, `ProductMapper`.
- Interfaces named by capability, not `I`-prefixed: `ProductService` (not `IProductService`).

```java
// Wrong
class Products {} // plural entity
class IProductService {} // I-prefix interface
class DoPayment {} // verb class name

// Right
class Product {}
interface ProductService {}
class ProductServiceImpl {}
class PaymentProcessor {}
```

## Constant Naming

- UPPER_SNAKE_CASE. Group per domain in `constant/` package.
- Use enums for fixed sets, constants for single values.

```java
// Wrong
public static final String role = "ADMIN";
public static final int max = 100;

// Right
public enum Role { ADMIN, MANAGER, EMPLOYEE }
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_TIMEZONE = "UTC";
```

## Package Naming

- All lowercase, singular nouns.

```java
// Wrong
com.mrtripop.Products
com.mrtripop.product.services.impls

// Right
com.mrtripop.product
com.mrtripop.product.services.impl
```

## No Abbreviations

- Use full words except universally known ones: `url`, `id`, `dto`, `api`, `html`.

```java
// Wrong
String desc;
String btn;
String pwd;
int cnt;

// Right
String description;
String button;
String password;
int count;
```

## Comments

- Do not comment obvious code. Only comment: hidden constraints, workarounds, non-obvious WHY.
- Never comment WHAT the code does — the code itself should say that.

```java
// Wrong
// increment counter by 1
counter++;

// set name to John
user.setName("John");

// Right
// Using 999 as sentinel because 0 is a valid order ID in the legacy system
private static final int SENTINEL_ORDER_ID = 999;
```

## Formatting

- Google Java Format: 100 char line limit, no wildcard imports, 2-space indentation.
- One class per file. Inner classes only for tightly coupled types (e.g., Builder).
- Blank line between logical sections within a method. Do not add blank lines between every statement.

```java
// Wrong
import com.mrtripop.product.models.*;
import com.mrtripop.product.models.db.*;
import com.mrtripop.product.services.*;

// Right
import com.mrtripop.product.models.db.Product;
import com.mrtripop.product.services.ProductService;
```

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
