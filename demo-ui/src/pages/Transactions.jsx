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
