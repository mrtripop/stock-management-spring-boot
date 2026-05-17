import { useState } from 'react'
import { Tab, TabGroup, TabList, TabPanel, TabPanels } from '@headlessui/react'
import { toast } from 'sonner'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { PageHeader } from '../molecules/PageHeader'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { AlertDialog } from '../organisms/AlertDialog'
import { useQueryList, useQueryDetail, usePostMutation } from '../lib/hooks'

const TYPE_MAP = { 0: 'Purchase', 1: 'Refund', 2: 'Adjustment' }
const TYPE_BADGE = { 0: 'teal', 1: 'orange', 2: 'neutral' }
const STATUS_MAP = { 0: 'Pending', 1: 'Completed', 2: 'Failed' }
const STATUS_BADGE = { 0: 'warning', 1: 'success', 2: 'danger' }

const INVOICE_STATUS_BADGE = {
  PENDING: 'warning',
  COMPLETED: 'success',
  VOIDED: 'danger',
}

const TXN_COLUMNS = [
  { key: 'id', label: 'Txn #' },
  { key: 'code', label: 'Code' },
  { key: 'type', label: 'Type' },
  { key: 'status', label: 'Status' },
  { key: 'date', label: 'Created' },
]

const INVOICE_COLUMNS = [
  { key: 'id', label: 'Invoice #' },
  { key: 'storeName', label: 'Store' },
  { key: 'status', label: 'Status' },
  { key: 'totalAmount', label: 'Total' },
  { key: 'date', label: 'Created' },
]

export default function Transactions() {
  const [tabIndex, setTabIndex] = useState(0)

  return (
    <div>
      <PageHeader title="Transactions" subtitle="Stock movements and invoices" />

      <TabGroup selectedIndex={tabIndex} onChange={setTabIndex}>
        <TabList className="flex gap-1 mb-5 bg-slate-100 rounded-[var(--radius-md)] p-1 w-fit">
          {['Stock Movements', 'Invoices'].map((name) => (
            <Tab key={name} className="px-4 py-1.5 text-sm font-medium rounded-[var(--radius-sm)] cursor-pointer transition-colors data-[selected]:bg-white data-[selected]:text-[var(--color-primary)] data-[selected]:shadow-sm text-[var(--color-text-secondary)]">
              {name}
            </Tab>
          ))}
        </TabList>
        <TabPanels>
          <TabPanel><StockMovementsTab /></TabPanel>
          <TabPanel><InvoicesTab /></TabPanel>
        </TabPanels>
      </TabGroup>
    </div>
  )
}

/* ---- Stock Movements Tab (original) ---- */
function StockMovementsTab() {
  const [page, setPage] = useState(1)

  const { items, totalPages, totalElements, loading } = useQueryList(
    ['transactions'], '/transactions', { page, size: 10 }
  )

  return (
    <DataTable columns={TXN_COLUMNS} data={items} loading={loading}
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
  )
}

/* ---- Invoices Tab ---- */
function InvoicesTab() {
  const [page, setPage] = useState(1)
  const [selectedInvoiceId, setSelectedInvoiceId] = useState(null)

  const { items, totalPages, totalElements, loading } = useQueryList(
    ['invoices'], '/transaction/invoices', { page, size: 10 }
  )

  return (
    <>
      <DataTable columns={INVOICE_COLUMNS} data={items} loading={loading}
        currentPage={page} totalPages={totalPages} totalElements={totalElements}
        pageSize={10} onPageChange={setPage} emptyMessage="No invoices found"
        renderRow={(inv) => (
          <tr key={inv.id}
            className="border-b border-[var(--color-border-light)] hover:bg-slate-50 cursor-pointer"
            onClick={() => setSelectedInvoiceId(inv.id)}
          >
            <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-primary)]">#{inv.id}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{inv.storeName || '-'}</td>
            <td className="px-4 py-2.5">
              <Badge variant={INVOICE_STATUS_BADGE[inv.status] || 'neutral'}>{inv.status}</Badge>
            </td>
            <td className="px-4 py-2.5 text-sm font-medium">${Number(inv.totalAmount ?? 0).toFixed(2)}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">
              {inv.createdAt ? new Date(inv.createdAt).toLocaleString() : '-'}
            </td>
          </tr>
        )}
      />
      <InvoiceDetailDrawer
        invoiceId={selectedInvoiceId}
        onClose={() => setSelectedInvoiceId(null)}
      />
    </>
  )
}

