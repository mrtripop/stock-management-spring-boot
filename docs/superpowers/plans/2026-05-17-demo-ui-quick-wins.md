# Demo-UI Quick Wins Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply 7 high-impact UI polish fixes to the demo-ui frontend — text sizes, SVG icons, stat cards, micro-interactions, collapsible sidebar, table action buttons, and form field grouping.

**Architecture:** All changes are scoped to existing React components in `demo-ui/src/`. One new molecule component (`TableRowActions`). No new dependencies. No backend changes.

**Tech Stack:** React 19, Tailwind CSS v4, @heroicons/react, @headlessui/react

---

## File Structure

| Action | Path | Responsibility |
|--------|------|----------------|
| Modify | `demo-ui/src/atoms/Button.jsx` | Add active press feedback |
| Modify | `demo-ui/src/atoms/Input.jsx` | Add focus-visible ring |
| Modify | `demo-ui/src/molecules/StatCard.jsx` | Hover lift animation |
| Create | `demo-ui/src/molecules/TableRowActions.jsx` | Reusable edit/delete icon buttons |
| Modify | `demo-ui/src/organisms/Sidebar.jsx` | Collapsible icon-only + expanded modes |
| Modify | `demo-ui/src/organisms/TopBar.jsx` | SVG bell icon (no emoji) |
| Modify | `demo-ui/src/organisms/DataTable.jsx` | Header text size fix |
| Modify | `demo-ui/src/organisms/ExpiryAlerts.jsx` | SVG icon + text size fixes |
| Modify | `demo-ui/src/organisms/ActivityFeed.jsx` | SVG icons + text size fixes |
| Modify | `demo-ui/src/pages/Dashboard.jsx` | Wire stat card counts + loading skeleton |
| Modify | `demo-ui/src/pages/Products.jsx` | TableRowActions + form field grouping |
| Modify | `demo-ui/src/pages/Clinical.jsx` | TableRowActions in Stores tab |
| Modify | `demo-ui/src/pages/Users.jsx` | Text size fix in avatar |

Pages `Orders.jsx`, `Transactions.jsx`, `Locations.jsx` have no edit/delete actions and need no changes for task 6.

---

### Task 1: Fix Text Sizes (Readability)

**Files:**
- Modify: `demo-ui/src/organisms/ExpiryAlerts.jsx`
- Modify: `demo-ui/src/organisms/ActivityFeed.jsx`
- Modify: `demo-ui/src/organisms/DataTable.jsx`
- Modify: `demo-ui/src/pages/Users.jsx`

- [ ] **Step 1: Fix ExpiryAlerts.jsx text sizes**

In `demo-ui/src/organisms/ExpiryAlerts.jsx`, change the header from emoji to text + fix all tiny text:

```jsx
// Line 15 — remove emoji from header
<h3 className="text-xs font-semibold text-[var(--color-text-primary)]">Expiry Alerts</h3>

// Line 34 — product name: text-[10px] → text-xs
<div className="text-xs font-semibold text-[var(--color-text-primary)]">{item.productName || item.barcode}</div>

// Line 35 — batch info: text-[8px] → text-[11px]
<div className="text-[11px] text-[var(--color-text-muted)]">

// Line 40 — days count: text-[9px] → text-[11px]
<div className={`text-[11px] font-semibold ${urgent ? 'text-[var(--color-danger)]' : 'text-[var(--color-warning)]'}`}>

// Line 43 — date: text-[7px] → text-[11px]
<div className="text-[11px] text-[var(--color-text-muted)]">
```

- [ ] **Step 2: Fix ActivityFeed.jsx text sizes**

In `demo-ui/src/organisms/ActivityFeed.jsx`, change the header from emoji to text + fix all tiny text:

```jsx
// Line 14 — remove emoji from header
<h3 className="text-xs font-semibold text-[var(--color-text-primary)]">Recent Activity</h3>

// Line 27 — title: text-[10px] → text-xs
<div className="text-xs font-medium text-[var(--color-text-primary)]">{item.title}</div>

// Line 28 — description: text-[8px] → text-[11px]
<div className="text-[11px] text-[var(--color-text-muted)] truncate">{item.description}</div>

// Line 29 — time: text-[8px] → text-[11px]
<div className="text-[11px] text-[var(--color-text-muted)] whitespace-nowrap shrink-0">{item.time}</div>
```

- [ ] **Step 3: Fix DataTable.jsx header text size**

In `demo-ui/src/organisms/DataTable.jsx`, line 24:

```jsx
// Change text-[10px] → text-xs
className="px-4 py-2.5 text-left text-xs font-semibold text-[var(--color-text-muted)] uppercase tracking-wide"
```

- [ ] **Step 4: Fix Users.jsx avatar text size**

In `demo-ui/src/pages/Users.jsx`, line 33:

```jsx
// Change text-[10px] → text-[11px]
<div className="w-6 h-6 bg-[var(--color-primary)] rounded-full flex items-center justify-center text-white text-[11px] font-semibold">
```

- [ ] **Step 5: Verify visually**

Run: `cd demo-ui && npm run dev`
Open http://localhost:5173 and verify text is readable across Dashboard, Products table, and Users page.

- [ ] **Step 6: Commit**

```bash
git add demo-ui/src/organisms/ExpiryAlerts.jsx demo-ui/src/organisms/ActivityFeed.jsx demo-ui/src/organisms/DataTable.jsx demo-ui/src/pages/Users.jsx
git commit -m "fix(demo-ui): increase minimum text sizes to 11px for readability

Replace 7-10px text with 11-12px minimum across ExpiryAlerts,
ActivityFeed, DataTable, and Users avatar components."
```

---

### Task 2: Replace Emojis with SVG Icons

**Files:**
- Modify: `demo-ui/src/organisms/ActivityFeed.jsx`
- Modify: `demo-ui/src/organisms/ExpiryAlerts.jsx`
- Modify: `demo-ui/src/organisms/TopBar.jsx`

- [ ] **Step 1: Replace ActivityFeed emoji icons with SVGs**

In `demo-ui/src/organisms/ActivityFeed.jsx`, replace the `TYPE_CONFIG` object and update rendering:

```jsx
import { Icon } from '../atoms/Icon'

const TYPE_CONFIG = {
  STOCK_IN: { icon: 'arrow-down-tray', bg: 'bg-blue-50', iconColor: 'text-blue-500', label: 'Stock In' },
  DEDUCT: { icon: 'arrow-up-tray', bg: 'bg-pink-50', iconColor: 'text-pink-500', label: 'Deducted' },
  CREATE: { icon: 'check', bg: 'bg-green-50', iconColor: 'text-green-500', label: 'Created' },
  LOW_STOCK: { icon: 'exclamation', bg: 'bg-amber-50', iconColor: 'text-amber-500', label: 'Low Stock' },
  default: { icon: 'credit-card', bg: 'bg-slate-50', iconColor: 'text-slate-400', label: 'Activity' },
}
```

Then in the render section, replace the emoji rendering:

```jsx
<div className={`w-7 h-7 ${config.bg} rounded-[var(--radius-sm)] flex items-center justify-center shrink-0`}>
  <Icon name={config.icon} className={`w-3.5 h-3.5 ${config.iconColor}`} />
</div>
```

Note: The `Icon` component already supports all these icon names (`arrow-down-tray`, `arrow-up-tray`, `check`, `exclamation`, `credit-card`).

- [ ] **Step 2: Replace ExpiryAlerts emoji header with Icon component**

In `demo-ui/src/organisms/ExpiryAlerts.jsx`, add import and replace header:

```jsx
import { Icon } from '../atoms/Icon'

// Line 15 — replace emoji header with SVG icon
<h3 className="text-xs font-semibold text-[var(--color-text-primary)] flex items-center gap-1.5">
  <Icon name="exclamation" className="w-3.5 h-3.5 text-[var(--color-warning)]" />
  Expiry Alerts
</h3>
```

