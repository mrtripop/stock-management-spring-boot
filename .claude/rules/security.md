# Security Standards

Concise rules for AI agents. Each rule has a correct and incorrect example.

## Input Validation

- Always validate at system boundaries (controllers). Trust nothing from HTTP requests.
- Use `@Valid @RequestBody` on POST/PUT. Use `@Validated` on query params.

```java
// Wrong
@PostMapping("/products")
public ResponseEntity<?> create(@RequestBody ProductDto dto) { ... }

// Right
@PostMapping("/products")
public ResponseEntity<?> create(@Valid @RequestBody ProductDto dto) { ... }
```

## SQL Injection Prevention

- Use JPA repository methods, `@Query` with named params, or `CriteriaBuilder`. Never concatenate user input into queries.

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

## XSS Prevention

- Sanitize user-supplied strings before rendering or storage.
- Use `@JsonIgnore` on fields that should never be exposed in API responses.

```java
// Wrong
@Data
public class UserDto {
  private String password; // exposed in JSON response
}

// Right
@Data
public class UserDto {
  private String username;
  @JsonIgnore private String password;
}
```

## Authentication & Authorization

- Use Spring Security. Never implement custom auth mechanisms.
- Role checks via `@PreAuthorize`, not manual `if` checks in service code.

```java
// Wrong
public void deleteUser(Long id) {
  User currentUser = getCurrentUser();
  if (!currentUser.getRole().equals("ADMIN")) {
    throw new AccessDeniedException();
  }
  // ...
}

// Right
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { ... }
```

## Secrets Management

- Never commit passwords, API keys, or tokens to source code.
- Use environment variables, `application.yml` with Spring profiles, or a secrets manager.

```java
// Wrong
private static final String DB_PASSWORD = "super_secret_123";

// Right (application.yml)
spring:
  datasource:
    password: ${DB_PASSWORD}

// Right (environment variable)
@Value("${stripe.api-key}")
private String stripeApiKey;
```

## CORS Configuration

- Configure explicitly in `SecurityConfig`. No wildcard `*` origins in production.

```java
// Wrong
@CrossOrigin(origins = "*")

// Right
@Bean
CorsConfigurationSource corsConfigurationSource() {
  CorsConfiguration config = new CorsConfiguration();
  config.setAllowedOrigins(List.of("https://app.example.com"));
  config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
  config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
  return new UrlBasedCorsConfigurationSource();
}
```

## HTTP Method Restrictions

- Use specific mappings (`@GetMapping`, `@PostMapping`). Never `@RequestMapping` without method.
- Sensitive actions (delete, update) must not accept GET requests.

```java
// Wrong
@RequestMapping("/products/{id}") // accepts all methods

// Right
@GetMapping("/products/{id}")
public ProductDto getById(@PathVariable Long id) { ... }

@DeleteMapping("/products/{id}")
@PreAuthorize("hasRole('ADMIN')")
public void delete(@PathVariable Long id) { ... }
```

## Response Filtering

- Never return JPA entities directly from controllers. Always use DTOs.
- Never expose internal IDs, stack traces, or system details in error responses.

```java
// Wrong
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) { ... } // returns entity with password hash

// Right
@GetMapping("/users/{id}")
public UserDto getUser(@PathVariable Long id) {
  return userMapper.toDto(userService.findById(id));
}
```

## Logging Security

- Never log passwords, tokens, PII, or full request/response bodies containing sensitive data.
- Use data masking for sensitive fields in log output.

```java
// Wrong
log.info("User login: {}", user); // logs password hash
log.debug("Request body: {}", requestBody); // may contain sensitive data

// Right
log.info("User login attempt: username={}", user.getUsername());
log.debug("Request: endpoint={}, method={}", request.getRequestURI(), request.getMethod());
```

## Dependency Security

- Keep dependencies updated. Check for known CVEs.
- No libraries with known unpatched vulnerabilities.
