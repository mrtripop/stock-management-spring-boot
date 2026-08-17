# Frontend Design System — Design Spec

**Date**: 2026-05-17
**Scope**: Spec 1 of 3 (Design System + Storybook + Token Architecture)
**Approach**: Token-first, component rebuild
**Visual direction**: Clean clinical, teal primary, light + dark mode

---

## 1. Design Token Architecture

### 1.1 Two-Layer Token System

| Layer | Purpose | Example |
|-------|---------|---------|
| **Primitive** | Raw color palette (50-950 scale per hue) | `--teal-600: #0d9488` |
| **Semantic** | Purpose-based aliases (light + dark) | `--color-primary: var(--teal-600)` |

Components only reference semantic tokens. Dark mode overrides the semantic layer — zero component changes needed. If a component needs a specific color not covered by semantic tokens, add a new semantic token rather than referencing primitives directly.

### 1.2 Color Palette

**Primary — Teal scale:**
50 `#f0fdfa`, 100 `#ccfbf1`, 200 `#99f6e4`, 300 `#5eead4`, 400 `#2dd4bf`, 500 `#14b8a6`, 600 `#0d9488`, 700 `#0f766e`, 800 `#115e59`, 900 `#134e4a`, 950 `#042f2e`

**Neutral — Slate scale:**
50 `#f8fafc`, 100 `#f1f5f9`, 200 `#e2e8f0`, 300 `#cbd5e1`, 400 `#94a3b8`, 500 `#64748b`, 600 `#475569`, 700 `#334155`, 800 `#1e293b`, 900 `#0f172a`, 950 `#020617`

**Feedback:**
- Success (Emerald): 50-950 scale
- Danger (Red): 50-950 scale
- Warning (Amber): 50-950 scale
- Info (Blue): 50-950 scale

**Pharmacy domain:**
- Purple: ADMIN role badge
- Teal: PHARMACIST role badge
- Slate: EMPLOYEE role badge
- Orange: REORDER_NEEDED task type

### 1.3 Semantic Token Mapping

```css
:root {
  /* Primary */
  --color-primary: var(--teal-600);
  --color-primary-hover: var(--teal-700);
  --color-primary-active: var(--teal-800);
  --color-primary-subtle: var(--teal-100);
  --color-primary-text: var(--teal-950);

  /* Feedback */
  --color-success: var(--emerald-600);
  --color-success-subtle: var(--emerald-100);
  --color-success-text: var(--emerald-900);
  --color-danger: var(--red-600);
  --color-danger-subtle: var(--red-100);
  --color-danger-text: var(--red-900);
  --color-warning: var(--amber-500);
  --color-warning-subtle: var(--amber-100);
  --color-warning-text: var(--amber-900);
  --color-info: var(--blue-600);
  --color-info-subtle: var(--blue-100);
  --color-info-text: var(--blue-900);

  /* Surfaces */
  --color-background: var(--slate-50);
  --color-surface: #ffffff;
  --color-surface-raised: #ffffff;
  --color-overlay: rgba(0, 0, 0, 0.5);

  /* Text */
  --color-text-primary: var(--slate-900);
  --color-text-secondary: var(--slate-600);
  --color-text-muted: var(--slate-400);
  --color-text-inverse: #ffffff;

  /* Borders */
  --color-border: var(--slate-200);
  --color-border-focus: var(--teal-500);

  /* Sidebar */
  --color-sidebar-bg: var(--slate-900);
  --color-sidebar-active: rgba(13, 148, 136, 0.2);
  --color-sidebar-text: var(--slate-300);
  --color-sidebar-text-active: #ffffff;
}

[data-theme="dark"] {
  --color-background: var(--slate-950);
  --color-surface: var(--slate-900);
  --color-surface-raised: var(--slate-800);
  --color-overlay: rgba(0, 0, 0, 0.7);
  --color-text-primary: var(--slate-50);
  --color-text-secondary: var(--slate-400);
  --color-text-muted: var(--slate-500);
  --color-text-inverse: var(--slate-900);
  --color-border: var(--slate-700);
  --color-border-focus: var(--teal-400);
  --color-primary: var(--teal-500);
  --color-primary-hover: var(--teal-400);
  --color-primary-subtle: var(--teal-900);
  --color-primary-text: var(--teal-100);
  --color-sidebar-bg: var(--slate-950);
}
```

