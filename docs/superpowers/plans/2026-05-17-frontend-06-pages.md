# Pages & Routing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Rebuild all 13 page files and routing against the new design system components and API hooks.

**Architecture:** Each page uses domain hooks from Plan 5 for data fetching, rebuilt atoms/molecules/organisms from Plans 2-4 for UI, React Hook Form + Zod for forms, and semantic design tokens from Plan 1. AdminLayout wraps authenticated routes with Sidebar + TopBar.

**Tech Stack:** React 19, Tailwind CSS, React Hook Form, Zod, React Router 7, React Query 5, Heroicons

**Depends on:** Plans 1-5 must be completed first.

---

### Task 1: App.jsx — Routing with Auth

**Files:**
- Modify: `demo-ui/src/App.jsx`

- [ ] **Step 1: Write App.jsx**

Replace `demo-ui/src/App/App.jsx` with:

```jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'sonner'
import { AuthProvider, useAuth } from './lib/auth'
import AdminLayout from './templates/AdminLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Products from './pages/Products'
import Inventory from './pages/Inventory'
import Clinical from './pages/Clinical'
import StoreProducts from './pages/StoreProducts'
import Dispensing from './pages/Dispensing'
import Orders from './pages/Orders'
import Transactions from './pages/Transactions'
import Locations from './pages/Locations'
import Users from './pages/Users'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false },
    mutations: { retry: 0 },
  },
})

function ProtectedRoute({ children }) {
  const { token } = useAuth()
  if (!token) return <Navigate to="/login" />
  return children
}

export default function App() {
  return (
    <AuthProvider>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<ProtectedRoute><AdminLayout /></ProtectedRoute>}>
              <Route index element={<Dashboard />} />
              <Route path="products" element={<Products />} />
              <Route path="inventory" element={<Inventory />} />
              <Route path="clinical" element={<Clinical />} />
              <Route path="clinical/stores/:storeId/products" element={<StoreProducts />} />
              <Route path="dispensing" element={<Dispensing />} />
              <Route path="orders" element={<Orders />} />
              <Route path="transactions" element={<Transactions />} />
              <Route path="locations" element={<Locations />} />
              <Route path="users" element={<Users />} />
            </Route>
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </BrowserRouter>
        <Toaster position="top-right" richColors closeButton />
      </QueryClientProvider>
    </AuthProvider>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/App.jsx
git commit -m "feat(ui): add AuthProvider wrapper and StoreProducts route"
```

---

### Task 2: AdminLayout — Sidebar + TopBar + Store Gate

**Files:**
- Modify: `demo-ui/src/templates/AdminLayout.jsx`

- [ ] **Step 1: Write AdminLayout**

```jsx
import { Outlet, useLocation, useParams } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { Sidebar } from '../organisms/Sidebar'
import { TopBar } from '../organisms/TopBar'
import { useAuth, useStoreId } from '../lib/auth'

const ROUTE_TITLES = {
  '/': 'Dashboard',
  '/products': 'Products',
  '/inventory': 'Inventory',
  '/clinical': 'Clinical',
  '/dispensing': 'Dispensing',
  '/orders': 'Orders',
  '/transactions': 'Transactions',
  '/locations': 'Locations',
  '/users': 'Users',
}

function buildBreadcrumb(pathname) {
  const segments = pathname.split('/').filter(Boolean)
  const crumbs = [{ label: 'Home', to: '/' }]
  let path = ''
  for (const seg of segments) {
    path += `/${seg}`
    const title = ROUTE_TITLES[path] || seg
    crumbs.push({ label: title.charAt(0).toUpperCase() + title.slice(1) })
    if (crumbs.length > 1) crumbs[crumbs.length - 2].to = path
  }
  return crumbs
}

export default function AdminLayout() {
  const location = useLocation()
  const { user, logout, storeOptions, storeId, selectStore } = useAuth()
  const [isDark, setIsDark] = useState(() => localStorage.getItem('theme') === 'dark')

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light')
    localStorage.setItem('theme', isDark ? 'dark' : 'light')
  }, [isDark])

  const breadcrumb = buildBreadcrumb(location.pathname)

  return (
    <div className="flex h-screen bg-[var(--color-background)]">
      <Sidebar
        user={user ? { username: user.username, role: user.role } : null}
        storeOptions={storeOptions}
        selectedStore={storeId}
        onStoreChange={selectStore}
        onLogout={logout}
        onToggleTheme={() => setIsDark(!isDark)}
        isDarkMode={isDark}
      />
      <div className="flex-1 flex flex-col min-w-0">
        <TopBar breadcrumb={breadcrumb} notificationCount={0} userAvatar={user?.username?.[0]?.toUpperCase()} />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/templates/AdminLayout.jsx
git commit -m "feat(ui): rebuild AdminLayout with new Sidebar, TopBar, theme toggle"
```

---

### Task 3: Login Page

**Files:**
- Modify: `demo-ui/src/pages/Login.jsx`

- [ ] **Step 1: Write Login page**

