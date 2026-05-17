import { Fragment } from 'react'
import { Dialog, DialogPanel, DialogTitle, Transition, TransitionChild } from '@headlessui/react'
import { Button } from '../atoms/Button'

const WIDTH_MAP = { sm: 'max-w-sm', md: 'max-w-md', lg: 'max-w-2xl' }

export function FormDrawer({ open, onClose, title, width = 'md', children, onSubmit, submitLabel = 'Save', loading = false, steps, currentStep = 0 }) {
  const hasSteps = steps && steps.length > 1
  const formContent = onSubmit ? (
    <form onSubmit={onSubmit} className="flex flex-col h-full">
      {hasSteps && (
        <div className="flex gap-1 px-5 pt-4">
          {steps.map((step, i) => (
            <div key={i} className="flex-1">
              <div className={`h-1 rounded-full transition-colors ${i <= currentStep ? 'bg-[var(--color-primary)]' : 'bg-[var(--color-border)]'}`} />
              {i <= currentStep && (
                <p className="text-xs text-[var(--color-text-muted)] mt-1 truncate">{step.title}</p>
              )}
            </div>
          ))}
        </div>
      )}
      <div className="flex-1 overflow-y-auto px-5 py-4">{children}</div>
      <div className="flex justify-end gap-2 px-5 py-3 border-t border-[var(--color-border)] bg-[var(--color-background)]">
        <Button variant="secondary" onClick={onClose} type="button">Cancel</Button>
        <Button type="submit" loading={loading}>{hasSteps && currentStep < steps.length - 1 ? 'Next' : submitLabel}</Button>
      </div>
    </form>
  ) : (
    <div className="flex-1 overflow-y-auto px-5 py-4">{children}</div>
  )

  return (
    <Transition show={open} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        <TransitionChild as={Fragment} enter="ease-in-out duration-300" enterFrom="opacity-0" enterTo="opacity-100" leave="ease-in-out duration-300" leaveFrom="opacity-100" leaveTo="opacity-0">
          <div className="fixed inset-0 bg-[var(--color-overlay)]" />
        </TransitionChild>
        <div className="fixed inset-0 overflow-hidden">
          <div className="absolute inset-0 overflow-hidden">
            <div className="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-10">
              <TransitionChild as={Fragment} enter="transform transition ease-in-out duration-300" enterFrom="translate-x-full" enterTo="translate-x-0" leave="transform transition ease-in-out duration-300" leaveFrom="translate-x-0" leaveTo="translate-x-full">
                <DialogPanel className={`pointer-events-auto w-screen ${WIDTH_MAP[width]} h-screen`}>
                  <div className="flex h-full flex-col bg-[var(--color-surface)] shadow-[var(--shadow-xl)]">
                    <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--color-border)]">
                      <DialogTitle className="text-sm font-semibold text-[var(--color-text-primary)]">{title}</DialogTitle>
                      <button onClick={onClose} className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] transition-colors cursor-pointer">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                      </button>
                    </div>
                    {formContent}
                  </div>
                </DialogPanel>
              </TransitionChild>
            </div>
          </div>
        </div>
      </Dialog>
    </Transition>
  )
}
