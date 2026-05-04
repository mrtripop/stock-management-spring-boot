# Component Inventory

## Shared Components

### ResponseBody (`model/ResponseBody<T>`)

Standard API response wrapper used across all endpoints. Builder pattern required.

**Pattern**: `ResponseBody.builder().code(code).message("msg").data(result).build().toResponseEntity(status)`

### BaseQueryParams (`model/BaseQueryParams`)

Pagination base class with `page`, `size`, and `orderBy` fields.

### ControllerExceptionHandler (`product/component/`)

Primary error handler with `@Order(HIGHEST_PRECEDENCE)` for `@RestController` endpoints.

### CustomControllerAdvice (`exception/`)

Catch-all error handler for unhandled exceptions.

### GlobalAspect (`aspect/GlobalAspect`)

AOP aspect for method-level logging (entry/exit at DEBUG).

### ApplicationException (`exception/`)

Base application exception with error code support.

### NotFoundException (`exception/`)

`@ResponseStatus(NOT_FOUND)` exception for missing resources.

## File Parser Component (`component/fileparser/`)

Strategy pattern implementation for parsing CSV, JSON, and XML files.

| Component | Type | Description |
|-----------|------|-------------|
| FileParserController | Controller | Upload endpoint |
| FileParserService | Service | Orchestration |
| FileParserFactory | Factory | Strategy selection |

Supports: CSV (Apache Commons CSV), JSON (Jackson), XML (Jackson XML)

## Domain Components

### Product Domain

| Component | Type | Description |
|-----------|------|-------------|
| ProductController | Controller | Product CRUD endpoints |
| ProductHistoryController | Controller | Product history endpoints |
| ProductService / ProductServiceImpl | Service | Business logic + caching |
| ProductManager | Service | Raw DB access layer |
| ProductMapper | Mapper (MapStruct) | Entity <-> DTO mapping |
| ControllerExceptionHandler | Handler | Product-domain error handling |
| ProductHistoryService | Service | History tracking |

### Clinical Domain

| Component | Type | Description |
|-----------|------|-------------|
| MasterCatalogController | Controller | Molecule + Brand endpoints |
| StoreProductController | Controller | Store + StoreProduct endpoints |
| MasterCatalogService / Impl | Service | Catalog business logic |
| StoreProductService / Impl | Service | Store-product management |
| AuditService / Impl | Service | Centralized audit recording |
| ClinicalMapper | Mapper (MapStruct) | Clinical entity <-> DTO |
| StoreProductMapper | Mapper (MapStruct) | StoreProduct mapping |

### Location Domain

| Component | Type | Description |
|-----------|------|-------------|
| AddressController | Controller | Address CRUD |
| WarehouseController | Controller | Warehouse CRUD |
| AddressService / Impl | Service | Address logic |
| WarehouseService / Impl | Service | Warehouse logic |

### Order Domain

| Component | Type | Description |
|-----------|------|-------------|
| OrderController | Controller | Order CRUD |
| OrderService | Service | Order logic |

### Transaction Domain

| Component | Type | Description |
|-----------|------|-------------|
| TransactionController | Controller | Transaction CRUD |
| TransactionService | Service | Transaction logic |
| UserBalanceService | Service | Balance management |

### Users Domain

| Component | Type | Description |
|-----------|------|-------------|
| UserController | Controller | User CRUD |
| UserService | Service | User logic |

## Configuration Components

| Component | Type | Description |
|-----------|------|-------------|
| SecurityConfig | Config | Spring Security (currently permitAll) |
| RedisConfig | Config | Redis cache serialization |
| OpenAPIConfig | Config | Swagger/OpenAPI setup |
| AppConfig | Config | Application properties binding |

## Error Code Components

Per-domain error code enums implementing `BaseStatusCode` interface:

| Domain | ErrorCode | SuccessCode |
|--------|-----------|-------------|
| Global (legacy) | `constant/ErrorCode` | `constant/SuccessCode` |
| Product | `product/constant/ErrorCode` | `product/constant/SuccessCode` |
| Location | `location/constant/ErrorCode` | `location/constant/SuccessCode` |
