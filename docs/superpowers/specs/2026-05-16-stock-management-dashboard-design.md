# Pharmacy Stock Management Dashboard — Frontend Design

**Date:** 2026-05-16
**Scope:** Full UI redesign of `demo-ui/` admin dashboard
**Status:** Draft — awaiting user review

---

## 1. Overview

Redesign the pharmacy stock management admin dashboard with a modern, efficient UI built on Tailwind CSS, Headless UI, and Atomic Design principles. The target users are pharmacy staff (pharmacists, technicians) who need fast, dense workflows for scanning in stock, checking expiries, and searching products.

### Design Goals

- **Workflow efficiency** — Minimize clicks for common pharmacy tasks
- **Visual consistency** — Design tokens + Storybook enforce a single source of truth
- **Component reuse** — Atomic Design hierarchy ensures every UI element is composed from shared atoms

### Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| UI Framework | Tailwind CSS v4 + @headlessui/react | Utility-first CSS + accessible primitives |
| Icons | @heroicons/react (outline) | Consistent style, tree-shakeable |
| State management | @tanstack/react-query | Server state with cache invalidation |
| Color theme | Teal Pharmacy Identity | Pharmacy cross branding, fresh health feel |
| Layout | Icon-only sidebar (56px) | Maximum content space for power users |
| Architecture | Atomic Design | Structured component hierarchy from atoms to pages |
| Design system | CSS custom properties + Storybook | Token-driven theming with living documentation |

---

## 2. Design Tokens

All visual primitives defined as CSS custom properties in `src/styles/tokens.css` and mirrored as a JS theme map in `src/foundations/theme.js` for Tailwind integration.

### 2.1 Color Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--color-primary` | `#0d9488` | Primary actions, active states, links |
| `--color-primary-hover` | `#0f766e` | Hover state for primary buttons |
| `--color-sidebar-bg` | `#134e4a` | Sidebar background |
| `--color-sidebar-active` | `rgba(13,148,136,0.2)` | Active nav item highlight |
| `--color-danger` | `#ef4444` | Destructive actions, error states |
| `--color-warning` | `#f59e0b` | Warnings, near-expiry alerts |
| `--color-success` | `#10b981` | Success states, positive indicators |
| `--color-surface` | `#ffffff` | Card/panel backgrounds |
| `--color-background` | `#f0fdfa` | Page background |
| `--color-text-primary` | `#0f172a` | Headings, body text |
| `--color-text-secondary` | `#64748b` | Muted labels, descriptions |
| `--color-border` | `#d1fae5` | Input/table borders |

### 2.2 Typography Tokens

| Token | Value |
|-------|-------|
| `--font-family` | `Inter, system-ui, -apple-system, sans-serif` |
| `--font-size-xs` | `0.75rem` |
| `--font-size-sm` | `0.875rem` |
| `--font-size-base` | `1rem` |
| `--font-size-lg` | `1.125rem` |
| `--font-size-xl` | `1.25rem` |
| `--font-size-2xl` | `1.5rem` |
| `--font-weight-normal` | `400` |
| `--font-weight-medium` | `500` |
| `--font-weight-semibold` | `600` |
| `--font-weight-bold` | `700` |

### 2.3 Spacing Tokens

| Token | Value | Pixels |
|-------|-------|--------|
| `--space-1` | `0.25rem` | 4px |
| `--space-2` | `0.5rem` | 8px |
| `--space-3` | `0.75rem` | 12px |
| `--space-4` | `1rem` | 16px |
| `--space-5` | `1.25rem` | 20px |
| `--space-6` | `1.5rem` | 24px |
| `--space-8` | `2rem` | 32px |

### 2.4 Border, Radius, and Shadow Tokens

| Token | Value |
|-------|-------|
| `--radius-sm` | `6px` |
| `--radius-md` | `8px` |
| `--radius-lg` | `12px` |
| `--radius-full` | `9999px` |
| `--border-color` | `#d1fae5` |
| `--shadow-sm` | `0 1px 2px rgba(0,0,0,0.05)` |
| `--shadow-md` | `0 4px 6px rgba(0,0,0,0.07)` |
| `--shadow-lg` | `0 10px 15px rgba(0,0,0,0.1)` |

