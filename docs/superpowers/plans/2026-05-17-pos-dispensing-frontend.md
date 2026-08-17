# POS Dispensing Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a POS-style dispensing UI for pharmacy staff to quickly record sales, auto-deduct inventory, and view daily summaries, plus enhance the Transactions page with invoice detail/void capabilities.

**Architecture:** Single-page dispensing workflow with two-column layout — left for invoice items, right for product search and payment summary. Product search follows a 3-step flow: search molecules → select brand → pick batch. The Transactions page gains an Invoices tab with a detail drawer for viewing/managing invoices.

**Tech Stack:** React 19, Vite, Tailwind CSS 4, TanStack React Query, Headless UI, Sonner (toasts). All API calls through existing `demo-ui/src/lib/api.js` and `demo-ui/src/lib/hooks.js`.

---

## File Structure

| Action | File | Responsibility |
|--------|------|---------------|
| Create | `demo-ui/src/pages/Dispensing.jsx` | Dispensing page: store selector, daily summary, product search, invoice builder, dispense action |
| Modify | `demo-ui/src/App.jsx` | Add `/dispensing` route |
| Modify | `demo-ui/src/organisms/Sidebar.jsx` | Add "Dispensing" nav item |
| Modify | `demo-ui/src/atoms/Icon.jsx` | Add `receipt` icon mapping |
| Modify | `demo-ui/src/pages/Transactions.jsx` | Add Invoices tab with listing, detail drawer, void flow |

---

## API Endpoints Used

| Purpose | Method | Endpoint |
|---------|--------|----------|
| List stores | GET | `/clinical/stores?page=1&size=100` |
| Search molecules | GET | `/clinical/catalog/molecules/search?query={q}` |
| List brands for molecule | GET | `/clinical/catalog/molecules/{id}/brands?page=1&size=100` |
| List batches for brand | GET | `/inventory/batches?brandId={id}&page=1&size=100` |
| Daily summary | GET | `/transaction/invoices/daily-summary?storeId={id}&date={date}` |
| Dispense | POST | `/transaction/invoices/dispense` |
| List invoices | GET | `/transaction/invoices?storeId={id}&page=1&size=20` |
| Get invoice detail | GET | `/transaction/invoices/{id}` |
| Void invoice | POST | `/transaction/invoices/{id}/void` |
| Complete invoice | POST | `/transaction/invoices/{id}/complete` |

---

### Task 1: Add Dispensing Route, Sidebar Entry, and Icon

**Files:**
- Modify: `demo-ui/src/App.jsx`
- Modify: `demo-ui/src/organisms/Sidebar.jsx`
- Modify: `demo-ui/src/atoms/Icon.jsx`

- [ ] **Step 1: Add receipt icon to Icon.jsx**

Add `receipt` to the icon map in `demo-ui/src/atoms/Icon.jsx`:

```javascript
// Add this entry to the iconMap object:
'receipt': Icons.DocumentTextIcon,
```

- [ ] **Step 2: Add Dispensing nav item to Sidebar.jsx**

Add an entry to the `NAV_ITEMS` array in `demo-ui/src/organisms/Sidebar.jsx`, after the "Clinical" entry:

```javascript
  { to: '/clinical', label: 'Clinical', icon: 'beaker' },
  { to: '/dispensing', label: 'Dispensing', icon: 'receipt' },
  { to: '/orders', label: 'Orders', icon: 'cart' },
```

- [ ] **Step 3: Add Dispensing route to App.jsx**

Add the import and route in `demo-ui/src/App.jsx`:

```javascript
// Add import at top with the other page imports:
import Dispensing from './pages/Dispensing'

// Add route inside the protected <Routes>, after the clinical route:
            <Route path="clinical" element={<Clinical />} />
            <Route path="dispensing" element={<Dispensing />} />
            <Route path="orders" element={<Orders />} />
```

- [ ] **Step 4: Create a placeholder Dispensing.jsx to verify routing**

Create `demo-ui/src/pages/Dispensing.jsx` with a minimal placeholder:

```javascript
import { PageHeader } from '../molecules/PageHeader'

export default function Dispensing() {
  return (
    <div>
      <PageHeader title="Dispensing" subtitle="Point of sale" />
      <p className="text-sm text-[var(--color-text-secondary)]">Loading...</p>
    </div>
  )
}
```

- [ ] **Step 5: Verify the route works**

Run: `cd demo-ui && npm run dev`

Open the app, verify "Dispensing" appears in the sidebar, click it, and confirm the placeholder page loads.

---

### Task 2: Build Store Selector and Daily Summary Section

**Files:**
- Modify: `demo-ui/src/pages/Dispensing.jsx` (full rewrite of placeholder)

This task builds the top section of the Dispensing page: a store selector dropdown and a row of daily summary stat cards.

- [ ] **Step 1: Write the Dispensing page with store selector and daily summary**

Replace the entire contents of `demo-ui/src/pages/Dispensing.jsx`:

