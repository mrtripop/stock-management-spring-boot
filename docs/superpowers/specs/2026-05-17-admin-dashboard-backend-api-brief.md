# Admin Dashboard — Backend API Product Brief

**Date**: 2026-05-17
**Audience**: Frontend implementation agent
**Goal**: Provide a complete reference of every backend API so the frontend agent can implement the admin dashboard with full understanding of each endpoint's intent, data shape, and how they connect.

---

## Context

This is a pharmacy stock management system. The backend is a Spring Boot REST API serving JSON over `/api/v1`. All endpoints (except auth) require a JWT token in the `Authorization: Bearer <token>` header.

The frontend admin dashboard lets pharmacy staff manage products, track inventory batches, handle clinical data (molecules, brands, stores), process sales invoices, and monitor compliance.

**Base URL**: `/api/v1`

## Conventions

Every API response follows the same wrapper format. Understanding these conventions saves you from learning each endpoint individually.

### Response Wrapper

All successful responses return `ResponseBody<T>`:

```json
{
  "code": "USR2001",
  "message": "Login successful",
  "data": { ... },
  "timestamp": "2026-05-17T10:30:00"
}
```

| Field | Type | Notes |
|-------|------|-------|
| code | String | Status code from SuccessCode or ErrorCode enum |
| message | String | Human-readable status message |
| data | T | The actual payload (varies per endpoint) |
| timestamp | LocalDateTime | Server timestamp |

The `error` field appears only when there is an error. The `data` field is `null` on error responses.

### Error Responses

Errors return HTTP status codes with `ErrorResponse`:

```json
{
  "timestamp": "17-05-2026 10:30:00",
  "code": 404,
  "status": "NOT_FOUND",
  "message": "Product not found",
  "stackTrace": null
}
```

Common HTTP status codes: `200` (success), `201` (created), `400` (validation error), `401` (unauthorized), `403` (forbidden), `404` (not found), `409` (conflict), `500` (server error).

### Pagination

List endpoints accept `BaseQueryParams` as query parameters:

| Parameter | Type | Default | Notes |
|-----------|------|---------|-------|
| page | Integer | 1 | Page number, minimum 1 |
| size | Integer | 200 | Items per page, minimum 1 |
| orderBy | String | ASC | `ASC` or `DESC` |

Paginated responses return Spring Data `Page<T>`:

