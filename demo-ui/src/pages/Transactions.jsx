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
