---
paths:
  - "src/main/java/**/controllers/*.java"
  - "src/main/java/**/models/dto/*.java"
---

# API Design Standards

Rules for designing REST endpoints and request/response DTOs. Each rule has a correct and incorrect example.

## URL Structure

- Use plural nouns for collections. Use kebab-case for multi-word segments.

```java
// Wrong
@GetMapping("/api/v1/Molecule")
@GetMapping("/api/v1/molecule")
@GetMapping("/api/v1/storeProducts")
@GetMapping("/getProductById")
@PostMapping("/api/v1/inventory/create-batch")

// Right
@GetMapping("/api/v1/molecules")
@GetMapping("/api/v1/store-products")
@GetMapping("/api/v1/products/{id}")
@PostMapping("/api/v1/inventory/batches")
```

- Use nested resources to express ownership. Nest at most 2 levels deep.

```java
// Wrong — flat URL loses relationship context
@GetMapping("/api/v1/brands")
List<BrandDto> getBrandsByMolecule(@RequestParam UUID moleculeId) { ... }

// Wrong — too deeply nested
@GetMapping("/api/v1/stores/{storeId}/products/{productId}/batches/{batchId}")

// Right
@GetMapping("/api/v1/clinical/catalog/molecules/{moleculeId}/brands")
@GetMapping("/api/v1/clinical/catalog/stores/{storeId}/products/{productId}")
@GetMapping("/api/v1/inventory/stores/{storeId}/stock")
```

- All endpoints use `/api/v1/{domain}` prefix. No exceptions.

```java
// Wrong
@RequestMapping("/api/inventory/products")
@RequestMapping("/api/users")

// Right
@RequestMapping("/api/v1/products")
@RequestMapping("/api/v1/users")
@RequestMapping("/api/v1/clinical/catalog")
@RequestMapping("/api/v1/inventory")
```

## HTTP Methods

- Use the correct HTTP method for each operation. Never use GET for state-changing operations.

```java
// Wrong
@GetMapping("/delete/{id}")
@PostMapping("/products/{id}/update")

// Right
@DeleteMapping("/products/{id}")
@PutMapping("/products/{id}")
@PatchMapping("/products/{id}/metadata")
```

- Method mapping by operation:

| Operation | Method | Status |
|-----------|--------|--------|
| List resources | `GET /resources` | 200 |
| Get single resource | `GET /resources/{id}` | 200 |
| Create resource | `POST /resources` | 201 |
| Full update | `PUT /resources/{id}` | 200 |
| Partial update | `PATCH /resources/{id}` | 200 |
| Delete resource | `DELETE /resources/{id}` | 200 |

- Use POST for action-oriented endpoints (non-CRUD operations).

```java
// Right — domain-specific actions
@PostMapping("/batches/stock-in")
@PostMapping("/stock/deduct")
@PostMapping("/barcode/resolve")
```

## Request DTOs

- Create separate request DTOs for creation. Do not reuse entity or response DTOs as request bodies.

```java
// Wrong — using entity as request body
@PostMapping("/products")
public ResponseEntity<Object> create(@RequestBody Product product) { ... }

// Wrong — using response DTO for creation (has id, createdAt, updatedAt)
@PostMapping("/products")
public ResponseEntity<Object> create(@RequestBody ProductDto dto) { ... }

// Right — dedicated request DTO
@PostMapping("/products")
public ResponseEntity<Object> create(@Valid @RequestBody CreateProductRequest request) { ... }
```

- Name request DTOs with `Request` suffix. Name response DTOs with `Dto` suffix. Complex responses use `ResponseDto` suffix.

```java
// Request DTOs (used as @RequestBody, have validation annotations)
StockEntryRequest
CreateUnitConversionRequest
ActivateProductRequest
UpdateOverrideRequest

// Response DTOs (returned in ResponseBody.data, no validation annotations)
BatchDto
UnitConversionDto
StoreProductDto

// Complex response DTOs (aggregate multiple pieces of data)
StockEntryResponseDto        // contains BatchDto + StoreStockDto
StockDeductionResponseDto    // contains List<DeductedBatchDto>
```

- Auto-generated fields in request DTOs must be `@Null`.

