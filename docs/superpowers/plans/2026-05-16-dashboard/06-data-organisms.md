### Task 6: Data Organisms — DataTable, FormDrawer, AlertDialog

**Files:**
- Create: `demo-ui/src/organisms/DataTable.jsx`
- Create: `demo-ui/src/organisms/FormDrawer.jsx`
- Create: `demo-ui/src/organisms/AlertDialog.jsx`

- [ ] **Step 1: Create DataTable organism**

Create `demo-ui/src/organisms/DataTable.jsx`:

```jsx
import { Spinner } from '../atoms/Spinner'
import { Badge } from '../atoms/Badge'
import { Pagination } from '../molecules/Pagination'

export function DataTable({
  columns,
  data = [],
  loading = false,
  currentPage = 1,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
  emptyMessage = 'No data found',
  renderRow,
  className = '',
}) {
  return (
    <div className={`bg-white rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] overflow-hidden border border-[var(--color-border-light)] ${className}`}>
      <table className="w-full border-collapse">
        <thead>
          <tr className="bg-slate-50 border-b border-[var(--color-border-light)]">
            {columns.map((col) => (
              <th
                key={col.key}
                className="px-4 py-2.5 text-left text-[10px] font-semibold text-[var(--color-text-muted)] uppercase tracking-wide"
                style={{ width: col.width }}
              >
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={columns.length} className="py-12 text-center">
                <Spinner size="lg" className="mx-auto" />
                <p className="text-xs text-[var(--color-text-muted)] mt-2">Loading...</p>
              </td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="py-12 text-center text-sm text-[var(--color-text-muted)]">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((item) => renderRow(item))
          )}
        </tbody>
      </table>
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        totalItems={totalElements}
        pageSize={pageSize}
        onPageChange={onPageChange}
      />
    </div>
  )
}
```

- [ ] **Step 2: Create FormDrawer organism**

Create `demo-ui/src/organisms/FormDrawer.jsx`:

```jsx
import { Dialog, DialogPanel, DialogTitle, Transition, TransitionChild } from '@headlessui/react'
import { Button } from '../atoms/Button'

export function FormDrawer({ open, onClose, title, children, onSubmit, submitLabel = 'Save', loading = false }) {
  const formContent = onSubmit ? (
    <form onSubmit={onSubmit} className="flex flex-col h-full">
      <div className="flex-1 overflow-y-auto px-5 py-4">
        {children}
      </div>
      <div className="flex justify-end gap-2 px-5 py-3 border-t border-[var(--color-border-light)] bg-slate-50">
        <Button variant="secondary" onClick={onClose} type="button">Cancel</Button>
        <Button type="submit" loading={loading}>{submitLabel}</Button>
      </div>
    </form>
  ) : (
    <div className="flex-1 overflow-y-auto px-5 py-4">{children}</div>
  )

  return (
    <Transition show={open} as="div">
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        {/* Backdrop */}
        <TransitionChild
          as="div"
          enter="ease-in-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in-out duration-300"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/30" />
        </TransitionChild>

        {/* Panel */}
        <div className="fixed inset-0 overflow-hidden">
          <div className="absolute inset-0 overflow-hidden">
            <div className="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-10">
              <TransitionChild
                as="div"
                enter="transform transition ease-in-out duration-300"
                enterFrom="translate-x-full"
                enterTo="translate-x-0"
                leave="transform transition ease-in-out duration-300"
                leaveFrom="translate-x-0"
                leaveTo="translate-x-full"
              >
                <DialogPanel className="pointer-events-auto w-screen max-w-md">
                  <div className="flex h-full flex-col bg-white shadow-[var(--shadow-lg)]">
                    {/* Header */}
                    <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--color-border-light)]">
                      <DialogTitle className="text-sm font-semibold text-[var(--color-text-primary)]">
                        {title}
                      </DialogTitle>
                      <button onClick={onClose} className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] transition-colors">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
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
```

- [ ] **Step 3: Create AlertDialog organism**

Create `demo-ui/src/organisms/AlertDialog.jsx`:

```jsx
import { Dialog, DialogPanel, DialogTitle, Transition, TransitionChild } from '@headlessui/react'
import { Button } from '../atoms/Button'

export function AlertDialog({ open, onClose, onConfirm, title, message, confirmLabel = 'Delete', loading = false }) {
  return (
    <Transition show={open} as="div">
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        <TransitionChild
          as="div"
          enter="ease-out duration-200"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-150"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/30" />
        </TransitionChild>

        <div className="fixed inset-0 flex items-center justify-center p-4">
          <TransitionChild
            as="div"
            enter="ease-out duration-200"
            enterFrom="opacity-0 scale-95"
            enterTo="opacity-100 scale-100"
            leave="ease-in duration-150"
            leaveFrom="opacity-100 scale-100"
            leaveTo="opacity-0 scale-95"
          >
            <DialogPanel className="w-full max-w-sm bg-white rounded-[var(--radius-lg)] shadow-[var(--shadow-lg)] overflow-hidden">
              <div className="p-6">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 bg-red-50 rounded-full flex items-center justify-center text-lg">⚠️</div>
                  <DialogTitle className="text-sm font-semibold text-[var(--color-text-primary)]">{title}</DialogTitle>
                </div>
                <p className="text-sm text-[var(--color-text-secondary)] leading-relaxed">{message}</p>
              </div>
              <div className="flex justify-end gap-2 px-6 py-3 bg-slate-50 border-t border-[var(--color-border-light)]">
                <Button variant="secondary" onClick={onClose}>Cancel</Button>
                <Button variant="danger" onClick={onConfirm} loading={loading}>{confirmLabel}</Button>
              </div>
            </DialogPanel>
          </TransitionChild>
        </div>
      </Dialog>
    </Transition>
  )
}
```

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/organisms/DataTable.jsx demo-ui/src/organisms/FormDrawer.jsx demo-ui/src/organisms/AlertDialog.jsx
git commit -m "feat(demo-ui): add DataTable, FormDrawer, and AlertDialog organisms

DataTable: sortable table with loading spinner, empty state, and pagination.
FormDrawer: slide-over panel using Headless UI Dialog with form support.
AlertDialog: confirmation modal for destructive actions with warning icon."
```
