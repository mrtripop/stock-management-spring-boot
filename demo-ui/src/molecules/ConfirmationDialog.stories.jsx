import { useState } from 'react'
import { ConfirmationDialog } from './ConfirmationDialog'

export default {
  title: 'Molecules/ConfirmationDialog',
  component: ConfirmationDialog,
  argTypes: {
    title: { control: 'text' },
    message: { control: 'text' },
    variant: { control: 'select', options: ['info', 'warning', 'danger'] },
    confirmText: { control: 'text' },
    cancelText: { control: 'text' },
    loading: { control: 'boolean' },
    open: { control: 'boolean' },
  },
}

export const Info = {
  args: {
    open: true,
    title: 'Switch Store',
    message: 'Are you sure you want to switch to the Main Street branch? Any unsaved changes will be lost.',
    variant: 'info',
    confirmText: 'Switch',
    onConfirm: () => console.log('confirmed'),
    onCancel: () => console.log('cancelled'),
  },
}

export const Warning = {
  args: {
    open: true,
    title: 'Acknowledge Task',
    message: 'You are about to acknowledge the expiry alert for Batch #BN-2026-0042. This will mark it as reviewed.',
    variant: 'warning',
    confirmText: 'Acknowledge',
    onConfirm: () => console.log('confirmed'),
    onCancel: () => console.log('cancelled'),
  },
}

export const Danger = {
  args: {
    open: true,
    title: 'Void Invoice',
    message: 'This action cannot be undone. Invoice #INV-00156 will be permanently voided and stock will be restored.',
    variant: 'danger',
    confirmText: 'Void Invoice',
    onConfirm: () => console.log('confirmed'),
    onCancel: () => console.log('cancelled'),
  },
}

export const Loading = {
  args: {
    open: true,
    title: 'Deleting Product',
    message: 'Please wait while the product is being deleted...',
    variant: 'danger',
    confirmText: 'Delete',
    loading: true,
    onConfirm: () => {},
    onCancel: () => {},
  },
}

export const CustomLabels = {
  args: {
    open: true,
    title: 'Export Data',
    message: 'This will export all products as a CSV file. It may take a moment for large datasets.',
    variant: 'info',
    confirmText: 'Download CSV',
    cancelText: 'Go Back',
    onConfirm: () => console.log('confirmed'),
    onCancel: () => console.log('cancelled'),
  },
}

export const Interactive = {
  render: () => {
    const [open, setOpen] = useState(false)
    const [loading, setLoading] = useState(false)

    const handleConfirm = () => {
      setLoading(true)
      setTimeout(() => {
        setLoading(false)
        setOpen(false)
      }, 1500)
    }

    return (
      <div>
        <button
          onClick={() => setOpen(true)}
          className="px-4 py-2 bg-[var(--color-danger)] text-white rounded-[var(--radius-md)] text-sm font-medium cursor-pointer"
        >
          Delete Product
        </button>
        <ConfirmationDialog
          open={open}
          title="Delete Product"
          message="Are you sure you want to delete Paracetamol 500mg? This action cannot be undone."
          variant="danger"
          confirmText="Delete"
          loading={loading}
          onConfirm={handleConfirm}
          onCancel={() => { setOpen(false); setLoading(false) }}
        />
      </div>
    )
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-8">
      <div>
        <p className="text-xs text-[var(--color-text-muted)] mb-2">Info variant</p>
        <ConfirmationDialog
          open
          title="Switch Store"
          message="Are you sure you want to switch stores?"
          variant="info"
          confirmText="Switch"
          onConfirm={() => {}}
          onCancel={() => {}}
        />
      </div>
    </div>
  ),
}
