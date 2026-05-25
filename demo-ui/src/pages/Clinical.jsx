import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { PageHeader } from '../molecules/PageHeader'
import { SearchBar } from '../molecules/SearchBar'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useSearchMolecules, useCreateMolecule, useBrandsByMolecule, useCreateBrand, useStoreList } from '../lib/hooks/useClinical'

const TABS = ['Molecules', 'Brands', 'Stores']
const moleculeSchema = z.object({ genericName: z.string().min(1), therapeuticClass: z.string().optional(), regulatorySchedule: z.string().optional(), dosageInstructions: z.string().optional(), safetyWarnings: z.string().optional() })
const storeTypeVariant = { PHYSICAL: 'teal', HUB: 'info', LOGICAL: 'neutral' }

export default function Clinical() {
  const navigate = useNavigate()
  const [tab, setTab] = useState(0)
  const [search, setSearch] = useState('')
  const [selectedMolecule, setSelectedMolecule] = useState(null)
  const [drawerOpen, setDrawerOpen] = useState(false)

  const { data: moleculesRaw } = useSearchMolecules(search)
  const { data: brandsRaw } = useBrandsByMolecule(selectedMolecule)
  const { items: stores, totalPages, loading: storesLoading } = useStoreList({ page: 1, size: 20 })
  const createMolecule = useCreateMolecule()
  const createBrand = useCreateBrand()

  const molecules = Array.isArray(moleculesRaw) ? moleculesRaw : (moleculesRaw?.content ?? [])
  const brands = Array.isArray(brandsRaw) ? brandsRaw : (brandsRaw?.content ?? [])

  const molForm = useForm({ resolver: zodResolver(moleculeSchema) })

  const handleCreateMolecule = async (data) => {
    await createMolecule.mutateAsync(data)
    setDrawerOpen(false)
  }

  return (
    <div className="space-y-4">
      <PageHeader title="Clinical" subtitle="Stores, molecules, and brands" actions={<Button onClick={() => setDrawerOpen(true)}>{tab === 0 ? 'Add Molecule' : tab === 2 ? 'Add Store' : ''}</Button>} />

      <div className="flex gap-1 bg-[var(--color-surface)] rounded-[var(--radius-md)] border border-[var(--color-border)] p-1">
        {TABS.map((t, i) => (
          <button key={t} onClick={() => setTab(i)} className={`flex-1 py-2 text-sm font-medium rounded-[var(--radius-md)] transition-colors cursor-pointer ${tab === i ? 'bg-[var(--color-primary)] text-white' : 'text-[var(--color-text-secondary)] hover:bg-[var(--color-background)]'}`}>{t}</button>
        ))}
      </div>

      {tab === 0 && (
        <>
          <SearchBar value={search} onChange={setSearch} placeholder="Search molecules..." />
          <DataTable
            columns={[
              { key: 'genericName', label: 'Generic Name' },
              { key: 'therapeuticClass', label: 'Class' },
              { key: 'regulatorySchedule', label: 'Schedule', render: (r) => r.regulatorySchedule ? <Badge variant="warning">{r.regulatorySchedule}</Badge> : '—' },
            ]}
            data={molecules}
            emptyMessage="Search for molecules..."
          />
        </>
      )}

      {tab === 1 && (
        <>
          <SearchBar value={search} onChange={setSearch} placeholder="Search molecule first..." />
          {molecules.length > 0 && (
            <div className="flex gap-2 flex-wrap">
              {molecules.slice(0, 10).map((m) => (
                <Button key={m.id} variant={selectedMolecule === m.id ? 'primary' : 'secondary'} size="sm" onClick={() => setSelectedMolecule(m.id)}>{m.genericName}</Button>
              ))}
            </div>
          )}
          {selectedMolecule && (
            <DataTable columns={[{ key: 'brandName', label: 'Brand' }, { key: 'strength', label: 'Strength' }, { key: 'form', label: 'Form' }]} data={brands} emptyMessage="No brands for this molecule" />
          )}
        </>
      )}

      {tab === 2 && (
        <DataTable
          columns={[
            { key: 'name', label: 'Name' },
            { key: 'type', label: 'Type', render: (r) => <Badge variant={storeTypeVariant[r.type] || 'neutral'}>{r.type}</Badge> },
            { key: 'active', label: 'Active', render: (r) => <Badge variant={r.active ? 'success' : 'neutral'}>{r.active ? 'Yes' : 'No'}</Badge> },
            { key: 'actions', label: '', render: (r) => <Button size="sm" variant="ghost" onClick={() => navigate(`/clinical/stores/${r.id}/products`)}>Products</Button> },
          ]}
          data={stores || []}
          loading={storesLoading}
          totalPages={totalPages}
        />
      )}

      <FormDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Create Molecule" onSubmit={molForm.handleSubmit(handleCreateMolecule)} loading={createMolecule.isPending}>
        <div className="space-y-4">
          <FormField label="Generic Name" required error={molForm.formState.errors.genericName?.message}><Input {...molForm.register('genericName')} /></FormField>
          <FormField label="Therapeutic Class"><Input {...molForm.register('therapeuticClass')} /></FormField>
          <FormField label="Regulatory Schedule"><Input {...molForm.register('regulatorySchedule')} placeholder="e.g. Schedule II" /></FormField>
          <FormField label="Dosage Instructions"><Input {...molForm.register('dosageInstructions')} /></FormField>
          <FormField label="Safety Warnings"><Input {...molForm.register('safetyWarnings')} /></FormField>
        </div>
      </FormDrawer>
    </div>
  )
}
