import { useState } from 'react'
import { AlertDialog } from './AlertDialog'
import { Button } from '../atoms/Button'

export default {
  title: 'Organisms/AlertDialog',
  component: AlertDialog,
}

export const Info = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button onClick={() => setOpen(true)}>Show Info</Button>
        <AlertDialog open={open} onClose={() => setOpen(false)} title="Information" message="This action will trigger a full inventory scan. This may take a few minutes." variant="info" actions={[{ label: 'Run Scan', onClick: () => setOpen(false) }]} />
      </>
    )
  },
}

export const Warning = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button variant="secondary" onClick={() => setOpen(true)}>Show Warning</Button>
        <AlertDialog open={open} onClose={() => setOpen(false)} title="Low Stock Warning" message="Product Amoxicillin 500mg is below reorder threshold. Current stock: 5, Reorder at: 20." variant="warning" actions={[{ label: 'Acknowledge', variant: 'primary', onClick: () => setOpen(false) }]} />
      </>
    )
  },
}

export const Danger = {
  render: () => {
    const [open, setOpen] = useState(false)
    return (
      <>
        <Button variant="danger" onClick={() => setOpen(true)}>Delete Product</Button>
        <AlertDialog open={open} onClose={() => setOpen(false)} title="Delete Product" message="Are you sure you want to delete Amoxicillin 500mg? This action cannot be undone." variant="danger" actions={[{ label: 'Delete', variant: 'danger', onClick: () => setOpen(false) }]} />
      </>
    )
  },
}
