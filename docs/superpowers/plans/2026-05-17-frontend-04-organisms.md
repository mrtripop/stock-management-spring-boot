# Organisms Rebuild Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Rebuild 4 existing organisms and create 3 new ones against the new design system tokens, atoms, and molecules.

**Architecture:** Each organism composes atoms/molecules from Plans 2-3 and uses semantic design tokens from Plan 1. Headless UI provides accessible Dialog/Transition primitives. All components are self-contained with no external state dependencies.

**Tech Stack:** React 19, Tailwind CSS, @headlessui/react, Heroicons, Storybook

**Depends on:** Plans 1 (tokens), 2 (atoms), 3 (molecules) must be completed first.

---

### Task 1: DataTable (rebuild)

**Files:**
- Modify: `demo-ui/src/organisms/DataTable.jsx`
- Modify: `demo-ui/src/organisms/DataTable.stories.jsx`

- [ ] **Step 1: Write DataTable component**

Replace `demo-ui/src/organisms/DataTable.jsx` with:

```jsx
import { useState, useCallback } from 'react'
import { Spinner } from '../atoms/Spinner'
import { Pagination } from '../molecules/Pagination'

const SKELETON_ROWS = 5

export function DataTable({
  columns,
  data = [],
  loading = false,
  emptyMessage = 'No data found',
  // Selection
  selectable = false,
  selectedKeys = [],
  keyField = 'id',
  onSelectionChange,
  // Sorting
  sortKey: externalSortKey,
  sortDir: externalSortDir = 'asc',
  onSort,
  // Pagination
  currentPage = 1,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
  className = '',
}) {
  const [internalSortKey, setInternalSortKey] = useState(null)
  const [internalSortDir, setInternalSortDir] = useState('asc')

  const activeSortKey = externalSortKey ?? internalSortKey
  const activeSortDir = externalSortDir ?? internalSortDir

  const isAllSelected = selectable && data.length > 0 && data.every((row) => selectedKeys.includes(row[keyField]))
  const isSomeSelected = selectable && !isAllSelected && data.some((row) => selectedKeys.includes(row[keyField]))

  const handleSort = useCallback(
    (col) => {
      if (!col.sortable) return
      const newDir = activeSortKey === col.key && activeSortDir === 'asc' ? 'desc' : 'asc'
      if (onSort) {
        onSort(col.key, newDir)
      } else {
        setInternalSortKey(col.key)
        setInternalSortDir(newDir)
      }
    },
    [activeSortKey, activeSortDir, onSort]
  )

  const toggleAll = useCallback(() => {
    if (!onSelectionChange) return
    if (isAllSelected) {
      onSelectionChange([])
    } else {
      onSelectionChange(data.map((row) => row[keyField]))
    }
  }, [data, keyField, isAllSelected, onSelectionChange])

  const toggleRow = useCallback(
    (row) => {
      if (!onSelectionChange) return
      const key = row[keyField]
      if (selectedKeys.includes(key)) {
        onSelectionChange(selectedKeys.filter((k) => k !== key))
      } else {
        onSelectionChange([...selectedKeys, key])
      }
    },
    [keyField, selectedKeys, onSelectionChange]
  )

  return (
    <div className={`bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] overflow-hidden border border-[var(--color-border)] ${className}`}>
      <div className="overflow-x-auto">
        <table className="w-full border-collapse min-w-[600px]">
          <thead>
            <tr className="bg-[var(--color-background)] border-b border-[var(--color-border)]">
              {selectable && (
                <th className="w-10 px-3 py-2.5">
                  <input
                    type="checkbox"
                    checked={isAllSelected}
                    ref={(el) => { if (el) el.indeterminate = isSomeSelected }}
                    onChange={toggleAll}
                    className="rounded border-[var(--color-border)] text-[var(--color-primary)] focus:ring-[var(--color-border-focus)] cursor-pointer"
                  />
                </th>
              )}
              {columns.map((col) => (
                <th
                  key={col.key}
                  className={`px-4 py-2.5 text-left text-xs font-semibold text-[var(--color-text-muted)] uppercase tracking-wide select-none ${col.sortable ? 'cursor-pointer hover:text-[var(--color-text-secondary)]' : ''}`}
                  style={{ width: col.width }}
                  onClick={() => handleSort(col)}
                >
                  <span className="inline-flex items-center gap-1">
                    {col.label}
                    {col.sortable && activeSortKey === col.key && (
                      <span className="text-[var(--color-primary)]">
                        {activeSortDir === 'asc' ? '↑' : '↓'}
                      </span>
                    )}
                  </span>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              Array.from({ length: SKELETON_ROWS }).map((_, i) => (
                <tr key={`skeleton-${i}`} className="border-b border-[var(--color-border)] last:border-0">
                  {selectable && (
                    <td className="px-3 py-3">
                      <div className="w-4 h-4 bg-[var(--color-border)] rounded animate-pulse" />
                    </td>
                  )}
                  {columns.map((col) => (
                    <td key={col.key} className="px-4 py-3">
                      <div className="h-4 bg-[var(--color-border)] rounded animate-pulse w-3/4" />
                    </td>
                  ))}
                </tr>
              ))
            ) : data.length === 0 ? (
              <tr>
                <td colSpan={columns.length + (selectable ? 1 : 0)} className="py-16 text-center">
                  <p className="text-sm text-[var(--color-text-muted)]">{emptyMessage}</p>
                </td>
              </tr>
            ) : (
              data.map((item) => (
                <tr
                  key={item[keyField]}
                  className={`border-b border-[var(--color-border)] last:border-0 hover:bg-[var(--color-background)] transition-colors ${
                    selectable && selectedKeys.includes(item[keyField]) ? 'bg-[var(--color-primary-subtle)]' : ''
                  }`}
                >
                  {selectable && (
                    <td className="px-3 py-3">
                      <input
                        type="checkbox"
                        checked={selectedKeys.includes(item[keyField])}
                        onChange={() => toggleRow(item)}
                        className="rounded border-[var(--color-border)] text-[var(--color-primary)] focus:ring-[var(--color-border-focus)] cursor-pointer"
                      />
                    </td>
                  )}
                  {columns.map((col) => (
                    <td key={col.key} className="px-4 py-3 text-sm text-[var(--color-text-primary)]">
                      {col.render ? col.render(item) : item[col.key]}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalElements={totalElements}
          pageSize={pageSize}
          onPageChange={onPageChange}
        />
      )}
    </div>
  )
}
```