### 1.4 Typography Scale

| Token | Size | Weight | Line-height | Use |
|-------|------|--------|-------------|-----|
| `--text-2xs` | 0.625rem | 400 | 1rem | Tiny labels |
| `--text-xs` | 0.75rem | 400 | 1rem | Badges, helper text |
| `--text-sm` | 0.875rem | 400 | 1.25rem | Table cells, form inputs |
| `--text-base` | 1rem | 400 | 1.5rem | Body text |
| `--text-lg` | 1.125rem | 500 | 1.75rem | Section titles |
| `--text-xl` | 1.25rem | 600 | 1.75rem | Page subtitles |
| `--text-2xl` | 1.5rem | 600 | 2rem | Page titles |
| `--text-3xl` | 1.875rem | 700 | 2.25rem | Dashboard stats |

Font weights: `--font-normal: 400`, `--font-medium: 500`, `--font-semibold: 600`, `--font-bold: 700`.

Font family: `Inter, system-ui, -apple-system, sans-serif`.

### 1.5 Spacing Scale

| Token | Value | Use |
|-------|-------|-----|
| `--space-0` | 0 | Reset |
| `--space-0-5` | 0.125rem | Tight gaps |
| `--space-1` | 0.25rem | Icon gaps |
| `--space-1-5` | 0.375rem | Badge padding |
| `--space-2` | 0.5rem | Inline spacing |
| `--space-3` | 0.75rem | Compact padding |
| `--space-4` | 1rem | Standard padding |
| `--space-5` | 1.25rem | Card padding |
| `--space-6` | 1.5rem | Section gaps |
| `--space-8` | 2rem | Page margins |
| `--space-10` | 2.5rem | Large gaps |
| `--space-12` | 3rem | Page sections |
| `--space-16` | 4rem | Major sections |

### 1.6 Shadows, Borders, Radii

Shadows:
```css
--shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.05);
--shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06);
--shadow-md: 0 4px 6px rgba(0, 0, 0, 0.07), 0 2px 4px rgba(0, 0, 0, 0.06);
--shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1), 0 4px 6px rgba(0, 0, 0, 0.05);
--shadow-xl: 0 20px 25px rgba(0, 0, 0, 0.1), 0 8px 10px rgba(0, 0, 0, 0.04);
```

Radii: `--radius-sm: 4px`, `--radius-md: 6px`, `--radius-lg: 8px`, `--radius-xl: 12px`, `--radius-2xl: 16px`, `--radius-full: 9999px`.

### 1.7 JavaScript Theme Object

`theme.js` mirrors all tokens as a JS object for programmatic use (charts, canvas, dynamic styles):

```js
export const theme = {
  colors: { /* all semantic tokens */ },
  typography: { /* size, weight, lineHeight scales */ },
  spacing: { /* all spacing values */ },
  radius: { /* all radii */ },
  shadow: { /* all shadows */ },
  breakpoints: { sm: '640px', md: '768px', lg: '1024px', xl: '1280px' },
}
```

---

## 2. Component Library

### 2.1 Atoms (6 components)

#### Button
- **Variants**: `primary`, `secondary`, `outline`, `danger`, `ghost`
- **Sizes**: `sm` (h-7), `md` (h-9), `lg` (h-11)
- **Props**: `variant`, `size`, `disabled`, `loading`, `icon`, `fullWidth`, `type` (submit/button), `className`, `children`
- **New vs current**: Added `outline` variant, `fullWidth` prop, `type` prop
- **Token usage**: All colors via `var(--color-primary)` etc., radius via `var(--radius-md)`

