import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { ExpiryAlerts } from '../organisms/ExpiryAlerts'
import { PageHeader } from '../molecules/PageHeader'
import { AlertBanner } from '../molecules/AlertBanner'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { Spinner } from '../atoms/Spinner'
import { useBatchList, useStockIn, useTaskList, useAcknowledgeTask, useResolveTask, useTriggerScan, useTriggerReconcile, useReconcileStatus } from '../lib/hooks/useInventory'
import { useStoreId, useHasRole } from '../lib/auth'
import { ConfirmationDialog } from '../molecules/ConfirmationDialog'

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
  const isAdmin = useHasRole('ADMIN')
  const [tab, setTab] = useState(0)
  const [page, setPage] = useState(1)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [confirmOpen, setConfirmOpen] = useState(false)

  const { items: batches, totalPages, loading: batchesLoading } = useBatchList({ page, size: 20 })
  const { items: tasks } = useTaskList({ storeId, status: 'PENDING', size: 50 })
  const stockIn = useStockIn()
  const ackTask = useAcknowledgeTask()
  const resolveTask = useResolveTask()
  const triggerScan = useTriggerScan()
  const triggerReconcile = useTriggerReconcile()
  const reconcileStatus = useReconcileStatus()

  const isProcessing = reconcileStatus.data?.status === 'PROCESSING' || reconcileStatus.data?.status === 'IN_PROGRESS'

  const form = useForm({ resolver: zodResolver(stockInSchema), defaultValues: { quantity: 1 } })

  const handleStockIn = async (data) => {
    await stockIn.mutateAsync({ ...data, storeId })
    setDrawerOpen(false)
    form.reset()
  }

  return (
    <div className="space-y-4">
      <PageHeader
        title="Inventory"
        subtitle="Batch management and stock operations"
        actions={
          tab === 2 ? (
            <div className="flex gap-2">
              <Button onClick={() => triggerScan.mutate()}>Run Scan</Button>
              <Button
                onClick={() => setConfirmOpen(true)}
                disabled={!isAdmin || isProcessing}
                title={!isAdmin ? "Administrator privileges required to trigger stock reconciliation." : isProcessing ? "Reconciliation already in progress." : ""}
              >
                Reconcile Stock
              </Button>
            </div>
          ) : tab === 1 ? (
            <Button onClick={() => setDrawerOpen(true)}>Stock In</Button>
          ) : null
        }
      />

      {isProcessing && (
        <AlertBanner message="Stock reconciliation in progress... Correcting quantity drifts across all batches." />
      )}

      {/* Tabs */}
      <div className="flex gap-1 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] p-1">
        {TABS.map((t, i) => (
          <button key={t} onClick={() => setTab(i)} className={`flex-1 py-2 text-sm font-medium rounded-[var(--radius-md)] transition-colors cursor-pointer ${tab === i ? 'bg-[var(--color-primary)] text-white' : 'text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]'}`}>{t}</button>
        ))}
      </div>

      {/* Batches tab */}
      {tab === 0 && <DataTable columns={batchColumns} data={batches || []} loading={batchesLoading} currentPage={page} totalPages={totalPages} onPageChange={setPage} emptyMessage="No batches found" />}

      {/* Stock-In tab placeholder */}
      {tab === 1 && !drawerOpen && (
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-8 text-center">
          <p className="text-sm text-[var(--color-text-muted)]">Click "Stock In" to add new stock.</p>
        </div>
      )}

      {/* Tasks tab */}
      {tab === 2 && <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] p-5"><ExpiryAlerts tasks={tasks || []} onAcknowledge={(id) => ackTask.mutate(id)} onResolve={(id) => resolveTask.mutate(id)} /></div>}

      {/* Conversions tab placeholder */}
      {tab === 3 && (
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-8 text-center">
          <p className="text-sm text-[var(--color-text-muted)]">Unit conversions coming soon.</p>
        </div>
      )}

      {/* Stock-In drawer */}
      <FormDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Stock In" onSubmit={form.handleSubmit(handleStockIn)} loading={stockIn.isPending}>
        <div className="space-y-4">
          <FormField label="Barcode" required error={form.formState.errors.barcode?.message}><Input {...form.register('barcode')} placeholder="Scan or enter barcode" /></FormField>
          <FormField label="Batch Number" required error={form.formState.errors.batchNumber?.message}><Input {...form.register('batchNumber')} /></FormField>
          <FormField label="Expiry Date" required error={form.formState.errors.expiryDate?.message}><Input type="date" {...form.register('expiryDate')} /></FormField>
          <FormField label="Quantity" required error={form.formState.errors.quantity?.message}><Input type="number" {...form.register('quantity', { valueAsNumber: true })} /></FormField>
        </div>
      </FormDrawer>

      <ConfirmationDialog
        open={confirmOpen}
        onCancel={() => setConfirmOpen(false)}
        title="Confirm Reconciliation"
        message="Triggering a full stock reconciliation will analyze all batches and correct any quantity drifts. This may take a moment. Do you wish to proceed?"
        onConfirm={async () => {
          await triggerReconcile.mutateAsync();
          setConfirmOpen(false);
        }}
        loading={triggerReconcile.isPending}
      />
    </div>
  )
}