```jsx
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useAuth } from '../lib/auth'
import { Button } from '../atoms/Button'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'

const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

const mfaSchema = z.object({
  totpCode: z.string().length(6, 'Code must be 6 digits'),
})

export default function Login() {
  const { login, verifyMfa, error: authError, loading } = useAuth()
  const [mfaRequired, setMfaRequired] = useState(false)
  const [tempToken, setTempToken] = useState(null)

  const loginForm = useForm({ resolver: zodResolver(loginSchema), defaultValues: { username: localStorage.getItem('remembered_username') || '', password: '' } })
  const mfaForm = useForm({ resolver: zodResolver(mfaSchema), defaultValues: { totpCode: '' } })

  const handleLogin = async (data) => {
    try {
      const result = await login(data.username, data.password)
      if (result?.mfaRequired) {
        setMfaRequired(true)
        setTempToken(result.tempToken)
      }
    } catch {}
  }

  const handleMfa = async (data) => {
    try {
      await verifyMfa(tempToken, data.totpCode)
    } catch {}
  }

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex items-center justify-center p-4">
      <div className="w-full max-w-sm">
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-xl)] shadow-[var(--shadow-lg)] p-8">
          <div className="flex justify-center mb-6">
            <div className="w-12 h-12 bg-[var(--color-primary)] rounded-[var(--radius-lg)] flex items-center justify-center text-white text-xl font-bold">P</div>
          </div>
          <h1 className="text-xl font-semibold text-[var(--color-text-primary)] text-center mb-1">PharmStock</h1>
          <p className="text-sm text-[var(--color-text-muted)] text-center mb-6">Sign in to your account</p>

          {authError && <div className="bg-[var(--color-danger-subtle)] text-[var(--color-danger-text)] text-sm rounded-[var(--radius-md)] px-3 py-2 mb-4">{authError}</div>}

          {!mfaRequired ? (
            <form onSubmit={loginForm.handleSubmit(handleLogin)} className="space-y-4">
              <FormField label="Username" error={loginForm.formState.errors.username?.message} required>
                <Input {...loginForm.register('username')} placeholder="Enter username" />
              </FormField>
              <FormField label="Password" error={loginForm.formState.errors.password?.message} required>
                <Input {...loginForm.register('password')} type="password" placeholder="Enter password" />
              </FormField>
              <label className="flex items-center gap-2 text-sm text-[var(--color-text-secondary)]">
                <input type="checkbox" defaultChecked={!!localStorage.getItem('remembered_username')} onChange={(e) => { if (!e.target.checked) localStorage.removeItem('remembered_username') }} className="rounded" />
                Remember username
              </label>
              <Button type="submit" loading={loading} fullWidth>Sign in</Button>
            </form>
          ) : (
            <form onSubmit={mfaForm.handleSubmit(handleMfa)} className="space-y-4">
              <p className="text-sm text-[var(--color-text-secondary)] text-center">Enter the 6-digit code from your authenticator app</p>
              <FormField label="TOTP Code" error={mfaForm.formState.errors.totpCode?.message} required>
                <Input {...mfaForm.register('totpCode')} placeholder="000000" maxLength={6} autoComplete="one-time-code" />
              </FormField>
              <Button type="submit" loading={loading} fullWidth>Verify</Button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Login.jsx
git commit -m "feat(ui): rebuild Login with RHF+Zod validation and MFA flow"
```

---

### Task 4: Dashboard Page

**Files:**
- Modify: `demo-ui/src/pages/Dashboard.jsx`

- [ ] **Step 1: Write Dashboard page**