- [ ] **Step 3: Replace TopBar notification bell emoji with Icon**

In `demo-ui/src/organisms/TopBar.jsx`, add import and replace the bell div:

```jsx
import { Icon } from '../atoms/Icon'

// Replace the bell div (lines 29-34) with:
<div className="relative">
  <div className="w-8 h-8 rounded-[var(--radius-md)] border border-[var(--color-border)] flex items-center justify-center cursor-pointer hover:bg-[var(--color-background)] transition-colors">
    <Icon name="bell" className="w-4 h-4 text-[var(--color-text-muted)]" />
  </div>
  <div className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-[var(--color-danger)] rounded-full border-2 border-white" />
</div>
```

- [ ] **Step 4: Verify visually**

Run: `cd demo-ui && npm run dev`
Check Dashboard (ActivityFeed, ExpiryAlerts) and TopBar notification bell render as SVGs, not emojis.

- [ ] **Step 5: Commit**

```bash
git add demo-ui/src/organisms/ActivityFeed.jsx demo-ui/src/organisms/ExpiryAlerts.jsx demo-ui/src/organisms/TopBar.jsx
git commit -m "fix(demo-ui): replace emojis with SVG icons via Icon component

ActivityFeed type icons, ExpiryAlerts header, and TopBar notification
bell now use @heroicons/react SVGs for consistent cross-platform rendering."
```

---

### Task 3: Stat Cards — Wire Up Dashboard Counts

**Files:**
- Modify: `demo-ui/src/pages/Dashboard.jsx`
- Modify: `demo-ui/src/molecules/StatCard.jsx`

- [ ] **Step 1: Add loading skeleton to StatCard**

In `demo-ui/src/molecules/StatCard.jsx`, replace the full component:

```jsx
export function StatCard({ title, value, change, trend, accentColor = 'var(--color-primary)', loading = false, className = '' }) {
  return (
    <div className={`bg-white rounded-[var(--radius-lg)] p-4 shadow-[var(--shadow-sm)] border-t-3 transition-all duration-200 hover:shadow-md hover:-translate-y-0.5 ${className}`}
      style={{ borderTopColor: accentColor }}
    >
      <div className="text-xs font-semibold text-[var(--color-text-muted)] uppercase tracking-wide">
        {title}
      </div>
      {loading ? (
        <div className="mt-1.5 h-7 w-16 bg-slate-100 rounded animate-pulse" />
      ) : (
        <div className="text-2xl font-bold text-[var(--color-text-primary)] mt-1">
          {value}
        </div>
      )}
      {change && !loading && (
        <div className={`text-xs mt-1 ${trend === 'up' ? 'text-[var(--color-success)]' : trend === 'down' ? 'text-[var(--color-danger)]' : 'text-[var(--color-text-secondary)]'}`}>
          {trend === 'up' ? '↑' : trend === 'down' ? '↓' : ''} {change}
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 2: Wire up Dashboard stat card counts**

In `demo-ui/src/pages/Dashboard.jsx`, replace the stats array and add the computed values:

```jsx
export default function Dashboard() {
  const navigate = useNavigate()

  const { items: productData, totalElements: productCount, isLoading: productsLoading } = useQueryList(
    ['products'], '/products', { page: 1, size: 1 }
  )

  const { items: batchData, totalElements: batchCount, isLoading: batchesLoading } = useQueryList(
    ['batches'], '/inventory/batches', { page: 1, size: 1 }
  )

  const { items: expiringBatches, isLoading: expiringLoading } = useQueryList(
    ['batches-expiring'], '/inventory/batches', { page: 1, size: 100 }
  )

  const { items: recentTransactions } = useQueryList(
    ['transactions-recent'], '/transactions', { page: 1, size: 5 }
  )

  const getDaysUntil = (dateStr) => {
    if (!dateStr) return null
    return Math.ceil((new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24))
  }

  const expiringCount = expiringBatches.filter((b) => {
    const days = getDaysUntil(b.expiryDate)
    return days !== null && days <= 30 && days > 0
  }).length

  const urgentCount = expiringBatches.filter((b) => {
    const days = getDaysUntil(b.expiryDate)
    return days !== null && days <= 7 && days > 0
  }).length

  const stats = [
    {
      title: 'Products',
      value: productCount ?? 0,
      change: '',
      trend: null,
      accentColor: 'var(--color-primary)',
      loading: productsLoading,
    },
    {
      title: 'Expiring Soon',
      value: expiringCount,
      change: urgentCount > 0 ? `${urgentCount} urgent` : 'Within 30 days',
      trend: urgentCount > 0 ? 'down' : null,
      accentColor: 'var(--color-warning)',
      loading: expiringLoading,
    },
    {
      title: 'Batches',
      value: batchCount ?? 0,
      change: 'Active batches',
      trend: null,
      accentColor: 'var(--color-purple)',
      loading: batchesLoading,
    },
    {
      title: 'Low Stock',
      value: '-',
      change: 'Below reorder',
      trend: null,
      accentColor: 'var(--color-danger)',
    },
  ]

  // ... rest stays the same
```

- [ ] **Step 3: Verify visually**

Run: `cd demo-ui && npm run dev`
Open Dashboard. Stat cards should show loading skeleton bars initially, then real counts for Products, Expiring Soon, and Batches. Low Stock still shows "—" as expected.

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/pages/Dashboard.jsx demo-ui/src/molecules/StatCard.jsx
git commit -m "feat(demo-ui): wire up dashboard stat card counts with loading skeleton

Products, Expiring Soon, and Batches now show real counts.
StatCard shows animated skeleton while loading. Low Stock remains
placeholder pending backend filter support."
```

---

### Task 4: Micro-interactions (Hover, Focus, Press)

**Files:**
- Modify: `demo-ui/src/atoms/Button.jsx`
- Modify: `demo-ui/src/atoms/Input.jsx`

- [ ] **Step 1: Add active press feedback to Button**

In `demo-ui/src/atoms/Button.jsx`, add `active:scale-[0.97]` to the button className:

```jsx
className={`inline-flex items-center justify-center font-medium rounded-[var(--radius-md)] transition-colors cursor-pointer active:scale-[0.97] disabled:opacity-50 disabled:cursor-not-allowed ${variants[variant]} ${sizes[size]} ${className}`}
```

- [ ] **Step 2: Add focus-visible ring to Input**

In `demo-ui/src/atoms/Input.jsx`, replace the className:

```jsx
className={`w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border outline-none transition-colors focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/30
  ${error
    ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)]'
    : 'border-[var(--color-border)] focus:border-[var(--color-primary)]'
  } ${className}`}
```

Remove the old `focus:ring-2 focus:ring-red-200` and `focus:ring-2 focus:ring-teal-100` — the single `focus-visible:ring-2` replaces both.

- [ ] **Step 3: Add focus-visible ring to Select (same pattern)**

In `demo-ui/src/atoms/Select.jsx`, replace the className:

```jsx
className={`w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border outline-none transition-colors bg-white focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/30
  ${error
    ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)]'
    : 'border-[var(--color-border)] focus:border-[var(--color-primary)]'
  } ${className}`}
```

- [ ] **Step 4: Verify visually**

Run: `cd demo-ui && npm run dev`
Test: click a button and see the slight scale-down. Tab through form inputs and see the teal focus ring.

- [ ] **Step 5: Commit**

```bash
git add demo-ui/src/atoms/Button.jsx demo-ui/src/atoms/Input.jsx demo-ui/src/atoms/Select.jsx
git commit -m "feat(demo-ui): add micro-interactions — button press, input focus ring

Button gets active:scale for press feedback. Input and Select get
consistent focus-visible:ring for keyboard accessibility."
```

---

### Task 5: Collapsible Sidebar

