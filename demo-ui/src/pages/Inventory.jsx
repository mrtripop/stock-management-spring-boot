import { useState } from 'react'
import { toast } from 'sonner'
import { Button } from '../atoms/Button'
import { Badge } from '../atoms/Badge'
import { Input } from '../atoms/Input'
import { PageHeader } from '../molecules/PageHeader'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { FormField } from '../molecules/FormField'
import { useQueryList, usePostMutation } from '../lib/hooks'

const EMPTY_STOCK_IN = { barcode: '', batchNumber: '', expiryDate: '', quantity: 1, storeId: '' }
const EMPTY_DEDUCT = { barcode: '', storeId: '', quantity: 1 }

const COLUMNS = [
  { key: 'product', label: 'Product' },
  { key: 'batchNumber', label: 'Batch #' },
  { key: 'expiry', label: 'Expiry' },
  { key: 'quantity', label: 'Qty' },
  { key: 'store', label: 'Store' },
]

export default function Inventory() {
  const [page, setPage] = useState(1)
  const [showStockIn, setShowStockIn] = useState(false)
  const [showDeduct, setShowDeduct] = useState(false)
  const [stockInForm, setStockInForm] = useState(EMPTY_STOCK_IN)
  const [deductForm, setDeductForm] = useState(EMPTY_DEDUCT)

  const { items: batches, totalPages, totalElements, loading } = useQueryList(
    ['batches'], '/inventory/batches', { page, size: 10 }
  )

  const stockInMutation = usePostMutation(['batches'], '/inventory/batches/stock-in')
  const deductMutation = usePostMutation(['batches'], '/inventory/stock/deduct')

  const getDaysUntil = (dateStr) => {
    if (!dateStr) return null
    return Math.ceil((new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24))
  }

  const handleStockIn = async (e) => {
    e.preventDefault()
    try {
      await stockInMutation.mutateAsync(stockInForm)
      toast.success('Stock added successfully')
      setShowStockIn(false)
      setStockInForm(EMPTY_STOCK_IN)
    } catch (err) {
      toast.error(err.message)
    }
  }

  const handleDeduct = async (e) => {
    e.preventDefault()
    try {
      await deductMutation.mutateAsync(deductForm)
      toast.success('Stock deducted successfully')
      setShowDeduct(false)
      setDeductForm(EMPTY_DEDUCT)
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <div>
      <PageHeader
        title="Inventory"
        subtitle="Batch management and stock operations"
        actions={
          <>
            <Button onClick={() => setShowStockIn(true)}>Stock In</Button>
            <Button variant="secondary" onClick={() => setShowDeduct(true)}>Deduct Stock</Button>
          </>
        }
      />

      <DataTable
        columns={COLUMNS}
        data={batches}
        loading={loading}
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={10}
        onPageChange={setPage}
        emptyMessage="No batches found"
        renderRow={(b) => {
          const days = getDaysUntil(b.expiryDate)
          const urgent = days !== null && days <= 7
          const warning = days !== null && days <= 30 && days > 7
          return (
            <tr key={b.id} className={`border-b border-[var(--color-border-light)] transition-colors ${
              urgent ? 'bg-red-50' : warning ? 'bg-amber-50' : 'hover:bg-slate-50'
            }`}>
              <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{b.barcode || '-'}</td>
              <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{b.batchNumber || '-'}</td>
              <td className="px-4 py-2.5">
                <div className="flex items-center gap-1.5">
                  {days !== null && days <= 7 && <Badge variant="danger">{days}d</Badge>}
                  {days !== null && days > 7 && days <= 30 && <Badge variant="warning">{days}d</Badge>}
                  <span className="text-sm text-[var(--color-text-secondary)]">
                    {b.expiryDate ? new Date(b.expiryDate).toLocaleDateString() : '-'}
                  </span>
                </div>
              </td>
              <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{b.quantity}</td>
              <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{b.storeId ? b.storeId.substring(0, 8) + '...' : '-'}</td>
            </tr>
          )
        }}
      />

      {/* Stock In Drawer */}
      <FormDrawer open={showStockIn} onClose={() => setShowStockIn(false)} title="Stock In" onSubmit={handleStockIn} submitLabel="Stock In" loading={stockInMutation.isPending}>
        <FormField label="Barcode" required>
          <Input value={stockInForm.barcode} onChange={(e) => setStockInForm({ ...stockInForm, barcode: e.target.value })} required />
        </FormField>
        <FormField label="Batch Number">
          <Input value={stockInForm.batchNumber} onChange={(e) => setStockInForm({ ...stockInForm, batchNumber: e.target.value })} />
        </FormField>
        <FormField label="Expiry Date" required>
          <Input type="date" value={stockInForm.expiryDate} onChange={(e) => setStockInForm({ ...stockInForm, expiryDate: e.target.value })} required />
        </FormField>
        <FormField label="Quantity" required>
          <Input type="number" min="1" value={stockInForm.quantity} onChange={(e) => setStockInForm({ ...stockInForm, quantity: +e.target.value })} required />
        </FormField>
        <FormField label="Store ID" required hint="UUID format">
          <Input value={stockInForm.storeId} onChange={(e) => setStockInForm({ ...stockInForm, storeId: e.target.value })} required placeholder="UUID" />
        </FormField>
      </FormDrawer>

      {/* Deduct Drawer */}
      <FormDrawer open={showDeduct} onClose={() => setShowDeduct(false)} title="Deduct Stock" onSubmit={handleDeduct} submitLabel="Deduct" loading={deductMutation.isPending}>
        <FormField label="Barcode" required>
          <Input value={deductForm.barcode} onChange={(e) => setDeductForm({ ...deductForm, barcode: e.target.value })} required />
        </FormField>
        <FormField label="Store ID" required hint="UUID format">
          <Input value={deductForm.storeId} onChange={(e) => setDeductForm({ ...deductForm, storeId: e.target.value })} required placeholder="UUID" />
        </FormField>
        <FormField label="Quantity" required>
          <Input type="number" min="1" value={deductForm.quantity} onChange={(e) => setDeductForm({ ...deductForm, quantity: +e.target.value })} required />
        </FormField>
      </FormDrawer>
    </div>
  )
}
