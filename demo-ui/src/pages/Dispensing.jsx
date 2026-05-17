import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Select } from '../atoms/Select'
import { Input } from '../atoms/Input'
import { Button } from '../atoms/Button'
import { Badge } from '../atoms/Badge'
import { StatCard } from '../molecules/StatCard'
import { PageHeader } from '../molecules/PageHeader'
import { AlertDialog } from '../organisms/AlertDialog'
import { useQueryList, usePostMutation, api } from '../lib/hooks'

export default function Dispensing() {
  const [storeId, setStoreId] = useState('')
  const [summaryDate, setSummaryDate] = useState(() => new Date().toISOString().split('T')[0])
  const [invoiceItems, setInvoiceItems] = useState([])
  const [completedInvoice, setCompletedInvoice] = useState(null)

  const { items: stores } = useQueryList(
    ['stores'], '/clinical/stores', { page: 1, size: 100 }
  )

  const { data: summary, isLoading: summaryLoading } = useQuery({
    queryKey: ['daily-summary', storeId, summaryDate],
    queryFn: () => api.get(`/transaction/invoices/daily-summary?storeId=${storeId}&date=${summaryDate}`),
    enabled: !!storeId,
  })

  const handleAddItem = (item) => {
    setInvoiceItems((prev) => [...prev, item])
  }

  const handleRemoveItem = (index) => {
    setInvoiceItems((prev) => prev.filter((_, i) => i !== index))
  }

  const handleDispenseSuccess = (invoice) => {
    setCompletedInvoice(invoice)
    setInvoiceItems([])
  }

  return (
    <div>
      <PageHeader title="Dispensing" subtitle="Point of sale" />

      {/* Store selector + date picker */}
      <div className="flex items-end gap-3 mb-4">
        <div className="w-64">
          <label className="block text-xs font-medium text-[var(--color-text-secondary)] mb-1">Store</label>
          <Select value={storeId} onChange={(e) => setStoreId(e.target.value)}>
            <option value="">-- Select store --</option>
            {stores.map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </Select>
        </div>
        <div className="w-40">
          <label className="block text-xs font-medium text-[var(--color-text-secondary)] mb-1">Date</label>
          <input
            type="date"
            value={summaryDate}
            onChange={(e) => setSummaryDate(e.target.value)}
            className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] outline-none focus:border-[var(--color-primary)]"
          />
        </div>
      </div>

      {/* Daily summary */}
      {storeId && (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 mb-6">
          <StatCard title="Invoices" value={summary?.totalInvoices ?? '-'} accentColor="var(--color-primary)" loading={summaryLoading} />
          <StatCard title="Revenue" value={summary?.totalRevenue != null ? `$${Number(summary.totalRevenue).toFixed(2)}` : '-'} accentColor="var(--color-success)" loading={summaryLoading} />
          <StatCard title="Patient Paid" value={summary?.totalPatientPaid != null ? `$${Number(summary.totalPatientPaid).toFixed(2)}` : '-'} accentColor="var(--color-purple)" loading={summaryLoading} />
          <StatCard title="Insurance" value={summary?.totalInsuranceClaims != null ? `$${Number(summary.totalInsuranceClaims).toFixed(2)}` : '-'} accentColor="var(--color-info, #3b82f6)" loading={summaryLoading} />
          <StatCard title="Items" value={summary?.totalItemsDispensed ?? '-'} accentColor="var(--color-warning)" loading={summaryLoading} />
          <StatCard title="Voided" value={summary?.voidedCount ?? '-'} accentColor="var(--color-danger)" loading={summaryLoading} />
        </div>
      )}

      {!storeId ? (
        <div className="text-center py-12 text-sm text-[var(--color-text-muted)]">
          Select a store to begin dispensing
        </div>
      ) : completedInvoice ? (
        <DispenseConfirmation
          invoice={completedInvoice}
          onClose={() => setCompletedInvoice(null)}
        />
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {/* Left column: Invoice items */}
          <InvoiceItemsList
            items={invoiceItems}
            onRemoveItem={handleRemoveItem}
            storeId={storeId}
            onSuccess={handleDispenseSuccess}
          />
          {/* Right column: Product search */}
          <ProductSearchPanel
            storeId={storeId}
            onAddItem={handleAddItem}
          />
        </div>
      )}
    </div>
  )
}

