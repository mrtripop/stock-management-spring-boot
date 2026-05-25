import { useState } from 'react'
import { PageHeader } from '../molecules/PageHeader'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Button } from '../atoms/Button'
import { useStoreId } from '../lib/auth'
import { useSearchMolecules, useBrandsByMolecule } from '../lib/hooks/useClinical'
import { useQuickDispense } from '../lib/hooks/useTransactions'

const STEPS = ['Search', 'Build Invoice', 'Review', 'Done']

function toArray(raw) {
  return Array.isArray(raw) ? raw : (raw?.content ?? [])
}

export default function Dispensing() {
  const storeId = useStoreId()
  const [step, setStep] = useState(0)
  const [search, setSearch] = useState('')
  const [selectedMolecule, setSelectedMolecule] = useState(null)
  const [selectedBrand, setSelectedBrand] = useState(null)
  const [items, setItems] = useState([])
  const [completedInvoice, setCompletedInvoice] = useState(null)

  const { data: moleculesRaw } = useSearchMolecules(search)
  const { data: brandsRaw } = useBrandsByMolecule(selectedMolecule)
  const dispense = useQuickDispense()

  const molecules = toArray(moleculesRaw)
  const brands = toArray(brandsRaw)

  const addItem = () => {
    if (!selectedBrand) return
    setItems([...items, { brandId: selectedBrand.id, brandName: selectedBrand.brandName, quantity: 1, insuranceCoveragePercent: 0 }])
  }

  const removeItem = (idx) => setItems(items.filter((_, i) => i !== idx))
  const updateItem = (idx, field, value) => {
    const updated = [...items]
    updated[idx] = { ...updated[idx], [field]: value }
    setItems(updated)
  }

  const handleDispense = async () => {
    const result = await dispense.mutateAsync({ storeId, items: items.map((i) => ({ brandId: i.brandId, quantity: i.quantity, insuranceCoveragePercent: i.insuranceCoveragePercent })) })
    setCompletedInvoice(result)
    setStep(3)
  }

  const reset = () => { setStep(0); setSearch(''); setSelectedMolecule(null); setSelectedBrand(null); setItems([]); setCompletedInvoice(null) }

  const totalAmount = items.reduce((sum, i) => sum + (i.quantity || 0), 0)

  return (
    <div className="space-y-4">
      <PageHeader title="Dispensing" subtitle="Point-of-sale dispensing" actions={step === 3 ? <Button onClick={reset}>New Dispense</Button> : null} />

      {/* Step indicator */}
      <div className="flex gap-1">
        {STEPS.map((s, i) => (
          <div key={s} className="flex-1">
            <div className={`h-1.5 rounded-full transition-colors ${i <= step ? 'bg-[var(--color-primary)]' : 'bg-[var(--color-border)]'}`} />
            <p className={`text-xs mt-1 ${i <= step ? 'text-[var(--color-primary)] font-medium' : 'text-[var(--color-text-muted)]'}`}>{s}</p>
          </div>
        ))}
      </div>

      {/* Step 1: Search */}
      {step === 0 && (
        <div className="space-y-4">
          <FormField label="Search molecule"><Input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="e.g. amoxicillin" /></FormField>
          {molecules.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-medium text-[var(--color-text-primary)]">Select molecule:</p>
              {molecules.slice(0, 10).map((m) => (
                <button key={m.id} onClick={() => { setSelectedMolecule(m.id); setSearch('') }} className="w-full text-left p-3 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] hover:border-[var(--color-primary)] transition-colors cursor-pointer">
                  <p className="text-sm font-medium">{m.genericName}</p>
                  <p className="text-xs text-[var(--color-text-muted)]">{m.therapeuticClass || 'No class'}</p>
                </button>
              ))}
            </div>
          )}
          {selectedMolecule && brands.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-medium">Select brand:</p>
              {brands.map((b) => (
                <button key={b.id} onClick={() => { setSelectedBrand(b); setStep(1) }} className="w-full text-left p-3 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] hover:border-[var(--color-primary)] cursor-pointer">
                  <p className="text-sm font-medium">{b.brandName} {b.strength}</p>
                  <p className="text-xs text-[var(--color-text-muted)]">{b.form || ''} — {b.baseUnit || ''}</p>
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Step 2: Build invoice */}
      {step === 1 && (
        <div className="space-y-4">
          <div className="flex items-center gap-3 p-3 bg-[var(--color-primary-subtle)] rounded-[var(--radius-md)]">
            <span className="text-sm font-medium">{selectedBrand?.brandName} {selectedBrand?.strength}</span>
            <Button size="sm" onClick={addItem}>Add to invoice</Button>
          </div>
          {items.length > 0 && (
            <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] overflow-hidden">
              <table className="w-full text-sm">
                <thead><tr className="bg-[var(--color-background)] border-b border-[var(--color-border)]"><th className="px-4 py-2 text-left text-xs font-semibold text-[var(--color-text-muted)]">Brand</th><th className="px-4 py-2 text-left text-xs font-semibold text-[var(--color-text-muted)]">Qty</th><th className="px-4 py-2 text-left text-xs font-semibold text-[var(--color-text-muted)]">Insurance %</th><th className="w-16"></th></tr></thead>
                <tbody>
                  {items.map((item, idx) => (
                    <tr key={idx} className="border-b border-[var(--color-border)] last:border-0">
                      <td className="px-4 py-2">{item.brandName}</td>
                      <td className="px-4 py-2"><Input type="number" value={item.quantity} onChange={(e) => updateItem(idx, 'quantity', Number(e.target.value))} className="w-20" /></td>
                      <td className="px-4 py-2"><Input type="number" value={item.insuranceCoveragePercent} onChange={(e) => updateItem(idx, 'insuranceCoveragePercent', Number(e.target.value))} className="w-20" min={0} max={100} /></td>
                      <td className="px-4 py-2"><Button size="sm" variant="ghost" onClick={() => removeItem(idx)}>X</Button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setStep(0)}>Back</Button>
            <Button onClick={() => setStep(2)} disabled={items.length === 0}>Review ({items.length} items)</Button>
          </div>
        </div>
      )}

      {/* Step 3: Review */}
      {step === 2 && (
        <div className="space-y-4">
          <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-5">
            <p className="text-sm font-semibold mb-3">Invoice Summary</p>
            {items.map((item, i) => (
              <div key={i} className="flex justify-between text-sm py-1"><span>{item.brandName} x{item.quantity}</span><span className="text-[var(--color-text-muted)]">{item.insuranceCoveragePercent}% insurance</span></div>
            ))}
            <div className="border-t border-[var(--color-border)] mt-2 pt-2 text-sm font-semibold">Total items: {totalAmount}</div>
          </div>
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setStep(1)}>Back</Button>
            <Button onClick={handleDispense} loading={dispense.isPending}>Dispense</Button>
          </div>
        </div>
      )}

      {/* Step 4: Done */}
      {step === 3 && (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-[var(--color-success-subtle)] rounded-full flex items-center justify-center mx-auto mb-4 text-2xl">&#10003;</div>
          <h3 className="text-lg font-semibold text-[var(--color-text-primary)] mb-1">Dispensed Successfully</h3>
          <p className="text-sm text-[var(--color-text-secondary)]">Invoice #{completedInvoice?.id}</p>
        </div>
      )}
    </div>
  )
}
