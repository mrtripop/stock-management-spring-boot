# CLAUDE.md

AI agent context for this repository. Read before making changes.

## Tech Stack

Java 17 | Spring Boot 3.4.2 | Maven | PostgreSQL 14.6 | Redis 7.2 | H2 (test)
Lombok | MapStruct | springdoc-openapi | Log4j2 | Spring AOP
Google Java Format | Conventional Commits (commitlint)

## Commands

- `mvn spring-boot:run` — Start app (needs postgres + redis running)
- `mvn clean compile` — Compile
- `mvn clean package` — Build JAR
- `mvn test` — Run all tests
- `mvn test -Dtest=ClassName` — Run specific test class
- `docker compose up -d postgres redis` — Start infra (database + cache)
- `docker compose up -d` — Start full stack (postgres, redis, redisinsight, ELK, otel-collector)
- `docker compose down` — Stop all containers
- `make setup-commitlint` — First-time commit hook setup

## Package Structure

Base: `com.mrtripop`

**Domains:**
- `product/` — controllers, services, services/impl, services/manager, models/db, models/dto, repository, component, constant
- `location/` — controllers, services, interfaces, models/entities, models/dtos, repositories, constant
- `clinical/` — controllers, services, services/impl, models/db, models/dto, repository, component
- `order/` — controllers, models, repositories, services
- `transaction/` — controller, models, repository, service
- `users/` — controllers, models, repositories, services

**Shared:**
- `model/` — `ResponseBody<T>`, `BaseQueryParams`
- `exception/` — `ApplicationException`, `NotFoundException`, `ErrorResponse`, `CustomControllerAdvice`
- `config/` — `SecurityConfig`, `RedisConfig`, `AppConfig`, `OpenAPIConfig`
- `component/fileparser/` — FileParser strategy (CSV/JSON/XML), `FileParserFactory`
- `constant/` — `BaseStatusCode` (interface), per-domain `ErrorCode`/`SuccessCode` enums
- `aspect/` — `GlobalAspect` (AOP method logging)

Preferred internal layout for new domains: `models/db/` + `models/dto/` (product/clinical style)

## Entity Patterns

Base class: `product.models.db.AuditEntity` (`@MappedSuperclass`)
- `@CreatedDate Long createdAt` | `@LastModifiedDate Long updatedAt`
- `@SuperBuilder` `@EntityListeners(AuditingEntityListener.class)`

Hierarchy: `AuditEntity` → `BaseProduct` → `Product` (each level `@SuperBuilder`)
ID generation: `@SequenceGenerator(allocationSize = 1)` for Long IDs | `GenerationType.UUID` for UUIDs
Naming: snake_case columns (`@Column(name = "generic_name")`), Jackson `SNAKE_CASE` globally
Indexes: `@Table(name = "x", indexes = {@Index(name = "x_code", columnList = "code")})`
Lombok: `@SuperBuilder @NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString` on entities

## DTO Patterns

`@Data @Builder @NoArgsConstructor @AllArgsConstructor`

Validation:
- Auto-generated IDs: `@Null(message = "Request body ID should be null")`
- Required strings: `@NotBlank(message = "...")`
- Required refs: `@NotNull(message = "...")`
- Bounds: `@Min(value = 0)`, `@Length(max = 300)`

MapStruct mappers:
- Product: static `INSTANCE = Mappers.getMapper(...)` — `@Mapper` (no componentModel)
- Clinical: Spring bean `@Mapper(componentModel = "spring")` — inject via `@RequiredArgsConstructor`
- **Prefer `componentModel = "spring"` for new mappers**

## Service Patterns

`@Slf4j @Service @RequiredArgsConstructor`

Two approaches (follow clinical for new code):
- **Product**: `ProductService` interface → `ProductServiceImpl` (caching + orchestration) + `ProductManager` (raw DB)
- **Clinical**: `MasterCatalogService` interface → `MasterCatalogServiceImpl` (direct repository access)

Caching: `@Cacheable` `@CachePut` `@CacheEvict` on product service methods
Transactions: `@Transactional` on writes, `@Transactional(readOnly = true)` on reads
Audit: inject `AuditService` for centralized audit recording with checksums

## Controller Patterns

`@Slf4j @RestController @RequiredArgsConstructor`

Endpoint styles:
- Product: `/api/inventory/{resource}s` (e.g., `/api/inventory/products`)
- Clinical: `/api/v1/clinical/catalog/{resource}s` (e.g., `/api/v1/clinical/catalog/molecules`)
- **Prefer `/api/v1/{domain}/{resource}s` for new domains**

