# Security Standards

Security-specific rules that go beyond standard API design and Spring Boot practices. Each rule has a correct and incorrect example.

**Note:** Input validation (`@Valid @RequestBody`) and response wrapping are in `api-design.md`. Repository query safety (parameterized queries) is in `spring-boot-practices.md`.

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

## Response Filtering

- Never expose internal IDs, stack traces, or system details in error responses.
- Never log passwords, tokens, PII, or full request/response bodies containing sensitive data.

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

## Do NOT

- Never implement custom authentication — use Spring Security
- Never commit secrets to source code — use env vars or config
- Never use `@CrossOrigin(origins = "*")` in production — configure CORS in `SecurityConfig`
- Never expose stack traces or internal IDs in error responses
- Never log passwords, tokens, or PII