#### Input
- **Props**: `label`, `error`, `helperText`, `leftIcon`, `rightIcon`, `disabled`, `placeholder`, `className` + native input props
- **States**: default, focus (border-focus), error (border-danger, error message), disabled
- **React Hook Form**: `forwardRef` to support `register()`
- **New vs current**: Added error state, icon slots, forwardRef

#### Select
- **Props**: Same pattern as Input + `options` array `{ value, label }`
- **React Hook Form**: `forwardRef` for `register()`
- **New vs current**: Added error state, placeholder, forwardRef

#### Badge
- **Variants**: `success`, `danger`, `warning`, `info`, `neutral`, `teal`, `purple`, `orange`
- **Variant mapping to API enums**:
  - BatchStatus: AVAILABLE=success, RECALLED=danger, QUARANTINED=warning
  - InvoiceStatus: PENDING=warning, COMPLETED=success, VOIDED=danger
  - TaskStatus: PENDING=warning, ACKNOWLEDGED=info, RESOLVED=success
  - UserRole: ADMIN=purple, MANAGER=teal, PHARMACIST=info, EMPLOYEE=neutral
  - StoreType: PHYSICAL=teal, HUB=info, LOGICAL=neutral
- **New vs current**: Added `dot` sub-variant for inline status indicators

#### Icon
- Heroicons wrapper. Props: `icon`, `size` (sm/md/lg mapped to 4/5/6 via spacing tokens).

#### Spinner
- Props: `size` (sm/md/lg), `color` (defaults to `--color-primary`).

### 2.2 Molecules (8 components)

#### FormField
- Wraps Input/Select/textarea with label, error message, helper text
- Props: `label`, `error`, `helperText`, `required`, `children` (the input element)
- React Hook Form compatible — passes `error` from form state

#### PageHeader
- Props: `title`, `subtitle`, `actions` (React node slot), `breadcrumb` array
- Renders h2 title, subtitle in muted text, action buttons on the right

#### Pagination
- Props: `currentPage`, `totalPages`, `onPageChange`, `pageSize`, `onPageSizeChange`
- Maps to API pagination params: `page` (1-based), `size`, `orderBy`
- Shows: prev/next arrows, page numbers (max 7 visible), total count

#### SearchBar
- Props: `value`, `onChange`, `placeholder`, `debounceMs` (default 300)
- Debounced input with clear button (X icon)

#### StatCard
- Props: `icon`, `value` (string/number), `label`, `trend` (up/down/flat), `trendValue` (e.g. "+12%"), `variant` (default/success/danger/warning)
- Dark mode aware via token colors

#### DatePicker (new)
- Native `<input type="date">` wrapped with FormField styling
- Props: `label`, `value`, `onChange`, `min`, `max`, `error`

#### FileUploader (new)
- Drag-and-drop zone + click to browse
- Props: `accept` (e.g. ".csv"), `onFile`, `maxSize`, `loading`, `error`
- Shows filename and size after selection

#### ConfirmationDialog (new)
- Props: `open`, `onConfirm`, `onCancel`, `title`, `message`, `variant` (info/warning/danger), `confirmText`, `loading`
- Reusable for delete, void, recall confirmations

### 2.3 Organisms (7 components)

#### DataTable
- **Column definition**: `{ key, label, sortable?, render?(row), width? }`
- **Features**: Sortable columns (click header), row selection (checkbox), loading skeleton, empty state, responsive horizontal scroll
- **Props**: `columns`, `data`, `loading`, `emptyMessage`, `selectable`, `onSelectionChange`, `sortKey`, `sortDir`, `onSort`
- **New vs current**: Added sorting, row selection, loading skeleton, responsive scroll

#### FormDrawer
- Slide-in panel from right
- **Props**: `open`, `onClose`, `title`, `width` (sm/md/lg)
- **Integration**: React Hook Form `FormProvider` wrapper. Zod schema resolver.
- **Features**: Multi-step form support (wizard steps), dirty state warning on close, submit button with loading state
- **New vs current**: Added multi-step, dirty warning, RHF integration