```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

### Business Enums

These enums appear throughout the API. Use them for badges, filters, and status indicators.

| Enum | Values | Used In |
|------|--------|---------|
| UserRole | `EMPLOYEE`, `PHARMACIST`, `MANAGER`, `ADMIN` | Users, Auth |
| StoreType | `PHYSICAL`, `HUB`, `LOGICAL` | Clinical stores |
| BatchStatus | `AVAILABLE`, `RECALLED`, `QUARANTINED` | Inventory batches |
| TaskStatus | `PENDING`, `ACKNOWLEDGED`, `RESOLVED` | Inventory action tasks |
| TaskType | `EXPIRY_WARNING`, `REORDER_NEEDED`, `RECALL_ALERT` | Inventory action tasks |
| InvoiceStatus | `PENDING`, `COMPLETED`, `VOIDED` | Transaction invoices |
| VerificationStatus | `VERIFIED`, `FAILED`, `PENDING` | Digital signatures |

---

## 1. Authentication

The auth system uses JWT with optional MFA (TOTP). The login flow has three paths depending on whether MFA is enabled.

**Base path**: `/api/v1/auth`
**Auth required**: No (all auth endpoints are public)

### 1.1 Login

```
POST /api/v1/auth/login
```

Starts the authentication flow. If the user has MFA enabled, the response returns a temporary token instead of a full access token.

**Request body** (`LoginRequest`):

| Field | Type | Validation |
|-------|------|------------|
| username | String | @NotBlank |
| password | String | @NotBlank |

**Response when MFA is not enabled** — `AuthResponse`:

```json
{
  "code": "USR2001",
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "role": "PHARMACIST",
    "username": "john",
    "storeId": "uuid-of-selected-store"
  }
}
```

**Response when MFA is enabled** — `LoginResponse`:

```json
{
  "code": "USR2007",
  "message": "MFA verification required",
  "data": {
    "mfaRequired": true,
    "tempToken": "temp-eyJhbGciOi...",
    "message": "Please verify your TOTP code"
  }
}
```

**Frontend intent**: Show login form. If response contains `mfaRequired: true`, transition to a TOTP code input screen. Otherwise, store the `accessToken` and redirect to dashboard.

### 1.2 Verify MFA

```
POST /api/v1/auth/verify-mfa
```

Completes login for MFA-enabled users by verifying the TOTP code.

**Request body** (`MfaVerifyRequest`):

| Field | Type | Validation |
|-------|------|------------|
| tempToken | String | @NotBlank |
| totpCode | String | @NotBlank, 6 characters |

**Response**: `AuthResponse` (same as successful login above)

### 1.3 Verify TOTP (alternative endpoint)

```
POST /api/v1/auth/verify-totp
```

Verifies a TOTP code using the `Authorization` header token instead of a temp token.

**Request body** (`VerifyTotpRequest`):

| Field | Type | Validation |
|-------|------|------------|
| code | String | @NotBlank, 6 characters |

**Response** (`VerifyTotpResponse`):

```json
{
  "token": "eyJhbGciOi..."
}
```

### 1.4 Register

```
POST /api/v1/auth/register
```

Creates a new user account.

**Request body** (`CreateAuthUserRequest`):

| Field | Type | Validation |
|-------|------|------------|
| username | String | @NotBlank, max 100 chars |
| password | String | @NotBlank, min 8 chars |

**Response**: `AuthResponse`

### 1.5 Select Store

```
POST /api/v1/auth/select-store
```

After login, the user selects which store they are working at. This is required because most operations are store-scoped.

**Request body** (`StoreSelectionRequest`):

| Field | Type | Validation |
|-------|------|------------|
| storeId | UUID | @NotNull |

**Response**: `AuthResponse` (with `storeId` populated)

**Frontend intent**: After login, if the user has no store selected, show a store picker before allowing access to the main dashboard.

### 1.6 Get Current User

```
GET /api/v1/auth/me
```

Returns the currently authenticated user's profile.

**Response** (`AuthUserDto`):

```json
{
  "id": "uuid",
  "username": "john",
  "role": "PHARMACIST",
  "mfaEnabled": true,
  "createdAt": 1715932800000,
  "updatedAt": 1715932800000
}
```

### 1.7 Setup MFA

```
POST /api/v1/auth/setup-mfa
```

Generates a new TOTP secret and QR code for the current user.

**Response** (`MfaSetupResponse`):

```json
{
  "secret": "JBSWY3DPEHPK3PXP",
  "qrCodeDataUri": "data:image/png;base64,..."
}
```

**Frontend intent**: Show the QR code for the user to scan with an authenticator app (Google Authenticator, Authy, etc.).

---

## 2. Users

User management for administrators.

**Base path**: `/api/v1/users`
**Auth required**: Yes

### 2.1 List Users

```
GET /api/v1/users?page=1&size=20&orderBy=ASC
```

**Response**: `ResponseBody<Page<User>>`

User entity includes `id`, `username`, `role`, `createdAt`, `updatedAt`.

### 2.2 Get User by ID

```
GET /api/v1/users/{id}
```

**Response**: `ResponseBody<User>`

### 2.3 Create User

```
POST /api/v1/users
```

**Request body**: User entity (fields: `username`, `role`, etc.)

**Response**: `ResponseBody<User>`

### 2.4 Update User General Info

```
PUT /api/v1/users/{id}/general
```

**Request body**: User entity with updated fields

**Response**: `ResponseBody<User>`

### 2.5 Delete User

```
DELETE /api/v1/users/{id}
```

**Response**: `ResponseBody<String>`

**Frontend intent**: User listing page with role badges (ADMIN=purple, MANAGER=teal, PHARMACIST=green, EMPLOYEE=gray). Admin-only CRUD. Search by username.

---

## 3. Products

Product catalog management. Products are the generic items in the system — each has a code, barcode, name, category, and physical dimensions.

**Base path**: `/api/v1/products`
**Auth required**: Yes

### 3.1 List Products

```
GET /api/v1/products?page=1&size=20&orderBy=ASC
```

**Response**: `ResponseBody<Page<ProductDTO>>`

### 3.2 Get Product by ID

```
GET /api/v1/products/{productId}
```

**Response**: `ResponseBody<ProductDTO>`

### 3.3 Create Product

```
POST /api/v1/products
```

**Request body** (`ProductDTO`):

| Field | Type | Validation |
|-------|------|------------|
| code | String | @NotBlank, @NotEmpty |
| barcode | String | @NotBlank, @NotEmpty |
| name | String | @NotBlank, @NotEmpty |
| description | String | @NotBlank, max 300 chars |
| category | String | @NotBlank, @NotEmpty |
| reorderQuantity | Integer | @NotNull, min 0 |
| packedWeight | Double | @NotNull, min 0 |
| packedHeight | Double | @NotNull, min 0 |
| packedWidth | Double | @NotNull, min 0 |
| packedDepth | Double | @NotNull, min 0 |
| isActive | Boolean | @NotNull |

**Response**: `ResponseBody<ProductDTO>`

### 3.4 Update Product

```
PUT /api/v1/products/{productId}
```

**Request body**: Same as create (`ProductDTO`)

**Response**: `ResponseBody<ProductDTO>`

### 3.5 Delete Product

```
DELETE /api/v1/products/{productId}
```

**Response**: `ResponseBody<Void>`

### 3.6 Upload Products via CSV

```
POST /api/v1/products/upload
Content-Type: multipart/form-data
```

**Request**: `MultipartFile` (CSV file)

**Response**: `ResponseBody<Void>`

### 3.7 Export Products

```
GET /api/v1/products/export?fileType=csv
```

**Response**: Binary file (`byte[]`) with appropriate Content-Disposition header.

### 3.8 Product History

```
GET /api/v1/products/histories?page=1&size=20&orderBy=ASC
GET /api/v1/products/histories/{productCode}?page=1&size=20&orderBy=ASC
```

Returns change history for all products or a specific product by code.

**Response**: `ResponseBody<Page<ProductHistory>>`

**Frontend intent**: Product listing with data table (code, name, category, status badge). FormDrawer for create/edit. CSV upload and export buttons. History view shows audit trail of product changes.

---

## 4. Clinical (Master Catalog + Stores)

The clinical module manages the pharmacy-specific data: molecules (active ingredients), brands (specific products made by manufacturers), and stores (pharmacy locations).

### Master Catalog

**Base path**: `/api/v1/clinical/catalog`

#### 4.1 Create Molecule

```
POST /api/v1/clinical/catalog/molecules
```

**Request body** (`MoleculeDto`):

| Field | Type | Validation |
|-------|------|------------|
| genericName | String | @NotBlank |
| therapeuticClass | String | Optional |
| regulatorySchedule | String | Optional (e.g., controlled substance schedule) |
| dosageInstructions | String | Optional |
| safetyWarnings | String | Optional |

**Response**: `ResponseBody<MoleculeDto>`

#### 4.2 Get Molecule

```
GET /api/v1/clinical/catalog/molecules/{id}
```

**Response**: `ResponseBody<MoleculeDto>`

#### 4.3 Update Molecule Metadata

```
PATCH /api/v1/clinical/catalog/molecules/{id}/metadata
```

**Request body**: `MoleculeDto` (partial update)

**Response**: `ResponseBody<MoleculeDto>`

#### 4.4 Search Molecules

```
GET /api/v1/clinical/catalog/molecules/search?query=amox
```

**Response**: `ResponseBody<List<MoleculeDto>>`

#### 4.5 Create Brand

```
POST /api/v1/clinical/catalog/brands
```

**Request body** (`BrandDto`):

| Field | Type | Validation |
|-------|------|------------|
| moleculeId | UUID | @NotNull |
| brandName | String | @NotBlank |
| strength | String | Optional (e.g., "500mg") |
| form | String | Optional (e.g., "tablet", "capsule", "syrup") |
| baseUnit | String | Optional (e.g., "tablet", "ml") |
| barcode | String | Optional |

**Response**: `ResponseBody<BrandDto>`

#### 4.6 Get Brands by Molecule

```
GET /api/v1/clinical/catalog/molecules/{moleculeId}/brands
```

**Response**: `ResponseBody<List<BrandDto>>`

**Frontend intent**: Molecules tab with search-as-you-type for generic names. Brands tab shows brands grouped by molecule. Creating a brand requires selecting a molecule first. The `regulatorySchedule` field indicates if a substance is controlled (affects dispensing flow — requires digital signature).

### Stores

**Base path**: `/api/v1/clinical/stores`

#### 4.7 List Stores

```
GET /api/v1/clinical/stores?page=1&size=20&orderBy=ASC
```

**Response**: `ResponseBody<Page<StoreDto>>`

`StoreDto` fields: `id` (UUID), `name`, `type` (PHYSICAL/HUB/LOGICAL), `createdAt`, `updatedAt`, `active` (boolean).

#### 4.8 Get Store

```
GET /api/v1/clinical/stores/{storeId}
```

#### 4.9 Create Store

```
POST /api/v1/clinical/stores
```

**Request body** (`CreateStoreRequest`):

| Field | Type | Validation |
|-------|------|------------|
| name | String | @NotBlank, max 255 |
| type | StoreType | @NotNull (PHYSICAL, HUB, LOGICAL) |

#### 4.10 Update Store

```
PATCH /api/v1/clinical/stores/{storeId}
```

**Request body** (`UpdateStoreRequest`):

| Field | Type | Validation |
|-------|------|------------|
| name | String | Optional, max 255 |
| type | StoreType | Optional |

#### 4.11 Delete Store

```
DELETE /api/v1/clinical/stores/{storeId}
```

### Store Products

**Base path**: `/api/v1/clinical/catalog/stores/{storeId}/products`

Each store has its own list of active products (brands that are stocked at that store). This is how you control which brands are available for dispensing at each location.

#### 4.12 Activate Product in Store

```
POST /api/v1/clinical/catalog/stores/{storeId}/products
```

**Request body** (`ActivateProductRequest`):

| Field | Type | Validation |
|-------|------|------------|
| brandId | UUID | @NotNull |

**Response**: `ResponseBody<StoreProductDto>`

#### 4.13 List Store Products

```
GET /api/v1/clinical/catalog/stores/{storeId}/products?page=1&size=20&orderBy=ASC
```

**Response**: `ResponseBody<Page<StoreProductDto>>`

`StoreProductDto` includes brand details (name, strength, form, baseUnit) plus molecule info (genericName, therapeuticClass, regulatorySchedule) alongside store-specific fields (price, shelfLocation, isActive).

#### 4.14 Get Store Product

```
GET /api/v1/clinical/catalog/stores/{storeId}/products/{productId}
```

#### 4.15 Update Store Product Override

```
PATCH /api/v1/clinical/catalog/stores/{storeId}/products/{productId}
```

**Request body** (`UpdateOverrideRequest`):

| Field | Type | Validation |
|-------|------|------------|
| price | BigDecimal | @Digits(integer=8, fraction=2) |
| shelfLocation | String | max 100 chars |

This lets stores override the default price and set a shelf location for a product.

#### 4.16 Deactivate Store Product

```
DELETE /api/v1/clinical/catalog/stores/{storeId}/products/{productId}
```

**Response**: `204 No Content`

**Frontend intent**: Stores tab with CRUD table showing name and type badge. Clicking a store drills into its product catalog — showing which brands are active, their prices, and shelf locations. Store products page is also where you control what appears in the POS dispensing flow for that store.

---

## 5. Inventory

Inventory management handles batch tracking, stock-in (receiving), stock deduction (dispensing/sales), barcode resolution, action tasks, unit conversions, batch recalls, and mesh warehouse searches.

**Base path**: `/api/v1/inventory`
**Auth required**: Yes

### Batch Operations

#### 5.1 Stock-In (Create Batch from Barcode)

```
POST /api/v1/inventory/batches/stock-in
```

Records new stock arriving at a store. Creates or updates a batch and updates the store's stock level.

**Request body** (`StockEntryRequest`):

| Field | Type | Validation |
|-------|------|------------|
| barcode | String | @NotBlank |
| batchNumber | String | @NotBlank, max 100 |
| expiryDate | LocalDate | @NotNull, must be in the future |
| quantity | Long | @NotNull, min 1 |
| storeId | UUID | @NotNull |
| supplierReference | String | Optional |
| manufacturerLotNumber | String | Optional |
| storageConditions | String | Optional |

**Response** (`StockEntryResponseDto`):

```json
{
  "batch": {
    "id": 1,
    "brandId": "uuid",
    "batchNumber": "BN-2026-001",
    "expiryDate": "2027-06-01",
    "quantity": 100,
    "supplierReference": "SUP-001",
    "manufacturerLotNumber": "ML-001",
    "storageConditions": "Room temperature",
    "status": "AVAILABLE",
    "createdAt": 1715932800000,
    "updatedAt": 1715932800000
  },
  "storeStock": {
    "id": 1,
    "storeId": "uuid",
    "batchId": 1,
    "quantity": 100,
    "location": "Shelf A-1",
    "createdAt": 1715932800000,
    "updatedAt": 1715932800000
  }
}
```

**Frontend intent**: The stock-in form is the primary way pharmacy staff records incoming inventory. Staff scans or enters a barcode, enters the batch number, expiry date, and quantity. The barcode is resolved to a brand automatically.

#### 5.2 Deduct Stock

```
POST /api/v1/inventory/stock/deduct
```

Deducts stock from inventory. Used when dispensing or adjusting stock. Follows FEFO (First Expiry, First Out) — automatically picks the earliest-expiring batch.

**Request body** (`StockDeductionRequest`):

| Field | Type | Validation |
|-------|------|------------|
| barcode | String | @NotBlank |
| storeId | UUID | @NotNull |
| quantity | Long | @NotNull, min 1 |
| unit | String | Optional (uses baseUnit if omitted) |
| signature | DigitalSignatureRequest | Conditional (required for controlled substances) |

`DigitalSignatureRequest`:

| Field | Type |
|-------|------|
| licenseNumber | String |
| signaturePayload | String |

**Response** (`StockDeductionResponseDto`):

```json
{
  "barcode": "8901234567890",
  "brandId": "uuid",
  "brandName": "Amoxicillin 500mg",
  "requestedUnit": "tablet",
  "requestedQuantity": 10,
  "baseUnit": "tablet",
  "deductedQuantity": 10,
  "unitPrice": 2.50,
  "totalAmount": 25.00,
  "items": [
    {
      "batchId": 1,
      "batchNumber": "BN-2026-001",
      "expiryDate": "2027-06-01",
      "deductedQuantity": 10,
      "remainingQuantity": 90,
      "baseUnit": "tablet"
    }
  ],
  "signatureVerification": {
    "licenseNumber": "RPH-12345",
    "verifiedAt": 1715932800000,
    "verificationStatus": "VERIFIED"
  }
}
```

#### 5.3 Get Batch by ID

```
GET /api/v1/inventory/batches/{id}
```

**Response**: `ResponseBody<BatchDto>`

#### 5.4 List Batches

```
GET /api/v1/inventory/batches?brandId={brandId}&page=1&size=20&orderBy=ASC
```

The `brandId` query parameter is optional. When provided, filters batches for that specific brand.

**Response**: `ResponseBody<Page<BatchDto>>`

#### 5.5 Get Store Stock

```
GET /api/v1/inventory/stores/{storeId}/stock?page=1&size=20&orderBy=DESC
```

Returns stock levels per batch for a specific store. This shows what is currently available at that location.

**Response**: `ResponseBody<Page<StoreStockDto>>`

#### 5.6 Resolve Barcode

```
GET /api/v1/inventory/barcode/resolve?barcode=8901234567890
```

Looks up a barcode against the master catalog. Returns the brand information associated with that barcode.

**Response**: Brand information matching the barcode.

**Frontend intent**: The barcode resolver is the entry point for both stock-in and dispensing workflows. When staff scans a barcode, the frontend calls this endpoint to identify the product before proceeding.

### Action Tasks (Expiry/Reorder Alerts)

Action tasks are system-generated alerts for expiry warnings, reorder needs, and recall notifications. Staff must acknowledge and resolve these tasks.

#### 5.7 List Tasks

```
GET /api/v1/inventory/tasks?storeId={storeId}&status={status}&page=0&size=20
```

Query parameters: `storeId` (UUID), `status` (TaskStatus), plus standard pagination.

**Response**: `ResponseBody<Page<TaskDto>>`

`TaskDto` fields: `id`, `storeId`, `storeName`, `taskType` (EXPIRY_WARNING/REORDER_NEEDED/RECALL_ALERT), `status` (PENDING/ACKNOWLEDGED/RESOLVED), `batchId`, `batchNumber`, `brandId`, `brandName`, `message`, `currentQuantity`, `thresholdQuantity`, `daysUntilExpiry`, `createdAt`, `updatedAt`.

#### 5.8 Get Task

```
GET /api/v1/inventory/tasks/{id}
```

#### 5.9 Acknowledge Task

```
PATCH /api/v1/inventory/tasks/{id}/acknowledge
```

Moves task from PENDING to ACKNOWLEDGED. Staff confirms they have seen the alert.

#### 5.10 Resolve Task

```
PATCH /api/v1/inventory/tasks/{id}/resolve
```

Moves task to RESOLVED. Staff confirms they have taken action on the alert.

#### 5.11 Trigger Full Scan

```
POST /api/v1/inventory/tasks/scan
```

Triggers a full inventory scan that generates new expiry warnings and reorder alerts.

**Response**: `ResponseBody<ActionQueueScanResult>` with counts of warnings/alerts created and updated.

**Frontend intent**: Tasks page shows a priority-sorted list of alerts. Expiry warnings with `daysUntilExpiry < 7` are red, `< 30` are amber. Reorder alerts show current vs threshold quantity. Recall alerts are critical (red badge). Staff can acknowledge and resolve each task. A "Run Scan" button triggers the full scan.

### Batch Recall (Compliance)

#### 5.12 Recall Batch

```
POST /api/v1/inventory/compliance/recall
```

Initiates a batch recall. The batch status changes to RECALLED and all affected store stock entries are quarantined.

**Request body** (`RecallBatchRequest`):

| Field | Type | Validation |
|-------|------|------------|
| batchId | Long | @NotNull |

**Response** (`RecallBatchResponse`):

```json
{
  "batchId": 1,
  "batchNumber": "BN-2026-001",
  "brandName": "Amoxicillin 500mg",
  "affectedStores": 3,
  "recallStatus": "RECALLED"
}
```

### Unit Conversions

Unit conversions define how to convert between different measurement units for the same brand (e.g., 1 box = 10 tablets).

**Base path**: `/api/v1/inventory/conversions`

#### 5.13 Create Unit Conversion

```
POST /api/v1/inventory/conversions
```

**Request body** (`CreateUnitConversionRequest`):

| Field | Type | Validation |
|-------|------|------------|
| brandId | UUID | @NotNull |
| fromUnit | String | @NotBlank, max 50 |
| toUnit | String | @NotBlank, max 50 |
| ratio | Integer | @NotNull, min 2 |

#### 5.14 Get Conversions by Brand

```
GET /api/v1/inventory/conversions?brandId={brandId}
```

#### 5.15 Delete Unit Conversion

```
DELETE /api/v1/inventory/conversions/{id}
```

### Mesh Warehouse Search

Search across the mesh network to find stock at other stores/warehouses.

**Base path**: `/api/v1/mesh/stock`

#### 5.16 Search Mesh Stock

```
GET /api/v1/mesh/stock/search?moleculeId={uuid}&genericName={name}&requestingStoreId={uuid}
```

At least one of `moleculeId` or `genericName` is required. Returns both local store stock and mesh network stock.

**Response** (`MeshStockResponseDto`):

```json
{
  "localStoreStocks": [
    {
      "storeId": "uuid",
      "storeName": "Main Pharmacy",
      "brandId": "uuid",
      "brandName": "Amoxicillin 500mg",
      "genericName": "Amoxicillin",
      "totalQuantity": 200,
      "batchCount": 3
    }
  ],
  "meshStoreStocks": [
    {
      "storeId": "uuid",
      "storeName": "Branch Pharmacy",
      "brandId": "uuid",
      "brandName": "Amoxicillin 250mg",
      "genericName": "Amoxicillin",
      "totalQuantity": 150,
      "batchCount": 2
    }
  ],
  "totalMeshQuantity": 350
}
```

**Frontend intent**: When a product is out of stock locally, staff can search the mesh to find it at other stores. Show results split into "Your Store" and "Other Stores" sections.

---

## 6. Transactions (Invoices + Receipts + Reports)

The transaction module handles the point-of-sale workflow: creating invoices, completing them, voiding them, generating receipts, and producing reconciliation reports.

**Base path**: `/api/v1/transaction`
**Auth required**: Yes

### Invoices

**Base path**: `/api/v1/transaction/invoices`

#### 6.1 List Invoices

```
GET /api/v1/transaction/invoices?storeId={storeId}&page=1&size=20&orderBy=DESC
```

Query parameters: `storeId` (UUID, optional) plus standard pagination.

**Response**: `ResponseBody<Page<InvoiceDto>>`

`InvoiceDto`:

| Field | Type | Notes |
|-------|------|-------|
| id | Long | Invoice ID |
| storeId | UUID | Store reference |
| storeName | String | Store display name |
| status | InvoiceStatus | PENDING, COMPLETED, VOIDED |
| totalAmount | BigDecimal | Sum of all line totals |
| patientOwed | BigDecimal | Amount patient pays after insurance |
| insuranceClaimAmount | BigDecimal | Amount covered by insurance |
| items | List\<InvoiceItemDto\> | Line items |
| createdAt | Long | Epoch millis |
| updatedAt | Long | Epoch millis |

`InvoiceItemDto`:

| Field | Type | Notes |
|-------|------|-------|
| id | Long | Item ID |
| brandName | String | Brand display name |
| batchNumber | String | Batch identifier |
| quantity | Long | Quantity dispensed |
| unitPrice | BigDecimal | Price per unit |
| lineTotal | BigDecimal | unitPrice x quantity |
| patientOwed | BigDecimal | Line-level patient amount |
| insuranceClaimAmount | BigDecimal | Line-level insurance amount |
| insuranceCoveragePercent | Integer | 0-100 |

#### 6.2 Get Invoice

```
GET /api/v1/transaction/invoices/{invoiceId}
```

**Response**: `ResponseBody<InvoiceDto>` with full `items` array.

#### 6.3 Create Invoice

```
POST /api/v1/transaction/invoices
```

Creates an invoice in PENDING status. Stock is not deducted until the invoice is completed.

**Request body** (`CreateInvoiceRequest`):

| Field | Type | Validation |
|-------|------|------------|
| storeId | UUID | @NotNull |
| items | List\<InvoiceItemRequest\> | @NotEmpty |

`InvoiceItemRequest`:

| Field | Type | Validation |
|-------|------|------------|
| brandId | UUID | @NotNull |
| batchId | Long | @NotNull |
| quantity | Long | @NotNull, min 1 |
| insuranceCoveragePercent | Integer | 0-100, default 0 |

#### 6.4 Complete Invoice

```
POST /api/v1/transaction/invoices/{invoiceId}/complete
```

Transitions invoice from PENDING to COMPLETED. Triggers inventory deduction (FEFO) for all items.

#### 6.5 Void Invoice

```
POST /api/v1/transaction/invoices/{invoiceId}/void
```

Voids the invoice. For COMPLETED invoices, stock is restored to inventory. PENDING invoices are simply cancelled.

#### 6.6 Quick Dispense

```
POST /api/v1/transaction/invoices/dispense
```

All-in-one operation: creates the invoice, completes it, and deducts inventory in a single call. This is the primary endpoint for POS dispensing.

**Request body**: Same as `CreateInvoiceRequest` (storeId + items array)

**Response**: `ResponseBody<InvoiceDto>` with `status: "COMPLETED"`

#### 6.7 Daily Sales Summary

```
GET /api/v1/transaction/invoices/daily-summary?storeId={storeId}&date=2026-05-17
```

**Response** (`DailySalesSummaryDto`):

```json
{
  "date": "2026-05-17",
  "totalInvoices": 12,
  "totalRevenue": 1500.00,
  "totalPatientPaid": 1050.00,
  "totalInsuranceClaims": 450.00,
  "totalItemsDispensed": 47,
  "voidedCount": 1
}
```

**Frontend intent**: The daily summary card appears on the dashboard. Show stat cards for revenue, patient paid, insurance claims, items dispensed, and voided count. Include a date picker (defaults to today).

### Receipts

#### 6.8 Generate Receipt

```
GET /api/v1/transaction/invoices/{invoiceId}/receipt
```

**Response** (`ReceiptDto`):

```json
{
  "invoiceId": 1,
  "storeName": "Main Pharmacy",
  "status": "COMPLETED",
  "totalAmount": 150.00,
  "patientOwed": 105.00,
  "insuranceClaimAmount": 45.00,
  "items": [
    {
      "brandName": "Amoxicillin 500mg",
      "batchNumber": "BN-2026-001",
      "quantity": 10,
      "unitPrice": 2.50,
      "lineTotal": 25.00,
      "patientOwed": 25.00,
      "insuranceClaimAmount": 0.00,
      "dosageInstructions": "Take 1 tablet 3 times daily",
      "safetyWarnings": "Complete the full course",
      "digitalLeafletUrl": "https://..."
    }
  ],
  "generatedAt": "2026-05-17T10:30:00"
}
```

**Frontend intent**: Receipt view is a print-friendly format. Each item includes dosage instructions and safety warnings from the molecule master data. Show a "Print" button.

### Reconciliation Reports

**Base path**: `/api/v1/transaction/reports`

#### 6.9 Generate Reconciliation Report

```
POST /api/v1/transaction/reports/reconciliation
```

Compares the audit ledger against invoice totals to detect discrepancies for a given period.

**Request body** (`ReconciliationRequest`):

| Field | Type | Validation |
|-------|------|------------|
| storeId | UUID | @NotNull |
| reportDate | LocalDate | @NotNull |
| periodStart | Long | @NotNull (epoch millis) |
| periodEnd | Long | @NotNull (epoch millis), must be >= periodStart |

**Response** (`ReconciliationReportDto`):

```json
{
  "storeId": "uuid",
  "storeName": "Main Pharmacy",
  "reportDate": "2026-05-17",
  "periodStart": 1715932800000,
  "periodEnd": 1716019200000,
  "ledgerTotal": 5000.00,
  "invoiceTotal": 4995.00,
  "discrepancy": 5.00,
  "discrepancyCount": 2,
  "entries": [
    {
      "actionType": "DEDUCTION",
      "entityName": "Amoxicillin 500mg",
      "entityId": "uuid",
      "oldValue": "100",
      "newValue": "90",
      "timestamp": "2026-05-17T10:30:00",
      "orphaned": false
    }
  ]
}
```

**Frontend intent**: Reconciliation page lets managers select a date range and store, then generates a report showing ledger vs invoice totals. Discrepancies are highlighted. The entries list shows the audit trail with orphaned entries flagged.

### Legacy Transaction Endpoints

**Base path**: `/api/v1/transactions`

These are the original transaction endpoints (pre-invoice system):

#### 6.10 List Transactions

```
GET /api/v1/transactions?page=1&size=20&orderBy=ASC
```

#### 6.11 Get Transactions by User

```
GET /api/v1/transactions/{userId}/users
```

#### 6.12 Get Transaction by ID

```
GET /api/v1/transactions/{transactionId}
```

#### 6.13 Update Transaction

```
PUT /api/v1/transactions/{transactionId}
```

---

## 7. Locations (Addresses + Warehouses)

Physical location management for the supply chain.

### Addresses

**Base path**: `/api/v1/addresses`

#### 7.1 List Addresses

```
GET /api/v1/addresses?page=1&size=20&orderBy=ASC
```

**Response**: Paginated list of `AddressDTO`

`AddressDTO` fields: `id`, `addressName`, `line1`, `line2`, `city`, `province`, `country`, `postalCode`, `warehouseList`.

#### 7.2 Get Address

```
GET /api/v1/addresses/{addressId}
```

#### 7.3 Create Address

```
POST /api/v1/addresses
```

**Request body** (`AddressDTO`): All fields except `id` are required (`@NotNull`).

#### 7.4 Update Address

```
PUT /api/v1/addresses/{addressId}
```

#### 7.5 Delete Address

```
DELETE /api/v1/addresses/{addressId}
```

### Warehouses

**Base path**: `/api/v1/warehouses`

#### 7.6 List Warehouses

```
GET /api/v1/warehouses?page=1&size=20&orderBy=ASC
```

`WarehouseDTO` fields: `warehouseId`, `warehouseName`, `isRefrigerated`, `addressId`.

#### 7.7 Get Warehouse

```
GET /api/v1/warehouses/{warehouseId}
```

#### 7.8 Create Warehouse

```
POST /api/v1/warehouses
```

**Request body** (`WarehouseDTO`)

**Frontend intent**: Locations page shows addresses with their associated warehouses. Warehouses marked as refrigerated should have a badge. CRUD for addresses, read-only or CRUD for warehouses.

---

## 8. Orders

Order management for procurement.

**Base path**: `/api/v1/orders`

### 8.1 List Orders for User

```
GET /api/v1/orders/users/{userId}?page=1&size=20&orderBy=ASC
```

### 8.2 Get Order

```
GET /api/v1/orders/users/{userId}/{orderId}
```

### 8.3 Create Order

```
POST /api/v1/orders/users/{userId}?addressId={addressId}
```

### 8.4 Update Order

```
PUT /api/v1/orders/users/{userId}/{orderId}
```

### 8.5 Delete All User Orders

```
DELETE /api/v1/orders/users/{userId}
```

### 8.6 Delete Order

```
DELETE /api/v1/orders/users/{userId}/{orderId}
```

**Frontend intent**: Orders page is a read-only listing showing order history. Status badges for order state. Row expansion shows order items. Admin can view all users' orders.

---

## 9. Security Notes

Understanding auth requirements helps the frontend agent implement the login flow and token management correctly.

- **JWT-based auth**: All endpoints except `/api/v1/auth/login`, `/api/v1/auth/verify-mfa`, `/api/v1/auth/verify-totp`, and `/api/v1/auth/register` require a valid JWT token.
- **Token storage**: Store the JWT in localStorage or httpOnly cookie (frontend choice).
- **Token expiry**: Access tokens expire after 24 hours (86400000ms). The `expiresIn` field in `AuthResponse` tells you the exact duration.
- **Temp tokens**: MFA flow uses a temporary 5-minute token (`tempTokenExpiration: 300000`) for the MFA verification step.
- **Method-level security**: Some endpoints may use `@PreAuthorize` for role-based access control (e.g., ADMIN-only user management). If you get a 403, the user doesn't have the required role.
- **Swagger UI**: Available at `/swagger-ui/**` and `/v3/api-docs/**` for interactive API exploration during development. No auth required for these.

---

## 10. Dashboard Page Data Sources

Here is how each widget on the admin dashboard maps to API calls.

| Dashboard Widget | API Call(s) | Notes |
|-----------------|-------------|-------|
| Product count stat card | `GET /api/v1/products?page=1&size=1` | Use `totalElements` from the page response |
| Expiring batches alert list | `GET /api/v1/inventory/tasks?status=PENDING` | Filter for `taskType=EXPIRY_WARNING`, sort by `daysUntilExpiry` |
| Active batches stat card | `GET /api/v1/inventory/batches?orderBy=ASC` | Use `totalElements` |
| Low stock alert | `GET /api/v1/inventory/tasks?status=PENDING` | Filter for `taskType=REORDER_NEEDED` |
| Recent activity | `GET /api/v1/transactions?orderBy=DESC&size=10` | Latest 10 transactions |
| Daily sales summary | `GET /api/v1/transaction/invoices/daily-summary?storeId={storeId}` | Today's numbers |
| Quick action: Stock In | Navigate to Inventory page | Opens stock-in form |
| Quick action: Dispense | Navigate to Dispensing page | Opens POS screen |
| Quick action: Search Product | Navigate to Products page | Focuses search bar |
| Quick action: Run Report | Navigate to Reports page | Opens reconciliation form |

---

## 11. Page-to-API Mapping

Quick reference for which APIs each dashboard page uses.

| Page | Primary APIs | Secondary APIs |
|------|-------------|----------------|
| Login | `POST /auth/login`, `POST /auth/verify-mfa`, `POST /auth/register`, `POST /auth/verify-totp` | `POST /auth/setup-mfa` |
| Dashboard | `GET /products`, `GET /inventory/tasks`, `GET /inventory/batches`, `GET /transaction/invoices/daily-summary` | `GET /transactions` |
| Products | `GET/POST/PUT/DELETE /products`, `POST /products/upload`, `GET /products/export` | `GET /products/histories` |
| Inventory | `GET /inventory/batches`, `POST /inventory/batches/stock-in`, `POST /inventory/stock/deduct`, `GET /inventory/tasks` | `GET /inventory/barcode/resolve`, `GET /inventory/stores/{id}/stock` |
| Clinical | `GET/POST/PATCH /clinical/catalog/molecules`, `GET/POST /clinical/catalog/brands`, `GET/POST/PATCH/DELETE /clinical/stores` | Store products CRUD |
| Dispensing | `POST /transaction/invoices/dispense`, `GET /inventory/batches`, `GET /inventory/stores/{id}/stock` | `GET /clinical/catalog/molecules/search`, `GET /clinical/catalog/molecules/{id}/brands` |
| Transactions | `GET /transaction/invoices`, `GET /transaction/invoices/{id}`, `POST .../complete`, `POST .../void` | `GET /transaction/invoices/{id}/receipt`, `GET /transaction/invoices/daily-summary` |
| Orders | `GET /orders/users/{userId}`, `GET /orders/users/{userId}/{orderId}` | — |
| Locations | `GET/POST/PUT/DELETE /addresses`, `GET/POST /warehouses` | — |
| Users | `GET/POST/PUT/DELETE /users` | `GET /auth/me`, `POST /auth/register` |

---

## 12. Cross-Cutting Workflows

These multi-step workflows span multiple APIs. Understanding them helps the frontend agent build the right user flows.

### Stock-In Workflow

1. Staff scans barcode → `GET /inventory/barcode/resolve?barcode=...`
2. Frontend shows brand info, staff fills batch details
3. Staff submits → `POST /inventory/batches/stock-in` with barcode, batch number, expiry, quantity, store
4. Success: Show new batch and stock info. System may auto-generate action tasks if expiry is soon.

### POS Dispensing Workflow

1. Staff searches for product → `GET /clinical/catalog/molecules/search?query=amox`
2. Selects molecule → `GET /clinical/catalog/molecules/{id}/brands` to see brands
3. Selects brand → `GET /inventory/batches?brandId={id}` to see available batches
4. Staff adds items to invoice list with quantity and insurance %
5. If controlled substance (`regulatorySchedule` is set), require digital signature
6. Staff clicks "Dispense" → `POST /transaction/invoices/dispense`
7. Success: Show confirmation with receipt link → `GET /transaction/invoices/{id}/receipt`

### Batch Recall Workflow

1. Staff receives recall notice → Task appears with `taskType=RECALL_ALERT`
2. Staff reviews affected batch details
3. Staff initiates recall → `POST /inventory/compliance/recall` with `batchId`
4. System quarantines all stock for that batch across stores
5. Confirmation shows affected store count

### Reconciliation Workflow

1. Manager selects store and date range
2. Frontend calls `POST /transaction/reports/reconciliation` with storeId, reportDate, period
3. Report shows ledger total vs invoice total
4. Discrepancies highlighted in entries list
5. Orphaned entries (no matching invoice) flagged for investigation

---

## 13. Implementation Checklist

Before starting, verify these prerequisites:

- [ ] API client configured with JWT token management (`Authorization: Bearer <token>`)
- [ ] snake_case to camelCase conversion working for all API calls
- [ ] Error handling displays `message` from error responses
- [ ] Pagination controls wired to `page`, `size`, `orderBy` query params
- [ ] Store ID available in app context (from login or store selection)
- [ ] All enum values mapped to frontend display (badges, labels, colors)
