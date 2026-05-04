# Source Tree Analysis

## Project Root

```
stock-management-spring-boot/
├── src/
│   ├── main/
│   │   ├── java/com/mrtripop/
│   │   │   ├── aspect/                    # AOP cross-cutting concerns (logging)
│   │   │   │   └── GlobalAspect.java
│   │   │   ├── clinical/                  # Clinical catalog domain
│   │   │   │   ├── component/             # Domain mappers
│   │   │   │   │   ├── ClinicalMapper.java
│   │   │   │   │   └── StoreProductMapper.java
│   │   │   │   ├── controllers/           # REST API endpoints
│   │   │   │   │   ├── MasterCatalogController.java
│   │   │   │   │   └── StoreProductController.java
│   │   │   │   ├── models/
│   │   │   │   │   ├── db/                # JPA entities
│   │   │   │   │   │   ├── AuditLedger.java
│   │   │   │   │   │   ├── Brand.java
│   │   │   │   │   │   ├── Molecule.java
│   │   │   │   │   │   ├── Store.java
│   │   │   │   │   │   ├── StoreProduct.java
│   │   │   │   │   │   └── StoreType.java
│   │   │   │   │   └── dto/               # Data transfer objects
│   │   │   │   │       ├── BrandDto.java
│   │   │   │   │       ├── MoleculeDto.java
│   │   │   │   │       ├── StoreDto.java
│   │   │   │   │       └── StoreProductDto.java
│   │   │   │   ├── repository/            # JPA repositories
│   │   │   │   │   ├── AuditLedgerRepository.java
│   │   │   │   │   ├── BrandRepository.java
│   │   │   │   │   ├── MoleculeRepository.java
│   │   │   │   │   ├── StoreProductRepository.java
│   │   │   │   │   └── StoreRepository.java
│   │   │   │   └── services/
│   │   │   │       ├── impl/              # Service implementations
│   │   │   │       │   ├── AuditServiceImpl.java
│   │   │   │       │   ├── MasterCatalogServiceImpl.java
│   │   │   │       │   └── StoreProductServiceImpl.java
│   │   │   │       ├── AuditService.java
│   │   │   │       ├── MasterCatalogService.java
│   │   │   │       └── StoreProductService.java
│   │   │   ├── component/
│   │   │   │   └── fileparser/            # File parsing strategy pattern
│   │   │   │       ├── FileParserController.java
│   │   │   │       ├── FileParserService.java
│   │   │   │       └── FileParserFactory.java
│   │   │   ├── config/                    # Spring configuration
│   │   │   │   ├── AppConfig.java         # App-level properties
│   │   │   │   ├── OpenAPIConfig.java     # Swagger/OpenAPI setup
│   │   │   │   ├── RedisConfig.java       # Redis cache config
│   │   │   │   └── SecurityConfig.java    # Spring Security config
│   │   │   ├── constant/                  # Global constants (legacy)
│   │   │   │   ├── ErrorCode.java
│   │   │   │   └── SuccessCode.java
│   │   │   ├── exception/                 # Global exception handling
│   │   │   │   ├── ApplicationException.java
│   │   │   │   ├── ControllerExceptionHandler.java
│   │   │   │   ├── CustomControllerAdvice.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── NotFoundException.java
│   │   │   ├── location/                  # Location domain
│   │   │   │   ├── components/            # Location mappers
│   │   │   │   ├── constant/              # Location error/success codes
│   │   │   │   ├── controllers/           # Address & Warehouse endpoints
│   │   │   │   ├── interfaces/            # Service interfaces
│   │   │   │   ├── models/
│   │   │   │   │   ├── dtos/              # Location DTOs
│   │   │   │   │   └── entities/          # JPA entities
│   │   │   │   ├── repositories/          # JPA repositories
│   │   │   │   ├── services/              # Service implementations
│   │   │   │   └── utils/                 # Location utilities
│   │   │   ├── model/                     # Shared model classes
│   │   │   │   ├── BaseQueryParams.java   # Pagination base
│   │   │   │   └── ResponseBody.java      # Standard API response wrapper
│   │   │   ├── order/                     # Order domain
│   │   │   │   ├── controllers/
│   │   │   │   ├── models/
│   │   │   │   ├── repositories/
│   │   │   │   └── services/
│   │   │   ├── product/                   # Product domain
│   │   │   │   ├── component/             # Mappers + exception handler
│   │   │   │   ├── constant/              # Product error/success codes
│   │   │   │   ├── controllers/           # Product & History endpoints
│   │   │   │   ├── models/
│   │   │   │   │   ├── db/                # JPA entities (AuditEntity base)
│   │   │   │   │   └── dto/               # Product DTOs
│   │   │   │   ├── repository/            # JPA repositories
│   │   │   │   ├── services/
│   │   │   │   │   ├── impl/              # Service implementations
│   │   │   │   │   ├── ProductService.java
│   │   │   │   │   └── ProductManager.java # Raw DB access layer
│   │   │   │   └── util/                  # Product utilities
│   │   │   ├── transaction/               # Transaction domain
│   │   │   │   ├── controller/
│   │   │   │   ├── models/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── users/                     # Users domain
│   │   │   │   ├── controllers/
│   │   │   │   ├── models/
│   │   │   │   ├── repositories/
│   │   │   │   ├── services/
│   │   │   │   └── utils/
│   │   │   └── util/                      # Global utilities
│   │   └── resources/
│   │       ├── application.yml            # Main config (env-driven)
│   │       ├── application-test.yml       # Test profile (H2)
│   │       └── log4j2-spring.xml          # Logging config
│   └── test/
│       └── java/com/mrtripop/
│           ├── clinical/
│           │   ├── controllers/
│           │   ├── fixture/               # Test data fixtures
│           │   ├── repository/
│           │   └── services/impl/
│           ├── component/fileparser/      # File parser tests
│           └── util/                      # Utility tests
├── docs/                                  # Project documentation
├── features/                              # Feature specifications
├── elk/                                   # ELK stack config (Logstash, Filebeat)
├── commitlint/                            # Commitlint + Husky hooks
├── .claude/                               # Claude Code config + rules
├── .env.dev                               # Dev environment variables
├── .env.local                             # Local environment variables
├── Dockerfile                             # Multi-stage Docker build
├── docker-compose.yml                     # Full stack (Postgres, Redis, ELK, OTEL)
├── Makefile                               # Build utilities
├── mvnw / mvnw.cmd                        # Maven wrapper
├── otel-config.yaml                       # OpenTelemetry collector config
├── pom.xml                                # Maven project descriptor
├── CLAUDE.md                              # AI agent context (authoritative)
├── GEMINI.md                              # Cross-tool compatibility
├── CHANGELOG.md                           # Project changelog
└── README.md                              # Project readme
```