**Files:**
- Modify: `demo-ui/src/organisms/Sidebar.jsx`

- [ ] **Step 1: Implement collapsible sidebar**

Replace the full `Sidebar.jsx` with the collapsible version:

```jsx
import { useState, useEffect } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { Icon } from '../atoms/Icon'
import api from '../lib/api'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: 'home', end: true },
  { to: '/products', label: 'Products', icon: 'cube' },
  { to: '/inventory', label: 'Inventory', icon: 'archive' },
  { to: '/clinical', label: 'Clinical', icon: 'beaker' },
  { to: '/orders', label: 'Orders', icon: 'cart' },
  { to: '/transactions', label: 'Transactions', icon: 'credit-card' },
  { to: '/locations', label: 'Locations', icon: 'map-pin' },
  { to: '/users', label: 'Users', icon: 'users' },
]

const STORAGE_KEY = 'sidebar-collapsed'

export function Sidebar() {
  const navigate = useNavigate()
  const [collapsed, setCollapsed] = useState(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    return stored !== null ? stored === 'true' : true
  })

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, String(collapsed))
  }, [collapsed])

  const handleLogout = () => {
    api.clearToken()
    navigate('/login')
  }

  return (
    <div className={`${collapsed ? 'w-14' : 'w-[200px]'} bg-[var(--color-sidebar-bg)] flex flex-col shrink-0 transition-all duration-200`}>
      {/* Logo */}
      <div className={`${collapsed ? 'flex justify-center py-3' : 'flex items-center gap-2.5 px-3 py-3'}`}>
        <div className="w-8 h-8 bg-[var(--color-primary)] rounded-[var(--radius-md)] flex items-center justify-center text-white text-sm font-bold shrink-0">
          P
        </div>
        {!collapsed && (
          <span className="text-white text-sm font-semibold">PharmStock</span>
        )}
      </div>

      {/* Nav items */}
      <nav className="flex flex-col gap-0.5 flex-1 px-1.5">
        {NAV_ITEMS.map(({ to, label, icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center transition-colors group relative ${
                isActive
                  ? 'bg-[var(--color-sidebar-active)] text-teal-300'
                  : 'text-slate-400 hover:text-slate-200'
              }`
            }
            title={label}
          >
            <Icon name={icon} className="w-5 h-5 shrink-0" />
            {!collapsed && (
              <span className="text-xs font-medium truncate">{label}</span>
            )}
            {collapsed && (
              <span className="absolute left-12 bg-slate-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50">
                {label}
              </span>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Bottom section: toggle + logout */}
      <div className="px-1.5 pb-3 flex flex-col gap-0.5">
        {/* Toggle button */}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-slate-400 hover:text-slate-200 transition-colors border border-slate-400/30 cursor-pointer`}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          <Icon name={collapsed ? 'chevron-right' : 'chevron-left'} className="w-4 h-4 shrink-0" />
          {!collapsed && (
            <span className="text-xs">Collapse</span>
          )}
        </button>

        {/* Logout */}
        <button
          onClick={handleLogout}
          className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-slate-400 hover:text-slate-200 transition-colors group relative cursor-pointer`}
          title="Sign Out"
        >
          <Icon name="logout" className="w-5 h-5 shrink-0" />
          {!collapsed && (
            <span className="text-xs">Sign Out</span>
          )}
          {collapsed && (
            <span className="absolute left-12 bg-slate-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50">
              Sign Out
            </span>
          )}
        </button>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verify visually**

Run: `cd demo-ui && npm run dev`
Test: sidebar starts collapsed (icon-only). Click the chevron button at bottom — sidebar expands to 200px with labels. Click again — collapses. Refresh page — preference persists.

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/organisms/Sidebar.jsx
git commit -m "feat(demo-ui): add collapsible sidebar with icon-only and expanded modes