#### Sidebar
- Collapsible: expanded (240px) / collapsed (64px icon-only)
- **Sections**: Navigation links (icon + label), Store selector dropdown (top), User menu (bottom: profile, theme toggle, logout)
- **Props**: `collapsed`, `onToggle`, `activeRoute`, `storeOptions`, `selectedStore`, `onStoreChange`, `user`, `onLogout`, `onToggleTheme`, `isDarkMode`
- **New vs current**: Added store selector, user menu, theme toggle

#### AlertDialog
- Props: `open`, `onClose`, `title`, `message`, `variant` (info/warning/danger), `actions` array `{ label, onClick, variant }`
- Uses semantic token colors per variant

#### TopBar (new)
- **Props**: `breadcrumb`, `onSearchClick`, `notificationCount`, `userAvatar`
- Contains: breadcrumb trail, search trigger button, notification bell with count badge, user avatar (clicks open dropdown)

#### ActivityFeed (new)
- **Props**: `items` array `{ id, type, message, timestamp }`, `loading`
- Timeline-style list with icons per type (transaction, task, alert)
- "View all" link at bottom

#### ExpiryAlerts (new)
- **Props**: `tasks` array (from `useTasks` hook), `onAcknowledge`, `onResolve`, `loading`
- Priority-sorted by urgency: red (< 7 days), amber (< 30 days), default
- Each row: icon, brand name, batch number, days until expiry, quantity, action buttons

### 2.4 Storybook Organization

Stories are co-located with components (e.g., `atoms/Button.jsx` + `atoms/Button.stories.jsx`). The Storybook sidebar is organized via the `title` property in each story's default export:

```
Foundations/Colors      ← src/foundations/Colors.stories.jsx
Foundations/Typography  ← src/foundations/Typography.stories.jsx
Foundations/Spacing     ← src/foundations/Spacing.stories.jsx
Foundations/Shadows     ← src/foundations/Shadows.stories.jsx

Atoms/Button            ← src/atoms/Button.stories.jsx
Atoms/Input             ← src/atoms/Input.stories.jsx
Atoms/Select            ← src/atoms/Select.stories.jsx
Atoms/Badge             ← src/atoms/Badge.stories.jsx
Atoms/Icon              ← src/atoms/Icon.stories.jsx
Atoms/Spinner           ← src/atoms/Spinner.stories.jsx

Molecules/FormField     ← src/molecules/FormField.stories.jsx
Molecules/PageHeader    ← src/molecules/PageHeader.stories.jsx
Molecules/Pagination    ← src/molecules/Pagination.stories.jsx
Molecules/SearchBar     ← src/molecules/SearchBar.stories.jsx
Molecules/StatCard      ← src/molecules/StatCard.stories.jsx
Molecules/DatePicker    ← src/molecules/DatePicker.stories.jsx
Molecules/FileUploader  ← src/molecules/FileUploader.stories.jsx
Molecules/ConfirmationDialog ← src/molecules/ConfirmationDialog.stories.jsx

Organisms/DataTable     ← src/organisms/DataTable.stories.jsx
Organisms/FormDrawer    ← src/organisms/FormDrawer.stories.jsx
Organisms/Sidebar       ← src/organisms/Sidebar.stories.jsx
Organisms/AlertDialog   ← src/organisms/AlertDialog.stories.jsx
Organisms/TopBar        ← src/organisms/TopBar.stories.jsx
Organisms/ActivityFeed  ← src/organisms/ActivityFeed.stories.jsx
Organisms/ExpiryAlerts  ← src/organisms/ExpiryAlerts.stories.jsx
```

Foundations stories live in `src/foundations/` alongside `theme.js`. All other stories are co-located with their components.

Each story includes: **args table** (all props documented), **variant matrix** (visual grid of all states), **dark mode** toggle, and **playground** for interactive testing.

