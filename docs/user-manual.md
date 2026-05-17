# User Manual: Stock Management Demo UI

A browser-based demo for the Stock Management System backend. This guide walks through the full user journey — from login to managing inventory.

---

## 1. Getting Started

### Start the Backend

```bash
./mvnw spring-boot:run
```

Requires PostgreSQL and Redis running. The API serves at `http://localhost:8080/api/v1`.

### Start the Demo UI

```bash
cd demo-ui && npm run dev
```

Opens at `http://localhost:5173`. The Vite dev server proxies `/api` requests to the backend.

---

## 2. Authentication

The landing page is a **Login/Register** screen.

1. **Register** — Switch to the "Register" tab, pick a username and password (min 8 characters), submit.
2. **Sign In** — Enter your credentials on the "Sign In" tab. On success you're redirected to the Dashboard.

The system uses JWT tokens stored in your browser's local storage. If MFA is enabled on your account, the demo UI will show a notice (full MFA entry is not yet supported in the UI).

---

## 3. Dashboard

The **Dashboard** shows at-a-glance counts:
- **Products** — total items in the product catalog
- **Batches** — total inventory batches
- **Stores** — total clinical stores
- **Orders** — total orders for user #1

These numbers update live as you add data through other pages.

---

## 4. User Journey: Complete Product Workflow

The system has **two catalogs** that work together:

| Catalog | Purpose | Pages |
|---------|---------|-------|
| **Product Catalog** | General product info (SKU, barcode, category) | Products |
| **Master Catalog** | Pharmacy-specific: molecules → brands → inventory | Clinical (Molecules, Brands) |

The **Inventory** module uses the **Master Catalog** (Brands), not the Product Catalog. This is the key to understanding the workflow.

### Step-by-step: From nothing to stocked inventory

#### Step 1 — Create a Product (Product Catalog)

Go to **Products** → click **+ Add Product**:

| Field | Example | Notes |
|-------|---------|-------|
| Code (SKU) | `SKU001` | Internal product code |
| Barcode | `Bar111` | External barcode (UPC) |
| Name | `Paracetamol 500mg` | Product display name |
| Description | `Pain reliever and fever reducer` | Up to 300 chars |
| Category | `OTC` | Any category label |
| Reorder Quantity | `100` | Minimum reorder threshold |
| Packed Weight/Height/Width/Depth | `0.01` / `0.05` / `0.04` / `0.03` | Package dimensions |
| Active | `Yes` | Is the product active? |

The product is now in the catalog. You can **Edit** or **Delete** it from the table.

> **Note:** The product barcode here is for the product catalog only. Inventory uses a separate barcode from the Master Catalog (Brands).

#### Step 2 — Create a Molecule (Master Catalog)

Go to **Clinical** → **Molecules** tab → click **+ Molecule**:

| Field | Example | Notes |
|-------|---------|-------|
| Generic Name | `Paracetamol` | Required — the drug's generic name |
| Therapeutic Class | `Analgesic` | Drug classification |
| Regulatory Schedule | `OTC` | `OTC` = over-the-counter, `SCHEDULE_2` etc. = controlled |
| Dosage Instructions | `Take 1-2 tablets every 4-6 hours` | Optional |
| Safety Warnings | `Do not exceed 8 tablets in 24 hours` | Optional |

Use the **search bar** to find molecules after creating them.

#### Step 3 — Create a Brand (Master Catalog)

Go to **Clinical** → **Brands** tab → click **+ Brand**:

| Field | Example | Notes |
|-------|---------|-------|
| Molecule | Select from dropdown | Must exist (created in Step 2) |
| Brand Name | `Panadol Extra` | Required — commercial brand name |
| Barcode | `BrandPana001` | **This is the barcode inventory uses** |
| Strength | `500mg` | Optional |
| Form | `Tablet` | Optional |
| Base Unit | `tablet` | The unit for inventory counting |

> **Important:** The **Brand barcode** is what the inventory system recognizes. This is separate from the Product barcode.

#### Step 4 — Create a Store

Go to **Clinical** → **Stores** tab → click **+ Store**:

| Field | Example | Notes |
|-------|---------|-------|
| Name | `Main Pharmacy` | Store display name |
| Type | `Physical` / `Hub` / `Logical` | Store type |

After creation, **copy the Store ID (UUID)** from the table — you'll need it for stock-in.

#### Step 5 — Stock In (Add Inventory)

Go to **Inventory** → click **Stock In**:

| Field | Example | Notes |
|-------|---------|-------|
| Barcode | `BrandPana001` | The **Brand** barcode from Step 3 |
| Batch Number | `BATCH-2026-001` | Unique batch identifier |
| Expiry Date | `2027-12-31` | Must be a future date |
| Quantity | `500` | Number of units |
| Store ID | `550e8400-e29b-...` | Paste the Store UUID from Step 4 |

On success, a new batch is created and linked to the store's stock.

#### Step 6 — View Batches

The **Inventory** page table shows all batches:
- Batch ID, Barcode, Batch Number, Quantity, Expiry Date

Use **Prev/Next** pagination to browse.

#### Step 7 — Deduct Stock

Go to **Inventory** → click **Deduct Stock**:

| Field | Example | Notes |
|-------|---------|-------|
| Barcode | `BrandPana001` | Brand barcode |
| Store ID | `550e8400-e29b-...` | Store UUID |
| Quantity | `10` | Units to remove |

The system uses **FEFO** (First Expiry, First Out) — it deducts from the batch expiring soonest. If the brand's molecule has a controlled regulatory schedule, a pharmacist signature is required (not available in demo UI).

---

## 5. Other Pages

### Locations

Manage addresses (warehouses, offices, etc.). Click **+ Add Address** and fill in:
- Address Name, Line 1, Line 2, City, Province, Country, Postal Code

### Orders

View orders by user. Enter a **User ID** in the filter box to see that user's orders. The table shows:
- Order ID, Status (Pending/Processing/Shipped/Delivered/Cancelled), Subtotal, Tax, Total, Created date

### Transactions

View all financial transactions. The table shows:
- Transaction ID, Code, Type (Purchase/Refund/Adjustment), Status (Pending/Completed/Failed), Created date

### Users

View all registered users. Read-only list showing:
- User ID, Username, Full Name, Email, Registration Date

---

## 6. Quick Reference: API Endpoints

| Module | Base Path |
|--------|-----------|
| Auth | `/api/v1/auth` |
| Products | `/api/v1/products` |
| Clinical Stores | `/api/v1/clinical/stores` |
| Clinical Catalog | `/api/v1/clinical/catalog` (molecules, brands) |
| Inventory | `/api/v1/inventory` (batches, stock-in, deduct) |
| Addresses | `/api/v1/addresses` |
| Orders | `/api/v1/orders` |
| Transactions | `/api/v1/transactions` |
| Users | `/api/v1/users` |

Full API documentation available at `http://localhost:8080/swagger-ui/index.html` when the backend is running.

---

## 7. Common Issues

| Problem | Cause | Solution |
|---------|-------|----------|
| "Barcode not recognized in Master Catalog" | Barcode is on a Product, not a Brand | Create a Brand in Clinical → Brands with that barcode |
| Product list shows "No products found" | Backend returned non-paginated list | Already fixed — refresh the page |
| Blank page on Clinical | JavaScript variable ordering | Already fixed — refresh the page |
| Login fails with MFA message | Account has TOTP enabled | Use API directly for MFA flow |
| 401 Unauthorized | Token expired | Log out and sign in again |
