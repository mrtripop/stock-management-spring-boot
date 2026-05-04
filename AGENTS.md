# Agents

AI agent context for this repository. Read before making changes.

## Tech Stack

Java 17 | Spring Boot 3.4.2 | Maven | PostgreSQL 14.6 | Redis 7.2 | H2 (test)
Lombok | MapStruct 1.6.0 | springdoc-openapi 2.8.6 | Log4j2 | Spring AOP
Google Java Format | Conventional Commits (commitlint)

## Commands

- `./mvnw spring-boot:run` — Start app (needs postgres + redis)
- `./mvnw clean compile` / `./mvnw clean package` — Compile / Build JAR
- `./mvnw test` / `./mvnw test -Dtest=ClassName` — Run tests
- `docker compose up -d postgres redis` — Start infra
- `docker compose up -d` — Start full stack (postgres, redis, redisinsight, ELK, otel-collector)
- `docker compose down` — Stop all containers
- `make setup-commitlint` — First-time commit hook setup

## Searching Code

Prefer dedicated tools and simple bash commands. Do not use `find -exec` — it triggers permission prompts.

| Goal | Use |
|------|-----|
| Read a known file | `Read` tool |
| Find files by name/glob | `find . -name "*.java"` or `find . -path "*/controllers/*"` |
| Search file contents | `grep -rn "pattern" src/` or `grep -rl "pattern" src/` |
| Find a symbol/definition | `LSP` tool (`goToDefinition`, `workspaceSymbol`) |
| Find all references | `LSP` tool (`findReferences`) |
| Broad codebase exploration | `Agent` with `subagent_type=Explore` |

**Never** use `find -exec` — run `find` to list files, then use `Read` tool to read them.
**Never** use `cat`, `head`, `tail` — use `Read` tool instead.

## Package Structure

Base: `com.mrtripop`

```
com.mrtripop/
├── product/          # Product catalog
├── clinical/         # Pharmacy — molecules, brands, stores, store-products, audit ledger
├── inventory/        # Stock — batches, store-stock, unit-conversions, FEFO deduction
├── location/         # Addresses, warehouses
├── order/            # Order management
├── transaction/      # Transaction processing
├── users/            # User management
│
├── model/            # ResponseBody<T>, BaseQueryParams
├── exception/        # ApplicationException, NotFoundException, ErrorResponse, CustomControllerAdvice
├── config/           # SecurityConfig, RedisConfig, AppConfig, OpenAPIConfig
├── component/fileparser/  # FileParser strategy (CSV/JSON/XML), FileParserFactory
├── constant/         # BaseStatusCode (interface), ErrorCode, SuccessCode
├── aspect/           # GlobalAspect (AOP method logging)
├── util/             # Shared utilities
```

**New domain layout:**

```
domain/
├── controllers/
├── services/
│   ├── DomainService.java
│   └── impl/DomainServiceImpl.java
├── models/
│   ├── db/    (JPA entities)
│   └── dto/   (request/response DTOs)
├── repository/
├── component/DomainMapper.java   (MapStruct, componentModel = "spring")
└── constant/ErrorCode.java, SuccessCode.java
```

## Project Conventions

These are project-specific decisions that aren't obvious from reading code. For coding rules, see the `.claude/rules/` files.

**Entities:**
- Base: `product.models.db.AuditEntity` — `@CreatedDate Long createdAt`, `@LastModifiedDate Long updatedAt` (epoch millis, not `LocalDateTime`)
- `@SuperBuilder` at every hierarchy level. ID: `@SequenceGenerator(allocationSize=1)` for Long, `GenerationType.UUID` for UUID
- `@Version Long version` for optimistic locking on concurrent entities

**Responses:**
- Always wrap in `ResponseBody.builder()...toResponseEntity()` — code and message from `SuccessCode` enum
- Return type: `ResponseEntity<Object>`. Never return entities directly.
- Throw `ApplicationException(ErrorCode.X, HttpStatus.Y)` for errors — never construct error responses manually

**Error codes:**
- `{PREFIX}{STATUS_DIGIT}{SEQ}` — `GB` (global), `PRO` (product), `INV` (inventory)
- `ControllerExceptionHandler` is in `product.component` package (not `exception`)

**Infrastructure:**
- Security: `permitAll()` (disabled). In-memory user: user/password
- Caching: Redis, `GenericJackson2JsonRedisSerializer`, TTL via `CACHE_REDIS_TTL` env var
- DB: PostgreSQL, `ddl-auto: update`, snake_case naming, `@EnableJpaAuditing`
- Tests: H2 in-memory (PostgreSQL mode), cache type=none, `@ActiveProfiles("test")`, `create-drop`
- Env vars: `DATASOURCE_URL/USERNAME/PASSWORD`, `REDIS_URL/USERNAME/PASSWORD/TIMEOUT`, `CACHE_REDIS_TTL`, `LOGGING_LEVEL_COM_MRTRIPOP`

## Detailed Rules

Each file below contains correct/incorrect examples. **Read the relevant file before implementing.**

| File | Scope |
|------|-------|
| `.claude/rules/api-design.md` | Endpoints: URLs, HTTP methods, request/response DTOs, validation on endpoints, pagination, response wrapping, file ops |
| `.claude/rules/spring-boot-practices.md` | Framework: DI, stereotypes, config properties, transactions, service/repo patterns, mappers, caching, Lombok, AOP |
| `.claude/rules/coding-style.md` | Java language: naming, formatting, comments, magic numbers/strings, immutability, collections, streams, concurrency |
| `.claude/rules/testing.md` | Tests: naming, @Nested grouping, AAA pattern, fixtures with constants, unit/integration setup |
| `.claude/rules/security.md` | Security: input validation, SQL injection, XSS, auth, secrets, CORS, response filtering, logging |
| `.claude/rules/performance.md` | Performance: N+1 prevention, DB indexes, lazy loading, batch operations, connection management, async |

## Notes

- `learning/` package is practice code — do not copy its patterns
- `constant/ErrorCode.java` (root) and `constant/SuccessCode.java` (root) are root-level codes — per-domain codes are canonical
- `ControllerExceptionHandler` is in `product.component` package (not `exception`)
- `AuditLedgerRepository` overrides delete methods as no-op — audit entries are immutable
- All domains use `/api/v1/{domain}` prefix — domains not yet migrated should be updated to follow this pattern
