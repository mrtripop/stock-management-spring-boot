import { Dialog, DialogPanel, DialogTitle } from '@headlessui/react'
import { Button } from '../atoms/Button'

const variantConfig = {
  info: {
    iconColor: 'text-[var(--color-info)]',
    confirmVariant: 'primary',
  },
  warning: {
    iconColor: 'text-[var(--color-warning)]',
    confirmVariant: 'primary',
  },
  danger: {
    iconColor: 'text-[var(--color-danger)]',
    confirmVariant: 'danger',
  },
}

export function ConfirmationDialog({
  open,
  onConfirm,
  onCancel = () => {},
  title,
  message,
  variant = 'info',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  loading = false,
}) {
  const config = variantConfig[variant] || variantConfig.info

  return (
    <Dialog open={open} onClose={onCancel} className="relative z-50">
      <div className="fixed inset-0 bg-[var(--color-overlay)]" aria-hidden="true" />
      <div className="fixed inset-0 flex items-center justify-center p-4">
        <DialogPanel className="w-full max-w-sm bg-[var(--color-surface)] rounded-[var(--radius-xl)] shadow-[var(--shadow-xl)] p-[var(--space-6)]">
          {title && (
            <DialogTitle className="text-lg font-semibold text-[var(--color-text-primary)]">
              {title}
            </DialogTitle>
          )}
          {message && (
            <p className="text-sm text-[var(--color-text-secondary)] mt-[var(--space-2)]">
              {message}
            </p>
          )}
          <div className="flex justify-end gap-[var(--space-3)] mt-[var(--space-6)]">
            <Button
              variant="secondary"
              size="sm"
              onClick={onCancel}
              disabled={loading}
            >
              {cancelText}
            </Button>
            <Button
              variant={config.confirmVariant}
              size="sm"
              onClick={onConfirm}
              loading={loading}
            >
              {confirmText}
            </Button>
          </div>
        </DialogPanel>
      </div>
    </Dialog>
  )
}