```jsx
import { useNavigate } from 'react-router-dom'
import { useStoreId } from '../lib/auth'
import { useQueryList } from '../lib/hooks/useProducts'
import { useQueryList as useBatchList } from '../lib/hooks/useInventory'
import { useDailySummary } from '../lib/hooks/useTransactions'
import { StatCard } from '../molecules/StatCard'
import { ActivityFeed } from '../organisms/ActivityFeed'
import { ExpiryAlerts } from '../organisms/ExpiryAlerts'
import { Button } from '../atoms/Button'
import { Icon } from '../atoms/Icon'

const QUICK_ACTIONS = [
  { label: 'Stock In', icon: 'archive', to: '/inventory', color: 'text-teal-600 bg-teal-50' },
  { label: 'Dispense', icon: 'receipt', to: '/dispensing', color: 'text-blue-600 bg-blue-50' },
  { label: 'Search', icon: 'search', to: '/products', color: 'text-purple-600 bg-purple-50' },
  { label: 'Reports', icon: 'chart', to: '/transactions', color: 'text-amber-600 bg-amber-50' },
]

export default function Dashboard() {
  const navigate = useNavigate()
  const storeId = useStoreId()
  const { totalElements: productCount } = useQueryList(['products'], '/products', { size: 1 })
  const { totalElements: batchCount } = useQueryList(['batches'], '/inventory/batches', { size: 1 })
  const { data: summary } = useDailySummary({ storeId })
  const { items: taskItems } = useQueryList(['tasks'], '/inventory/tasks', { status: 'PENDING', size: 10 })
  const { items: recentTx } = useQueryList(['transactions'], '/transactions', { size: 10, orderBy: 'DESC' })

  const feedItems = (recentTx || []).map((tx) => ({
    id: tx.id,
    type: 'transaction',
    message: `Transaction #${tx.id}`,
    timestamp: new Date(tx.createdAt).toLocaleString(),
  }))

  return (
    <div className="space-y-6">
      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Products" value={productCount ?? 0} icon="cube" />
        <StatCard label="Active Batches" value={batchCount ?? 0} icon="archive" />
        <StatCard label="Today's Revenue" value={`$${(summary?.totalRevenue ?? 0).toFixed(2)}`} icon="credit-card" variant="success" />
        <StatCard label="Items Dispensed" value={summary?.totalItemsDispensed ?? 0} icon="receipt" />
      </div>

      {/* Alerts + Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] p-5">
          <h3 className="text-sm font-semibold text-[var(--color-text-primary)] mb-4">Active Alerts</h3>
          <ExpiryAlerts tasks={taskItems || []} onAcknowledge={() => {}} onResolve={() => {}} />
        </div>
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] p-5">
          <h3 className="text-sm font-semibold text-[var(--color-text-primary)] mb-4">Recent Activity</h3>
          <ActivityFeed items={feedItems} />
        </div>
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {QUICK_ACTIONS.map((action) => (
          <button key={action.label} onClick={() => navigate(action.to)} className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] p-4 flex flex-col items-center gap-2 hover:border-[var(--color-primary)] transition-colors cursor-pointer">
            <div className={`w-10 h-10 rounded-full ${action.color} flex items-center justify-center`}>
              <Icon name={action.icon} className="w-5 h-5" />
            </div>
            <span className="text-sm font-medium text-[var(--color-text-primary)]">{action.label}</span>
          </button>
        ))}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Dashboard.jsx
git commit -m "feat(ui): rebuild Dashboard with stat cards, alerts, activity feed"
```

---

### Task 5: Products Page

**Files:**
- Modify: `demo-ui/src/pages/Products.jsx`

- [ ] **Step 1: Write Products page**

```jsx
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { PageHeader } from '../molecules/PageHeader'
import { SearchBar } from '../molecules/SearchBar'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useProductList, useCreateProduct, useUpdateProduct, useDeleteProduct } from '../lib/hooks/useProducts'

const productSchema = z.object({
  code: z.string().min(1, 'Code is required'),
  barcode: z.string().min(1, 'Barcode is required'),
  name: z.string().min(1, 'Name is required'),
  description: z.string().max(300, 'Max 300 characters'),
  category: z.string().min(1, 'Category is required'),
  reorderQuantity: z.number().min(0, 'Must be >= 0'),
  packedWeight: z.number().min(0),
  packedHeight: z.number().min(0),
  packedWidth: z.number().min(0),
  packedDepth: z.number().min(0),
  isActive: z.boolean(),
})

const columns = [
  { key: 'code', label: 'Code', sortable: true },
  { key: 'name', label: 'Name', sortable: true },
  { key: 'category', label: 'Category', sortable: true },
  { key: 'isActive', label: 'Status', render: (row) => <Badge variant={row.isActive ? 'success' : 'neutral'}>{row.isActive ? 'Active' : 'Inactive'}</Badge> },
]

export default function Products() {
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editing, setEditing] = useState(null)

  const { items, totalPages, totalElements, loading } = useProductList({ page, size: 20 })
  const createProduct = useCreateProduct()
  const updateProduct = useUpdateProduct()
  const deleteProduct = useDeleteProduct()

  const form = useForm({ resolver: zodResolver(productSchema), defaultValues: { isActive: true, reorderQuantity: 0, packedWeight: 0, packedHeight: 0, packedWidth: 0, packedDepth: 0 } })

  const filtered = (items || []).filter((p) => !search || p.name?.toLowerCase().includes(search.toLowerCase()) || p.code?.toLowerCase().includes(search.toLowerCase()))

  const openCreate = () => { setEditing(null); form.reset({ isActive: true, reorderQuantity: 0, packedWeight: 0, packedHeight: 0, packedWidth: 0, packedDepth: 0 }); setDrawerOpen(true) }
  const openEdit = (row) => { setEditing(row); form.reset(row); setDrawerOpen(true) }

  const onSubmit = async (data) => {
    if (editing) { await updateProduct.mutateAsync({ id: editing.id, ...data }) }
    else { await createProduct.mutateAsync(data) }
    setDrawerOpen(false)
  }

  const actionColumn = { key: 'actions', label: '', width: '80px', render: (row) => (
    <div className="flex gap-1">
      <Button size="sm" variant="ghost" onClick={() => openEdit(row)}>Edit</Button>
      <Button size="sm" variant="ghost" onClick={() => { if (confirm('Delete?')) deleteProduct.mutate(row.id) }}>Del</Button>
    </div>
  )}

  return (
    <div className="space-y-4">
      <PageHeader title="Products" subtitle="Manage your product catalog" actions={<Button onClick={openCreate}>Add Product</Button>} />
      <SearchBar value={search} onChange={setSearch} placeholder="Search products..." />
      <DataTable columns={[...columns, actionColumn]} data={filtered} loading={loading} currentPage={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} emptyMessage="No products found" />
      <FormDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title={editing ? 'Edit Product' : 'Create Product'} onSubmit={form.handleSubmit(onSubmit)} loading={createProduct.isPending || updateProduct.isPending}>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Code" required error={form.formState.errors.code?.message}><Input {...form.register('code')} /></FormField>
            <FormField label="Barcode" required error={form.formState.errors.barcode?.message}><Input {...form.register('barcode')} /></FormField>
          </div>
          <FormField label="Name" required error={form.formState.errors.name?.message}><Input {...form.register('name')} /></FormField>
          <FormField label="Description" error={form.formState.errors.description?.message}><Input {...form.register('description')} /></FormField>
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Category" required error={form.formState.errors.category?.message}><Input {...form.register('category')} /></FormField>
            <FormField label="Reorder Qty" error={form.formState.errors.reorderQuantity?.message}><Input type="number" {...form.register('reorderQuantity', { valueAsNumber: true })} /></FormField>
          </div>
          <div className="grid grid-cols-4 gap-3">
            <FormField label="Weight" error={form.formState.errors.packedWeight?.message}><Input type="number" step="0.01" {...form.register('packedWeight', { valueAsNumber: true })} /></FormField>
            <FormField label="Height" error={form.formState.errors.packedHeight?.message}><Input type="number" step="0.01" {...form.register('packedHeight', { valueAsNumber: true })} /></FormField>
            <FormField label="Width" error={form.formState.errors.packedWidth?.message}><Input type="number" step="0.01" {...form.register('packedWidth', { valueAsNumber: true })} /></FormField>
            <FormField label="Depth" error={form.formState.errors.packedDepth?.message}><Input type="number" step="0.01" {...form.register('packedDepth', { valueAsNumber: true })} /></FormField>
          </div>
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" {...form.register('isActive')} className="rounded" /> Active</label>
        </div>
      </FormDrawer>
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Products.jsx
git commit -m "feat(ui): rebuild Products page with DataTable CRUD and form validation"
```

---

### Task 6: Inventory Page

**Files:**
- Modify: `demo-ui/src/pages/Inventory.jsx`

- [ ] **Step 1: Write Inventory page**

```jsx
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { ExpiryAlerts } from '../organisms/ExpiryAlerts'
import { PageHeader } from '../molecules/PageHeader'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useBatchList, useStockIn, useTaskList, useAcknowledgeTask, useResolveTask, useTriggerScan } from '../lib/hooks/useInventory'
import { useStoreId } from '../lib/auth'

const TABS = ['Batches', 'Stock-In', 'Tasks', 'Conversions']
const statusVariant = { AVAILABLE: 'success', RECALLED: 'danger', QUARANTINED: 'warning' }
const batchColumns = [
  { key: 'batchNumber', label: 'Batch #', sortable: true },
  { key: 'expiryDate', label: 'Expiry', sortable: true },
  { key: 'quantity', label: 'Qty', sortable: true },
  { key: 'status', label: 'Status', render: (row) => <Badge variant={statusVariant[row.status] || 'neutral'}>{row.status}</Badge> },
]

const stockInSchema = z.object({
  barcode: z.string().min(1),
  batchNumber: z.string().min(1),
  expiryDate: z.string().min(1),
  quantity: z.number().min(1),
})

export default function Inventory() {
  const storeId = useStoreId()
  const [tab, setTab] = useState(0)
  const [page, setPage] = useState(1)
  const [drawerOpen, setDrawerOpen] = useState(false)

  const { items: batches, totalPages, loading: batchesLoading } = useBatchList({ page, size: 20 })
  const { items: tasks } = useTaskList({ storeId, status: 'PENDING', size: 50 })
  const stockIn = useStockIn()
  const ackTask = useAcknowledgeTask()
  const resolveTask = useResolveTask()
  const triggerScan = useTriggerScan()

  const form = useForm({ resolver: zodResolver(stockInSchema), defaultValues: { quantity: 1 } })

  const handleStockIn = async (data) => {
    await stockIn.mutateAsync({ ...data, storeId })
    setDrawerOpen(false)
    form.reset()
  }

  return (
    <div className="space-y-4">
      <PageHeader title="Inventory" subtitle="Batch management and stock operations" actions={tab === 2 ? <Button onClick={() => triggerScan.mutate()}>Run Scan</Button> : tab === 1 ? <Button onClick={() => setDrawerOpen(true)}>Stock In</Button> : null} />

      {/* Tabs */}
      <div className="flex gap-1 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] p-1">
        {TABS.map((t, i) => (
          <button key={t} onClick={() => setTab(i)} className={`flex-1 py-2 text-sm font-medium rounded-[var(--radius-md)] transition-colors cursor-pointer ${tab === i ? 'bg-[var(--color-primary)] text-white' : 'text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]'}`}>{t}</button>
        ))}
      </div>

      {/* Batches tab */}
      {tab === 0 && <DataTable columns={batchColumns} data={batches || []} loading={batchesLoading} currentPage={page} totalPages={totalPages} onPageChange={setPage} emptyMessage="No batches found" />}

      {/* Tasks tab */}
      {tab === 2 && <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] p-5"><ExpiryAlerts tasks={tasks || []} onAcknowledge={(id) => ackTask.mutate(id)} onResolve={(id) => resolveTask.mutate(id)} /></div>}

      {/* Stock-In drawer */}
      <FormDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Stock In" onSubmit={form.handleSubmit(handleStockIn)} loading={stockIn.isPending}>
        <div className="space-y-4">
          <FormField label="Barcode" required error={form.formState.errors.barcode?.message}><Input {...form.register('barcode')} placeholder="Scan or enter barcode" /></FormField>
          <FormField label="Batch Number" required error={form.formState.errors.batchNumber?.message}><Input {...form.register('batchNumber')} /></FormField>
          <FormField label="Expiry Date" required error={form.formState.errors.expiryDate?.message}><Input type="date" {...form.register('expiryDate')} /></FormField>
          <FormField label="Quantity" required error={form.formState.errors.quantity?.message}><Input type="number" {...form.register('quantity', { valueAsNumber: true })} /></FormField>
        </div>
      </FormDrawer>
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Inventory.jsx
git commit -m "feat(ui): rebuild Inventory with tabs for batches, stock-in, tasks"
```

---

### Task 7: Clinical Page

**Files:**
- Modify: `demo-ui/src/pages/Clinical.jsx`

- [ ] **Step 1: Write Clinical page**

```jsx
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { PageHeader } from '../molecules/PageHeader'
import { SearchBar } from '../molecules/SearchBar'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useSearchMolecules, useCreateMolecule, useBrandsByMolecule, useCreateBrand, useStoreList } from '../lib/hooks/useClinical'

