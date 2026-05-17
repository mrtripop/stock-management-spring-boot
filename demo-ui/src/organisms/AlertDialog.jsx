import { Fragment } from 'react'
import { Dialog, DialogPanel, DialogTitle, Transition, TransitionChild } from '@headlessui/react'
import { Button } from '../atoms/Button'
import { Icon } from '../atoms/Icon'

const VARIANTS = {
  info: { icon: 'info', bg: 'bg-[var(--color-info-subtle)]', text: 'text-[var(--color-info)]' },
  warning: { icon: 'exclamation', bg: 'bg-[var(--color-warning-subtle)]', text: 'text-[var(--color-warning)]' },
  danger: { icon: 'exclamation', bg: 'bg-[var(--color-danger-subtle)]', text: 'text-[var(--color-danger)]' },
}

export function AlertDialog({ open, onClose, title, message, variant = 'info', actions = [], loading = false }) {
  const v = VARIANTS[variant] || VARIANTS.info
  return (
    <Transition show={open} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        <TransitionChild as={Fragment} enter="ease-out duration-200" enterFrom="opacity-0" enterTo="opacity-100" leave="ease-in duration-150" leaveFrom="opacity-100" leaveTo="opacity-0">
          <div className="fixed inset-0 bg-[var(--color-overlay)]" />
        </TransitionChild>
        <div className="fixed inset-0 flex items-center justify-center p-4">
          <TransitionChild as={Fragment} enter="ease-out duration-200" enterFrom="opacity-0 scale-95" enterTo="opacity-100 scale-100" leave="ease-in duration-150" leaveFrom="opacity-100 scale-100" leaveTo="opacity-0 scale-95">
            <DialogPanel className="w-full max-w-sm bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-xl)] overflow-hidden">
              <div className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className={`w-10 h-10 ${v.bg} rounded-full flex items-center justify-center`}>
                    <Icon name={v.icon} className={`w-5 h-5 ${v.text}`} />
                  </div>
                  <DialogTitle className="text-sm font-semibold text-[var(--color-text-primary)]">{title}</DialogTitle>
                </div>
                <p className="text-sm text-[var(--color-text-secondary)] leading-relaxed">{message}</p>
              </div>
              <div className="flex justify-end gap-2 px-6 py-3 bg-[var(--color-background)] border-t border-[var(--color-border)]">
                <Button variant="secondary" onClick={onClose}>Cancel</Button>
                {actions.map((action, i) => (
                  <Button key={i} variant={action.variant || 'primary'} onClick={action.onClick} loading={loading}>{action.label}</Button>
                ))}
              </div>
            </DialogPanel>
          </TransitionChild>
        </div>
      </Dialog>
    </Transition>
  )
}