/* ---- Product Search Panel ---- */
function ProductSearchPanel({ storeId, onAddItem }) {
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedMolecule, setSelectedMolecule] = useState(null)
  const [selectedBrand, setSelectedBrand] = useState(null)
  const [selectedBatch, setSelectedBatch] = useState(null)
  const [quantity, setQuantity] = useState(1)
  const [insurancePercent, setInsurancePercent] = useState(0)

  const { items: molecules, loading: moleculesLoading } = useQueryList(
    ['molecules-search', searchQuery],
    '/clinical/catalog/molecules/search',
    { query: searchQuery },
    { enabled: searchQuery.length >= 2 }
  )

  const { items: brands } = useQueryList(
    ['molecule-brands', selectedMolecule?.id],
    `/clinical/catalog/molecules/${selectedMolecule?.id}/brands`,
    { page: 1, size: 100 },
    { enabled: !!selectedMolecule }
  )

  const { items: batches } = useQueryList(
    ['brand-batches', selectedBrand?.id],
    '/inventory/batches',
    { brandId: selectedBrand?.id, page: 1, size: 100 },
    { enabled: !!selectedBrand }
  )

  const handleAdd = () => {
    if (!selectedBrand || !selectedBatch) {
      toast.error('Select a brand and batch')
      return
    }
    if (quantity < 1 || quantity > selectedBatch.quantity) {
      toast.error(`Quantity must be between 1 and ${selectedBatch.quantity}`)
      return
    }

    onAddItem({
      brandId: selectedBrand.id,
      brandName: selectedBrand.brandName,
      batchId: selectedBatch.id,
      batchNumber: selectedBatch.batchNumber,
      expiryDate: selectedBatch.expiryDate,
      availableQty: selectedBatch.quantity,
      quantity,
      unitPrice: selectedBatch.unitPrice ?? selectedBrand.basePrice ?? 0,
      insuranceCoveragePercent: insurancePercent,
    })

    setSelectedBatch(null)
    setQuantity(1)
    setInsurancePercent(0)
  }

  const handleResetSearch = () => {
    setSearchQuery('')
    setSelectedMolecule(null)
    setSelectedBrand(null)
    setSelectedBatch(null)
    setQuantity(1)
    setInsurancePercent(0)
  }

  return (
    <div className="bg-white rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border-light)] p-4">
      <h3 className="text-sm font-semibold text-[var(--color-text-primary)] mb-3">Add Item</h3>

      {/* Step 1: Search molecules */}
      <div className="mb-3">
        <label className="block text-xs font-medium text-[var(--color-text-secondary)] mb-1">Search Product</label>
        <div className="flex gap-2">
          <Input
            placeholder="Type molecule or product name..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value)
              setSelectedMolecule(null)
              setSelectedBrand(null)
              setSelectedBatch(null)
            }}
          />
          <Button variant="secondary" onClick={handleResetSearch}>Clear</Button>
        </div>
      </div>

      {/* Molecule results */}
      {searchQuery.length >= 2 && !selectedMolecule && (
        <div className="mb-3 border border-[var(--color-border-light)] rounded-[var(--radius-md)] max-h-40 overflow-y-auto">
          {moleculesLoading ? (
            <div className="px-3 py-4 text-center text-xs text-[var(--color-text-muted)]">Searching...</div>
          ) : molecules.length === 0 ? (
            <div className="px-3 py-4 text-center text-xs text-[var(--color-text-muted)]">No molecules found</div>
          ) : (
            molecules.map((m) => (
              <button
                key={m.id}
                onClick={() => setSelectedMolecule(m)}
                className="w-full text-left px-3 py-2 text-sm hover:bg-slate-50 border-b border-[var(--color-border-light)] last:border-b-0 cursor-pointer"
              >
                <span className="font-medium text-[var(--color-text-primary)]">{m.genericName}</span>
                {m.therapeuticClass && (
                  <span className="ml-2 text-xs text-[var(--color-text-secondary)]">{m.therapeuticClass}</span>
                )}
              </button>
            ))
          )}
        </div>
      )}

      {/* Selected molecule tag + Step 2: Brand selection */}
      {selectedMolecule && (
        <div className="mb-3">
          <div className="flex items-center gap-2 mb-2">
            <Badge variant="info">{selectedMolecule.genericName}</Badge>
            <button onClick={() => { setSelectedMolecule(null); setSelectedBrand(null); setSelectedBatch(null) }}
              className="text-xs text-[var(--color-text-muted)] hover:text-[var(--color-danger)] cursor-pointer">change</button>
          </div>
          <label className="block text-xs font-medium text-[var(--color-text-secondary)] mb-1">Brand</label>
          <Select value={selectedBrand?.id || ''} onChange={(e) => {
            const brand = brands.find((b) => b.id === e.target.value)
            setSelectedBrand(brand || null)
            setSelectedBatch(null)
          }}>
            <option value="">-- Select brand --</option>
            {brands.map((b) => (
              <option key={b.id} value={b.id}>{b.brandName} {b.strength ? `(${b.strength})` : ''}</option>
            ))}
          </Select>
        </div>
      )}

      {/* Step 3: Batch selection */}
      {selectedBrand && (
        <div className="mb-3">
          <label className="block text-xs font-medium text-[var(--color-text-secondary)] mb-1">Batch</label>
          <Select value={selectedBatch?.id || ''} onChange={(e) => {
            const batch = batches.find((b) => b.id === Number(e.target.value))
            setSelectedBatch(batch || null)
          }}>
            <option value="">-- Select batch --</option>
            {batches.map((b) => (
              <option key={b.id} value={b.id}>
                {b.batchNumber} | Exp: {b.expiryDate ? new Date(b.expiryDate).toLocaleDateString() : '-'} | Qty: {b.quantity}
              </option>
            ))}
          </Select>
        </div>
      )}

      {/* Quantity + Insurance + Add button */}
      {selectedBatch && (
        <div className="space-y-3 pt-2 border-t border-[var(--color-border-light)]">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-[var(--color-text-secondary)] mb-1">
                Quantity <span className="text-[var(--color-text-muted)]">(max: {selectedBatch.quantity})</span>
              </label>
              <Input
                type="number"
                min={1}
                max={selectedBatch.quantity}
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-[var(--color-text-secondary)] mb-1">
                Insurance % ({insurancePercent}%)
              </label>
              <input
                type="range"
                min={0}
                max={100}
                value={insurancePercent}
                onChange={(e) => setInsurancePercent(Number(e.target.value))}
                className="w-full mt-2 accent-[var(--color-primary)]"
              />
            </div>
          </div>
          <Button onClick={handleAdd} className="w-full">+ Add to Invoice</Button>
        </div>
      )}
    </div>
  )
}

