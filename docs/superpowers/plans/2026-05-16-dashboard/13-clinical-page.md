### Task 13: Clinical Page

**Files:**
- Rewrite: `demo-ui/src/pages/Clinical.jsx`

- [ ] **Step 1: Rewrite Clinical.jsx with tabs**

Replace `demo-ui/src/pages/Clinical.jsx`:

```jsx
import { useState } from 'react'
import { Tab, TabGroup, TabList, TabPanel, TabPanels } from '@headlessui/react'
import { toast } from 'sonner'
import { Button } from '../atoms/Button'
import { Badge } from '../atoms/Badge'
import { Input } from '../atoms/Input'
import { Select } from '../atoms/Select'
import { PageHeader } from '../molecules/PageHeader'
import { SearchBar } from '../molecules/SearchBar'
import { FormField } from '../molecules/FormField'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { AlertDialog } from '../organisms/AlertDialog'
import { useQueryList, useCreateMutation, useDeleteMutation } from '../lib/hooks'

export default function Clinical() {
  const [tabIndex, setTabIndex] = useState(0)

  return (
    <div>
      <PageHeader title="Clinical" subtitle="Stores, molecules, and brands" />

      <TabGroup selectedIndex={tabIndex} onChange={setTabIndex}>
        <TabList className="flex gap-1 mb-5 bg-slate-100 rounded-[var(--radius-md)] p-1 w-fit">
          {['Stores', 'Molecules', 'Brands'].map((name) => (
            <Tab key={name} className="px-4 py-1.5 text-sm font-medium rounded-[var(--radius-sm)] cursor-pointer transition-colors data-[selected]:bg-white data-[selected]:text-[var(--color-primary)] data-[selected]:shadow-sm text-[var(--color-text-secondary)]">
              {name}
            </Tab>
          ))}
        </TabList>
        <TabPanels>
          <TabPanel><StoresTab /></TabPanel>
          <TabPanel><MoleculesTab /></TabPanel>
          <TabPanel><BrandsTab /></TabPanel>
        </TabPanels>
      </TabGroup>
    </div>
  )
}

/* ---- Stores Tab ---- */
function StoresTab() {
  const [page, setPage] = useState(1)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', type: 'PHYSICAL' })
  const [deleteTarget, setDeleteTarget] = useState(null)

  const { items, totalPages, totalElements, loading } = useQueryList(
    ['stores'], '/clinical/stores', { page, size: 10 }
  )
  const createMutation = useCreateMutation(['stores'], '/clinical/stores')
  const deleteMutation = useDeleteMutation(['stores'], '/clinical/stores')

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await createMutation.mutateAsync(form)
      toast.success('Store created')
      setShowForm(false)
      setForm({ name: '', type: 'PHYSICAL' })
    } catch (err) { toast.error(err.message) }
  }

  const handleDelete = async () => {
    try {
      await deleteMutation.mutateAsync(deleteTarget.id)
      toast.success('Store deleted')
      setDeleteTarget(null)
    } catch (err) { toast.error(err.message) }
  }

  const columns = [
    { key: 'name', label: 'Name' },
    { key: 'type', label: 'Type' },
    { key: 'actions', label: '', width: '80px' },
  ]

  return (
    <>
      <div className="flex justify-end mb-3">
        <Button onClick={() => setShowForm(true)}>+ Add Store</Button>
      </div>
      <DataTable columns={columns} data={items} loading={loading}
        currentPage={page} totalPages={totalPages} totalElements={totalElements}
        pageSize={10} onPageChange={setPage} emptyMessage="No stores found"
        renderRow={(s) => (
          <tr key={s.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-primary)]">{s.name || '-'}</td>
            <td className="px-4 py-2.5"><Badge variant="success">{s.type}</Badge></td>
            <td className="px-4 py-2.5 text-right">
              <button onClick={() => setDeleteTarget(s)} className="text-[var(--color-danger)] text-xs font-medium hover:underline cursor-pointer">Delete</button>
            </td>
          </tr>
        )}
      />
      <FormDrawer open={showForm} onClose={() => setShowForm(false)} title="New Store" onSubmit={handleSubmit} loading={createMutation.isPending}>
        <FormField label="Name" required><Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></FormField>
        <FormField label="Type">
          <Select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            <option value="PHYSICAL">Physical</option>
            <option value="HUB">Hub</option>
            <option value="LOGICAL">Logical</option>
          </Select>
        </FormField>
      </FormDrawer>
      <AlertDialog open={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete}
        title="Delete Store" message={deleteTarget ? `Delete "${deleteTarget.name}"?` : ''} loading={deleteMutation.isPending} />
    </>
  )
}

/* ---- Molecules Tab ---- */
function MoleculesTab() {
  const [search, setSearch] = useState('a')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ genericName: '', therapeuticClass: '', regulatorySchedule: '', dosageInstructions: '', safetyWarnings: '' })

  const { items, loading } = useQueryList(
    ['molecules'], '/clinical/catalog/molecules/search', { query: search || 'a' }
  )
  const createMutation = useCreateMutation(['molecules'], '/clinical/catalog/molecules')

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await createMutation.mutateAsync(form)
      toast.success('Molecule created')
      setShowForm(false)
      setForm({ genericName: '', therapeuticClass: '', regulatorySchedule: '', dosageInstructions: '', safetyWarnings: '' })
    } catch (err) { toast.error(err.message) }
  }

  const columns = [
    { key: 'genericName', label: 'Generic Name' },
    { key: 'therapeuticClass', label: 'Therapeutic Class' },
    { key: 'regulatorySchedule', label: 'Schedule' },
  ]

  return (
    <>
      <div className="flex justify-between mb-3">
        <div className="w-64"><SearchBar placeholder="Search molecules..." onSearch={setSearch} /></div>
        <Button onClick={() => setShowForm(true)}>+ Add Molecule</Button>
      </div>
      <DataTable columns={columns} data={items} loading={loading} emptyMessage="No molecules found"
        renderRow={(m) => (
          <tr key={m.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
            <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{m.genericName}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{m.therapeuticClass || '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{m.regulatorySchedule || '-'}</td>
          </tr>
        )}
      />
      <FormDrawer open={showForm} onClose={() => setShowForm(false)} title="New Molecule" onSubmit={handleSubmit} loading={createMutation.isPending}>
        <FormField label="Generic Name" required><Input value={form.genericName} onChange={(e) => setForm({ ...form, genericName: e.target.value })} required /></FormField>
        <FormField label="Therapeutic Class"><Input value={form.therapeuticClass} onChange={(e) => setForm({ ...form, therapeuticClass: e.target.value })} /></FormField>
        <FormField label="Regulatory Schedule"><Input value={form.regulatorySchedule} onChange={(e) => setForm({ ...form, regulatorySchedule: e.target.value })} /></FormField>
        <FormField label="Dosage Instructions"><textarea value={form.dosageInstructions} onChange={(e) => setForm({ ...form, dosageInstructions: e.target.value })} rows={2} className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] outline-none" /></FormField>
        <FormField label="Safety Warnings"><textarea value={form.safetyWarnings} onChange={(e) => setForm({ ...form, safetyWarnings: e.target.value })} rows={2} className="w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] outline-none" /></FormField>
      </FormDrawer>
    </>
  )
}

/* ---- Brands Tab ---- */
function BrandsTab() {
  const [moleculeId, setMoleculeId] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ moleculeId: '', brandName: '', strength: '', form: '', baseUnit: '', barcode: '' })

  const { items: allMolecules } = useQueryList(['molecules-list'], '/clinical/catalog/molecules/search', { query: 'a' })
  const { items: brands, loading } = useQueryList(
    ['brands'], `/clinical/catalog/molecules/${moleculeId}/brands`, {}, { enabled: !!moleculeId }
  )
  const createMutation = useCreateMutation(['brands'], '/clinical/catalog/brands')

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await createMutation.mutateAsync({ ...form, moleculeId })
      toast.success('Brand created')
      setShowForm(false)
      setForm({ moleculeId: '', brandName: '', strength: '', form: '', baseUnit: '', barcode: '' })
    } catch (err) { toast.error(err.message) }
  }

  const columns = [
    { key: 'brandName', label: 'Brand Name' },
    { key: 'barcode', label: 'Barcode' },
    { key: 'strength', label: 'Strength' },
    { key: 'form', label: 'Form' },
    { key: 'baseUnit', label: 'Base Unit' },
  ]

  return (
    <>
      <div className="flex justify-between items-center mb-3">
        <div className="flex items-center gap-2">
          <label className="text-xs font-medium text-[var(--color-text-secondary)]">Filter by Molecule:</label>
          <select value={moleculeId} onChange={(e) => setMoleculeId(e.target.value)}
            className="px-3 py-1.5 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] outline-none bg-white">
            <option value="">-- Select molecule --</option>
            {allMolecules.map((m) => <option key={m.id} value={m.id}>{m.genericName}</option>)}
          </select>
        </div>
        <Button onClick={() => setShowForm(true)} disabled={!moleculeId}>+ Add Brand</Button>
      </div>
      <DataTable columns={columns} data={brands} loading={loading}
        emptyMessage={moleculeId ? 'No brands found' : 'Select a molecule above'}
        renderRow={(b) => (
          <tr key={b.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
            <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{b.brandName}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)] font-mono">{b.barcode || '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{b.strength || '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{b.form || '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{b.baseUnit || '-'}</td>
          </tr>
        )}
      />
      <FormDrawer open={showForm} onClose={() => setShowForm(false)} title="New Brand" onSubmit={handleSubmit} loading={createMutation.isPending}>
        <FormField label="Brand Name" required><Input value={form.brandName} onChange={(e) => setForm({ ...form, brandName: e.target.value })} required /></FormField>
        <FormField label="Barcode"><Input value={form.barcode} onChange={(e) => setForm({ ...form, barcode: e.target.value })} /></FormField>
        <FormField label="Strength"><Input value={form.strength} onChange={(e) => setForm({ ...form, strength: e.target.value })} placeholder="e.g. 500mg" /></FormField>
        <FormField label="Form"><Input value={form.form} onChange={(e) => setForm({ ...form, form: e.target.value })} placeholder="e.g. Tablet" /></FormField>
        <FormField label="Base Unit"><Input value={form.baseUnit} onChange={(e) => setForm({ ...form, baseUnit: e.target.value })} placeholder="e.g. tablet" /></FormField>
      </FormDrawer>
    </>
  )
}
```

- [ ] **Step 2: Verify clinical page tabs**

Navigate to `/clinical`. Expected: 3 tabs (Stores/Molecules/Brands) with Headless UI Tab styling, each tab has its own data table and create drawer.

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/pages/Clinical.jsx
git commit -m "feat(demo-ui): redesign Clinical page with Headless UI tabs

Three sub-tabs: Stores (CRUD), Molecules (search + create), Brands (filter by molecule + create).
Uses TabGroup from @headlessui/react for accessible tab navigation.
Each tab has its own DataTable and FormDrawer."
```
