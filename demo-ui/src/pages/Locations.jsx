import { useState } from 'react'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { PageHeader } from '../molecules/PageHeader'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useAddressList, useCreateAddress } from '../lib/hooks/useLocations'

const TABS = ['Addresses', 'Warehouses']

export default function Locations() {
  const [tab, setTab] = useState(0)
  const [page, setPage] = useState(1)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const { items: addresses, totalPages, loading } = useAddressList({ page, size: 20 })
  const createAddress = useCreateAddress()

  const handleSubmit = async (e) => {
    e.preventDefault()
    const fd = new FormData(e.target)
    await createAddress.mutateAsync({ addressName: fd.get('addressName'), line1: fd.get('line1'), city: fd.get('city'), province: fd.get('province'), country: fd.get('country'), postalCode: fd.get('postalCode') })
    setDrawerOpen(false)
  }

  return (
    <div className="space-y-4">
      <PageHeader title="Locations" subtitle="Addresses and warehouses" actions={<Button onClick={() => setDrawerOpen(true)}>Add Address</Button>} />
      <div className="flex gap-1 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] p-1">
        {TABS.map((t, i) => (
          <button key={t} onClick={() => setTab(i)} className={`flex-1 py-2 text-sm font-medium rounded-[var(--radius-md)] transition-colors cursor-pointer ${tab === i ? 'bg-[var(--color-primary)] text-white' : 'text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]'}`}>{t}</button>
        ))}
      </div>

      {tab === 0 && (
        <DataTable
          columns={[
            { key: 'addressName', label: 'Name' },
            { key: 'city', label: 'City' },
            { key: 'province', label: 'Province' },
            { key: 'country', label: 'Country' },
          ]}
          data={addresses || []}
          loading={loading}
          currentPage={page}
          totalPages={totalPages}
          onPageChange={setPage}
        />
      )}

      {tab === 1 && (
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] border border-[var(--color-border)] p-8 text-center">
          <p className="text-sm text-[var(--color-text-muted)]">Warehouse management coming soon.</p>
        </div>
      )}

      <FormDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Create Address" onSubmit={handleSubmit} loading={createAddress.isPending}>
        <div className="space-y-4">
          <FormField label="Address Name" required><Input name="addressName" placeholder="Main Office" /></FormField>
          <FormField label="Line 1" required><Input name="line1" placeholder="123 Main St" /></FormField>
          <div className="grid grid-cols-2 gap-4">
            <FormField label="City" required><Input name="city" /></FormField>
            <FormField label="Province"><Input name="province" /></FormField>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Country" required><Input name="country" /></FormField>
            <FormField label="Postal Code"><Input name="postalCode" /></FormField>
          </div>
        </div>
      </FormDrawer>
    </div>
  )
}
