### Task 14: Read-Only Pages — Orders, Transactions, Locations, Users

**Files:**
- Rewrite: `demo-ui/src/pages/Orders.jsx`
- Rewrite: `demo-ui/src/pages/Transactions.jsx`
- Rewrite: `demo-ui/src/pages/Locations.jsx`
- Rewrite: `demo-ui/src/pages/Users.jsx`

- [ ] **Step 1: Rewrite Orders.jsx**

Replace `demo-ui/src/pages/Orders.jsx`:

```jsx
import { useState } from 'react'
import { Badge } from '../atoms/Badge'
import { Input } from '../atoms/Input'
import { PageHeader } from '../molecules/PageHeader'
import { DataTable } from '../organisms/DataTable'
import { useQueryList } from '../lib/hooks'

const STATUS_MAP = { 0: 'Pending', 1: 'Processing', 2: 'Shipped', 3: 'Delivered', 4: 'Cancelled' }
const STATUS_BADGE = { 0: 'warning', 1: 'info', 2: 'info', 3: 'success', 4: 'danger' }

const COLUMNS = [
  { key: 'id', label: 'Order #' },
  { key: 'status', label: 'Status' },
  { key: 'subtotal', label: 'Subtotal' },
  { key: 'tax', label: 'Tax' },
  { key: 'total', label: 'Total' },
  { key: 'date', label: 'Created' },
]

export default function Orders() {
  const [page, setPage] = useState(1)
  const [userId, setUserId] = useState('1')

  const { items, totalPages, totalElements, loading } = useQueryList(
    ['orders'], `/orders/users/${userId}`, { page, size: 10 }
  )

  return (
    <div>
      <PageHeader title="Orders" subtitle="Order tracking and management"
        actions={
          <div className="flex items-center gap-2">
            <label className="text-xs text-[var(--color-text-secondary)]">User ID:</label>
            <Input type="number" value={userId} onChange={(e) => { setUserId(e.target.value); setPage(1) }}
              className="w-20" />
          </div>
        }
      />
      <DataTable columns={COLUMNS} data={items} loading={loading}
        currentPage={page} totalPages={totalPages} totalElements={totalElements}
        pageSize={10} onPageChange={setPage} emptyMessage="No orders found"
        renderRow={(o) => (
          <tr key={o.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
            <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{o.id}</td>
            <td className="px-4 py-2.5"><Badge variant={STATUS_BADGE[o.status] || 'neutral'}>{STATUS_MAP[o.status] || o.status}</Badge></td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">${o.subTotal?.toFixed(2) ?? '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">${o.tax?.toFixed(2) ?? '-'}</td>
            <td className="px-4 py-2.5 text-sm font-semibold text-[var(--color-text-primary)]">${o.grandTotal?.toFixed(2) ?? o.total?.toFixed(2) ?? '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{o.createdAt ? new Date(o.createdAt).toLocaleDateString() : '-'}</td>
          </tr>
        )}
      />
    </div>
  )
}
```

- [ ] **Step 2: Rewrite Transactions.jsx**

Replace `demo-ui/src/pages/Transactions.jsx`:

```jsx
import { useState } from 'react'
import { Badge } from '../atoms/Badge'
import { PageHeader } from '../molecules/PageHeader'
import { DataTable } from '../organisms/DataTable'
import { useQueryList } from '../lib/hooks'

const TYPE_MAP = { 0: 'Purchase', 1: 'Refund', 2: 'Adjustment' }
const TYPE_BADGE = { 0: 'teal', 1: 'orange', 2: 'neutral' }
const STATUS_MAP = { 0: 'Pending', 1: 'Completed', 2: 'Failed' }
const STATUS_BADGE = { 0: 'warning', 1: 'success', 2: 'danger' }

const COLUMNS = [
  { key: 'id', label: 'Txn #' },
  { key: 'code', label: 'Code' },
  { key: 'type', label: 'Type' },
  { key: 'status', label: 'Status' },
  { key: 'date', label: 'Created' },
]

export default function Transactions() {
  const [page, setPage] = useState(1)

  const { items, totalPages, totalElements, loading } = useQueryList(
    ['transactions'], '/transactions', { page, size: 10 }
  )

  return (
    <div>
      <PageHeader title="Transactions" subtitle="Stock movement audit log" />
      <DataTable columns={COLUMNS} data={items} loading={loading}
        currentPage={page} totalPages={totalPages} totalElements={totalElements}
        pageSize={10} onPageChange={setPage} emptyMessage="No transactions found"
        renderRow={(t) => (
          <tr key={t.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
            <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{t.id}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)] font-mono">{t.code || '-'}</td>
            <td className="px-4 py-2.5"><Badge variant={TYPE_BADGE[t.type] || 'neutral'}>{TYPE_MAP[t.type] || t.type}</Badge></td>
            <td className="px-4 py-2.5"><Badge variant={STATUS_BADGE[t.status] || 'neutral'}>{STATUS_MAP[t.status] || t.status}</Badge></td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{t.createdAt ? new Date(t.createdAt).toLocaleDateString() : '-'}</td>
          </tr>
        )}
      />
    </div>
  )
}
```