```javascript
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Select } from '../atoms/Select'
import { StatCard } from '../molecules/StatCard'
import { PageHeader } from '../molecules/PageHeader'
import { useQueryList, api } from '../lib/hooks'

export default function Dispensing() {
  const [storeId, setStoreId] = useState('')
  const [summaryDate, setSummaryDate] = useState(() => new Date().toISOString().split('T')[0])

  const { items: stores } = useQueryList(
    ['stores'], '/clinical/stores', { page: 1, size: 100 }
  )

  const { data: summary, isLoading: summaryLoading } = useQuery({
    queryKey: ['daily-summary', storeId, summaryDate],
    queryFn: () => api.get(`/transaction/invoices/daily-summary?storeId=${storeId}&date=${summaryDate}`),
    enabled: !!storeId,
  })

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

      {/* Daily summary stat cards */}
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

      {!storeId && (
        <div className="text-center py-12 text-sm text-[var(--color-text-muted)]">
          Select a store to begin dispensing
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 2: Verify store selector and summary**

Run: `cd demo-ui && npm run dev`

Select a store from the dropdown. Verify stat cards appear with data (or loading spinners). Change the date and confirm the summary refreshes.

---

### Task 3: Build Product Search Panel (Molecule → Brand → Batch Selection)

**Files:**
- Modify: `demo-ui/src/pages/Dispensing.jsx`

This task adds the right column: a product search panel with a 3-step flow (search molecules → pick brand → pick batch → set quantity/insurance → add to invoice).

- [ ] **Step 1: Add the ProductSearchPanel component to Dispensing.jsx**

Add the following component and wire it into the main Dispensing component. The full updated file:

```javascript
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Select } from '../atoms/Select'
import { Input } from '../atoms/Input'
import { Button } from '../atoms/Button'
import { Badge } from '../atoms/Badge'
import { StatCard } from '../molecules/StatCard'
import { PageHeader } from '../molecules/PageHeader'
import { useQueryList, api } from '../lib/hooks'

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
```

- [ ] **Step 2: Verify product search flow**

Run: `cd demo-ui && npm run dev`

Select a store, type in the search box, verify molecules appear. Select a molecule, verify brands populate. Select a brand, verify batches populate. Select a batch and confirm quantity/insurance controls appear. Click "Add to Invoice" and verify it fires without errors (the list component comes next).

---

### Task 4: Build Invoice Items List with Running Totals and Dispense Action

**Files:**
- Modify: `demo-ui/src/pages/Dispensing.jsx`

This task adds the left column (invoice items list, running totals, dispense button) and the dispense confirmation view.

- [ ] **Step 1: Add InvoiceItemsList and DispenseConfirmation components**

Append these two components at the end of `demo-ui/src/pages/Dispensing.jsx` (after the `ProductSearchPanel` component's closing brace):

```javascript
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
                const insuranceAmt = (lineTotal * item.insuranceCoveragePercent) / 100
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
```

Also add the missing imports at the top of the file. The full import block should be:

```javascript
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
```

- [ ] **Step 2: Verify full dispensing flow**

Run: `cd demo-ui && npm run dev`

1. Select a store
2. Search for a molecule, select a brand, pick a batch
3. Set quantity and insurance, click "Add to Invoice"
4. Verify the item appears in the left column with correct totals
5. Click "Dispense" and verify the confirmation screen appears
6. Click "New Dispense" and verify the form resets

---

### Task 5: Enhance Transactions Page with Invoices Tab and Detail Drawer

**Files:**
- Modify: `demo-ui/src/pages/Transactions.jsx`

This task adds an "Invoices" tab to the Transactions page, listing invoices from the invoice API, and a detail drawer that opens on row click showing full invoice details with status-based action buttons.

- [ ] **Step 1: Replace Transactions.jsx with tabbed layout**

Replace the entire contents of `demo-ui/src/pages/Transactions.jsx`:

```javascript
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
```

- [ ] **Step 2: Verify Transactions page**

Run: `cd demo-ui && npm run dev`

1. Navigate to Transactions page
2. Verify "Stock Movements" tab shows the original transaction log
3. Switch to "Invoices" tab
4. Verify invoice list loads with ID, store name, status, total, and date
5. Click an invoice row — verify the detail drawer opens
6. Verify items table, totals, and action buttons appear based on status
7. Test the Void flow — verify the confirmation dialog shows the correct message

---

### Task 6: Final Wiring and Commit

**Files:** No new files — final verification and commit.

- [ ] **Step 1: Run the dev server and verify the complete flow**

Run: `cd demo-ui && npm run dev`

Complete end-to-end smoke test:
1. Sidebar shows "Dispensing" link, navigates to `/dispensing`
2. Store selector populates from the API
3. Selecting a store shows daily summary stat cards
4. Product search: type → select molecule → select brand → select batch → set quantity/insurance → add to invoice
5. Invoice items list shows running totals (total, insurance, patient owed)
6. Dispense button posts to API and shows confirmation on success
7. Confirmation shows invoice details and void option
8. Transactions page has Invoices tab
9. Invoice rows are clickable, opening detail drawer
10. Void confirmation shows appropriate message per status

- [ ] **Step 2: Commit**

```bash
git add demo-ui/src/pages/Dispensing.jsx demo-ui/src/pages/Transactions.jsx demo-ui/src/App.jsx demo-ui/src/organisms/Sidebar.jsx demo-ui/src/atoms/Icon.jsx
git commit -m "feat(ui): add POS dispensing page and invoice management

- Dispensing page with product search (molecule→brand→batch),
  invoice builder with running totals, and dispense action
- Daily summary stat cards with store selector
- Invoices tab on Transactions page with detail drawer
- Invoice void flow with status-aware confirmation messages
- Dispense confirmation with invoice details and void option"
```