- [ ] **Step 2: Write DataTable story**

Replace `demo-ui/src/organisms/DataTable.stories.jsx` with:

```jsx
import { useState } from 'react'
import { DataTable } from './DataTable'
import { Badge } from '../atoms/Badge'

const sampleColumns = [
  { key: 'name', label: 'Name', sortable: true },
  { key: 'category', label: 'Category', sortable: true },
  { key: 'status', label: 'Status', render: (row) => <Badge variant={row.status === 'Active' ? 'success' : 'danger'}>{row.status}</Badge> },
  { key: 'quantity', label: 'Qty', sortable: true },
]

const sampleData = Array.from({ length: 25 }, (_, i) => ({
  id: i + 1,
  name: `Product ${i + 1}`,
  category: ['Tablet', 'Capsule', 'Syrup'][i % 3],
  status: i % 3 === 0 ? 'Active' : 'Inactive',
  quantity: Math.floor(Math.random() * 500),
}))

export default {
  title: 'Organisms/DataTable',
  component: DataTable,
  argTypes: {
    onSort: { action: 'sort' },
    onPageChange: { action: 'pageChange' },
    onSelectionChange: { action: 'selectionChange' },
  },
}

export const Default = {
  args: {
    columns: sampleColumns,
    data: sampleData.slice(0, 10),
    currentPage: 1,
    totalPages: 3,
    totalElements: 25,
  },
}

export const Loading = {
  args: {
    columns: sampleColumns,
    data: [],
    loading: true,
  },
}

export const Empty = {
  args: {
    columns: sampleColumns,
    data: [],
    emptyMessage: 'No products found',
  },
}

export const Selectable = {
  render: (args) => {
    const [selected, setSelected] = useState([])
    return <DataTable {...args} selectedKeys={selected} onSelectionChange={setSelected} />
  },
  args: {
    columns: sampleColumns,
    data: sampleData.slice(0, 10),
    selectable: true,
  },
}

export const Sortable = {
  render: (args) => {
    const [sortKey, setSortKey] = useState(null)
    const [sortDir, setSortDir] = useState('asc')
    const sorted = [...sampleData.slice(0, 10)].sort((a, b) => {
      if (!sortKey) return 0
      const valA = a[sortKey]
      const valB = b[sortKey]
      return sortDir === 'asc' ? (valA > valB ? 1 : -1) : (valA < valB ? 1 : -1)
    })
    return <DataTable {...args} data={sorted} sortKey={sortKey} sortDir={sortDir} onSort={(k, d) => { setSortKey(k); setSortDir(d) }} />
  },
  args: {
    columns: sampleColumns,
  },
}
```

- [ ] **Step 3: Verify**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/organisms/DataTable.jsx demo-ui/src/organisms/DataTable.stories.jsx
git commit -m "feat(ui): rebuild DataTable with sorting, selection, skeleton loading"
```

---

### Task 2: FormDrawer (rebuild)

**Files:**
- Modify: `demo-ui/src/organisms/FormDrawer.jsx`
- Modify: `demo-ui/src/organisms/FormDrawer.stories.jsx`

- [ ] **Step 1: Write FormDrawer component**

Replace `demo-ui/src/organisms/FormDrawer.jsx` with:

```jsx
import { Fragment } from 'react'
import { Dialog, DialogPanel, DialogTitle, Transition, TransitionChild } from '@headlessui/react'
import { useFormContext } from 'react-hook-form'
import { Button } from '../atoms/Button'

const WIDTH_MAP = { sm: 'max-w-sm', md: 'max-w-md', lg: 'max-w-2xl' }

