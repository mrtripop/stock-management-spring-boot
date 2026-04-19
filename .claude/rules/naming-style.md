# Naming & Style Standards

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