Response wrapping (both use `com.mrtripop.model.ResponseBody<T>`):
```java
// Clinical style (direct ResponseEntity)
ResponseEntity.status(HttpStatus.CREATED)
    .body(new ResponseBody<>(String.valueOf(status.value()), "message", data));

// Product style (builder + toResponseEntity)
ResponseBody.builder().code(code).message("msg").data(result)
    .build().toResponseEntity(HttpStatus.OK);
```

Validation: `@Valid @RequestBody` on POST/PUT, omit `@Valid` on PATCH for partial updates
Pagination: `@Valid BaseQueryParams` (page, size, orderBy)

## Error Handling

Two `@ControllerAdvice` handlers:
1. `ControllerExceptionHandler` (`@Order(HIGHEST_PRECEDENCE)`, scoped to `@RestController`)
   - `ApplicationException` → `ResponseBody` with error code/message
   - `MethodArgumentNotValidException` → GB4041
   - `HandlerMethodValidationException` → GB4042
   - `MethodArgumentTypeMismatchException` → GB4043
2. `CustomControllerAdvice` (catch-all, legacy)
   - `NullPointerException` → `ErrorResponse` with stacktrace (404)
   - `Exception` → `ErrorResponse` with stacktrace (500)

Error codes: enum implementing `BaseStatusCode` (`getCode()`, `getMessage()`)
- Pattern: `{PREFIX}{HTTP_STATUS_DIGIT}{SEQUENCE}` e.g., `PRO1001`, `GB4041`
- Per-domain files: `product/constant/ErrorCode.java`, `location/constant/ErrorCode.java`
- `NotFoundException` (`@ResponseStatus(NOT_FOUND)`) used in clinical domain

## Infrastructure

- **Security**: Currently `permitAll()` (disabled). Role hierarchy planned: EMPLOYEE → MANAGER → ADMIN
- **Caching**: Redis with `GenericJackson2JsonRedisSerializer`. TTL via `CACHE_REDIS_TTL` env var
- **Logging**: Log4j2. `GlobalAspect` AOP logs method in/outs at DEBUG
- **DB**: PostgreSQL with `ddl-auto: update`. Snake case naming strategy. `@EnableJpaAuditing`
- **Tests**: H2 in-memory (PostgreSQL mode), cache type=none, `@ActiveProfiles("test")`
- **Swagger**: http://localhost:8080/swagger-ui/index.html

## Code Style

See `.claude/standards/naming-style.md` for full naming and style standards.

Quick reference:
- Google Java Format (100 char line limit, no wildcard imports)
- camelCase methods/variables, PascalCase classes
- Conventional Commits enforced via commitlint
- `@Slf4j` for logging (never manual `LoggerFactory`)
- `@RequiredArgsConstructor` for constructor injection (not `@Autowired`)
- Prefer MapStruct over manual mapping

## No Hardcoding

See `.claude/standards/hardcoding.md` for full no-hardcoding standards.

Quick reference:
- No magic numbers/strings — use constants or enums (except `0`, `1`, `-1`, `""`)
- Config values in `application.yml`, not in code
- Error messages from error code enums, not inline strings
- No string concatenation in queries

## Security

See `.claude/standards/security.md` for full security standards.

Quick reference:
- `@Valid @RequestBody` on all POST/PUT endpoints
- Parameterized queries only — never concatenate user input
- `@PreAuthorize` for role checks — never manual `if` checks
- Never commit secrets — use env vars or config
- Never return entities from controllers — use DTOs
- Never log passwords, tokens, or PII

## Performance

See `.claude/standards/performance.md` for full performance standards.

Quick reference:
- Always paginate list endpoints — never unbounded results
- `JOIN FETCH` / `@EntityGraph` to prevent N+1 queries
- `FetchType.LAZY` by default for relationships
- `saveAll()` over individual `save()` in loops
- Keep transactions short — no external calls inside `@Transactional`

## Testing

See `.claude/standards/testing.md` for full testing standards.

Quick reference:
- `@ExtendWith(MockitoExtension.class)` for unit tests
- SQL query tests only for integration tests — no API/HTTP integration tests
- Fixture classes in `src/test/java/{domain}/fixture/` (e.g., `MoleculeFixture`, `BrandFixture`)
- H2 in-memory with `create-drop` for test isolation
- `@DisplayName` with clear business intention and test purpose (e.g., "should find all brands associated with a specific molecule")
- Each @DisplayName should explain why the test exists and what business behavior it verifies

## Notes

- `learning/` package is practice code — do not copy its patterns into production code
- `src/main/java/com/mrtripop/constant/ErrorCode.java` (root) is legacy — per-domain error codes are canonical
- GEMINI.md exists for cross-tool compatibility; CLAUDE.md is the authoritative context
- Clinical response style (direct `ResponseEntity`) diverges from Product/Location (`ResponseBody.builder`). Follow whichever domain you are extending