/* ---- Invoice Items List ---- */
function InvoiceItemsList({ items, onRemoveItem, storeId, onSuccess }) {
  const dispenseMutation = usePostMutation(
    ['invoices', 'daily-summary'],
    '/transaction/invoices/dispense'
  )

  const totalAmount = items.reduce((sum, item) => sum + item.quantity * item.unitPrice, 0)
  const totalInsurance = items.reduce(
    (sum, item) => sum + (item.quantity * item.unitPrice * item.insuranceCoveragePercent) / 100, 0
  )
  const patientOwed = totalAmount - totalInsurance

  const handleDispense = async () => {
    const payload = {
      storeId,
      items: items.map((item) => ({
        brandId: item.brandId,
        batchId: item.batchId,
        quantity: item.quantity,
        insuranceCoveragePercent: item.insuranceCoveragePercent,
      })),
    }
    try {
      const result = await dispenseMutation.mutateAsync(payload)
      toast.success(`Invoice #${result.data?.id ?? ''} dispensed successfully`)
      onSuccess(result.data)
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <div className="bg-white rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border-light)] flex flex-col">
      <div className="px-4 py-3 border-b border-[var(--color-border-light)]">
        <h3 className="text-sm font-semibold text-[var(--color-text-primary)]">
          Invoice Items ({items.length})
        </h3>
      </div>

      {/* Items list */}
      <div className="flex-1 overflow-y-auto max-h-[400px]">
        {items.length === 0 ? (
          <div className="px-4 py-8 text-center text-sm text-[var(--color-text-muted)]">
            No items added yet. Use the search panel to add products.
          </div>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-[var(--color-border-light)]">
                <th className="px-3 py-2 text-left text-xs font-semibold text-[var(--color-text-muted)] uppercase">Product</th>
                <th className="px-3 py-2 text-right text-xs font-semibold text-[var(--color-text-muted)] uppercase">Qty</th>
                <th className="px-3 py-2 text-right text-xs font-semibold text-[var(--color-text-muted)] uppercase">Price</th>
                <th className="px-3 py-2 text-right text-xs font-semibold text-[var(--color-text-muted)] uppercase">Ins.</th>
                <th className="px-3 py-2 text-right text-xs font-semibold text-[var(--color-text-muted)] uppercase">Line</th>
                <th className="px-3 py-2 w-8"></th>
              </tr>
            </thead>
            <tbody>
              {items.map((item, index) => {
                const lineTotal = item.quantity * item.unitPrice
                return (
                  <tr key={index} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
                    <td className="px-3 py-2">
                      <div className="text-sm font-medium text-[var(--color-text-primary)]">{item.brandName}</div>
                      <div className="text-xs text-[var(--color-text-secondary)]">Batch: {item.batchNumber}</div>
                    </td>
                    <td className="px-3 py-2 text-right text-sm">{item.quantity}</td>
                    <td className="px-3 py-2 text-right text-sm">${item.unitPrice.toFixed(2)}</td>
                    <td className="px-3 py-2 text-right text-sm">{item.insuranceCoveragePercent}%</td>
                    <td className="px-3 py-2 text-right text-sm font-medium">${lineTotal.toFixed(2)}</td>
                    <td className="px-3 py-2">
                      <button onClick={() => onRemoveItem(index)}
                        className="text-[var(--color-text-muted)] hover:text-[var(--color-danger)] cursor-pointer">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Totals + Dispense button */}
      {items.length > 0 && (
        <div className="px-4 py-3 border-t border-[var(--color-border-light)] bg-slate-50 space-y-1">
          <div className="flex justify-between text-sm">
            <span className="text-[var(--color-text-secondary)]">Total</span>
            <span className="font-medium">${totalAmount.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-[var(--color-text-secondary)]">Insurance</span>
            <span className="text-[var(--color-info, #3b82f6)]">${totalInsurance.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm font-semibold border-t border-[var(--color-border-light)] pt-1">
            <span>Patient Owed</span>
            <span>${patientOwed.toFixed(2)}</span>
          </div>
          <Button onClick={handleDispense} loading={dispenseMutation.isPending} className="w-full mt-2">
            Dispense ({items.length} item{items.length > 1 ? 's' : ''})
          </Button>
        </div>
      )}
    </div>
  )
}

/* ---- Dispense Confirmation ---- */
function DispenseConfirmation({ invoice, onClose }) {
  const [showVoidDialog, setShowVoidDialog] = useState(false)
  const voidMutation = usePostMutation(
    ['invoices', 'daily-summary'],
    `/transaction/invoices/${invoice?.id}/void`
  )

  const handleVoid = async () => {
    try {
      await voidMutation.mutateAsync({})
      toast.success('Invoice voided successfully')
      setShowVoidDialog(false)
      onClose()
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <div className="bg-white rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border-light)] p-6 max-w-lg mx-auto">
      <div className="text-center mb-4">
        <div className="w-12 h-12 bg-emerald-100 rounded-full flex items-center justify-center mx-auto mb-3">
          <svg className="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h3 className="text-lg font-semibold text-[var(--color-text-primary)]">Dispensed Successfully</h3>
        <p className="text-sm text-[var(--color-text-secondary)] mt-1">Invoice #{invoice?.id}</p>
      </div>

      {/* Invoice summary */}
      <div className="space-y-2 mb-4 text-sm">
        <div className="flex justify-between">
          <span className="text-[var(--color-text-secondary)]">Total Amount</span>
          <span className="font-medium">${Number(invoice?.totalAmount ?? 0).toFixed(2)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-[var(--color-text-secondary)]">Patient Owed</span>
          <span className="font-medium">${Number(invoice?.patientOwed ?? 0).toFixed(2)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-[var(--color-text-secondary)]">Insurance Claims</span>
          <span className="font-medium">${Number(invoice?.insuranceClaimAmount ?? 0).toFixed(2)}</span>
        </div>
      </div>

      {/* Items list */}
      {invoice?.items?.length > 0 && (
        <div className="border-t border-[var(--color-border-light)] pt-3 mb-4">
          <h4 className="text-xs font-semibold text-[var(--color-text-muted)] uppercase mb-2">Items</h4>
          <div className="space-y-1">
            {invoice.items.map((item, i) => (
              <div key={i} className="flex justify-between text-sm">
                <span className="text-[var(--color-text-primary)]">{item.brandName} x{item.quantity}</span>
                <span className="text-[var(--color-text-secondary)]">${Number(item.lineTotal ?? 0).toFixed(2)}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Action buttons */}
      <div className="flex gap-2">
        <Button variant="danger" onClick={() => setShowVoidDialog(true)} className="flex-1">Void Invoice</Button>
        <Button onClick={onClose} className="flex-1">New Dispense</Button>
      </div>

      {/* Void confirmation */}
      <AlertDialog
        open={showVoidDialog}
        onClose={() => setShowVoidDialog(false)}
        onConfirm={handleVoid}
        title="Void Invoice"
        message="Voiding will restore stock to inventory. This cannot be undone."
        confirmLabel="Void"
        loading={voidMutation.isPending}
      />
    </div>
  )
}
