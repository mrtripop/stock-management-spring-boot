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