const TABS = ['Molecules', 'Brands', 'Stores']
const moleculeSchema = z.object({ genericName: z.string().min(1), therapeuticClass: z.string().optional(), regulatorySchedule: z.string().optional(), dosageInstructions: z.string().optional(), safetyWarnings: z.string().optional() })
const storeTypeVariant = { PHYSICAL: 'teal', HUB: 'info', LOGICAL: 'neutral' }

export default function Clinical() {
  const navigate = useNavigate()
  const [tab, setTab] = useState(0)
  const [search, setSearch] = useState('')
  const [selectedMolecule, setSelectedMolecule] = useState(null)
  const [drawerOpen, setDrawerOpen] = useState(false)

  const { items: molecules } = useSearchMolecules(search)
  const { items: brands } = useBrandsByMolecule(selectedMolecule)
  const { items: stores, totalPages, loading: storesLoading } = useStoreList({ page: 1, size: 20 })
  const createMolecule = useCreateMolecule()
  const createBrand = useCreateBrand()

  const molForm = useForm({ resolver: zodResolver(moleculeSchema) })

  const handleCreateMolecule = async (data) => {
    await createMolecule.mutateAsync(data)
    setDrawerOpen(false)
  }

  return (
    <div className="space-y-4">
      <PageHeader title="Clinical" subtitle="Stores, molecules, and brands" actions={<Button onClick={() => setDrawerOpen(true)}>{tab === 0 ? 'Add Molecule' : tab === 2 ? 'Add Store' : ''}</Button>} />

      <div className="flex gap-1 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] p-1">
        {TABS.map((t, i) => (
          <button key={t} onClick={() => setTab(i)} className={`flex-1 py-2 text-sm font-medium rounded-[var(--radius-md)] transition-colors cursor-pointer ${tab === i ? 'bg-[var(--color-primary)] text-white' : 'text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]'}`}>{t}</button>
        ))}
      </div>

      {tab === 0 && (
        <>
          <SearchBar value={search} onChange={setSearch} placeholder="Search molecules..." />
          <DataTable
            columns={[
              { key: 'genericName', label: 'Generic Name' },
              { key: 'therapeuticClass', label: 'Class' },
              { key: 'regulatorySchedule', label: 'Schedule', render: (r) => r.regulatorySchedule ? <Badge variant="warning">{r.regulatorySchedule}</Badge> : '—' },
            ]}
            data={molecules || []}
            emptyMessage="Search for molecules..."
          />
        </>
      )}

      {tab === 1 && (
        <>
          <SearchBar value={search} onChange={setSearch} placeholder="Search molecule first..." />
          {molecules?.length > 0 && (
            <div className="flex gap-2 flex-wrap">
              {molecules.slice(0, 10).map((m) => (
                <Button key={m.id} variant={selectedMolecule === m.id ? 'primary' : 'secondary'} size="sm" onClick={() => setSelectedMolecule(m.id)}>{m.genericName}</Button>
              ))}
            </div>
          )}
          {selectedMolecule && (
            <DataTable columns={[{ key: 'brandName', label: 'Brand' }, { key: 'strength', label: 'Strength' }, { key: 'form', label: 'Form' }]} data={brands || []} emptyMessage="No brands for this molecule" />
          )}
        </>
      )}

      {tab === 2 && (
        <DataTable
          columns={[
            { key: 'name', label: 'Name' },
            { key: 'type', label: 'Type', render: (r) => <Badge variant={storeTypeVariant[r.type] || 'neutral'}>{r.type}</Badge> },
            { key: 'active', label: 'Active', render: (r) => <Badge variant={r.active ? 'success' : 'neutral'}>{r.active ? 'Yes' : 'No'}</Badge> },
            { key: 'actions', label: '', render: (r) => <Button size="sm" variant="ghost" onClick={() => navigate(`/clinical/stores/${r.id}/products`)}>Products</Button> },
          ]}
          data={stores || []}
          loading={storesLoading}
          totalPages={totalPages}
        />
      )}

      <FormDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Create Molecule" onSubmit={molForm.handleSubmit(handleCreateMolecule)} loading={createMolecule.isPending}>
        <div className="space-y-4">
          <FormField label="Generic Name" required error={molForm.formState.errors.genericName?.message}><Input {...molForm.register('genericName')} /></FormField>
          <FormField label="Therapeutic Class"><Input {...molForm.register('therapeuticClass')} /></FormField>
          <FormField label="Regulatory Schedule"><Input {...molForm.register('regulatorySchedule')} placeholder="e.g. Schedule II" /></FormField>
          <FormField label="Dosage Instructions"><Input {...molForm.register('dosageInstructions')} /></FormField>
          <FormField label="Safety Warnings"><Input {...molForm.register('safetyWarnings')} /></FormField>
        </div>
      </FormDrawer>
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Clinical.jsx
git commit -m "feat(ui): rebuild Clinical with molecule search, brands, store tabs"
```

---

### Task 8: StoreProducts Page (new)

**Files:**
- Create: `demo-ui/src/pages/StoreProducts.jsx`

- [ ] **Step 1: Write StoreProducts page**

```jsx
import { useParams, useNavigate } from 'react-router-dom'
import { DataTable } from '../organisms/DataTable'
import { PageHeader } from '../molecules/PageHeader'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useStoreProducts } from '../lib/hooks/useClinical'

export default function StoreProducts() {
  const { storeId } = useParams()
  const navigate = useNavigate()
  const { items, loading } = useStoreProducts(storeId, { page: 1, size: 50 })

  return (
    <div className="space-y-4">
      <PageHeader title="Store Products" subtitle={`Store: ${storeId}`} actions={<Button variant="secondary" onClick={() => navigate('/clinical')}>Back to Clinical</Button>} />
      <DataTable
        columns={[
          { key: 'brandName', label: 'Brand' },
          { key: 'strength', label: 'Strength' },
          { key: 'form', label: 'Form' },
          { key: 'price', label: 'Price' },
          { key: 'shelfLocation', label: 'Shelf' },
          { key: 'isActive', label: 'Active', render: (r) => <Badge variant={r.isActive ? 'success' : 'neutral'}>{r.isActive ? 'Yes' : 'No'}</Badge> },
        ]}
        data={items || []}
        loading={loading}
        emptyMessage="No products activated for this store"
      />
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/StoreProducts.jsx
git commit -m "feat(ui): add StoreProducts sub-page for store product catalog"
```

---

### Task 9: Dispensing (POS) Page

**Files:**
- Modify: `demo-ui/src/pages/Dispensing.jsx`

- [ ] **Step 1: Write Dispensing page**

```jsx
import { useState } from 'react'
import { PageHeader } from '../molecules/PageHeader'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useStoreId } from '../lib/auth'
import { useSearchMolecules, useBrandsByMolecule } from '../lib/hooks/useClinical'
import { useQuickDispense } from '../lib/hooks/useTransactions'

const STEPS = ['Search', 'Build Invoice', 'Review', 'Done']

export default function Dispensing() {
  const storeId = useStoreId()
  const [step, setStep] = useState(0)
  const [search, setSearch] = useState('')
  const [selectedMolecule, setSelectedMolecule] = useState(null)
  const [selectedBrand, setSelectedBrand] = useState(null)
  const [items, setItems] = useState([])
  const [completedInvoice, setCompletedInvoice] = useState(null)

  const { items: molecules } = useSearchMolecules(search)
  const { items: brands } = useBrandsByMolecule(selectedMolecule)
  const dispense = useQuickDispense()

  const addItem = () => {
    if (!selectedBrand) return
    setItems([...items, { brandId: selectedBrand.id, brandName: selectedBrand.brandName, quantity: 1, insuranceCoveragePercent: 0 }])
  }

  const removeItem = (idx) => setItems(items.filter((_, i) => i !== idx))
  const updateItem = (idx, field, value) => {
    const updated = [...items]
    updated[idx] = { ...updated[idx], [field]: value }
    setItems(updated)
  }

  const handleDispense = async () => {
    const result = await dispense.mutateAsync({ storeId, items: items.map((i) => ({ brandId: i.brandId, quantity: i.quantity, insuranceCoveragePercent: i.insuranceCoveragePercent })) })
    setCompletedInvoice(result)
    setStep(3)
  }

  const reset = () => { setStep(0); setSearch(''); setSelectedMolecule(null); setSelectedBrand(null); setItems([]); setCompletedInvoice(null) }

  const totalAmount = items.reduce((sum, i) => sum + (i.quantity || 0), 0)

  return (
    <div className="space-y-4">
      <PageHeader title="Dispensing" subtitle="Point-of-sale dispensing" actions={step === 3 ? <Button onClick={reset}>New Dispense</Button> : null} />

      {/* Step indicator */}
      <div className="flex gap-1">
        {STEPS.map((s, i) => (
          <div key={s} className="flex-1">
            <div className={`h-1.5 rounded-full transition-colors ${i <= step ? 'bg-[var(--color-primary)]' : 'bg-[var(--color-border)]'}`} />
            <p className={`text-xs mt-1 ${i <= step ? 'text-[var(--color-primary)] font-medium' : 'text-[var(--color-text-muted)]'}`}>{s}</p>
          </div>
        ))}
      </div>

      {/* Step 1: Search */}
      {step === 0 && (
        <div className="space-y-4">
          <FormField label="Search molecule"><Input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="e.g. amoxicillin" /></FormField>
          {molecules?.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-medium text-[var(--color-text-primary)]">Select molecule:</p>
              {molecules.slice(0, 10).map((m) => (
                <button key={m.id} onClick={() => { setSelectedMolecule(m.id); setSearch('') }} className="w-full text-left p-3 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] hover:border-[var(--color-primary)] transition-colors cursor-pointer">
                  <p className="text-sm font-medium">{m.genericName}</p>
                  <p className="text-xs text-[var(--color-text-muted)]">{m.therapeuticClass || 'No class'}</p>
                </button>
              ))}
            </div>
          )}
          {selectedMolecule && brands?.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-medium">Select brand:</p>
              {brands.map((b) => (
                <button key={b.id} onClick={() => { setSelectedBrand(b); setStep(1) }} className="w-full text-left p-3 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] hover:border-[var(--color-primary)] cursor-pointer">
                  <p className="text-sm font-medium">{b.brandName} {b.strength}</p>
                  <p className="text-xs text-[var(--color-text-muted)]">{b.form || ''} — {b.baseUnit || ''}</p>
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Step 2: Build invoice */}
      {step === 1 && (
        <div className="space-y-4">
          <div className="flex items-center gap-3 p-3 bg-[var(--color-primary-subtle)] rounded-[var(--radius-md)]">
            <span className="text-sm font-medium">{selectedBrand?.brandName} {selectedBrand?.strength}</span>
            <Button size="sm" onClick={addItem}>Add to invoice</Button>
          </div>
          {items.length > 0 && (
            <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] overflow-hidden">
              <table className="w-full text-sm">
                <thead><tr className="bg-[var(--color-background)] border-b border-[var(--color-border)]"><th className="px-4 py-2 text-left text-xs font-semibold text-[var(--color-text-muted)]">Brand</th><th className="px-4 py-2 text-left text-xs font-semibold text-[var(--color-text-muted)]">Qty</th><th className="px-4 py-2 text-left text-xs font-semibold text-[var(--color-text-muted)]">Insurance %</th><th className="w-16"></th></tr></thead>
                <tbody>
                  {items.map((item, idx) => (
                    <tr key={idx} className="border-b border-[var(--color-border)] last:border-0">
                      <td className="px-4 py-2">{item.brandName}</td>
                      <td className="px-4 py-2"><Input type="number" value={item.quantity} onChange={(e) => updateItem(idx, 'quantity', Number(e.target.value))} className="w-20" /></td>
                      <td className="px-4 py-2"><Input type="number" value={item.insuranceCoveragePercent} onChange={(e) => updateItem(idx, 'insuranceCoveragePercent', Number(e.target.value))} className="w-20" min={0} max={100} /></td>
                      <td className="px-4 py-2"><Button size="sm" variant="ghost" onClick={() => removeItem(idx)}>X</Button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setStep(0)}>Back</Button>
            <Button onClick={() => setStep(2)} disabled={items.length === 0}>Review ({items.length} items)</Button>
          </div>
        </div>
      )}

      {/* Step 3: Review */}
      {step === 2 && (
        <div className="space-y-4">
          <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-5">
            <p className="text-sm font-semibold mb-3">Invoice Summary</p>
            {items.map((item, i) => (
              <div key={i} className="flex justify-between text-sm py-1"><span>{item.brandName} x{item.quantity}</span><span className="text-[var(--color-text-muted)]">{item.insuranceCoveragePercent}% insurance</span></div>
            ))}
            <div className="border-t border-[var(--color-border)] mt-2 pt-2 text-sm font-semibold">Total items: {totalAmount}</div>
          </div>
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setStep(1)}>Back</Button>
            <Button onClick={handleDispense} loading={dispense.isPending}>Dispense</Button>
          </div>
        </div>
      )}

      {/* Step 4: Done */}
      {step === 3 && (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-[var(--color-success-subtle)] rounded-full flex items-center justify-center mx-auto mb-4 text-2xl">✓</div>
          <h3 className="text-lg font-semibold text-[var(--color-text-primary)] mb-1">Dispensed Successfully</h3>
          <p className="text-sm text-[var(--color-text-secondary)]">Invoice #{completedInvoice?.id}</p>
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Dispensing.jsx
git commit -m "feat(ui): rebuild Dispensing as 4-step POS wizard"
```

---

### Task 10: Transactions Page

**Files:**
- Modify: `demo-ui/src/pages/Transactions.jsx`

- [ ] **Step 1: Write Transactions page**

```jsx
import { useState } from 'react'
import { DataTable } from '../organisms/DataTable'
import { PageHeader } from '../molecules/PageHeader'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useInvoiceList, useCompleteInvoice, useVoidInvoice } from '../lib/hooks/useTransactions'
import { useStoreId } from '../lib/auth'

const TABS = ['Invoices', 'Reports']
const statusVariant = { PENDING: 'warning', COMPLETED: 'success', VOIDED: 'danger' }

export default function Transactions() {
  const storeId = useStoreId()
  const [tab, setTab] = useState(0)
  const [page, setPage] = useState(1)
  const { items, totalPages, totalElements, loading } = useInvoiceList({ storeId, page, size: 20, orderBy: 'DESC' })
  const completeInvoice = useCompleteInvoice()
  const voidInvoice = useVoidInvoice()

  return (
    <div className="space-y-4">
      <PageHeader title="Transactions" subtitle="Invoices and reports" />
      <div className="flex gap-1 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] p-1">
        {TABS.map((t, i) => (
          <button key={t} onClick={() => setTab(i)} className={`flex-1 py-2 text-sm font-medium rounded-[var(--radius-md)] transition-colors cursor-pointer ${tab === i ? 'bg-[var(--color-primary)] text-white' : 'text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]'}`}>{t}</button>
        ))}
      </div>

      {tab === 0 && (
        <DataTable
          columns={[
            { key: 'id', label: 'ID', sortable: true },
            { key: 'storeName', label: 'Store' },
            { key: 'status', label: 'Status', render: (r) => <Badge variant={statusVariant[r.status] || 'neutral'}>{r.status}</Badge> },
            { key: 'totalAmount', label: 'Total', render: (r) => `$${(r.totalAmount || 0).toFixed(2)}` },
            { key: 'createdAt', label: 'Date', render: (r) => new Date(r.createdAt).toLocaleDateString() },
            { key: 'actions', label: '', render: (r) => (
              <div className="flex gap-1">
                {r.status === 'PENDING' && <Button size="sm" variant="ghost" onClick={() => completeInvoice.mutate(r.id)}>Complete</Button>}
                {r.status !== 'VOIDED' && <Button size="sm" variant="ghost" onClick={() => { if (confirm('Void this invoice?')) voidInvoice.mutate(r.id) }}>Void</Button>}
              </div>
            )},
          ]}
          data={items || []}
          loading={loading}
          currentPage={page}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={setPage}
        />
      )}

      {tab === 1 && (
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-8 text-center">
          <p className="text-sm text-[var(--color-text-muted)]">Reconciliation reports coming soon. Use the API directly: POST /transaction/reports/reconciliation</p>
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Transactions.jsx
git commit -m "feat(ui): rebuild Transactions with invoices tab and status actions"
```

---

### Task 11: Orders Page

**Files:**
- Modify: `demo-ui/src/pages/Orders.jsx`

- [ ] **Step 1: Write Orders page**

```jsx
import { useState } from 'react'
import { DataTable } from '../organisms/DataTable'
import { PageHeader } from '../molecules/PageHeader'
import { Badge } from '../atoms/Badge'
import { useOrderList } from '../lib/hooks/useOrders'
import { useAuth } from '../lib/auth'

export default function Orders() {
  const { user } = useAuth()
  const [page, setPage] = useState(1)
  const { items, totalPages, loading } = useOrderList(user?.id, { page, size: 20 })

  return (
    <div className="space-y-4">
      <PageHeader title="Orders" subtitle="Order tracking and management" />
      <DataTable
        columns={[
          { key: 'id', label: 'Order ID' },
          { key: 'status', label: 'Status', render: (r) => <Badge variant="info">{r.status || 'Placed'}</Badge> },
          { key: 'createdAt', label: 'Date', render: (r) => new Date(r.createdAt).toLocaleDateString() },
        ]}
        data={items || []}
        loading={loading}
        currentPage={page}
        totalPages={totalPages}
        onPageChange={setPage}
        emptyMessage="No orders found"
      />
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Orders.jsx
git commit -m "feat(ui): rebuild Orders as read-only listing"
```

---

### Task 12: Locations Page

**Files:**
- Modify: `demo-ui/src/pages/Locations.jsx`

- [ ] **Step 1: Write Locations page**

```jsx
import { useState } from 'react'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { PageHeader } from '../molecules/PageHeader'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useAddressList, useCreateAddress } from '../lib/hooks/useLocations'

const TABS = ['Addresses', 'Warehouses']

export default function Locations() {
  const [tab, setTab] = useState(0)
  const [page, setPage] = useState(1)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const { items: addresses, totalPages, loading } = useAddressList({ page, size: 20 })
  const createAddress = useCreateAddress()

  const handleSubmit = async (e) => {
    e.preventDefault()
    const fd = new FormData(e.target)
    await createAddress.mutateAsync({ addressName: fd.get('addressName'), line1: fd.get('line1'), city: fd.get('city'), province: fd.get('province'), country: fd.get('country'), postalCode: fd.get('postalCode') })
    setDrawerOpen(false)
  }

  return (
    <div className="space-y-4">
      <PageHeader title="Locations" subtitle="Addresses and warehouses" actions={<Button onClick={() => setDrawerOpen(true)}>Add Address</Button>} />
      <div className="flex gap-1 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] p-1">
        {TABS.map((t, i) => (
          <button key={t} onClick={() => setTab(i)} className={`flex-1 py-2 text-sm font-medium rounded-[var(--radius-md)] transition-colors cursor-pointer ${tab === i ? 'bg-[var(--color-primary)] text-white' : 'text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]'}`}>{t}</button>
        ))}
      </div>

      {tab === 0 && (
        <DataTable
          columns={[
            { key: 'addressName', label: 'Name' },
            { key: 'city', label: 'City' },
            { key: 'province', label: 'Province' },
            { key: 'country', label: 'Country' },
          ]}
          data={addresses || []}
          loading={loading}
          currentPage={page}
          totalPages={totalPages}
          onPageChange={setPage}
        />
      )}

      {tab === 1 && (
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-8 text-center">
          <p className="text-sm text-[var(--color-text-muted)]">Warehouse management coming soon.</p>
        </div>
      )}

      <FormDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Create Address" onSubmit={handleSubmit} loading={createAddress.isPending}>
        <div className="space-y-4">
          <FormField label="Address Name" required><Input name="addressName" placeholder="Main Office" /></FormField>
          <FormField label="Line 1" required><Input name="line1" placeholder="123 Main St" /></FormField>
          <div className="grid grid-cols-2 gap-4">
            <FormField label="City" required><Input name="city" /></FormField>
            <FormField label="Province"><Input name="province" /></FormField>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Country" required><Input name="country" /></FormField>
            <FormField label="Postal Code"><Input name="postalCode" /></FormField>
          </div>
        </div>
      </FormDrawer>
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Locations.jsx
git commit -m "feat(ui): rebuild Locations with address CRUD"
```

---

### Task 13: Users Page (Admin)

**Files:**
- Modify: `demo-ui/src/pages/Users.jsx`

- [ ] **Step 1: Write Users page**

```jsx
import { useState } from 'react'
import { DataTable } from '../organisms/DataTable'
import { PageHeader } from '../molecules/PageHeader'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { RequireRole } from '../lib/auth'
import { useUserList } from '../lib/hooks/useUsers'

const roleVariant = { ADMIN: 'purple', MANAGER: 'teal', PHARMACIST: 'info', EMPLOYEE: 'neutral' }

export default function Users() {
  const [page, setPage] = useState(1)
  const { items, totalPages, loading } = useUserList({ page, size: 20 })

  return (
    <RequireRole role={['ADMIN']}>
      <div className="space-y-4">
        <PageHeader title="Users" subtitle="User accounts and roles" />
        <DataTable
          columns={[
            { key: 'username', label: 'Username', sortable: true },
            { key: 'role', label: 'Role', render: (r) => <Badge variant={roleVariant[r.role] || 'neutral'}>{r.role}</Badge> },
            { key: 'createdAt', label: 'Created', render: (r) => new Date(r.createdAt).toLocaleDateString() },
          ]}
          data={items || []}
          loading={loading}
          currentPage={page}
          totalPages={totalPages}
          onPageChange={setPage}
          emptyMessage="No users found"
        />
      </div>
    </RequireRole>
  )
}
```

- [ ] **Step 2: Verify build**

Run: `cd demo-ui && npm run build`

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Users.jsx
git commit -m "feat(ui): rebuild Users page with RequireRole guard and role badges"
```

---

### Task 14: Final Verification

- [ ] **Step 1: Full build check**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds with no errors

- [ ] **Step 2: Verify all routes resolve**

Run: `cd demo-ui && npm run dev`
Expected: Dev server starts, all routes load without crash

- [ ] **Step 3: Commit any remaining fixes**

```bash
git add -u demo-ui/src/pages/ demo-ui/src/App.jsx demo-ui/src/templates/
git commit -m "feat(ui): complete all page rebuilds against new design system"
```
