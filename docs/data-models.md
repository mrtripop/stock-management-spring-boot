# Data Models

## Overview

The application uses JPA entities with Hibernate ORM. Entity inheritance follows a hierarchy: `AuditEntity` (base) -> `BaseProduct` -> `Product`. All entities use snake_case column naming with Jackson SNAKE_CASE serialization.

## Database

- **Engine**: PostgreSQL 14.6
- **DDL Strategy**: `ddl-auto: update`
- **Dialect**: PostgreSQLDialect
- **Naming Strategy**: Snake case (`@Column(name = "generic_name")`)
- **ID Generation**: `@SequenceGenerator(allocationSize = 1)` for Long IDs, `GenerationType.UUID` for UUIDs
- **Auditing**: `@CreatedDate` / `@LastModifiedDate` via `AuditEntity` with `@EntityListeners(AuditingEntityListener.class)`

## Entity Hierarchy

```
AuditEntity (@MappedSuperclass)
├── createdAt (Long)
├── updatedAt (Long)
├── BaseProduct
│   ├── code, name, description, etc.
│   └── Product
│       ├── price, dosageForm, etc.
│       └── ProductHistory
```

## Entities by Domain

### Product Domain

#### AuditEntity (Base)

| Field | Type | Description |
|-------|------|-------------|
| createdAt | Long | Created timestamp |
| updatedAt | Long | Last modified timestamp |

#### BaseProduct

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key (sequence) |
| code | String | Product code |
| name | String | Product name |
| description | String | Product description |
| createdAt | Long | Inherited |
| updatedAt | Long | Inherited |

#### Product

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key (sequence) |
| code | String | Product code |
| name | String | Product name |
| description | String | Product description |
| price | BigDecimal | Product price |
| dosageForm | String | Dosage form |
| createdAt | Long | Inherited |
| updatedAt | Long | Inherited |

#### ProductHistory

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| product | Product | Reference to Product |
| changeType | String | Type of change |
| createdAt | Long | Inherited |
| updatedAt | Long | Inherited |

### Clinical Domain

#### Molecule

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key (UUID) |
| genericName | String | Generic name of molecule |

#### Brand

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key (UUID) |
| name | String | Brand name |
| molecule | Molecule | Reference to Molecule |

#### Store

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key (UUID) |
| name | String | Store name |
| storeType | StoreType | Enum type |
| address | String | Store address |

#### StoreProduct

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key (UUID) |
| store | Store | Reference to Store |
| product | Product | Reference to Product |

#### StoreType (Enum)

Enumeration of store types (e.g., RETAIL, WAREHOUSE, etc.)

#### AuditLedger

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key (UUID) |
| action | String | Action performed |
| entity | String | Entity type |
| entityId | String | Entity identifier |
| checksum | String | Data integrity checksum |

### Location Domain

#### Address

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| street | String | Street address |
| city | String | City |
| state | String | State/province |
| zipCode | String | Postal code |
| country | String | Country |

#### Warehouse

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| name | String | Warehouse name |
| address | Address | Reference to Address |

### Transaction Domain

#### Transaction

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| type | String | Transaction type |
| amount | BigDecimal | Transaction amount |

#### UserBalance

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| user | User | Reference to User |
| balance | BigDecimal | Current balance |

## Repositories (13 total)

| Repository | Domain | Notes |
|-----------|--------|-------|
| ProductRepository | Product | Product CRUD |
| ProductHistoryRepository | Product | Product history tracking |
| MoleculeRepository | Clinical | Molecule CRUD |
| BrandRepository | Clinical | Brand CRUD |
| StoreRepository | Clinical | Store CRUD |
| StoreProductRepository | Clinical | Store-Product junction |
| AuditLedgerRepository | Clinical | Audit trail |
| AddressRepository | Location | Address CRUD |
| WarehouseRepository | Location | Warehouse CRUD |
| OrderRepository | Order | Order CRUD |
| TransactionRepository | Transaction | Transaction CRUD |
| UserRepository | Users | User CRUD |
| UserBalanceRepository | Transaction | User balance tracking |

## Caching

- **Provider**: Redis 7.2
- **Serializer**: GenericJackson2JsonRedisSerializer
- **TTL**: Configurable via `CACHE_REDIS_TTL` env var
- **Usage**: `@Cacheable`, `@CachePut`, `@CacheEvict` on product service methods
- **Test**: Cache disabled (`type: none`) in test profile
