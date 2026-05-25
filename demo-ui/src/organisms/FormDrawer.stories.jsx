import { useState } from 'react'
import { FormDrawer } from './FormDrawer'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Button } from '../atoms/Button'

export default {
  title: 'Organisms/FormDrawer',
  component: FormDrawer,
}

export const CreateForm = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button onClick={() => setOpen(true)}>Open Form</Button>
        <FormDrawer open={open} onClose={() => setOpen(false)} title="Create Product" onSubmit={(e) => { e.preventDefault(); setOpen(false) }}>
          <div className="space-y-4">
            <FormField label="Product Name" required>
              <Input placeholder="Enter product name" />
            </FormField>
            <FormField label="Code">
              <Input placeholder="e.g. PRD-001" />
            </FormField>
            <FormField label="Category">
              <Input placeholder="e.g. Tablet" />
            </FormField>
          </div>
        </FormDrawer>
      </>
    )
  },
}

export const MultiStepForm = {
  render: () => {
    const [open, setOpen] = useState(false)
    const [step, setStep] = useState(0)
    const steps = [{ title: 'Details' }, { title: 'Pricing' }, { title: 'Review' }]
    return (
      <>
        <Button onClick={() => { setOpen(true); setStep(0) }}>Open Multi-Step Form</Button>
        <FormDrawer open={open} onClose={() => setOpen(false)} title="Stock-In" onSubmit={(e) => { e.preventDefault(); if (step < 2) setStep(step + 1); else setOpen(false) }} steps={steps} currentStep={step}>
          {step === 0 && (
            <div className="space-y-4">
              <FormField label="Barcode"><Input placeholder="Scan or enter barcode" /></FormField>
              <FormField label="Batch Number"><Input placeholder="BN-2026-001" /></FormField>
            </div>
          )}
          {step === 1 && (
            <div className="space-y-4">
              <FormField label="Quantity"><Input placeholder="100" /></FormField>
              <FormField label="Expiry Date"><Input type="date" /></FormField>
            </div>
          )}
          {step === 2 && <p className="text-sm text-[var(--color-text-secondary)]">Review your stock-in details before submitting.</p>}
        </FormDrawer>
      </>
    )
  },
}

export const ViewOnly = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button onClick={() => setOpen(true)}>Open Details</Button>
        <FormDrawer open={open} onClose={() => setOpen(false)} title="Product Details">
          <div className="space-y-3 text-sm">
            <p><span className="text-[var(--color-text-muted)]">Name:</span> Amoxicillin 500mg</p>
            <p><span className="text-[var(--color-text-muted)]">Code:</span> PRD-001</p>
            <p><span className="text-[var(--color-text-muted)]">Category:</span> Tablet</p>
          </div>
        </FormDrawer>
      </>
    )
  },
}