/* ---- Invoice Detail Drawer ---- */
function InvoiceDetailDrawer({ invoiceId, onClose }) {
  const [showVoidDialog, setShowVoidDialog] = useState(false)
  const [voidTarget, setVoidTarget] = useState(null)

  const { data: invoice, isLoading } = useQueryDetail(
    ['invoice-detail'], '/transaction/invoices', invoiceId
  )

  const completeMutation = usePostMutation(
    ['invoices', 'invoice-detail'],
    `/transaction/invoices/${invoiceId}/complete`
  )
  const voidMutation = usePostMutation(
    ['invoices', 'invoice-detail'],
    `/transaction/invoices/${voidTarget}/void`
  )

  const handleComplete = async () => {
    try {
      await completeMutation.mutateAsync({})
      toast.success('Invoice completed')
    } catch (err) {
      toast.error(err.message)
    }
  }

  const handleVoid = async () => {
    try {
      await voidMutation.mutateAsync({})
      toast.success('Invoice voided')
      setShowVoidDialog(false)
      setVoidTarget(null)
      onClose()
    } catch (err) {
      toast.error(err.message)
    }
  }

  const detail = invoice?.data

  return (
    <>
      <FormDrawer open={!!invoiceId} onClose={onClose} title={`Invoice #${invoiceId}`}>
        {isLoading ? (
          <div className="py-12 text-center text-sm text-[var(--color-text-muted)]">Loading...</div>
        ) : !detail ? (
          <div className="py-12 text-center text-sm text-[var(--color-text-muted)]">Invoice not found</div>
        ) : (
          <div className="space-y-4">
            {/* Status + Store */}
            <div className="flex items-center justify-between">
              <Badge variant={INVOICE_STATUS_BADGE[detail.status] || 'neutral'}>
                {detail.status}
              </Badge>
              <span className="text-sm text-[var(--color-text-secondary)]">{detail.storeName}</span>
            </div>

            {/* Items table */}
            <div>
              <h4 className="text-xs font-semibold text-[var(--color-text-muted)] uppercase mb-2">Items</h4>
              <table className="w-full text-sm border-collapse">
                <thead>
                  <tr className="border-b border-[var(--color-border-light)]">
                    <th className="py-1.5 text-left text-xs font-semibold text-[var(--color-text-muted)]">Brand</th>
                    <th className="py-1.5 text-left text-xs font-semibold text-[var(--color-text-muted)]">Batch</th>
                    <th className="py-1.5 text-right text-xs font-semibold text-[var(--color-text-muted)]">Qty</th>
                    <th className="py-1.5 text-right text-xs font-semibold text-[var(--color-text-muted)]">Unit</th>
                    <th className="py-1.5 text-right text-xs font-semibold text-[var(--color-text-muted)]">Line</th>
                    <th className="py-1.5 text-right text-xs font-semibold text-[var(--color-text-muted)]">Ins%</th>
                  </tr>
                </thead>
                <tbody>
                  {detail.items?.map((item) => (
                    <tr key={item.id} className="border-b border-[var(--color-border-light)]">
                      <td className="py-1.5 font-medium">{item.brandName}</td>
                      <td className="py-1.5 text-[var(--color-text-secondary)]">{item.batchNumber}</td>
                      <td className="py-1.5 text-right">{item.quantity}</td>
                      <td className="py-1.5 text-right">${Number(item.unitPrice ?? 0).toFixed(2)}</td>
                      <td className="py-1.5 text-right">${Number(item.lineTotal ?? 0).toFixed(2)}</td>
                      <td className="py-1.5 text-right">{item.insuranceCoveragePercent}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Totals */}
            <div className="border-t border-[var(--color-border-light)] pt-3 space-y-1 text-sm">
              <div className="flex justify-between">
                <span className="text-[var(--color-text-secondary)]">Total</span>
                <span className="font-medium">${Number(detail.totalAmount ?? 0).toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[var(--color-text-secondary)]">Patient Owed</span>
                <span className="font-medium">${Number(detail.patientOwed ?? 0).toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[var(--color-text-secondary)]">Insurance Claims</span>
                <span className="font-medium">${Number(detail.insuranceClaimAmount ?? 0).toFixed(2)}</span>
              </div>
            </div>

            {/* Actions */}
            <div className="flex gap-2 pt-2">
              {detail.status === 'PENDING' && (
                <>
                  <Button onClick={handleComplete} loading={completeMutation.isPending}>Complete</Button>
                  <Button variant="danger" onClick={() => { setVoidTarget(invoiceId); setShowVoidDialog(true) }}>Void</Button>
                </>
              )}
              {detail.status === 'COMPLETED' && (
                <Button variant="danger" onClick={() => { setVoidTarget(invoiceId); setShowVoidDialog(true) }}>Void</Button>
              )}
            </div>

            {/* Timestamps */}
            <div className="text-xs text-[var(--color-text-muted)] pt-2 border-t border-[var(--color-border-light)]">
              Created: {detail.createdAt ? new Date(detail.createdAt).toLocaleString() : '-'}
              {detail.updatedAt && ` | Updated: ${new Date(detail.updatedAt).toLocaleString()}`}
            </div>
          </div>
        )}
      </FormDrawer>

      {/* Void confirmation */}
      <AlertDialog
        open={showVoidDialog}
        onClose={() => setShowVoidDialog(false)}
        onConfirm={handleVoid}
        title="Void Invoice"
        message={
          detail?.status === 'PENDING'
            ? 'This invoice was not completed. Void to cancel.'
            : 'Voiding will restore stock to inventory. This cannot be undone.'
        }
        confirmLabel="Void"
        loading={voidMutation.isPending}
      />
    </>
  )
}