---

## 3. Auth & API Integration Layer

### 3.1 AuthProvider

Centralized context wrapping the app:

```jsx
<AuthProvider>
  <QueryClientProvider>
    <App />
  </QueryClientProvider>
</AuthProvider>
```

**State**: `{ user, role, storeId, token, isMfaRequired, tempToken, loading, error }`

**Actions**: `login(username, password)`, `verifyMfa(tempToken, totpCode)`, `register(username, password)`, `logout()`, `selectStore(storeId)`, `setupMfa()`, `refreshProfile()`

**Hooks**: `useAuth()` returns full context, `useHasRole(roles)` returns boolean, `useStoreId()` returns current store ID

**Store selection gate**: Component that renders a store picker overlay when `storeId` is null after login. Blocks all store-scoped API calls.

**Role-based access**: `<RequireRole role={["ADMIN", "MANAGER"]}>` component wraps protected routes/UI. Renders `null` or redirect if user lacks the role.

### 3.2 API Client (enhanced)

Enhancements over current `api.js`:

- **Interceptors**: auto-attach Bearer token from AuthProvider context, snake-to-camel conversion on response, camel-to-snake on request body
- **Typed errors**: `AuthError` (401), `ForbiddenError` (403), `NotFoundError` (404), `ValidationError` (400), `ServerError` (5xx)
- **File handling**: `upload(path, file)` method for multipart/form-data, `download(path)` for binary responses (export)
- **No retry on mutations**: GET requests retry once on network error; mutations never retry

### 3.3 React Query Hooks (domain-organized)

```
hooks/
├── useAuth.js
│   └── useLogin, useVerifyMfa, useRegister, useSetupMfa, useSelectStore, useCurrentUser
├── useProducts.js
│   └── useProductList, useProductDetail, useCreateProduct, useUpdateProduct, useDeleteProduct, useUploadProducts, useExportProducts, useProductHistories
├── useInventory.js
│   └── useBatchList, useBatchDetail, useStockIn, useStockDeduct, useBarcodeResolve, useStoreStock, useTaskList, useTaskDetail, useAcknowledgeTask, useResolveTask, useTriggerScan, useUnitConversions, useCreateConversion, useDeleteConversion, useRecallBatch
├── useClinical.js
│   └── useMolecules, useMoleculeDetail, useCreateMolecule, useUpdateMolecule, useSearchMolecules, useBrandsByMolecule, useCreateBrand, useStoreList, useStoreDetail, useCreateStore, useUpdateStore, useDeleteStore, useStoreProducts, useStoreProductDetail, useActivateStoreProduct, useUpdateStoreProduct, useDeactivateStoreProduct
├── useTransactions.js
│   └── useInvoiceList, useInvoiceDetail, useCreateInvoice, useCompleteInvoice, useVoidInvoice, useQuickDispense, useDailySummary, useReceipt, useReconciliation
├── useOrders.js
│   └── useOrderList, useOrderDetail
├── useLocations.js
│   └── useAddressList, useAddressDetail, useCreateAddress, useUpdateAddress, useDeleteAddress, useWarehouseList, useWarehouseDetail, useCreateWarehouse
├── useUsers.js
│   └── useUserList, useUserDetail, useCreateUser, useUpdateUser, useDeleteUser
└── useMesh.js
    └── useMeshStockSearch
```

Each hook file exports named hooks. All list hooks return `{ items, totalPages, totalElements, loading, error }`. All mutation hooks return `{ mutate, mutateAsync, isPending, error }`.

Cache invalidation: each mutation invalidates its parent query key. Cross-domain invalidations (e.g., stock-in updates both batches and store stock) invalidate multiple keys.

### 3.4 Error Handling

Three layers:

1. **API client** — catches HTTP errors, throws typed error objects (`AuthError`, `ValidationError`, etc.)
2. **React Query global `onError`** — shows toast notification for unexpected errors (500s, network errors)
3. **Component level** — form validation errors display inline via React Hook Form + Zod schemas; mutation errors shown in the form's error summary