---

## 3. Layout Shell

### AdminLayout Template

The persistent chrome wrapping all authenticated pages:

```
┌──────┬──────────────────────────────────┐
│      │  TopBar (56px)                    │
│ Icon │───────────────────────────────────│
│ Side │                                   │
│ bar  │  Page Outlet                      │
│(56px)│  (React Router renders here)      │
│      │                                   │
│      │                                   │
└──────┴──────────────────────────────────┘
```

### Sidebar Organism

- **Width:** 56px fixed
- **Background:** `--color-sidebar-bg` (#134e4a)
- **Top:** Pharmacy logo icon (teal circle with "P")
- **Nav items:** Icon-only with tooltip on hover. Uses @heroicons/react
  - Dashboard (HomeIcon), Products (CubeIcon), Inventory (ArchiveBoxIcon), Clinical (BeakerIcon), Orders (ShoppingCartIcon), Transactions (CreditCardIcon), Locations (MapPinIcon), Users (UserGroupIcon)
- **Bottom:** Sign out (ArrowRightOnRectangleIcon)
- **Active state:** `--color-sidebar-active` background + teal icon color
- **Inactive state:** Gray (#94a3b8) icon color

### TopBar Organism

- **Height:** 56px fixed
- **Background:** White with bottom border `--color-border`
- **Left:** Page title + breadcrumb subtitle
- **Right:** Search input (light teal background) + notification bell (red dot badge) + user avatar (teal circle with initials)

---

## 4. Component Library

### 4.1 Atoms

Indivisible UI primitives. Each has a Storybook story.

| Atom | Variants | Props |
|------|----------|-------|
| **Button** | primary, secondary, danger, ghost, icon-left, icon-right | `variant`, `size` (sm/md/lg), `disabled`, `loading`, `icon`, `onClick`, `children` |
| **Badge** | success, danger, warning, info, neutral | `variant`, `children` |
| **Input** | default, focused, error, disabled | Standard input props + `error`, `hint` |
| **Select** | default, focused, error | Standard select props + `error`, `hint` |
| **Spinner** | sm (20px), md (28px), lg (40px) | `size` |
| **Icon** | — | `name` (heroicon name), `size`, `color` |

### 4.2 Molecules

Composed from atoms + simple logic.

| Molecule | Composition | Props |
|----------|-------------|-------|
| **StatCard** | Card with colored top border + value + trend | `title`, `value`, `change?`, `trend?` ('up'/'down'), `accentColor` |
| **PageHeader** | Title + subtitle + action slot | `title`, `subtitle?`, `actions?` (ReactNode) |
| **SearchBar** | Input with icon + filter slot | `placeholder`, `onSearch`, `filterSlot?` |
| **Pagination** | Page buttons + info text | `currentPage`, `totalPages`, `totalItems`, `pageSize`, `onPageChange` |
| **FormField** | Label + input + hint/error wrapper | `label`, `required?`, `error?`, `hint?`, `children` (any input atom) |

### 4.3 Organisms

Complex composed components with state and API interaction.

| Organism | Description | Uses |
|----------|-------------|------|
| **Sidebar** | Icon-only navigation with active state and tooltips | Icon atom, Headless UI Tooltip |
| **TopBar** | Page header bar with search and user menu | SearchBar molecule, Badge atom |
| **DataTable** | Sortable, filterable table with search, pagination, row actions | SearchBar, Pagination, Badge, Button, Spinner atoms |
| **FormDrawer** | Slide-over panel for create/edit forms | Headless UI Dialog + TransitionPanel, FormField, Button atoms |
| **AlertDialog** | Confirmation modal for destructive actions | Headless UI Dialog, Button atoms |
| **ExpiryAlerts** | Color-coded list of expiring batches | Badge atom |
| **ActivityFeed** | Recent stock movement timeline | Icon atom |

### 4.4 Templates

Page-level layout compositions.

| Template | Composition |
|----------|-------------|
| **AdminLayout** | Sidebar + TopBar + `<Outlet/>` for page content |
| **TablePage** | PageHeader + SearchBar + DataTable + Pagination |
| **FormPage** | PageHeader + FormDrawer (triggered by button) |

---

## 5. Page Designs

### 5.1 Dashboard (`/`)

The landing page after login. Dense overview of pharmacy state.

**Widgets (top to bottom):**

1. **Quick Actions Bar** — Row of 4 buttons: Stock In (primary), Deduct Stock, Search Product, Run Report
2. **Stat Cards** — 4-column grid:
   - Products (total count, weekly trend) — teal accent
   - Expiring Soon (count, red "3 within 7 days") — amber accent
   - Batches (active count) — purple accent
   - Low Stock (count below reorder) — red accent
3. **Expiry Alerts** — Left half of 2-column grid:
   - Red border-left for batches expiring < 7 days
   - Amber border-left for batches expiring < 30 days
   - Each row: product name, batch number, quantity, days until expiry
   - "View all" link to Inventory page
4. **Recent Activity** — Right half of 2-column grid:
   - Timeline of stock movements with icon, description, and timestamp
   - Types: Stock In (blue icon), Deducted (pink icon), Product Created (green icon), Low Stock Alert (amber icon)

**APIs:** GET /products (count), GET /batches (expiring), GET /transactions (recent)

### 5.2 Products (`/products`)

Full CRUD for the product catalog.

**Table columns:** Code, Name, Category, Reorder Qty, Status (badge: Active/Inactive), Actions (Edit/Delete)

**Features:**
- Search by code or name
- Filter by category and status
- "Add Product" button opens FormDrawer
- Edit opens same drawer pre-filled
- Delete triggers AlertDialog with cascade warning
- Low-stock rows highlighted with amber background

**Form fields:** Product Code*, Barcode, Name*, Description, Category (select), Reorder Quantity, Packed Height/Width/Length/Weight, isActive (toggle)

**APIs:** CRUD /products, GET /products?search=&category=

### 5.3 Inventory (`/inventory`)

Batch management with stock-in and deduct operations.

**Table columns:** Product Name, Batch#, Expiry Date (color-coded), Quantity, Store, Actions (Stock In / Deduct)

**Features:**
- Expiry rows: red background < 7 days, amber < 30 days
- "Stock In" button opens drawer: select product, enter batch#, expiry date, quantity, select store
- "Deduct" button opens drawer: select batch, enter quantity, FEFO suggestion displayed
- Sort by expiry date (default ascending)

**APIs:** GET /batches, POST /batches/stock-in, POST /batches/deduct

### 5.4 Clinical (`/clinical`)

Three sub-tabs using Headless UI Tab component.

**Stores Tab:**
- DataTable: Name, Type (badge), Status (badge), Actions (Edit/Delete)
- CRUD via FormDrawer: Name, Type (select), Address, Phone, isActive toggle

**Molecules Tab:**
- DataTable: Name, Formula, Actions (Edit/Delete)
- CRUD via FormDrawer: Name, Formula, Description

**Brands Tab:**
- DataTable: Name, Manufacturer, Molecule (linked), Actions (Edit/Delete)
- CRUD via FormDrawer: Name, Manufacturer, Molecule (select from molecules list)

**APIs:** CRUD /stores, /molecules, /brands

### 5.5 Orders (`/orders`)

Read-only order listing.

**Table columns:** Order#, Supplier, Date, Items count, Total, Status (badge)

**Status badges:** Pending (amber), Processing (blue), Completed (green), Cancelled (red)

**Features:**
- Search by order number or supplier
- Filter by status
- Row expand shows order items

**APIs:** GET /orders, GET /orders/:id

### 5.6 Transactions (`/transactions`)

Read-only audit log of all stock movements.

**Table columns:** Txn#, Type (IN/OUT badge), Product, Quantity, Store, Date, User

**Type badges:** Stock In (teal), Deduct (orange), Adjust (gray)

**Features:**
- Date range filter in SearchBar
- Search by product name or transaction ID
- Sort by date (default newest first)

**APIs:** GET /transactions?from=&to=

### 5.7 Locations (`/locations`)

Read-only location listing.

**Table columns:** Name, Address, Type (Warehouse/Store badge), Phone, Status

**Type badges:** Warehouse (teal), Store (blue)

**Features:**
- Search by name or address
- Filter by type

**APIs:** GET /locations

### 5.8 Users (`/users`)

Read-only user listing.

**Table columns:** Avatar (initials circle), Username, Email, Role (badge), Last Login, Status

**Role badges:** Admin (purple), Manager (teal), Staff (gray)

**Features:**
- Search by username or email
- Filter by role

**APIs:** GET /users

### 5.9 Login (`/login`)

Full-page login/register form. No sidebar or topbar.

**Layout:**
- Full-screen teal gradient background (#134e4a → #0d9488)
- Centered white card (max-width 400px), rounded-xl, shadow-lg
- Pharmacy logo (teal circle with "P" or cross icon) at top
- "Pharmacy Stock Manager" heading

**Login tab:**
- Username + Password fields
- "Sign In" button (primary, full-width)
- Error states: red border + message below field
- Loading: Spinner replaces button text

**Register tab:**
- Username + Email + Password + Confirm Password fields
- "Create Account" button

**MFA step:**
- If API returns `tempToken`, show TOTP code input (6-digit field)
- "Verify" button to complete authentication

**APIs:** POST /auth/login, POST /auth/register, POST /auth/verify-totp

---

## 6. Data Flow

### API Client

Keep existing `lib/api.js` unchanged. It handles:
- JWT token management (localStorage)
- Snake_case → camelCase response conversion
- camelCase → snake_case request conversion
- Base URL: `/api/v1`

### React Query Integration

Replace the existing `useApi` hook with `@tanstack/react-query`:

**`lib/hooks.js` exports:**
- `useQueryList(key, url, params)` — paginated list queries with search/filter params
- `useQueryDetail(key, url, id)` — single item queries
- `useCreateMutation(key, url)` — POST with automatic cache invalidation
- `useUpdateMutation(key, url, id)` — PUT with cache invalidation
- `useDeleteMutation(key, url)` — DELETE with cache invalidation

**Query key structure:**
```
['products', { search: 'amox', category: 'antibiotics', page: 1, size: 20 }]
['products', 123]  // single product
['batches', { expiry: 'soon' }]
['transactions', { from: '2026-05-01', to: '2026-05-16' }]
```

**Mutation pattern:**
```jsx
const createProduct = useCreateMutation('products', '/products');
// onSuccess: invalidate ['products'] → auto-refresh table
// + close FormDrawer
```

### Loading & Error States

- **Loading:** DataTable shows Spinner atom centered, or skeleton rows
- **Empty:** "No products found" with illustration and "Add your first product" CTA
- **Error:** Toast notification (sonner library) with error message
- **Mutation loading:** Submit button shows Spinner, form fields disabled

---

## 7. File Structure

```
demo-ui/src/
├── styles/
│   └── tokens.css              # CSS custom properties (design tokens)
├── foundations/
│   └── theme.js                # JS token map for Tailwind config
├── atoms/
│   ├── Button.jsx              # Primary, secondary, danger, ghost variants
│   ├── Button.stories.jsx      # Storybook story
│   ├── Badge.jsx               # Status badges
│   ├── Badge.stories.jsx
│   ├── Input.jsx               # Text input with error/hint states
│   ├── Input.stories.jsx
│   ├── Select.jsx              # Dropdown select
│   ├── Select.stories.jsx
│   ├── Spinner.jsx             # Loading spinner
│   ├── Spinner.stories.jsx
│   ├── Icon.jsx                # Heroicon wrapper
│   └── Icon.stories.jsx
├── molecules/
│   ├── StatCard.jsx            # Dashboard stat card
│   ├── StatCard.stories.jsx
│   ├── PageHeader.jsx          # Title + subtitle + actions
│   ├── PageHeader.stories.jsx
│   ├── SearchBar.jsx           # Search input + filter slot
│   ├── SearchBar.stories.jsx
│   ├── Pagination.jsx          # Page navigation
│   ├── Pagination.stories.jsx
│   ├── FormField.jsx           # Label + input + hint/error wrapper
│   └── FormField.stories.jsx
├── organisms/
│   ├── Sidebar.jsx             # Icon-only sidebar navigation
│   ├── Sidebar.stories.jsx
│   ├── TopBar.jsx              # Top bar with search, notifications, avatar
│   ├── TopBar.stories.jsx
│   ├── DataTable.jsx           # Sortable, filterable data table
│   ├── DataTable.stories.jsx
│   ├── FormDrawer.jsx          # Slide-over form panel
│   ├── FormDrawer.stories.jsx
│   ├── AlertDialog.jsx         # Confirmation dialog
│   ├── AlertDialog.stories.jsx
│   ├── ExpiryAlerts.jsx        # Expiry alert list
│   ├── ExpiryAlerts.stories.jsx
│   ├── ActivityFeed.jsx        # Recent activity timeline
│   └── ActivityFeed.stories.jsx
├── templates/
│   ├── AdminLayout.jsx         # Sidebar + TopBar + Outlet
│   ├── TablePage.jsx           # PageHeader + SearchBar + DataTable + Pagination
│   └── FormPage.jsx            # TablePage + FormDrawer trigger
├── pages/
│   ├── Dashboard.jsx           # Quick actions + stats + alerts + activity
│   ├── Products.jsx            # Product CRUD
│   ├── Inventory.jsx           # Batch management + stock-in/deduct
│   ├── Clinical.jsx            # 3 tabs: Stores, Molecules, Brands
│   ├── Orders.jsx              # Read-only order listing
│   ├── Transactions.jsx        # Read-only audit log
│   ├── Locations.jsx           # Read-only location listing
│   ├── Users.jsx               # Read-only user listing
│   └── Login.jsx               # Login/Register + MFA
├── lib/
│   ├── api.js                  # Existing API client (unchanged)
│   └── hooks.js                # React Query wrappers (replaces useApi)
├── App.jsx                     # Router configuration
├── main.jsx                    # Entry point
└── index.css                   # Tailwind imports + tokens

demo-ui/.storybook/
├── main.js                     # Storybook config (Vite builder)
├── preview.js                  # Global decorators + token CSS import
└── stories/                    # Organized by atomic level
    ├── foundations/             # Token documentation stories
    ├── atoms/
    ├── molecules/
    └── organisms/
```

---

## 8. Dependencies to Install

```bash
cd demo-ui
npm install tailwindcss @tailwindcss/vite @headlessui/react @heroicons/react @tanstack/react-query sonner
npm install -D @storybook/react-vite storybook
```

### Tailwind v4 Setup

Tailwind v4 uses CSS-first configuration. Import tokens in `index.css`:

```css
@import "tailwindcss";
@import "./styles/tokens.css";
```

No `tailwind.config.js` needed — Tailwind v4 detects utility classes automatically.

### Storybook Setup

- Builder: `@storybook/react-vite`
- Import `tokens.css` in `.storybook/preview.js` for consistent theming
- Stories co-located with components in `src/` using `.stories.jsx` suffix

---

## 9. Out of Scope

These items are explicitly excluded from this design:

- Mobile/responsive layouts (desktop-only for pharmacy terminals)
- Dark mode (single teal light theme)
- Internationalization (English only)
- Offline mode or service workers
- Real-time WebSocket updates (polling via react-query refetch)
- Charts/graphs on dashboard (stat cards + lists only)
- Export/print functionality
- Accessibility beyond what Headless UI provides by default
