# API Contracts

## Overview

All endpoints return responses wrapped in `ResponseBody<T>` using the builder pattern. Error responses follow a dual `@ControllerAdvice` pattern with structured error codes.

## Base URL

- Local: `http://localhost:8080`
- Dev: `http://dev-inventory.mrtripop-int.com`
- Staging: `http://inventory.mrtripop-int.com`

## Response Format

```json
{
  "code": "string",
  "message": "string",
  "data": {}
}
```

## Error Response Format

```json
{
  "code": "string",
  "message": "string",
  "data": null
}
```

Error codes follow pattern: `{PREFIX}{HTTP_STATUS_DIGIT}{SEQUENCE}` (e.g., `PRO1001`, `GB4041`)

---

## Product Endpoints

Base path: `/api/inventory`

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/inventory/products` | List all products (paginated) | None (currently) |
| GET | `/api/inventory/products/{id}` | Get product by ID | None |
| POST | `/api/inventory/products` | Create product | None |
| PUT | `/api/inventory/products/{id}` | Update product (full) | None |
| PATCH | `/api/inventory/products/{id}` | Partial update product | None |
| DELETE | `/api/inventory/products/{id}` | Delete product | None |
| GET | `/api/inventory/product-histories` | List product history (paginated) | None |

### Product Query Parameters (pagination)

| Parameter | Type | Description |
|-----------|------|-------------|
| page | int | Page number |
| size | int | Page size |
| order_by | string | Sort field |

## Clinical Catalog Endpoints

Base path: `/api/v1/clinical/catalog`

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/clinical/catalog/molecules` | List molecules (paginated) | None |
| GET | `/api/v1/clinical/catalog/molecules/{id}` | Get molecule by ID | None |
| POST | `/api/v1/clinical/catalog/molecules` | Create molecule | None |
| PUT | `/api/v1/clinical/catalog/molecules/{id}` | Update molecule | None |
| GET | `/api/v1/clinical/catalog/brands` | List brands (paginated) | None |
| GET | `/api/v1/clinical/catalog/brands/{id}` | Get brand by ID | None |
| POST | `/api/v1/clinical/catalog/brands` | Create brand | None |
| PUT | `/api/v1/clinical/catalog/brands/{id}` | Update brand | None |
| GET | `/api/v1/clinical/catalog/stores` | List stores (paginated) | None |
| POST | `/api/v1/clinical/catalog/stores` | Create store | None |
| GET | `/api/v1/clinical/catalog/store-products` | List store products (paginated) | None |
| POST | `/api/v1/clinical/catalog/store-products` | Create store product | None |

## Location Endpoints

Base path: `/api/v1/location` (inferred pattern)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/location/addresses` | List addresses | None |
| GET | `/api/v1/location/warehouses` | List warehouses | None |

## Order Endpoints

Base path: `/api/v1/order` (inferred pattern)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/order/orders` | List orders | None |

## Transaction Endpoints

Base path: `/api/v1/transaction` (inferred pattern)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/transaction/transactions` | List transactions | None |

## User Endpoints

Base path: `/api/v1/users` (inferred pattern)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/users/users` | List users | None |

## File Parser Endpoint

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/file-parser` | Parse uploaded file (CSV/JSON/XML) | None |

---

## Authentication

Currently `permitAll()` (security disabled). Role hierarchy planned: EMPLOYEE -> MANAGER -> ADMIN.

## Validation

- POST/PUT: `@Valid @RequestBody` (full validation)
- PATCH: `@RequestBody` without `@Valid` (partial updates)
- Pagination: `@Valid BaseQueryParams`

## HTTP Status Codes

| Code | Usage |
|------|-------|
| 200 OK | Successful GET/PUT/PATCH/DELETE |
| 201 Created | Successful POST |
| 400 Bad Request | Validation failure (GB4041, GB4042, GB4043) |
| 404 Not Found | Resource not found (domain-specific codes) |
| 500 Internal Server Error | Unhandled exceptions |