---

## 4. Page Redesigns

### 4.1 Login Page
- Standalone layout (no sidebar)
- Form: username + password with Zod validation
- MFA flow: on `mfaRequired`, transition to TOTP code input (6-digit, auto-submit on 6th digit)
- "Remember me" stores username in localStorage (not password)

### 4.2 Dashboard
- **Stat cards row** (4 cards): Product count, Active batches, Daily revenue, Items dispensed
- **Alerts section**: Expiry warnings (red < 7 days, amber < 30), reorder alerts, recall alerts. Links to Inventory Tasks.
- **Activity feed**: Latest 10 transactions with type icons
- **Quick actions**: 4 cards — Stock-In, Dispense, Search Product, Run Report
- Responsive: 2x2 stat cards on tablet, single column on mobile

### 4.3 Products Page
- DataTable: Code, Name, Category, Status (active/inactive Badge), Actions (edit, delete)
- SearchBar filters client-side
- FormDrawer for create/edit with all ProductDTO fields
- Toolbar: Upload CSV (FileUploader), Export CSV (download)
- History sub-drawer: audit trail per product

### 4.4 Inventory Page
- **Tabs**: Batches | Stock-In | Tasks | Conversions
- **Batches tab**: DataTable with brand, batch number, expiry, quantity, status Badge. Filter by brandId.
- **Stock-In tab**: Barcode input → resolve → batch details form → submit
- **Tasks tab**: Priority list with color-coded urgency, acknowledge/resolve buttons, "Run Scan" button
- **Conversions tab**: CRUD for unit conversions per brand

### 4.5 Clinical Page
- **Tabs**: Molecules | Brands | Stores
- **Molecules tab**: Search-as-you-type, CRUD with FormDrawer, regulatory schedule badge
- **Brands tab**: Grouped by molecule, create requires molecule selection (cascading)
- **Stores tab**: DataTable with name, type Badge, active status

### 4.6 Store Products (sub-page of Clinical)
- DataTable: brand name, strength, form, price, shelf location, active status
- Activate product (brand selector), update price/shelf override, deactivate

### 4.7 Dispensing (POS) Page
- Multi-step wizard:
  1. Search — molecule search → select molecule → brands → select brand → batches
  2. Build invoice — add items with quantity + insurance %, running totals
  3. Review & dispense — summary, totals, controlled substance signature form if needed
  4. Confirmation — success message + receipt link

### 4.8 Transactions Page
- **Tabs**: Invoices | Reports
- **Invoices tab**: DataTable with ID, store, status Badge, total amount, date. Row expansion for line items. Actions: Complete, Void, View Receipt.
- **Reports tab**: Reconciliation form (store selector, date range) → results with discrepancy highlighting, orphaned entry flags

### 4.9 Orders Page
- Read-only DataTable with status badges, row expansion for items
- Admin: user filter to view all users' orders

### 4.10 Locations Page
- **Tabs**: Addresses | Warehouses
- CRUD with DataTable + FormDrawer
- Warehouses show refrigerated Badge

### 4.11 Users Page
- Admin-only (`<RequireRole role="ADMIN">`)
- DataTable: username, role Badge (color-coded), created date
- CRUD with FormDrawer

### 4.12 Responsive Behavior

| Breakpoint | Layout |
|-----------|--------|
| Desktop (>=1024px) | Sidebar visible (240px or 64px collapsed), full layout |
| Tablet (768-1023px) | Sidebar collapsed to icons, stat cards 2x2 |
| Mobile (<768px) | Sidebar hidden (hamburger), single column, DataTable horizontal scroll |

### 4.13 Theme Toggle

Sun/moon icon in TopBar. Toggles `[data-theme="dark"]` on `<html>`. Preference in localStorage key `theme`. All components use semantic tokens only.

---