```java
// Right
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateBatchRequest {
  @Null(message = "Batch ID should be null")
  private Long id;

  @NotBlank(message = "Batch number is required")
  @Size(max = 100)
  private String batchNumber;
}
```

- Optional fields in request DTOs: omit `@NotNull`/`@NotBlank`. Document behavior in the field or endpoint description.

```java
// Right — supplierReference is optional
@NotBlank @Size(max = 100) String batchNumber;     // required
String supplierReference;                           // optional
```

- PATCH request DTOs must allow all fields to be null (partial update). Do not use `@Valid` on PATCH endpoints.

```java
// Right
@PatchMapping("/products/{id}")
public ResponseEntity<Object> updatePartial(
    @PathVariable Long id,
    @RequestBody UpdateProductRequest request) { ... }  // no @Valid

@Data
public class UpdateOverrideRequest {
  @Digits(integer = 8, fraction = 2)
  private BigDecimal price;           // null = no change

  @Size(max = 100)
  private String shelfLocation;       // null = no change
}
```

## Response Wrapping

- **Always** wrap responses in `ResponseBody.builder()`. Code and message from `SuccessCode` enum. Never return raw objects or entities directly.

```java
// Wrong — returning entity directly
@GetMapping("/orders/{id}")
public ResponseEntity<Order> getOrder(@PathVariable Long id) { ... }

// Wrong — hardcoded strings
return ResponseBody.builder()
    .code("SUCCESS")
    .message("Product retrieved")
    .data(result)
    .build().toResponseEntity(HttpStatus.OK);

// Right — code and message from enum
@GetMapping("/products/{id}")
public ResponseEntity<Object> getProduct(@PathVariable Long id) {
  ProductDto result = productService.findById(id);
  BaseStatusCode success = SuccessCode.PRO2002_GET_PRODUCTS_BY_ID_IS_SUCCESS;
  return ResponseBody.builder()
      .code(success.getCode())
      .message(success.getMessage())
      .data(result)
      .build()
      .toResponseEntity(HttpStatus.OK);
}
```

- Return type on controller methods should be `ResponseEntity<Object>`. Do not parameterize `ResponseBody` in the return type.

```java
// Wrong
public ResponseEntity<ResponseBody<ProductDto>> getProduct(...) { ... }

// Right
public ResponseEntity<Object> getProduct(...) { ... }
```

- DELETE responses: wrap in `ResponseBody` with success code and `null` data. Do not return `ResponseEntity<Void>`.

```java
// Wrong
return ResponseEntity.noContent().build();

// Right
BaseStatusCode success = SuccessCode.PRO2005_DELETE_PRODUCT_IS_SUCCESS;
return ResponseBody.builder()
    .code(success.getCode())
    .message(success.getMessage())
    .build()
    .toResponseEntity(HttpStatus.OK);
```

## Validation on Endpoints

- Use `@Valid @RequestBody` on POST and PUT. Omit `@Valid` on PATCH for partial updates.

```java
// Wrong
@PostMapping("/products")
public ResponseEntity<Object> create(@RequestBody ProductDto dto) { ... }

@PatchMapping("/products/{id}")
public ResponseEntity<Object> update(@Valid @RequestBody ProductDto dto) { ... }

// Right
@PostMapping("/products")
public ResponseEntity<Object> create(@Valid @RequestBody CreateProductRequest request) { ... }

@PatchMapping("/products/{id}")
public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody UpdateProductRequest request) { ... }
```

- Add `@Validated` on the controller class when using validation annotations on `@PathVariable` or `@RequestParam`.

```java
// Wrong — @Min on path variable won't trigger without @Validated
@RestController
@RequestMapping("/api/v1/inventory")
public class BatchController {
  @GetMapping("/batches/{id}")
  public ResponseEntity<Object> getBatch(@PathVariable @Min(1) Long id) { ... }
}

// Right
@RestController
@RequestMapping("/api/v1/inventory")
@Validated
public class BatchController {
  @GetMapping("/batches/{id}")
  public ResponseEntity<Object> getBatch(@PathVariable @Min(1) Long id) { ... }
}
```

- Put validation annotations on DTOs, not on entities. Validate at the boundary (controller), trust internal code.

## Pagination

