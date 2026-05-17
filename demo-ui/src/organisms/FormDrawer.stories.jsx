import { useState } from 'react'
import { FormDrawer } from './FormDrawer'
import { Input } from '../atoms/Input'
import { FormField } from '../molecules/FormField'

export default {
  title: 'Organisms/FormDrawer',
  component: FormDrawer,
}

export const WithForm = {
  render: () => {
    const [open, setOpen] = useState(true)
    return (
      <FormDrawer
        open={open}
        onClose={() => setOpen(false)}
        title="New Product"
        onSubmit={(e) => { e.preventDefault(); setOpen(false) }}
        submitLabel="Create"
      >
        <FormField label="Product Code" required>
          <Input placeholder="e.g. PAR-500" />
        </FormField>
        <FormField label="Product Name" required>
          <Input placeholder="e.g. Paracetamol 500mg" />
        </FormField>
        <FormField label="Category">
          <Input placeholder="e.g. Analgesic" />
        </FormField>
        <FormField label="Reorder Quantity">
          <Input type="number" placeholder="0" />
        </FormField>
      </FormDrawer>
    )
  },
}

export const ReadOnly = {
  render: () => {
    const [open, setOpen] = useState(true)
    return (
      <FormDrawer open={open} onClose={() => setOpen(false)} title="Product Details">
        <div className="space-y-3">
          <div><span className="text-xs text-[var(--color-text-muted)]">Code</span><p className="text-sm font-medium">PAR-500</p></div>
          <div><span className="text-xs text-[var(--color-text-muted)]">Name</span><p className="text-sm font-medium">Paracetamol 500mg</p></div>
          <div><span className="text-xs text-[var(--color-text-muted)]">Category</span><p className="text-sm">Analgesic</p></div>
          <div><span className="text-xs text-[var(--color-text-muted)]">Reorder Qty</span><p className="text-sm">100</p></div>
        </div>
      </FormDrawer>
    )
  },
}

export const Loading = {
  render: () => {
    const [open, setOpen] = useState(true)
    return (
      <FormDrawer
        open={open}
        onClose={() => setOpen(false)}
        title="Saving..."
        onSubmit={(e) => e.preventDefault()}
        submitLabel="Save"
        loading={true}
      >
        <FormField label="Name"><Input value="Paracetamol" /></FormField>
      </FormDrawer>
    )
  },
}