## 5. Dependencies to Add

| Package | Purpose |
|---------|---------|
| `react-hook-form` | Form state management |
| `@hookform/resolvers` | Zod resolver bridge |
| `zod` | Schema validation |
| `sonner` | Toast notifications (already present) |

No additional UI library dependencies. Tailwind CSS + Headless UI + custom components.

---

## 6. File Structure

```
src/
├── styles/
│   ├── tokens.css          ← rebuilt (3-layer tokens + dark mode)
│   └── global.css          ← base styles, resets
├── foundations/
│   ├── theme.js            ← rebuilt (full token mirror)
│   ├── Colors.stories.jsx  ← palette swatches, semantic mapping, dark mode
│   ├── Typography.stories.jsx ← size/weight scale
│   ├── Spacing.stories.jsx ← spacing scale visual
│   └── Shadows.stories.jsx ← shadow scale
├── atoms/                  ← rebuilt (6 components + stories)
│   ├── Badge.jsx
│   ├── Badge.stories.jsx
│   ├── Button.jsx
│   ├── Button.stories.jsx
│   ├── Icon.jsx
│   ├── Icon.stories.jsx
│   ├── Input.jsx
│   ├── Input.stories.jsx
│   ├── Select.jsx
│   ├── Select.stories.jsx
│   ├── Spinner.jsx
│   └── Spinner.stories.jsx
├── molecules/              ← rebuilt (5) + new (3)
│   ├── ConfirmationDialog.jsx
│   ├── ConfirmationDialog.stories.jsx
│   ├── DatePicker.jsx
│   ├── DatePicker.stories.jsx
│   ├── FileUploader.jsx
│   ├── FileUploader.stories.jsx
│   ├── FormField.jsx
│   ├── FormField.stories.jsx
│   ├── PageHeader.jsx
│   ├── PageHeader.stories.jsx
│   ├── Pagination.jsx
│   ├── Pagination.stories.jsx
│   ├── SearchBar.jsx
│   ├── SearchBar.stories.jsx
│   ├── StatCard.jsx
│   └── StatCard.stories.jsx
├── organisms/              ← rebuilt (4) + new (3)
│   ├── ActivityFeed.jsx
│   ├── ActivityFeed.stories.jsx
│   ├── AlertDialog.jsx
│   ├── AlertDialog.stories.jsx
│   ├── DataTable.jsx
│   ├── DataTable.stories.jsx
│   ├── ExpiryAlerts.jsx
│   ├── ExpiryAlerts.stories.jsx
│   ├── FormDrawer.jsx
│   ├── FormDrawer.stories.jsx
│   ├── Sidebar.jsx
│   ├── Sidebar.stories.jsx
│   ├── TopBar.jsx
│   └── TopBar.stories.jsx
├── templates/
│   └── AdminLayout.jsx     ← rebuilt with new Sidebar/TopBar
├── lib/
│   ├── api.js              ← enhanced (typed errors, file handling, interceptors)
│   ├── auth.jsx            ← new (AuthProvider, useAuth, useHasRole, RequireRole)
│   └── hooks/              ← domain-organized hooks (9 files)
│       ├── useAuth.js
│       ├── useProducts.js
│       ├── useInventory.js
│       ├── useClinical.js
│       ├── useTransactions.js
│       ├── useOrders.js
│       ├── useLocations.js
│       ├── useUsers.js
│       └── useMesh.js
├── pages/                  ← rebuilt against new components + hooks
│   ├── Login.jsx
│   ├── Dashboard.jsx
│   ├── Products.jsx
│   ├── Inventory.jsx
│   ├── Clinical.jsx
│   ├── StoreProducts.jsx   ← new sub-page
│   ├── Dispensing.jsx
│   ├── Transactions.jsx
│   ├── Orders.jsx
│   ├── Locations.jsx
│   └── Users.jsx
├── App.jsx                 ← rebuilt with AuthProvider, new routes
└── main.jsx                ← entry point
```