- [ ] **Step 3: Rewrite Locations.jsx**

Replace `demo-ui/src/pages/Locations.jsx`:

```jsx
import { useState } from 'react'
import { Badge } from '../atoms/Badge'
import { PageHeader } from '../molecules/PageHeader'
import { DataTable } from '../organisms/DataTable'
import { useQueryList } from '../lib/hooks'

const COLUMNS = [
  { key: 'name', label: 'Name' },
  { key: 'line1', label: 'Address' },
  { key: 'city', label: 'City' },
  { key: 'province', label: 'Province' },
  { key: 'country', label: 'Country' },
  { key: 'postal', label: 'Postal' },
]

export default function Locations() {
  const [page, setPage] = useState(1)

  const { items, totalPages, totalElements, loading } = useQueryList(
    ['addresses'], '/addresses', { page, size: 10 }
  )

  return (
    <div>
      <PageHeader title="Locations" subtitle="Warehouse and store locations" />
      <DataTable columns={COLUMNS} data={items} loading={loading}
        currentPage={page} totalPages={totalPages} totalElements={totalElements}
        pageSize={10} onPageChange={setPage} emptyMessage="No locations found"
        renderRow={(a) => (
          <tr key={a.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
            <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{a.addressName}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.line1}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.city}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.province}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.country}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.postalCode}</td>
          </tr>
        )}
      />
    </div>
  )
}
```

- [ ] **Step 4: Rewrite Users.jsx**

Replace `demo-ui/src/pages/Users.jsx`:

```jsx
import { useState } from 'react'
import { PageHeader } from '../molecules/PageHeader'
import { DataTable } from '../organisms/DataTable'
import { useQueryList } from '../lib/hooks'

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'username', label: 'Username' },
  { key: 'name', label: 'Name' },
  { key: 'email', label: 'Email' },
  { key: 'registered', label: 'Registered' },
]

export default function Users() {
  const [page, setPage] = useState(1)

  const { items, totalPages, totalElements, loading } = useQueryList(
    ['users'], '/users', { page, size: 10 }
  )

  return (
    <div>
      <PageHeader title="Users" subtitle="User accounts and roles" />
      <DataTable columns={COLUMNS} data={items} loading={loading}
        currentPage={page} totalPages={totalPages} totalElements={totalElements}
        pageSize={10} onPageChange={setPage} emptyMessage="No users found"
        renderRow={(u) => (
          <tr key={u.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{u.id}</td>
            <td className="px-4 py-2.5">
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-[var(--color-primary)] rounded-full flex items-center justify-center text-white text-[10px] font-semibold">
                  {(u.username || '?').substring(0, 2).toUpperCase()}
                </div>
                <span className="text-sm font-medium text-[var(--color-text-primary)]">{u.username}</span>
              </div>
            </td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{[u.firstName, u.lastName].filter(Boolean).join(' ') || '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{u.email || '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{u.registeredAt ? new Date(u.registeredAt).toLocaleDateString() : '-'}</td>
          </tr>
        )}
      />
    </div>
  )
}
```

- [ ] **Step 5: Verify all read-only pages**

Navigate to `/orders`, `/transactions`, `/locations`, `/users` — each should show a data table with badges, pagination, and teal theme.

- [ ] **Step 6: Commit**

```bash
git add demo-ui/src/pages/Orders.jsx demo-ui/src/pages/Transactions.jsx demo-ui/src/pages/Locations.jsx demo-ui/src/pages/Users.jsx
git commit -m "feat(demo-ui): redesign read-only pages — Orders, Transactions, Locations, Users

All pages use DataTable organism with PageHeader and React Query hooks.
Orders: status badges (amber/blue/green/red), user ID filter.
Transactions: type badges (teal/orange/gray), status badges.
Locations: address listing with city/province/country columns.
Users: avatar initials circle, username, name, email, registration date."
```