- All list endpoints must accept `BaseQueryParams` for pagination. Never return unbounded lists.

```java
// Wrong — no pagination
@GetMapping("/products")
public ResponseEntity<Object> getAll() { ... }

// Right
@GetMapping("/products")
public ResponseEntity<Object> getProducts(@Valid BaseQueryParams params) {
  Page<ProductDto> page = productService.findAll(params.toPageable());
  BaseStatusCode success = SuccessCode.PRO2001_GET_ALL_PRODUCTS_IS_SUCCESS;
  return ResponseBody.builder()
      .code(success.getCode())
      .message(success.getMessage())
      .data(page)
      .build()
      .toResponseEntity(HttpStatus.OK);
}
```

- Return `Page<Dto>` in the response data (not `List<Dto>`) so clients get total count and page info.

- Filter parameters should be `@RequestParam(required = false)` alongside `BaseQueryParams`.

```java
// Right — optional filter with pagination
@GetMapping("/batches")
public ResponseEntity<Object> getBatches(
    @RequestParam(required = false) UUID brandId,
    @Valid BaseQueryParams params) { ... }
```

## Path & Query Parameters

- Use `@PathVariable` for resource identifiers. Name the variable to match the path segment.

```java
// Wrong — generic name
@GetMapping("/products/{productId}")
public ResponseEntity<Object> getById(@PathVariable Long id) { ... }

// Right
@GetMapping("/products/{productId}")
public ResponseEntity<Object> getById(@PathVariable Long productId) { ... }
```

- Validate path variable IDs with `@Min(1)` for Long IDs. Requires `@Validated` on the controller class.

- Use `@RequestParam` for filters and optional parameters. Always specify `required` explicitly.

```java
// Wrong — unclear if required
@GetMapping("/batches")
public ResponseEntity<Object> getBatches(@RequestParam UUID brandId) { ... }

// Right — explicit required/optional
@GetMapping("/batches")
public ResponseEntity<Object> getBatches(
    @RequestParam(required = false) UUID brandId,
    @RequestParam(defaultValue = "csv") String format) { ... }
```

## Controller Class Structure

- Annotations: `@Slf4j @RestController @RequiredArgsConstructor`. Add `@Validated` when validating path/query params.
- Controllers must only delegate to services. No business logic in controllers.
- Never use `@Transactional` on controller methods. Transactions belong in the service layer.

## Error Responses

- Throw `ApplicationException` with domain error code and HTTP status. Do not construct error responses manually.

```java
// Wrong
return ResponseBody.builder()
    .code("ERROR")
    .message("Batch not found")
    .build()
    .toResponseEntity(HttpStatus.NOT_FOUND);

// Right — throw, let ControllerExceptionHandler handle it
throw new ApplicationException(ErrorCode.BATCH_NOT_FOUND, HttpStatus.NOT_FOUND);
```

- Error code must reference the domain enum, never inline strings.

```java
// Wrong
throw new ApplicationException("INV4001", "Batch not found", HttpStatus.NOT_FOUND);

// Right
throw new ApplicationException(ErrorCode.BATCH_NOT_FOUND, HttpStatus.NOT_FOUND);
```

## File Operations

- File upload: use `@RequestParam("file") MultipartFile`.
- File export: return `ResponseEntity<byte[]>` with content type and disposition headers.

```java
@GetMapping("/export")
public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "csv") String fileType) {
  byte[] data = productService.exportProducts(fileType);
  return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products." + fileType)
      .contentType(MediaType.parseMediaType(getMediaType(fileType)))
      .body(data);
}
```

## Do NOT

- Never return JPA entities from controllers — always use DTOs
- Never put `@Transactional` on controller methods
- Never hardcode error codes or `.message()` strings — always use enums
- Never use `ResponseEntity<Void>` or `ResponseEntity.noContent()` — wrap in `ResponseBody`
- Never parameterize `ResponseBody` in controller return types — use `ResponseEntity<Object>`
- Never return unbounded lists — always paginate
- Never put business logic in controllers — delegate to services
- Never use GET for state-changing operations
- Never reuse entity classes as request or response DTOs
- Never use `/api/inventory/` prefix — all domains use `/api/v1/{domain}`
