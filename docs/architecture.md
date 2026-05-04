# Architecture

## Executive Summary

Stock Management System is a Spring Boot 3.4.2 backend application for managing pharmaceutical inventory. It follows a domain-driven design with 7 business domains, RESTful API endpoints, PostgreSQL persistence, Redis caching, and ELK-based observability.

## Technology Stack

| Category | Technology | Version | Justification |
|----------|-----------|---------|---------------|
| Language | Java | 17 | LTS release, modern features |
| Framework | Spring Boot | 3.4.2 | Enterprise Java standard |
| Build Tool | Maven | 3.9+ | Java ecosystem standard |
| Database | PostgreSQL | 14.6 | ACID-compliant relational DB |
| Cache | Redis | 7.2 | High-performance in-memory cache |
| Test DB | H2 | (embedded) | PostgreSQL-compatible in-memory |
| ORM | Spring Data JPA / Hibernate | (managed) | JPA standard with Spring integration |
| Object Mapping | MapStruct | 1.6.0 | Compile-time type-safe mapping |
| Boilerplate | Lombok | 1.18.30 | Reduces ceremony code |
| API Docs | springdoc-openapi | 2.0.3 | OpenAPI 3 / Swagger UI |
| Security | Spring Security | (managed) | Authentication & authorization |
| Logging | Log4j2 | (managed) | Replaces default Logback |
| AOP | Spring AOP | (managed) | Cross-cutting concerns (logging) |
| Observability | OpenTelemetry | 1.24.0 | Distributed tracing |
| File Parsing | Apache Commons CSV | 1.8 | CSV parsing |
| XML Support | Jackson XML | (managed) | XML serialization |
| Reactive | Spring WebFlux | (managed) | Reactive programming support |
| Containerization | Docker | Latest | Multi-stage build |
| Commit Convention | commitlint + Husky | Latest | Conventional Commits enforcement |

## Architecture Pattern

**Layered Architecture with Domain-Driven Package Structure**

```
┌─────────────────────────────────────────────┐
│              Controllers (REST API)          │
│   Validation (@Valid), ResponseBody wrapping │
├─────────────────────────────────────────────┤
│              Services (Business Logic)       │
│   Caching, Transactions, Orchestration      │
├─────────────────────────────────────────────┤
│              Managers (Optional)             │
│   Raw DB access, complex queries            │
├─────────────────────────────────────────────┤
│              Repositories (Data Access)      │
│   Spring Data JPA, derived + custom queries │
├─────────────────────────────────────────────┤
│              Models (Entities + DTOs)        │
│   JPA entities, MapStruct mappers           │
└─────────────────────────────────────────────┘
         │                    │
    ┌────┴────┐         ┌────┴────┐
    │PostgreSQL│         │  Redis   │
    │  14.6    │         │  7.2     │
    └─────────┘         └──────────┘
```

### Service Layer Patterns

Two approaches exist (prefer clinical style for new code):

1. **Product pattern**: Interface -> Impl (caching + orchestration) + Manager (raw DB)
2. **Clinical pattern**: Interface -> Impl (direct repository access)

### Error Handling Architecture

Dual `@ControllerAdvice` pattern:

```
Request
  │
  ├── Validation Error ──→ ControllerExceptionHandler (HIGHEST_PRECEDENCE)
  │                          ├── ApplicationException → ResponseBody with error code
  │                          ├── MethodArgumentNotValidException → GB4041
  │                          ├── HandlerMethodValidationException → GB4042
  │                          └── MethodArgumentTypeMismatchException → GB4043
  │
  └── Runtime Error ────→ CustomControllerAdvice (catch-all)
                             ├── NullPointerException → ErrorResponse (404)
                             └── Exception → ErrorResponse (500, with stacktrace)
```

## Domain Architecture

### Domain Map

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│ Product  │   │ Clinical │   │ Location │   │  Order   │
│          │   │          │   │          │   │          │
│ Products │◄──│StoreProd │   │ Address  │   │  Orders  │
│ History  │   │ Molecule │   │Warehouse │   │          │
│          │   │ Brand    │   │          │   │          │
└────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘
     │              │              │              │
     └──────────────┴──────────────┴──────────────┘
                         │
              ┌──────────┴──────────┐
              │ Transaction │ Users │
              │ Balance     │ Auth  │
              └─────────────┴───────┘
```

### Domain Maturity

| Domain | Maturity | Features |
|--------|----------|----------|
| Product | High | CRUD, caching, history tracking, file import, entity hierarchy |
| Clinical | High | CRUD (molecules, brands, stores, store products), audit ledger |
| Location | Medium | CRUD (addresses, warehouses) |
| Order | Low | Basic CRUD |
| Transaction | Low | Basic CRUD, user balance |
| Users | Low | Basic CRUD |

## Caching Strategy

- Redis with `GenericJackson2JsonRedisSerializer`
- `@Cacheable` on read operations (e.g., `findById`)
- `@CachePut` / `@CacheEvict` on write operations
- TTL configurable via `CACHE_REDIS_TTL` environment variable
- Cache disabled in test profile (`type: none`)

## Observability Architecture

```
Application (Log4j2 + OpenTelemetry Agent)
     │                    │
     │ (OTLP)             │ (logs)
     ▼                    ▼
OpenTelemetry        Filebeat
Collector               │
     │                  ▼
     ▼              Logstash
Elasticsearch ◄────────┘
     │
     ▼
   Kibana (localhost:5601)
```

## Security Architecture

- Spring Security with `permitAll()` (currently disabled)
- Planned role hierarchy: EMPLOYEE -> MANAGER -> ADMIN
- `@PreAuthorize` planned for endpoint protection
- Input validation via `@Valid @RequestBody` on all POST/PUT endpoints
- Per-domain error codes prevent information leakage

## Configuration Architecture

All configuration externalized via environment variables:

```
application.yml (template)
       │
       ├── .env.local (local development)
       ├── .env.dev (development environment)
       └── Environment variables (production)
```

No hardcoded configuration values in source code.