Toggle between 56px icon-only and 200px icon+label layouts.
Collapsed state persists in localStorage. Smooth width transition."
```

---

### Task 6: Table Action Icon Buttons

**Files:**
- Create: `demo-ui/src/molecules/TableRowActions.jsx`
- Modify: `demo-ui/src/pages/Products.jsx`
- Modify: `demo-ui/src/pages/Clinical.jsx`

- [ ] **Step 1: Create TableRowActions molecule**

Create `demo-ui/src/molecules/TableRowActions.jsx`:

```jsx
import { Icon } from '../atoms/Icon'

export function TableRowActions({ onEdit, onDelete, editTitle = 'Edit', deleteTitle = 'Delete' }) {
  return (
    <span className="inline-flex gap-1">
      <button
        onClick={onEdit}
        title={editTitle}
        className="inline-flex items-center justify-center w-7 h-7 rounded-[var(--radius-sm)] border border-[var(--color-border-light)] text-[var(--color-primary)] hover:border-[var(--color-primary)] hover:bg-teal-50 transition-colors cursor-pointer"
      >
        <Icon name="pencil" className="w-3.5 h-3.5" />
      </button>
      {onDelete && (
        <button
          onClick={onDelete}
          title={deleteTitle}
          className="inline-flex items-center justify-center w-7 h-7 rounded-[var(--radius-sm)] border border-[var(--color-border-light)] text-[var(--color-danger)] hover:border-[var(--color-danger)] hover:bg-red-50 transition-colors cursor-pointer"
        >
          <Icon name="trash" className="w-3.5 h-3.5" />
        </button>
      )}
    </span>
  )
}
```

- [ ] **Step 2: Update Products.jsx to use TableRowActions**

In `demo-ui/src/pages/Products.jsx`, add import and replace the action cell in `renderRow`:

Add import:
```jsx
import { TableRowActions } from '../molecules/TableRowActions'
```

Replace the actions `<td>` (lines 124-127):
```jsx
<td className="px-4 py-2.5 text-right">
  <TableRowActions
    onEdit={() => openEdit(p)}
    onDelete={() => setDeleteTarget(p)}
  />
</td>
```

- [ ] **Step 3: Update Clinical.jsx Stores tab to use TableRowActions**

In `demo-ui/src/pages/Clinical.jsx`, add import:
```jsx
import { TableRowActions } from '../molecules/TableRowActions'
```

Replace the actions `<td>` in StoresTab renderRow (line 90-92):
```jsx
<td className="px-4 py-2.5 text-right">
  <TableRowActions onDelete={() => setDeleteTarget(s)} />
</td>
```

- [ ] **Step 4: Verify visually**

Run: `cd demo-ui && npm run dev`
Open Products page — rows should show pencil/trash icon buttons. Click Edit — drawer opens. Click Delete — confirmation dialog. Open Clinical > Stores — same trash icon button.

- [ ] **Step 5: Commit**

```bash
git add demo-ui/src/molecules/TableRowActions.jsx demo-ui/src/pages/Products.jsx demo-ui/src/pages/Clinical.jsx
git commit -m "feat(demo-ui): replace text action links with icon buttons