## Critical Folders Summary

| Folder | Purpose |
|--------|---------|
| `src/main/java/com/mrtripop/product/` | Core product domain with entity hierarchy (AuditEntity -> BaseProduct -> Product) |
| `src/main/java/com/mrtripop/clinical/` | Clinical catalog domain (molecules, brands, stores, store products) |
| `src/main/java/com/mrtripop/location/` | Location domain (addresses, warehouses) |
| `src/main/java/com/mrtripop/order/` | Order management domain |
| `src/main/java/com/mrtripop/transaction/` | Transaction/balance domain |
| `src/main/java/com/mrtripop/users/` | User management domain |
| `src/main/java/com/mrtripop/config/` | Spring configuration (Security, Redis, OpenAPI, App) |
| `src/main/java/com/mrtripop/exception/` | Global exception handling with dual ControllerAdvice |
| `src/main/java/com/mrtripop/component/fileparser/` | Strategy pattern for CSV/JSON/XML file parsing |
| `src/main/java/com/mrtripop/model/` | Shared ResponseBody wrapper and BaseQueryParams |
| `src/main/java/com/mrtripop/constant/` | Legacy global constants (per-domain constants are canonical) |

## Entry Point

- **Application entry**: Spring Boot auto-configuration via `pom.xml` parent starter
- **HTTP port**: 8080 (configurable via `server.port`)
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
