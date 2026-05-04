# Coding Style Standards

Concise rules for AI agents. Each rule has a correct and incorrect example.

## Variable Naming

- Use descriptive names that reveal intent. Never single letters except loop indices `i`, `j`.
- Use full words — no abbreviations except universally known ones: `url`, `id`, `dto`, `api`, `html`.
- Boolean variables/methods: prefix with `is`, `has`, `should`, `can`.

```java
// Wrong
int d; // elapsed time in days
String msg;
String desc;
String btn;
int cnt;
List cl;
boolean flag;
User usr;

// Right
int elapsedTimeInDays;
String message;
String description;
String button;
int count;
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

- Every string literal (except `""` for empty, `"UTC"`, content strings) must be a named constant or enum.
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

## Immutability

- Prefer immutable classes where possible. Use `final` fields and no setters.
- Use Lombok `@Value` for immutable DTOs when mutability is not needed.

```java
// Wrong
public class Money {
  private BigDecimal amount;
  public void setAmount(BigDecimal amount) { this.amount = amount; }
}

// Right
@Value
public class Money {
  BigDecimal amount;
}
```

## Program to Interfaces

- Declare variables and return types as interfaces, not concrete implementations.

```java
// Wrong
ArrayList<String> names = new ArrayList<>();
HashMap<String, String> map = new HashMap<>();

// Right
List<String> names = new ArrayList<>();
Map<String, String> map = new HashMap<>();
```

## Collections

- Use `isEmpty()` over `size() == 0`.
- Return empty collections, never `null`.
- Use `for-each` over index-based loops.

```java
// Wrong
if (names.size() == 0) { ... }
if (items == null) { return null; }
for (int i = 0; i < items.size(); i++) { ... }

// Right
if (names.isEmpty()) { ... }
if (items == null) { return Collections.emptyList(); }
for (Item item : items) { ... }
```

## Date and Time

- Use `java.time` API (`LocalDate`, `Instant`, `ZonedDateTime`, `Duration`). Never use `java.util.Date` or `java.util.Calendar`.

```java
// Wrong
Date now = new Date();
Calendar cal = Calendar.getInstance();

// Right
LocalDate today = LocalDate.now();
Instant now = Instant.now();
ZonedDateTime meeting = ZonedDateTime.of(2026, 5, 1, 9, 30, 0, 0, ZoneId.of("UTC"));
```

## Strings

- Use text blocks (`"""`) for multi-line strings (SQL, JSON, XML). Never use string concatenation with `\n`.

```java
// Wrong
String sql = "SELECT id, name\n" + "FROM products\n" + "WHERE status = 'ACTIVE'";

// Right
String sql = """
    SELECT id, name
    FROM products
    WHERE status = 'ACTIVE'
    """;
```

## Concurrency

- Use `java.util.concurrent` (ExecutorService, CompletableFuture). Never create raw `Thread` instances.
- Use `volatile` only for simple flags. For compound operations, use `AtomicInteger`/`AtomicReference`.

```java
// Wrong
new Thread(() -> processOrder(order)).start();

// Right
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> processOrder(order));
```

## Streams

- No side effects in `map`/`filter`. Use `forEach` only for terminal side-effect operations.
- Prefer method references over lambdas.

```java
// Wrong
List<String> names = new ArrayList<>();
customers.stream().forEach(c -> names.add(c.getName()));
customers.stream().map(c -> c.getName()).toList();

// Right
List<String> names = customers.stream()
    .map(Customer::getName)
    .toList();
```