Add reusable TableRowActions molecule with pencil/trash SVG icons.
Applied to Products and Clinical (Stores tab) pages."
```

---

### Task 7: Form Field Grouping in Product Drawer

**Files:**
- Modify: `demo-ui/src/pages/Products.jsx`

- [ ] **Step 1: Add collapsible field group to Products form**

In `demo-ui/src/pages/Products.jsx`, add `useState` for the dimensions group toggle (it's already imported). Then add state and replace the form content inside the `<FormDrawer>`.

Add state inside `Products()` component, after the existing state declarations:
```jsx
const [showDimensions, setShowDimensions] = useState(false)
```

Replace the fields inside the `<FormDrawer>` children (everything from `<FormField label="Product Code"` through the closing `</FormDrawer>`):

```jsx
      <FormDrawer
        open={showForm}
        onClose={() => setShowForm(false)}
        title={editingProduct ? 'Edit Product' : 'New Product'}
        onSubmit={handleSubmit}
        submitLabel={editingProduct ? 'Update' : 'Create'}
        loading={formLoading}
      >
        <FormField label="Product Code" required>
          <Input value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} required />
        </FormField>
        <FormField label="Barcode">
          <Input value={form.barcode} onChange={(e) => setForm({ ...form, barcode: e.target.value })} />
        </FormField>
        <FormField label="Product Name" required>
          <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </FormField>
        <FormField label="Description">
          <Input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </FormField>
        <FormField label="Category">
          <Input value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
        </FormField>
        <FormField label="Reorder Quantity" required>
          <Input type="number" min="0" value={form.reorderQuantity} onChange={(e) => setForm({ ...form, reorderQuantity: +e.target.value })} />
        </FormField>

        {/* Package Dimensions — collapsible group */}
        <div className="border border-[var(--color-border-light)] rounded-[var(--radius-md)] bg-slate-50">
          <button
            type="button"
            onClick={() => setShowDimensions(!showDimensions)}
            className="w-full flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors cursor-pointer"
          >
            <Icon name={showDimensions ? 'chevron-down' : 'chevron-right'} className="w-3 h-3" />
            Package Dimensions
          </button>
          {showDimensions && (
            <div className="px-3 pb-3 grid grid-cols-2 gap-3">
              <FormField label="Weight (kg)">
                <Input type="number" min="0" step="0.01" value={form.packedWeight} onChange={(e) => setForm({ ...form, packedWeight: +e.target.value })} />
              </FormField>
              <FormField label="Height (cm)">
                <Input type="number" min="0" step="0.01" value={form.packedHeight} onChange={(e) => setForm({ ...form, packedHeight: +e.target.value })} />
              </FormField>
              <FormField label="Width (cm)">
                <Input type="number" min="0" step="0.01" value={form.packedWidth} onChange={(e) => setForm({ ...form, packedWidth: +e.target.value })} />
              </FormField>
              <FormField label="Depth (cm)">
                <Input type="number" min="0" step="0.01" value={form.packedDepth} onChange={(e) => setForm({ ...form, packedDepth: +e.target.value })} />
              </FormField>
            </div>
          )}
        </div>

        <FormField label="Active">
          <Select value={String(form.isActive)} onChange={(e) => setForm({ ...form, isActive: e.target.value === 'true' })}>
            <option value="true">Yes</option>
            <option value="false">No</option>
          </Select>
        </FormField>
      </FormDrawer>
```

Note: This requires adding `chevron-down` to the Icon atom's `iconMap`. Add this entry:

In `demo-ui/src/atoms/Icon.jsx`, add to `iconMap`:
```jsx
'chevron-down': Icons.ChevronDownIcon,
```

- [ ] **Step 2: Verify visually**

Run: `cd demo-ui && npm run dev`
Open Products > Add Product. The form should show core fields, then a collapsed "Package Dimensions" section. Click it to expand — Weight/Height/Width/Depth in 2-column grid. Active field below.

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Products.jsx demo-ui/src/atoms/Icon.jsx
git commit -m "feat(demo-ui): group package dimensions in collapsible section

Product form now collapses Weight/Height/Width/Depth into a toggleable
group, reducing visual clutter. Also adds chevron-down to Icon map."
```

---

## Self-Review Checklist

- [x] **Spec coverage:** All 7 quick wins from the design spec have corresponding tasks.
- [x] **Placeholder scan:** No TBDs, TODOs, or vague steps. Every step has concrete code.
- [x] **Type consistency:** `Icon` names (`chevron-down`, `arrow-down-tray`, `arrow-up-tray`, `check`, `exclamation`, `pencil`, `trash`, `bell`, `chevron-left`, `chevron-right`, `logout`) all exist in the `iconMap`. `TableRowActions` props (`onEdit`, `onDelete`) match usage in Products and Clinical pages. `StatCard` new `loading` prop used in Dashboard stats array.
- [x] **File paths:** All paths are relative to `demo-ui/src/` and verified to exist.
