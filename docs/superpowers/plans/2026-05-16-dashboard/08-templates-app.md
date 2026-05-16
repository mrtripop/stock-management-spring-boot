### Task 8: Templates + App Root Integration

**Files:**
- Create: `demo-ui/src/templates/AdminLayout.jsx`
- Rewrite: `demo-ui/src/App.jsx`
- Modify: `demo-ui/src/main.jsx`
- Delete: `demo-ui/src/pages/Layout.jsx` (replaced by AdminLayout)

- [ ] **Step 1: Create AdminLayout template**

Create `demo-ui/src/templates/AdminLayout.jsx`:

```jsx
import { Outlet, useLocation } from 'react-router-dom'
import { Sidebar } from '../organisms/Sidebar'
import { TopBar } from '../organisms/TopBar'

const PAGE_TITLES = {
  '/': { title: 'Dashboard', subtitle: '' },
  '/products': { title: 'Products', subtitle: 'Manage your product catalog' },
  '/inventory': { title: 'Inventory', subtitle: 'Batch management and stock operations' },
  '/clinical': { title: 'Clinical', subtitle: 'Stores, molecules, and brands' },
  '/orders': { title: 'Orders', subtitle: 'Order tracking and management' },
  '/transactions': { title: 'Transactions', subtitle: 'Stock movement audit log' },
  '/locations': { title: 'Locations', subtitle: 'Warehouse and store locations' },
  '/users': { title: 'Users', subtitle: 'User accounts and roles' },
}

export default function AdminLayout() {
  const location = useLocation()
  const page = PAGE_TITLES[location.pathname] || { title: 'Dashboard', subtitle: '' }

  return (
    <div className="flex h-screen bg-[var(--color-background)]">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <TopBar title={page.title} subtitle={page.subtitle} />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Rewrite App.jsx with QueryClient + new router**

Replace `demo-ui/src/App.jsx`:

```jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'sonner'
import { useState, useEffect } from 'react'
import api from './lib/api'
import AdminLayout from './templates/AdminLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Products from './pages/Products'
import Inventory from './pages/Inventory'
import Clinical from './pages/Clinical'
import Orders from './pages/Orders'
import Transactions from './pages/Transactions'
import Locations from './pages/Locations'
import Users from './pages/Users'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})

function ProtectedRoute({ children }) {
  if (!api.token) return <Navigate to="/login" />
  return children
}

export default function App() {
  const [authed, setAuthed] = useState(!!api.token)

  useEffect(() => {
    if (api.token) {
      api.get('/auth/me').then(() => setAuthed(true)).catch(() => {
        api.clearToken()
        setAuthed(false)
      })
    }
  }, [])

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login onLogin={() => setAuthed(true)} />} />
          <Route path="/" element={
            <ProtectedRoute><AdminLayout /></ProtectedRoute>
          }>
            <Route index element={<Dashboard />} />
            <Route path="products" element={<Products />} />
            <Route path="inventory" element={<Inventory />} />
            <Route path="clinical" element={<Clinical />} />
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
  )
}
```

- [ ] **Step 3: Update main.jsx — no changes needed**

The current `main.jsx` already renders `<App />` which now includes QueryClientProvider internally. No changes needed.

- [ ] **Step 4: Delete old Layout.jsx**

```bash
rm demo-ui/src/pages/Layout.jsx
```

- [ ] **Step 5: Verify dev server starts**

Run: `cd demo-ui && npm run dev`
Expected: Server starts. Pages will show errors because they still use old imports — this is OK, fixed in tasks 9-14.

- [ ] **Step 6: Commit**

```bash
mkdir -p demo-ui/src/templates
git add demo-ui/src/templates/AdminLayout.jsx demo-ui/src/App.jsx
git rm demo-ui/src/pages/Layout.jsx
git commit -m "feat(demo-ui): add AdminLayout template and rewrite App.jsx

AdminLayout composes Sidebar + TopBar + page Outlet.
App.jsx wraps routes with QueryClientProvider and Toaster.
Remove old Layout.jsx replaced by AdminLayout template."
```
