# Development Guide

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Java JDK | 17.0.4+ | Amazon Corretto or OpenJDK |
| Maven | 3.9+ | Wrapper included (`./mvnw`) |
| Docker | Latest | For PostgreSQL, Redis, ELK stack |
| Docker Compose | Latest | For multi-container orchestration |
| Node.js | 18+ | Only for commitlint setup |

## Environment Setup

### 1. Clone and build

```bash
git clone <repo-url>
cd stock-management-spring-boot
./mvnw clean compile
```

### 2. Start infrastructure

```bash
docker compose up -d postgres redis
```

For full stack (includes ELK, OpenTelemetry, RedisInsight):

```bash
docker compose up -d
```

### 3. Configure environment variables

Copy and configure environment files:

```bash
# Required variables (see .env.dev / .env.local)
DATASOURCE_URL=jdbc:postgresql://localhost:5432/inventory
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=postgres
REDIS_URL=redis://localhost:6379
REDIS_USERNAME=default
REDIS_PASSWORD=<redis-password>
REDIS_TIMEOUT=2000
CACHE_REDIS_TTL=600000
JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true
JPA_PROPERTIES_SHOW_SQL=false
LOGGING_LEVEL_COM_MRTRIPOP=DEBUG
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

Application starts at: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui/index.html

## Common Commands

| Command | Description |
|---------|-------------|
| `./mvnw spring-boot:run` | Start application |
| `./mvnw clean compile` | Compile |
| `./mvnw clean package` | Build JAR |
| `./mvnw test` | Run all tests |
| `./mvnw test -Dtest=ClassName` | Run specific test |
| `docker compose up -d postgres redis` | Start DB + cache |
| `docker compose up -d` | Start full stack |
| `docker compose down` | Stop all containers |
| `make setup-commitlint` | Setup commit hooks |

## Testing

### Test Profile

- Database: H2 in-memory (PostgreSQL compatibility mode)
- DDL: `create-drop` (test isolation)
- Cache: `none` (disabled)
- Logging: `DEBUG` for `com.mrtripop`

### Test Patterns

- **Unit tests**: `@ExtendWith(MockitoExtension.class)` with `@Mock` / `@InjectMocks`
- **Fixtures**: Object Mother pattern in `src/test/java/{domain}/fixture/` (e.g., `MoleculeFixture`, `BrandFixture`)
- **Organization**: `@Nested` classes for grouping, `@DisplayName` with business intention
- **AAA Pattern**: Arrange-Act-Assert with blank lines between phases

### Running Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=MasterCatalogServiceImplTest

# Specific test method
./mvnw test -Dtest=MasterCatalogServiceImplTest#shouldSaveAndReturnDto
```

## Code Style

- **Formatter**: Google Java Format (100 char line limit, no wildcard imports)
- **Lombok**: `@Slf4j`, `@RequiredArgsConstructor`, `@Data`, `@Builder`, `@Getter`, `@Setter`
- **Naming**: camelCase methods/variables, PascalCase classes, UPPER_SNAKE_CASE constants
- **No magic numbers/strings**: Use constants or enums (except `0`, `1`, `-1`, `""`)
- **Constructor injection**: `@RequiredArgsConstructor` (not `@Autowired`)
- **DTO mapping**: MapStruct with `componentModel = "spring"` for new mappers

## Commit Convention

- **Format**: Conventional Commits enforced via commitlint + Husky
- **Setup**: `make setup-commitlint` (first time only)
- **Examples**: `feat(product): add product search`, `fix(clinical): resolve duplicate molecule`, `chore: update dependencies`

## Project Structure Conventions

### New Domain Layout

Follow the `clinical/` or `product/` pattern:

```
{domain}/
├── controllers/
├── services/
│   └── impl/
├── models/
│   ├── db/          # JPA entities
│   └── dto/         # Data transfer objects
├── repository/
├── component/       # Mappers
└── constant/        # ErrorCode, SuccessCode enums
```

### Endpoint URL Convention

- **Preferred**: `/api/v1/{domain}/{resource}s` (e.g., `/api/v1/location/addresses`)
- **Legacy product**: `/api/inventory/{resource}s`
- **Legacy clinical**: `/api/v1/clinical/catalog/{resource}s`

### Response Convention

Always use `ResponseBody.builder()` pattern:

```java
return ResponseBody.builder()
    .code(SuccessCode.PRODUCT_CREATED)
    .message("Product created successfully")
    .data(productDto)
    .build()
    .toResponseEntity(HttpStatus.CREATED);
```
