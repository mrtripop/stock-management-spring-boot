import { useState } from 'react'
import { AlertDialog } from './AlertDialog'

export default {
  title: 'Organisms/AlertDialog',
  component: AlertDialog,
}

export const Default = {
  render: () => {
    const [open, setOpen] = useState(true)
    return (
      <AlertDialog
        open={open}
        onClose={() => setOpen(false)}
        onConfirm={() => setOpen(false)}
        title="Delete Product"
        message='Are you sure you want to delete "Paracetamol 500mg"? This action cannot be undone.'
      />
    )
  },
}

export const CustomLabel = {
  render: () => {
    const [open, setOpen] = useState(true)
    return (
      <AlertDialog
        open={open}
        onClose={() => setOpen(false)}
        onConfirm={() => setOpen(false)}
        title="Archive Batch"
        message="This will archive the batch and remove it from active inventory."
        confirmLabel="Archive"
      />
    )
  },
}

export const Loading = {
  render: () => {
    const [open, setOpen] = useState(true)
    return (
      <AlertDialog
        open={open}
        onClose={() => setOpen(false)}
        onConfirm={() => {}}
        title="Deleting..."
        message="Please wait while the product is being deleted."
        loading={true}
      />
    )
  },
}
