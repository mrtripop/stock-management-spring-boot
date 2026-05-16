### Task 3: Molecules

**Files:**
- Create: `demo-ui/src/molecules/StatCard.jsx`
- Create: `demo-ui/src/molecules/PageHeader.jsx`
- Create: `demo-ui/src/molecules/SearchBar.jsx`
- Create: `demo-ui/src/molecules/Pagination.jsx`
- Create: `demo-ui/src/molecules/FormField.jsx`

- [ ] **Step 1: Create StatCard molecule**

Create `demo-ui/src/molecules/StatCard.jsx`:

```jsx
export function StatCard({ title, value, change, trend, accentColor = 'var(--color-primary)', className = '' }) {
  return (
    <div className={`bg-white rounded-[var(--radius-lg)] p-4 shadow-[var(--shadow-sm)] border-t-3 ${className}`}
      style={{ borderTopColor: accentColor }}
    >
      <div className="text-xs font-semibold text-[var(--color-text-muted)] uppercase tracking-wide">
        {title}
      </div>
      <div className="text-2xl font-bold text-[var(--color-text-primary)] mt-1">
        {value}
      </div>
      {change && (
        <div className={`text-xs mt-1 ${trend === 'up' ? 'text-[var(--color-success)]' : trend === 'down' ? 'text-[var(--color-danger)]' : 'text-[var(--color-text-secondary)]'}`}>
          {trend === 'up' ? '↑' : trend === 'down' ? '↓' : ''} {change}
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 2: Create PageHeader molecule**

Create `demo-ui/src/molecules/PageHeader.jsx`:

```jsx
export function PageHeader({ title, subtitle, actions, className = '' }) {
  return (
    <div className={`flex justify-between items-center mb-6 ${className}`}>
      <div>
        <h1 className="text-xl font-bold text-[var(--color-text-primary)]">{title}</h1>
        {subtitle && <p className="text-sm text-[var(--color-text-secondary)] mt-0.5">{subtitle}</p>}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  )
}
```

- [ ] **Step 3: Create SearchBar molecule**

Create `demo-ui/src/molecules/SearchBar.jsx`:

```jsx
import { useState } from 'react'
import { Input } from '../atoms/Input'
import { Icon } from '../atoms/Icon'

export function SearchBar({ placeholder = 'Search...', onSearch, filterSlot, className = '' }) {
  const [value, setValue] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    onSearch?.(value)
  }

  return (
    <form onSubmit={handleSubmit} className={`flex items-center gap-2 ${className}`}>
      <div className="relative flex-1">
        <Icon name="search" className="absolute left-2.5 top-1/2 -translate-y-1/2 w-4 h-4 text-[var(--color-text-muted)]" />
        <Input
          value={value}
          onChange={(e) => { setValue(e.target.value); if (!e.target.value) onSearch?.('') }}
          placeholder={placeholder}
          className="pl-8"
        />
      </div>
      {filterSlot}
    </form>
  )
}
```

- [ ] **Step 4: Create Pagination molecule**

Create `demo-ui/src/molecules/Pagination.jsx`:

```jsx
import { Button } from '../atoms/Button'

export function Pagination({ currentPage, totalPages, totalItems, pageSize, onPageChange, className = '' }) {
  if (totalPages <= 1) return null

  const start = (currentPage - 1) * pageSize + 1
  const end = Math.min(currentPage * pageSize, totalItems)

  return (
    <div className={`flex justify-between items-center py-3 px-4 border-t border-[var(--color-border-light)] ${className}`}>
      <div className="text-xs text-[var(--color-text-secondary)]">
        Showing {start}–{end} of {totalItems}
      </div>
      <div className="flex gap-1">
        <Button variant="secondary" size="sm" disabled={currentPage <= 1} onClick={() => onPageChange(currentPage - 1)}>
          ‹
        </Button>
        {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
          const page = i + 1
          return (
            <Button key={page} variant={page === currentPage ? 'primary' : 'secondary'} size="sm" onClick={() => onPageChange(page)}>
              {page}
            </Button>
          )
        })}
        <Button variant="secondary" size="sm" disabled={currentPage >= totalPages} onClick={() => onPageChange(currentPage + 1)}>
          ›
        </Button>
      </div>
    </div>
  )
}
```

- [ ] **Step 5: Create FormField molecule**

Create `demo-ui/src/molecules/FormField.jsx`:

```jsx
export function FormField({ label, required, error, hint, children, className = '' }) {
  return (
    <div className={`mb-3 ${className}`}>
      <label className="block text-xs font-medium text-[var(--color-text-secondary)] mb-1">
        {label}
        {required && <span className="text-[var(--color-danger)] ml-0.5">*</span>}
      </label>
      {children}
      {error && <p className="text-xs text-[var(--color-danger)] mt-1">{error}</p>}
      {hint && !error && <p className="text-xs text-[var(--color-text-muted)] mt-1">{hint}</p>}
    </div>
  )
}
```

- [ ] **Step 6: Commit**

```bash
mkdir -p demo-ui/src/molecules
git add demo-ui/src/molecules/
git commit -m "feat(demo-ui): add molecule components — StatCard, PageHeader, SearchBar, Pagination, FormField

StatCard supports colored top border accent and trend indicators.
PageHeader provides title + subtitle + action slot pattern.
SearchBar wraps Input with search icon and filter slot.
Pagination shows page info and navigation buttons.
FormField wraps any input atom with label, hint, and error states."
```