export function FormDrawer({ open, onClose, title, width = 'md', children, onSubmit, submitLabel = 'Save', loading = false, steps, currentStep = 0 }) {
  const hasSteps = steps && steps.length > 1
  const formContent = onSubmit ? (
    <form onSubmit={onSubmit} className="flex flex-col h-full">
      {hasSteps && (
        <div className="flex gap-1 px-5 pt-4">
          {steps.map((step, i) => (
            <div key={i} className="flex-1">
              <div className={`h-1 rounded-full transition-colors ${i <= currentStep ? 'bg-[var(--color-primary)]' : 'bg-[var(--color-border)]'}`} />
              {i <= currentStep && (
                <p className="text-xs text-[var(--color-text-muted)] mt-1 truncate">{step.title}</p>
              )}
            </div>
          ))}
        </div>
      )}
      <div className="flex-1 overflow-y-auto px-5 py-4">{children}</div>
      <div className="flex justify-end gap-2 px-5 py-3 border-t border-[var(--color-border)] bg-[var(--color-background)]">
        <Button variant="secondary" onClick={onClose} type="button">Cancel</Button>
        <Button type="submit" loading={loading}>{hasSteps && currentStep < steps.length - 1 ? 'Next' : submitLabel}</Button>
      </div>
    </form>
  ) : (
    <div className="flex-1 overflow-y-auto px-5 py-4">{children}</div>
  )

  return (
    <Transition show={open} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        <TransitionChild as={Fragment} enter="ease-in-out duration-300" enterFrom="opacity-0" enterTo="opacity-100" leave="ease-in-out duration-300" leaveFrom="opacity-100" leaveTo="opacity-0">
          <div className="fixed inset-0 bg-[var(--color-overlay)]" />
        </TransitionChild>
        <div className="fixed inset-0 overflow-hidden">
          <div className="absolute inset-0 overflow-hidden">
            <div className="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-10">
              <TransitionChild as={Fragment} enter="transform transition ease-in-out duration-300" enterFrom="translate-x-full" enterTo="translate-x-0" leave="transform transition ease-in-out duration-300" leaveFrom="translate-x-0" leaveTo="translate-x-full">
                <DialogPanel className={`pointer-events-auto w-screen ${WIDTH_MAP[width]} h-screen`}>
                  <div className="flex h-full flex-col bg-[var(--color-surface)] shadow-[var(--shadow-xl)]">
                    <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--color-border)]">
                      <DialogTitle className="text-sm font-semibold text-[var(--color-text-primary)]">{title}</DialogTitle>
                      <button onClick={onClose} className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] transition-colors cursor-pointer">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                      </button>
                    </div>
                    {formContent}
                  </div>
                </DialogPanel>
              </TransitionChild>
            </div>
          </div>
        </div>
      </Dialog>
    </Transition>
  )
}
```

- [ ] **Step 2: Write FormDrawer story**

Replace `demo-ui/src/organisms/FormDrawer.stories.jsx` with:

```jsx
import { useState } from 'react'
import { FormDrawer } from './FormDrawer'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Button } from '../atoms/Button'

export default {
  title: 'Organisms/FormDrawer',
  component: FormDrawer,
}

export const CreateForm = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button onClick={() => setOpen(true)}>Open Form</Button>
        <FormDrawer open={open} onClose={() => setOpen(false)} title="Create Product" onSubmit={(e) => { e.preventDefault(); setOpen(false) }}>
          <div className="space-y-4">
            <FormField label="Product Name" required>
              <Input placeholder="Enter product name" />
            </FormField>
            <FormField label="Code">
              <Input placeholder="e.g. PRD-001" />
            </FormField>
            <FormField label="Category">
              <Input placeholder="e.g. Tablet" />
            </FormField>
          </div>
        </FormDrawer>
      </>
    )
  },
}

export const MultiStepForm = {
  render: () => {
    const [open, setOpen] = useState(false)
    const [step, setStep] = useState(0)
    const steps = [{ title: 'Details' }, { title: 'Pricing' }, { title: 'Review' }]
    return (
      <>
        <Button onClick={() => { setOpen(true); setStep(0) }}>Open Multi-Step Form</Button>
        <FormDrawer open={open} onClose={() => setOpen(false)} title="Stock-In" onSubmit={(e) => { e.preventDefault(); if (step < 2) setStep(step + 1); else setOpen(false) }} steps={steps} currentStep={step}>
          {step === 0 && (
            <div className="space-y-4">
              <FormField label="Barcode"><Input placeholder="Scan or enter barcode" /></FormField>
              <FormField label="Batch Number"><Input placeholder="BN-2026-001" /></FormField>
            </div>
          )}
          {step === 1 && (
            <div className="space-y-4">
              <FormField label="Quantity"><Input placeholder="100" /></FormField>
              <FormField label="Expiry Date"><Input type="date" /></FormField>
            </div>
          )}
          {step === 2 && <p className="text-sm text-[var(--color-text-secondary)]">Review your stock-in details before submitting.</p>}
        </FormDrawer>
      </>
    )
  },
}

export const ViewOnly = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button onClick={() => setOpen(true)}>Open Details</Button>
        <FormDrawer open={open} onClose={() => setOpen(false)} title="Product Details">
          <div className="space-y-3 text-sm">
            <p><span className="text-[var(--color-text-muted)]">Name:</span> Amoxicillin 500mg</p>
            <p><span className="text-[var(--color-text-muted)]">Code:</span> PRD-001</p>
            <p><span className="text-[var(--color-text-muted)]">Category:</span> Tablet</p>
          </div>
        </FormDrawer>
      </>
    )
  },
}
```

- [ ] **Step 3: Verify**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/organisms/FormDrawer.jsx demo-ui/src/organisms/FormDrawer.stories.jsx
git commit -m "feat(ui): rebuild FormDrawer with multi-step support and width variants"
```

---

### Task 3: Sidebar (rebuild)

**Files:**
- Modify: `demo-ui/src/organisms/Sidebar.jsx`
- Modify: `demo-ui/src/organisms/Sidebar.stories.jsx`

- [ ] **Step 1: Write Sidebar component**

Replace `demo-ui/src/organisms/Sidebar.jsx` with:

```jsx
import { useState, useEffect } from 'react'
import { NavLink } from 'react-router-dom'
import { Icon } from '../atoms/Icon'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: 'home', end: true },
  { to: '/products', label: 'Products', icon: 'cube' },
  { to: '/inventory', label: 'Inventory', icon: 'archive' },
  { to: '/clinical', label: 'Clinical', icon: 'beaker' },
  { to: '/dispensing', label: 'Dispensing', icon: 'receipt' },
  { to: '/orders', label: 'Orders', icon: 'cart' },
  { to: '/transactions', label: 'Transactions', icon: 'credit-card' },
  { to: '/locations', label: 'Locations', icon: 'map-pin' },
  { to: '/users', label: 'Users', icon: 'users' },
]

const STORAGE_KEY = 'sidebar-collapsed'

export function Sidebar({
  collapsed: controlledCollapsed,
  onToggle,
  activeRoute,
  storeOptions = [],
  selectedStore,
  onStoreChange,
  user,
  onLogout,
  onToggleTheme,
  isDarkMode = false,
}) {
  const [internalCollapsed, setInternalCollapsed] = useState(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    return stored !== null ? stored === 'true' : true
  })

  const collapsed = controlledCollapsed ?? internalCollapsed

  const handleToggle = () => {
    if (onToggle) { onToggle() } else { setInternalCollapsed((c) => !c) }
  }

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, String(collapsed))
  }, [collapsed])

  return (
    <div className={`${collapsed ? 'w-14' : 'w-[200px]'} bg-[var(--color-sidebar-bg)] flex flex-col shrink-0 transition-all duration-200 h-screen`}>
      {/* Logo + Store selector */}
      <div className={collapsed ? 'flex justify-center py-3' : 'px-3 py-3'}>
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 bg-[var(--color-primary)] rounded-[var(--radius-md)] flex items-center justify-center text-white text-sm font-bold shrink-0">P</div>
          {!collapsed && <span className="text-white text-sm font-semibold">PharmStock</span>}
        </div>
      </div>

      {/* Store selector */}
      {storeOptions.length > 0 && !collapsed && (
        <div className="px-3 pb-2">
          <select
            value={selectedStore ?? ''}
            onChange={(e) => onStoreChange?.(e.target.value)}
            className="w-full bg-[var(--color-sidebar-bg)] border border-white/20 rounded-[var(--radius-md)] text-white text-xs px-2 py-1.5 focus:outline-none focus:border-[var(--color-primary)] cursor-pointer"
          >
            <option value="" disabled>Select store</option>
            {storeOptions.map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </div>
      )}

      {/* Nav items */}
      <nav className="flex flex-col gap-0.5 flex-1 px-1.5 mt-1">
        {NAV_ITEMS.map(({ to, label, icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center transition-colors group relative ${
                isActive ? 'bg-[var(--color-sidebar-active)] text-teal-300' : 'text-[var(--color-sidebar-text)] hover:text-[var(--color-sidebar-text-active)]'
              }`
            }
            title={label}
          >
            <Icon name={icon} className="w-5 h-5 shrink-0" />
            {!collapsed && <span className="text-xs font-medium truncate">{label}</span>}
            {collapsed && (
              <span className="absolute left-12 bg-slate-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50">{label}</span>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Bottom: user + theme + toggle */}
      <div className="px-1.5 pb-3 flex flex-col gap-0.5">
        {/* User info */}
        {!collapsed && user && (
          <div className="px-2.5 py-2 mb-1 rounded-[var(--radius-md)] bg-white/5">
            <p className="text-xs font-medium text-white truncate">{user.username}</p>
            <p className="text-xs text-[var(--color-sidebar-text)]">{user.role}</p>
          </div>
        )}

        {/* Theme toggle */}
        {onToggleTheme && (
          <button
            onClick={onToggleTheme}
            className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-[var(--color-sidebar-text)] hover:text-[var(--color-sidebar-text-active)] transition-colors cursor-pointer`}
            title={isDarkMode ? 'Switch to light mode' : 'Switch to dark mode'}
          >
            <Icon name={isDarkMode ? 'sun' : 'moon'} className="w-4 h-4 shrink-0" />
            {!collapsed && <span className="text-xs">{isDarkMode ? 'Light' : 'Dark'}</span>}
          </button>
        )}

        {/* Collapse toggle */}
        <button
          onClick={handleToggle}
          className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-[var(--color-sidebar-text)] hover:text-[var(--color-sidebar-text-active)] transition-colors border border-white/20 cursor-pointer`}
          title={collapsed ? 'Expand' : 'Collapse'}
        >
          <Icon name={collapsed ? 'chevron-right' : 'chevron-left'} className="w-4 h-4 shrink-0" />
          {!collapsed && <span className="text-xs">Collapse</span>}
        </button>

        {/* Logout */}
        {onLogout && (
          <button
            onClick={onLogout}
            className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-[var(--color-sidebar-text)] hover:text-[var(--color-sidebar-text-active)] transition-colors cursor-pointer`}
            title="Sign Out"
          >
            <Icon name="logout" className="w-5 h-5 shrink-0" />
            {!collapsed && <span className="text-xs">Sign Out</span>}
          </button>
        )}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Write Sidebar story**

Replace `demo-ui/src/organisms/Sidebar.stories.jsx` with:

```jsx
import { useState } from 'react'
import { MemoryRouter } from 'react-router-dom'
import { Sidebar } from './Sidebar'

const Wrapper = (props) => (
  <MemoryRouter><div className="h-screen"><Sidebar {...props} /></div></MemoryRouter>
)

export default {
  title: 'Organisms/Sidebar',
  component: Sidebar,
  decorators: [(Story) => <div className="h-[700px]"><Story /></div>],
}

export const Expanded = {
  render: () => <Wrapper collapsed={false} user={{ username: 'john', role: 'ADMIN' }} onLogout={() => {}} />,
}

export const Collapsed = {
  render: () => <Wrapper collapsed={true} user={{ username: 'john', role: 'PHARMACIST' }} onLogout={() => {}} />,
}

export const WithStoreSelector = {
  render: () => (
    <Wrapper
      collapsed={false}
      user={{ username: 'john', role: 'MANAGER' }}
      onLogout={() => {}}
      storeOptions={[
        { id: '1', name: 'Main Pharmacy' },
        { id: '2', name: 'Branch Pharmacy' },
        { id: '3', name: 'Warehouse Hub' },
      ]}
      selectedStore="1"
      onStoreChange={() => {}}
    />
  ),
}

export const WithThemeToggle = {
  render: () => {
    const [dark, setDark] = useState(false)
    return (
      <Wrapper
        collapsed={false}
        user={{ username: 'john', role: 'ADMIN' }}
        onLogout={() => {}}
        onToggleTheme={() => setDark(!dark)}
        isDarkMode={dark}
      />
    )
  },
}
```

- [ ] **Step 3: Verify**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/organisms/Sidebar.jsx demo-ui/src/organisms/Sidebar.stories.jsx
git commit -m "feat(ui): rebuild Sidebar with store selector, user menu, theme toggle"
```

---

### Task 4: AlertDialog (rebuild)

**Files:**
- Modify: `demo-ui/src/organisms/AlertDialog.jsx`
- Modify: `demo-ui/src/organisms/AlertDialog.stories.jsx`

- [ ] **Step 1: Write AlertDialog component**

Replace `demo-ui/src/organisms/AlertDialog.jsx` with:

```jsx
import { Fragment } from 'react'
import { Dialog, DialogPanel, DialogTitle, Transition, TransitionChild } from '@headlessui/react'
import { Button } from '../atoms/Button'
import { Icon } from '../atoms/Icon'

const VARIANTS = {
  info: { icon: 'info', bg: 'bg-[var(--color-info-subtle)]', text: 'text-[var(--color-info)]' },
  warning: { icon: 'exclamation', bg: 'bg-[var(--color-warning-subtle)]', text: 'text-[var(--color-warning)]' },
  danger: { icon: 'exclamation', bg: 'bg-[var(--color-danger-subtle)]', text: 'text-[var(--color-danger)]' },
}

export function AlertDialog({ open, onClose, title, message, variant = 'info', actions = [], loading = false }) {
  const v = VARIANTS[variant] || VARIANTS.info
  return (
    <Transition show={open} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        <TransitionChild as={Fragment} enter="ease-out duration-200" enterFrom="opacity-0" enterTo="opacity-100" leave="ease-in duration-150" leaveFrom="opacity-100" leaveTo="opacity-0">
          <div className="fixed inset-0 bg-[var(--color-overlay)]" />
        </TransitionChild>
        <div className="fixed inset-0 flex items-center justify-center p-4">
          <TransitionChild as={Fragment} enter="ease-out duration-200" enterFrom="opacity-0 scale-95" enterTo="opacity-100 scale-100" leave="ease-in duration-150" leaveFrom="opacity-100 scale-100" leaveTo="opacity-0 scale-95">
            <DialogPanel className="w-full max-w-sm bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-xl)] overflow-hidden">
              <div className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className={`w-10 h-10 ${v.bg} rounded-full flex items-center justify-center`}>
                    <Icon name={v.icon} className={`w-5 h-5 ${v.text}`} />
                  </div>
                  <DialogTitle className="text-sm font-semibold text-[var(--color-text-primary)]">{title}</DialogTitle>
                </div>
                <p className="text-sm text-[var(--color-text-secondary)] leading-relaxed">{message}</p>
              </div>
              <div className="flex justify-end gap-2 px-6 py-3 bg-[var(--color-background)] border-t border-[var(--color-border)]">
                <Button variant="secondary" onClick={onClose}>Cancel</Button>
                {actions.map((action, i) => (
                  <Button key={i} variant={action.variant || 'primary'} onClick={action.onClick} loading={loading}>{action.label}</Button>
                ))}
              </div>
            </DialogPanel>
          </TransitionChild>
        </div>
      </Dialog>
    </Transition>
  )
}
```

- [ ] **Step 2: Write AlertDialog story**

Replace `demo-ui/src/organisms/AlertDialog.stories.jsx` with:

```jsx
import { useState } from 'react'
import { AlertDialog } from './AlertDialog'
import { Button } from '../atoms/Button'

export default {
  title: 'Organisms/AlertDialog',
  component: AlertDialog,
}

export const Info = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button onClick={() => setOpen(true)}>Show Info</Button>
        <AlertDialog open={open} onClose={() => setOpen(false)} title="Information" message="This action will trigger a full inventory scan. This may take a few minutes." variant="info" actions={[{ label: 'Run Scan', onClick: () => setOpen(false) }]} />
      </>
    )
  },
}

export const Warning = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button variant="secondary" onClick={() => setOpen(true)}>Show Warning</Button>
        <AlertDialog open={open} onClose={() => setOpen(false)} title="Low Stock Warning" message="Product Amoxicillin 500mg is below reorder threshold. Current stock: 5, Reorder at: 20." variant="warning" actions={[{ label: 'Acknowledge', variant: 'primary', onClick: () => setOpen(false) }]} />
      </>
    )
  },
}

export const Danger = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button variant="danger" onClick={() => setOpen(true)}>Delete Product</Button>
        <AlertDialog open={open} onClose={() => setOpen(false)} title="Delete Product" message="Are you sure you want to delete Amoxicillin 500mg? This action cannot be undone." variant="danger" actions={[{ label: 'Delete', variant: 'danger', onClick: () => setOpen(false) }]} />
      </>
    )
  },
}
```

- [ ] **Step 3: Verify**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/organisms/AlertDialog.jsx demo-ui/src/organisms/AlertDialog.stories.jsx
git commit -m "feat(ui): rebuild AlertDialog with variant colors and action slots"
```

---

### Task 5: TopBar (new)

**Files:**
- Create: `demo-ui/src/organisms/TopBar.jsx`
- Create: `demo-ui/src/organisms/TopBar.stories.jsx`

- [ ] **Step 1: Write TopBar component**

Create `demo-ui/src/organisms/TopBar.jsx`:

```jsx
import { Icon } from '../atoms/Icon'
import { Badge } from '../atoms/Badge'

export function TopBar({ breadcrumb = [], onSearchClick, notificationCount = 0, userAvatar }) {
  return (
    <header className="h-14 bg-[var(--color-surface)] border-b border-[var(--color-border)] flex items-center justify-between px-6 shrink-0">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-1 text-sm">
        {breadcrumb.map((item, i) => (
          <span key={i} className="flex items-center gap-1">
            {i > 0 && <Icon name="chevron-right" className="w-3 h-3 text-[var(--color-text-muted)]" />}
            {item.to ? (
              <a href={item.to} className="text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] transition-colors">{item.label}</a>
            ) : (
              <span className="text-[var(--color-text-primary)] font-medium">{item.label}</span>
            )}
          </span>
        ))}
      </nav>

      {/* Right actions */}
      <div className="flex items-center gap-3">
        {onSearchClick && (
          <button onClick={onSearchClick} className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] transition-colors cursor-pointer" title="Search">
            <Icon name="search" className="w-5 h-5" />
          </button>
        )}
        <button className="relative text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] transition-colors cursor-pointer" title="Notifications">
          <Icon name="bell" className="w-5 h-5" />
          {notificationCount > 0 && (
            <span className="absolute -top-1 -right-1 w-4 h-4 bg-[var(--color-danger)] text-white text-[10px] font-bold rounded-full flex items-center justify-center">
              {notificationCount > 9 ? '9+' : notificationCount}
            </span>
          )}
        </button>
        <div className="w-8 h-8 rounded-full bg-[var(--color-primary)] flex items-center justify-center text-white text-xs font-bold cursor-pointer" title={userAvatar}>
          {userAvatar || 'U'}
        </div>
      </div>
    </header>
  )
}
```

- [ ] **Step 2: Write TopBar story**

Create `demo-ui/src/organisms/TopBar.stories.jsx`:

```jsx
import { TopBar } from './TopBar'

export default {
  title: 'Organisms/TopBar',
  component: TopBar,
}

export const Default = {
  args: {
    breadcrumb: [{ label: 'Dashboard' }],
    notificationCount: 3,
    userAvatar: 'JD',
  },
}

export const WithBreadcrumb = {
  args: {
    breadcrumb: [{ label: 'Clinical', to: '/clinical' }, { label: 'Molecules' }],
    notificationCount: 0,
    userAvatar: 'PS',
  },
}

export const WithNotifications = {
  args: {
    breadcrumb: [{ label: 'Inventory' }],
    notificationCount: 12,
    userAvatar: 'AB',
  },
}
```

- [ ] **Step 3: Verify**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/organisms/TopBar.jsx demo-ui/src/organisms/TopBar.stories.jsx
git commit -m "feat(ui): add TopBar organism with breadcrumb and notification bell"
```

---

### Task 6: ActivityFeed (new)

**Files:**
- Create: `demo-ui/src/organisms/ActivityFeed.jsx`
- Create: `demo-ui/src/organisms/ActivityFeed.stories.jsx`

- [ ] **Step 1: Write ActivityFeed component**

Create `demo-ui/src/organisms/ActivityFeed.jsx`:

```jsx
import { Icon } from '../atoms/Icon'
import { Spinner } from '../atoms/Spinner'

const TYPE_ICONS = {
  transaction: 'credit-card',
  task: 'exclamation',
  alert: 'bell',
  stock: 'archive',
  default: 'info',
}

export function ActivityFeed({ items = [], loading = false }) {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Spinner size="md" />
      </div>
    )
  }

  if (items.length === 0) {
    return <p className="text-sm text-[var(--color-text-muted)] text-center py-8">No recent activity</p>
  }

  return (
    <div className="space-y-0">
      {items.map((item) => (
        <div key={item.id} className="flex gap-3 py-3 border-b border-[var(--color-border)] last:border-0">
          <div className="w-8 h-8 rounded-full bg-[var(--color-background)] flex items-center justify-center shrink-0">
            <Icon name={TYPE_ICONS[item.type] || TYPE_ICONS.default} className="w-4 h-4 text-[var(--color-text-muted)]" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm text-[var(--color-text-primary)] truncate">{item.message}</p>
            <p className="text-xs text-[var(--color-text-muted)]">{item.timestamp}</p>
          </div>
        </div>
      ))}
    </div>
  )
}
```

- [ ] **Step 2: Write ActivityFeed story**

Create `demo-ui/src/organisms/ActivityFeed.stories.jsx`:

```jsx
import { ActivityFeed } from './ActivityFeed'

const sampleItems = [
  { id: 1, type: 'transaction', message: 'Invoice #42 completed — Amoxicillin 500mg x10', timestamp: '2 minutes ago' },
  { id: 2, type: 'task', message: 'Expiry warning: Paracetamol 500mg expires in 5 days', timestamp: '15 minutes ago' },
  { id: 3, type: 'stock', message: 'Stock-in: Ibuprofen 200mg — 200 units added', timestamp: '1 hour ago' },
  { id: 4, type: 'alert', message: 'Batch BN-2026-003 recalled by manufacturer', timestamp: '2 hours ago' },
  { id: 5, type: 'transaction', message: 'Invoice #41 voided by admin', timestamp: '3 hours ago' },
]

export default {
  title: 'Organisms/ActivityFeed',
  component: ActivityFeed,
}

export const Default = { args: { items: sampleItems } }
export const Loading = { args: { loading: true } }
export const Empty = { args: { items: [] } }
```

- [ ] **Step 3: Verify**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/organisms/ActivityFeed.jsx demo-ui/src/organisms/ActivityFeed.stories.jsx
git commit -m "feat(ui): add ActivityFeed organism for dashboard timeline"
```

---

### Task 7: ExpiryAlerts (new)

**Files:**
- Create: `demo-ui/src/organisms/ExpiryAlerts.jsx`
- Create: `demo-ui/src/organisms/ExpiryAlerts.stories.jsx`

- [ ] **Step 1: Write ExpiryAlerts component**

Create `demo-ui/src/organisms/ExpiryAlerts.jsx`:

```jsx
import { Icon } from '../atoms/Icon'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { Spinner } from '../atoms/Spinner'

function getUrgency(task) {
  if (task.taskType === 'RECALL_ALERT') return 'critical'
  if (task.taskType === 'REORDER_NEEDED') return 'warning'
  if (task.daysUntilExpiry != null && task.daysUntilExpiry < 7) return 'critical'
  if (task.daysUntilExpiry != null && task.daysUntilExpiry < 30) return 'warning'
  return 'default'
}

const URGENCY_STYLES = {
  critical: { border: 'border-l-[var(--color-danger)]', bg: 'bg-[var(--color-danger-subtle)]', badge: 'danger' },
  warning: { border: 'border-l-[var(--color-warning)]', bg: 'bg-[var(--color-warning-subtle)]', badge: 'warning' },
  default: { border: 'border-l-[var(--color-info)]', bg: 'bg-[var(--color-info-subtle)]', badge: 'info' },
}

export function ExpiryAlerts({ tasks = [], onAcknowledge, onResolve, loading = false }) {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Spinner size="md" />
      </div>
    )
  }

  if (tasks.length === 0) {
    return <p className="text-sm text-[var(--color-text-muted)] text-center py-8">No active alerts</p>
  }

  const sorted = [...tasks].sort((a, b) => {
    const order = { critical: 0, warning: 1, default: 2 }
    return (order[getUrgency(a)] ?? 2) - (order[getUrgency(b)] ?? 2)
  })

  return (
    <div className="space-y-2">
      {sorted.map((task) => {
        const urgency = getUrgency(task)
        const style = URGENCY_STYLES[urgency]
        return (
          <div key={task.id} className={`flex items-center gap-3 p-3 rounded-[var(--radius-md)] border-l-4 ${style.border} ${style.bg}`}>
            <Icon name={task.taskType === 'RECALL_ALERT' ? 'exclamation' : 'clock'} className="w-5 h-5 shrink-0" />
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <p className="text-sm font-medium text-[var(--color-text-primary)] truncate">{task.brandName}</p>
                <Badge variant={style.badge}>{task.taskType?.replace('_', ' ')}</Badge>
              </div>
              <p className="text-xs text-[var(--color-text-secondary)] mt-0.5">
                Batch {task.batchNumber}
                {task.daysUntilExpiry != null && ` — ${task.daysUntilExpiry} days until expiry`}
                {task.currentQuantity != null && ` — Qty: ${task.currentQuantity}`}
              </p>
            </div>
            <div className="flex items-center gap-1.5 shrink-0">
              {task.status === 'PENDING' && onAcknowledge && (
                <Button size="sm" variant="secondary" onClick={() => onAcknowledge(task.id)}>Ack</Button>
              )}
              {task.status === 'ACKNOWLEDGED' && onResolve && (
                <Button size="sm" variant="primary" onClick={() => onResolve(task.id)}>Resolve</Button>
              )}
              {task.status === 'RESOLVED' && (
                <Badge variant="success">Resolved</Badge>
              )}
            </div>
          </div>
        )
      })}
    </div>
  )
}
```

- [ ] **Step 2: Write ExpiryAlerts story**

Create `demo-ui/src/organisms/ExpiryAlerts.stories.jsx`:

```jsx
import { ExpiryAlerts } from './ExpiryAlerts'

const sampleTasks = [
  { id: 1, taskType: 'EXPIRY_WARNING', brandName: 'Amoxicillin 500mg', batchNumber: 'BN-001', daysUntilExpiry: 3, currentQuantity: 50, status: 'PENDING' },
  { id: 2, taskType: 'RECALL_ALERT', brandName: 'Paracetamol 250mg', batchNumber: 'BN-045', currentQuantity: 200, status: 'PENDING' },
  { id: 3, taskType: 'EXPIRY_WARNING', brandName: 'Ibuprofen 200mg', batchNumber: 'BN-012', daysUntilExpiry: 18, currentQuantity: 100, status: 'ACKNOWLEDGED' },
  { id: 4, taskType: 'REORDER_NEEDED', brandName: 'Cetirizine 10mg', batchNumber: 'BN-078', currentQuantity: 5, status: 'PENDING' },
  { id: 5, taskType: 'EXPIRY_WARNING', brandName: 'Omeprazole 20mg', batchNumber: 'BN-023', daysUntilExpiry: 60, currentQuantity: 75, status: 'RESOLVED' },
]

export default {
  title: 'Organisms/ExpiryAlerts',
  component: ExpiryAlerts,
}

export const MixedAlerts = {
  args: {
    tasks: sampleTasks,
    onAcknowledge: (id) => console.log('Acknowledge', id),
    onResolve: (id) => console.log('Resolve', id),
  },
}

export const Loading = { args: { loading: true } }
export const Empty = { args: { tasks: [] } }
```

- [ ] **Step 3: Verify**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/organisms/ExpiryAlerts.jsx demo-ui/src/organisms/ExpiryAlerts.stories.jsx
git commit -m "feat(ui): add ExpiryAlerts organism with priority sorting and actions"
```

---

### Task 8: Final verification

- [ ] **Step 1: Run full build**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds with no errors

- [ ] **Step 2: Run Storybook**

Run: `cd demo-ui && npm run storybook`
Expected: All 7 organism stories render correctly — DataTable, FormDrawer, Sidebar, AlertDialog, TopBar, ActivityFeed, ExpiryAlerts

- [ ] **Step 3: Commit any remaining fixes**

```bash
git add -u demo-ui/src/organisms/
git commit -m "feat(ui): complete organisms rebuild — all 7 components with stories"
```
