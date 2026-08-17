# Molecules — Rebuild 5 + Create 3 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans.

**Goal:** Rebuild 5 existing molecule components and create 3 new ones, all using semantic design tokens (Plan 1) and rebuilt atoms (Plan 2).
**Architecture:** Molecules compose atoms into functional patterns — form fields, page headers, pagination, search, stats, date picking, file upload, and confirmation dialogs. Each molecule uses semantic CSS tokens exclusively (no primitive colors) and is React Hook Form compatible where applicable.
**Tech Stack:** React 19, Tailwind CSS v4, @heroicons/react, @headlessui/react (Dialog), Storybook 8

**Dependencies:** Plan 1 (tokens.css + theme.js) and Plan 2 (all 6 atoms: Button, Input, Select, Badge, Icon, Spinner) must be completed first.

---

### Task 1: Rebuild FormField

**Files:**
- Rewrite: `demo-ui/src/molecules/FormField.jsx`
- Rewrite: `demo-ui/src/molecules/FormField.stories.jsx`

- [ ] **Step: Rewrite FormField with id association, htmlFor, and React Hook Form compatibility**

Design decision: FormField does NOT inject error styles into children. The parent passes `error` directly to the child input atom. FormField only handles label + error message + helper text layout. This is the cleanest pattern for React Hook Form — the form state drives both the field wrapper and the input atom independently.

Rewrite `demo-ui/src/molecules/FormField.jsx`:

```jsx
import { useId } from 'react'

export function FormField({ label, error, helperText, required, children, className = '' }) {
  const generatedId = useId()
  // Allow children to provide their own id; fall back to generated one
  const childId = children?.props?.id || generatedId

  return (
    <div className={className}>
      {label && (
        <label
          htmlFor={childId}
          className="block text-[length:var(--text-sm)] font-[var(--font-medium)] text-[var(--color-text-secondary)] mb-1"
        >
          {label}
          {required && <span className="text-[var(--color-danger)] ml-0.5">*</span>}
        </label>
      )}
      {children}
      {error && (
        <p className="text-[length:var(--text-xs)] text-[var(--color-danger)] mt-1" role="alert">
          {error}
        </p>
      )}
      {helperText && !error && (
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mt-1">
          {helperText}
        </p>
      )}
    </div>
  )
}
```

- [ ] **Step: Rewrite FormField Storybook story**

Rewrite `demo-ui/src/molecules/FormField.stories.jsx`:

```jsx
import { FormField } from './FormField'
import { Input } from '../atoms/Input'
import { Select } from '../atoms/Select'

export default {
  title: 'Molecules/FormField',
  component: FormField,
  argTypes: {
    label: { control: 'text' },
    required: { control: 'boolean' },
    error: { control: 'text' },
    helperText: { control: 'text' },
  },
}

export const Default = {
  args: {
    label: 'Username',
    children: <Input placeholder="Enter username" />,
  },
}

export const Required = {
  args: {
    label: 'Email',
    required: true,
    children: <Input type="email" placeholder="email@example.com" />,
  },
}

export const WithError = {
  args: {
    label: 'Password',
    required: true,
    error: 'Password must be at least 8 characters',
    children: <Input type="password" placeholder="Enter password" error />,
  },
}

export const WithHelperText = {
  args: {
    label: 'Batch Number',
    helperText: 'Format: BN-YYYY-XXXX',
    children: <Input placeholder="BN-2026-0001" />,
  },
}

export const WithSelect = {
  args: {
    label: 'Category',
    required: true,
    children: (
      <Select>
        <option value="">Select category</option>
        <option value="1">Tablets</option>
        <option value="2">Capsules</option>
        <option value="3">Syrup</option>
      </Select>
    ),
  },
}

export const ErrorOverridesHelper = {
  args: {
    label: 'Quantity',
    helperText: 'Must be a positive integer',
    error: 'Quantity cannot be negative',
    children: <Input type="number" error />,
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-6 max-w-sm">
      <FormField label="Default" helperText="Standard input field">
        <Input placeholder="Default state" />
      </FormField>
      <FormField label="Required" required>
        <Input placeholder="Required field" />
      </FormField>
      <FormField label="With Error" required error="This field is required">
        <Input placeholder="Error state" error />
      </FormField>
      <FormField label="Select" required helperText="Choose one option">
        <Select>
          <option value="">Pick one</option>
          <option value="a">Option A</option>
          <option value="b">Option B</option>
        </Select>
      </FormField>
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**
  ```
  feat(ui): rebuild FormField molecule with semantic tokens and React Hook Form support

  Adds useId-based label association, role=alert on error messages,
  and helperText prop. Error styling is passed to child input by parent.
  ```

---

### Task 2: Rebuild PageHeader

**Files:**
- Rewrite: `demo-ui/src/molecules/PageHeader.jsx`
- Rewrite: `demo-ui/src/molecules/PageHeader.stories.jsx`

- [ ] **Step: Rewrite PageHeader with breadcrumb support and semantic tokens**

Rewrite `demo-ui/src/molecules/PageHeader.jsx`:

```jsx
import { Icon } from '../atoms/Icon'

export function PageHeader({ title, subtitle, actions, breadcrumb, className = '' }) {
  return (
    <div className={className}>
      {breadcrumb && breadcrumb.length > 0 && (
        <nav aria-label="Breadcrumb" className="flex items-center gap-1 text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">
          {breadcrumb.map((item, index) => (
            <span key={index} className="contents">
              {index > 0 && (
                <Icon name="chevron-right" className="w-3 h-3 text-[var(--color-text-muted)]" />
              )}
              {item.href ? (
                <a
                  href={item.href}
                  className="hover:text-[var(--color-primary)] transition-colors"
                >
                  {item.label}
                </a>
              ) : (
                <span className={index === breadcrumb.length - 1 ? 'text-[var(--color-text-primary)] font-[var(--font-medium)]' : ''}>
                  {item.label}
                </span>
              )}
            </span>
          ))}
        </nav>
      )}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-[length:var(--text-2xl)] font-[var(--font-semibold)] text-[var(--color-text-primary)]">
            {title}
          </h1>
          {subtitle && (
            <p className="text-[length:var(--text-sm)] text-[var(--color-text-secondary)] mt-0.5">
              {subtitle}
            </p>
          )}
        </div>
        {actions && <div className="flex items-center gap-[var(--space-2)]">{actions}</div>}
      </div>
    </div>
  )
}
```

- [ ] **Step: Rewrite PageHeader Storybook story**

Rewrite `demo-ui/src/molecules/PageHeader.stories.jsx`:

```jsx
import { PageHeader } from './PageHeader'
import { Button } from '../atoms/Button'

export default {
  title: 'Molecules/PageHeader',
  component: PageHeader,
  argTypes: {
    title: { control: 'text' },
    subtitle: { control: 'text' },
  },
}

export const Default = {
  args: { title: 'Products' },
}

export const WithSubtitle = {
  args: { title: 'Inventory', subtitle: 'Manage your stock and batches' },
}

export const WithActions = {
  render: () => (
    <PageHeader
      title="Products"
      subtitle="Manage your product catalog"
      actions={<Button>Add Product</Button>}
    />
  ),
}

export const WithBreadcrumb = {
  args: {
    title: 'Batch #BN-2026-0042',
    subtitle: 'Paracetamol 500mg',
    breadcrumb: [
      { label: 'Dashboard', href: '/' },
      { label: 'Inventory', href: '/inventory' },
      { label: 'Batch #BN-2026-0042' },
    ],
  },
}

export const FullFeatured = {
  render: () => (
    <PageHeader
      title="Products"
      subtitle="Manage your product catalog"
      breadcrumb={[
        { label: 'Dashboard', href: '/' },
        { label: 'Products' },
      ]}
      actions={
        <div className="flex items-center gap-2">
          <Button variant="secondary">Export CSV</Button>
          <Button>Add Product</Button>
        </div>
      }
    />
  ),
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-8">
      <PageHeader title="Simple Title" />
      <PageHeader title="With Subtitle" subtitle="A brief description of this page" />
      <PageHeader
        title="With Actions"
        actions={<Button size="sm">Action</Button>}
      />
      <PageHeader
        title="Full Example"
        subtitle="Everything together"
        breadcrumb={[
          { label: 'Home', href: '/' },
          { label: 'Section', href: '/section' },
          { label: 'Current Page' },
        ]}
        actions={
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm">Cancel</Button>
            <Button size="sm">Save</Button>
          </div>
        }
      />
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**
  ```
  feat(ui): rebuild PageHeader molecule with breadcrumb nav and semantic tokens

  Adds breadcrumb array prop with linked items, uses text-2xl title,
  and semantic color tokens throughout.
  ```

---

### Task 3: Rebuild Pagination

**Files:**
- Rewrite: `demo-ui/src/molecules/Pagination.jsx`
- Rewrite: `demo-ui/src/molecules/Pagination.stories.jsx`

- [ ] **Step: Rewrite Pagination with smart page range, page size selector, and total count**

Design decision: Prev/next buttons use HTML entities (`&lsaquo;` / `&rsaquo;`) instead of Icon components. The Button atom expects either `icon` prop or text children, so this avoids the ambiguity. The page range algorithm shows at most 7 page numbers with ellipsis.

Rewrite `demo-ui/src/molecules/Pagination.jsx`:

```jsx
import { Button } from '../atoms/Button'
import { Select } from '../atoms/Select'

function getPageRange(current, total, maxVisible = 7) {
  if (total <= maxVisible) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  const half = Math.floor((maxVisible - 2) / 2)
  let start = current - half
  let end = current + half

  if (start <= 1) {
    start = 2
    end = maxVisible - 1
  } else if (end >= total) {
    end = total - 1
    start = total - maxVisible + 2
  }

  const pages = []
  pages.push(1)

  if (start > 2) {
    pages.push('...')
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  if (end < total - 1) {
    pages.push('...')
  }

  if (total > 1) {
    pages.push(total)
  }

  return pages
}

const PAGE_SIZE_OPTIONS = [10, 25, 50, 100]

export function Pagination({
  currentPage,
  totalPages,
  totalElements,
  pageSize = 10,
  onPageChange,
  onPageSizeChange,
  className = '',
}) {
  if (totalPages <= 1 && !onPageSizeChange) return null

  const startItem = totalElements > 0 ? (currentPage - 1) * pageSize + 1 : 0
  const endItem = Math.min(currentPage * pageSize, totalElements)
  const pages = getPageRange(currentPage, totalPages, 7)

  return (
    <div className={`flex flex-wrap justify-between items-center gap-[var(--space-3)] py-[var(--space-3)] px-[var(--space-4)] border-t border-[var(--color-border)] ${className}`}>
      <div className="flex items-center gap-[var(--space-3)]">
        <span className="text-[length:var(--text-xs)] text-[var(--color-text-secondary)]">
          Showing {startItem}–{endItem} of {totalElements}
        </span>
        {onPageSizeChange && (
          <div className="flex items-center gap-[var(--space-1)]">
            <span className="text-[length:var(--text-xs)] text-[var(--color-text-muted)]">per page</span>
            <Select
              value={pageSize}
              onChange={(e) => onPageSizeChange(Number(e.target.value))}
              className="w-16 h-7 text-[length:var(--text-xs)] py-0 px-1"
            >
              {PAGE_SIZE_OPTIONS.map((size) => (
                <option key={size} value={size}>{size}</option>
              ))}
            </Select>
          </div>
        )}
      </div>
      {totalPages > 1 && (
        <div className="flex items-center gap-0.5">
          <Button
            variant="ghost"
            size="sm"
            disabled={currentPage <= 1}
            onClick={() => onPageChange(currentPage - 1)}
            aria-label="Previous page"
          >
            &lsaquo;
          </Button>
          {pages.map((page, index) =>
            page === '...' ? (
              <span
                key={`ellipsis-${index}`}
                className="px-1 text-[length:var(--text-xs)] text-[var(--color-text-muted)] select-none"
              >
                &hellip;
              </span>
            ) : (
              <Button
                key={page}
                variant={page === currentPage ? 'primary' : 'ghost'}
                size="sm"
                onClick={() => onPageChange(page)}
                aria-current={page === currentPage ? 'page' : undefined}
              >
                {page}
              </Button>
            )
          )}
          <Button
            variant="ghost"
            size="sm"
            disabled={currentPage >= totalPages}
            onClick={() => onPageChange(currentPage + 1)}
            aria-label="Next page"
          >
            &rsaquo;
          </Button>
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step: Rewrite Pagination Storybook story**

Rewrite `demo-ui/src/molecules/Pagination.stories.jsx`:

```jsx
import { Pagination } from './Pagination'

export default {
  title: 'Molecules/Pagination',
  component: Pagination,
  argTypes: {
    currentPage: { control: 'number' },
    totalPages: { control: 'number' },
    totalElements: { control: 'number' },
    pageSize: { control: 'number' },
    onPageChange: { action: 'pageChange' },
    onPageSizeChange: { action: 'pageSizeChange' },
  },
}

export const Default = {
  args: {
    currentPage: 1,
    totalPages: 5,
    totalElements: 48,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const MiddlePage = {
  args: {
    currentPage: 3,
    totalPages: 5,
    totalElements: 48,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const ManyPages = {
  args: {
    currentPage: 15,
    totalPages: 50,
    totalElements: 492,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const FirstPage = {
  args: {
    currentPage: 1,
    totalPages: 20,
    totalElements: 198,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const LastPage = {
  args: {
    currentPage: 20,
    totalPages: 20,
    totalElements: 198,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const WithPageSizeSelector = {
  args: {
    currentPage: 2,
    totalPages: 10,
    totalElements: 96,
    pageSize: 10,
    onPageChange: () => {},
    onPageSizeChange: () => {},
  },
}

export const SinglePage = {
  args: {
    currentPage: 1,
    totalPages: 1,
    totalElements: 7,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-8">
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">5 pages, on page 1</p>
        <Pagination currentPage={1} totalPages={5} totalElements={48} pageSize={10} onPageChange={() => {}} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">50 pages, on page 15</p>
        <Pagination currentPage={15} totalPages={50} totalElements={492} pageSize={10} onPageChange={() => {}} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">With page size selector</p>
        <Pagination currentPage={2} totalPages={10} totalElements={96} pageSize={10} onPageChange={() => {}} onPageSizeChange={() => {}} />
      </div>
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**
  ```
  feat(ui): rebuild Pagination molecule with smart page range and size selector

  Shows max 7 page numbers with ellipsis, adds per-page size dropdown,
  uses semantic tokens, and maps to API pagination (page/size).
  ```

---

### Task 4: Rebuild SearchBar

**Files:**
- Rewrite: `demo-ui/src/molecules/SearchBar.jsx`
- Rewrite: `demo-ui/src/molecules/SearchBar.stories.jsx`

- [ ] **Step: Rewrite SearchBar with debounce, controlled value, and clear button**

Rewrite `demo-ui/src/molecules/SearchBar.jsx`:

```jsx
import { useState, useEffect, useRef, useCallback } from 'react'
import { Icon } from '../atoms/Icon'

export function SearchBar({ value, onChange, placeholder = 'Search...', debounceMs = 300, className = '' }) {
  const [internalValue, setInternalValue] = useState(value ?? '')
  const debounceRef = useRef(null)

  // Sync external value changes
  useEffect(() => {
    if (value !== undefined) {
      setInternalValue(value)
    }
  }, [value])

  const handleChange = useCallback(
    (e) => {
      const newValue = e.target.value
      setInternalValue(newValue)

      if (debounceRef.current) {
        clearTimeout(debounceRef.current)
      }

      debounceRef.current = setTimeout(() => {
        onChange?.(newValue)
      }, debounceMs)
    },
    [onChange, debounceMs]
  )

  const handleClear = useCallback(() => {
    setInternalValue('')
    onChange?.('')
  }, [onChange])

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current)
      }
    }
  }, [])

  const showClear = internalValue.length > 0

  return (
    <div className={`relative ${className}`}>
      <Icon
        name="magnifying-glass"
        className="absolute left-[var(--space-3)] top-1/2 -translate-y-1/2 w-4 h-4 text-[var(--color-text-muted)] pointer-events-none"
      />
      <input
        type="text"
        value={internalValue}
        onChange={handleChange}
        placeholder={placeholder}
        className="w-full pl-9 pr-8 py-2 text-[length:var(--text-sm)] rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text-primary)] outline-none transition-colors focus:border-[var(--color-border-focus)] focus:ring-2 focus:ring-[var(--color-primary)]/20 placeholder:text-[var(--color-text-muted)]"
      />
      {showClear && (
        <button
          type="button"
          onClick={handleClear}
          className="absolute right-[var(--space-2)] top-1/2 -translate-y-1/2 w-5 h-5 flex items-center justify-center rounded-[var(--radius-sm)] text-[var(--color-text-muted)] hover:text-[var(--color-text-secondary)] hover:bg-[var(--color-background)] transition-colors cursor-pointer"
          aria-label="Clear search"
        >
          <Icon name="x-mark" className="w-3.5 h-3.5" />
        </button>
      )}
    </div>
  )
}
```

- [ ] **Step: Rewrite SearchBar Storybook story**

Rewrite `demo-ui/src/molecules/SearchBar.stories.jsx`:

```jsx
import { SearchBar } from './SearchBar'

export default {
  title: 'Molecules/SearchBar',
  component: SearchBar,
  argTypes: {
    placeholder: { control: 'text' },
    debounceMs: { control: 'number' },
    onChange: { action: 'change' },
  },
}

export const Default = {
  args: {
    placeholder: 'Search products...',
    onChange: (value) => console.log('Search:', value),
  },
}

export const WithInitialValue = {
  args: {
    value: 'Paracetamol',
    placeholder: 'Search products...',
    onChange: (value) => console.log('Search:', value),
  },
}

export const SlowDebounce = {
  args: {
    placeholder: 'Slow debounce (1000ms)',
    debounceMs: 1000,
    onChange: (value) => console.log('Search:', value),
  },
}

export const NoDebounce = {
  args: {
    placeholder: 'Instant (0ms debounce)',
    debounceMs: 0,
    onChange: (value) => console.log('Search:', value),
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-6 max-w-md">
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Default (300ms debounce)</p>
        <SearchBar placeholder="Search products..." onChange={(v) => console.log(v)} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">With initial value</p>
        <SearchBar value="Amoxicillin" placeholder="Search..." onChange={(v) => console.log(v)} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Custom placeholder</p>
        <SearchBar placeholder="Search by molecule name..." onChange={(v) => console.log(v)} />
      </div>
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**
  ```
  feat(ui): rebuild SearchBar molecule with debounced onChange and clear button

  Adds controlled value prop, configurable debounce delay, X clear button,
  and uses semantic tokens. No longer wraps atom Input directly — uses native
  input for tighter control over the search UX.
  ```

---

### Task 5: Rebuild StatCard

**Files:**
- Rewrite: `demo-ui/src/molecules/StatCard.jsx`
- Rewrite: `demo-ui/src/molecules/StatCard.stories.jsx`

- [ ] **Step: Rewrite StatCard with icon, trend arrow, variant support, and loading skeleton**

Rewrite `demo-ui/src/molecules/StatCard.jsx`:

```jsx
import { Icon } from '../atoms/Icon'
import { Spinner } from '../atoms/Spinner'

const variantColors = {
  default: {
    icon: 'bg-[var(--color-primary-subtle)] text-[var(--color-primary)]',
    border: 'border-[var(--color-primary)]',
  },
  success: {
    icon: 'bg-[var(--color-success-subtle)] text-[var(--color-success)]',
    border: 'border-[var(--color-success)]',
  },
  danger: {
    icon: 'bg-[var(--color-danger-subtle)] text-[var(--color-danger)]',
    border: 'border-[var(--color-danger)]',
  },
  warning: {
    icon: 'bg-[var(--color-warning-subtle)] text-[var(--color-warning)]',
    border: 'border-[var(--color-warning)]',
  },
}

const trendIcons = {
  up: 'arrow-up',
  down: 'arrow-down',
}

const trendColors = {
  up: 'text-[var(--color-success)]',
  down: 'text-[var(--color-danger)]',
  flat: 'text-[var(--color-text-secondary)]',
}

export function StatCard({ icon, value, label, trend, trendValue, variant = 'default', loading = false, className = '' }) {
  const colors = variantColors[variant] || variantColors.default

  return (
    <div className={`bg-[var(--color-surface)] rounded-[var(--radius-lg)] p-[var(--space-5)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] transition-all duration-200 hover:shadow-[var(--shadow-md)] hover:-translate-y-0.5 ${className}`}>
      <div className="flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <div className="text-[length:var(--text-xs)] font-[var(--font-medium)] text-[var(--color-text-muted)] uppercase tracking-wider truncate">
            {label}
          </div>
          {loading ? (
            <div className="mt-[var(--space-2)] h-8 w-20 bg-[var(--color-background)] rounded-[var(--radius-sm)] animate-pulse" />
          ) : (
            <div className="text-[length:var(--text-3xl)] font-[var(--font-bold)] text-[var(--color-text-primary)] mt-[var(--space-1)] leading-tight">
              {value}
            </div>
          )}
          {trendValue && !loading && (
            <div className={`flex items-center gap-[var(--space-1)] text-[length:var(--text-xs)] mt-[var(--space-1)] ${trendColors[trend] || trendColors.flat}`}>
              {trend && trendIcons[trend] && (
                <Icon name={trendIcons[trend]} className="w-3 h-3" />
              )}
              <span>{trendValue}</span>
            </div>
          )}
        </div>
        {icon && (
          <div className={`flex items-center justify-center w-10 h-10 rounded-[var(--radius-lg)] ${colors.icon}`}>
            {typeof icon === 'string' ? <Icon name={icon} /> : icon}
          </div>
        )}
      </div>
    </div>
  )
}
```

- [ ] **Step: Rewrite StatCard Storybook story**

Rewrite `demo-ui/src/molecules/StatCard.stories.jsx`:

```jsx
import { StatCard } from './StatCard'

export default {
  title: 'Molecules/StatCard',
  component: StatCard,
  argTypes: {
    label: { control: 'text' },
    value: { control: 'text' },
    trend: { control: 'select', options: ['up', 'down', 'flat', undefined] },
    trendValue: { control: 'text' },
    variant: { control: 'select', options: ['default', 'success', 'danger', 'warning'] },
    loading: { control: 'boolean' },
  },
}

export const Default = {
  args: {
    label: 'Total Products',
    value: '1,234',
    icon: 'cube',
  },
}

export const WithTrendUp = {
  args: {
    label: 'Revenue',
    value: '$45,678',
    icon: 'credit-card',
    trend: 'up',
    trendValue: '+12.5%',
  },
}

export const WithTrendDown = {
  args: {
    label: 'Expired Items',
    value: '23',
    icon: 'exclamation',
    trend: 'down',
    trendValue: '-8.3%',
    variant: 'danger',
  },
}

export const Loading = {
  args: {
    label: 'Loading Stat',
    loading: true,
    icon: 'cube',
  },
}

export const NoIcon = {
  args: {
    label: 'Active Batches',
    value: '89',
    trend: 'up',
    trendValue: '+5 today',
    variant: 'success',
  },
}

export const Dashboard = {
  render: () => (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[var(--space-4)]">
      <StatCard
        label="Total Products"
        value="1,234"
        icon="cube"
        trend="up"
        trendValue="+48 this month"
      />
      <StatCard
        label="Active Batches"
        value="89"
        icon="archive"
        trend="flat"
        trendValue="No change"
        variant="success"
      />
      <StatCard
        label="Daily Revenue"
        value="$12,450"
        icon="credit-card"
        trend="up"
        trendValue="+12.5%"
      />
      <StatCard
        label="Expiring Soon"
        value="7"
        icon="exclamation"
        trend="down"
        trendValue="-3 resolved"
        variant="danger"
      />
    </div>
  ),
}

export const VariantMatrix = {
  render: () => (
    <div className="grid grid-cols-2 gap-[var(--space-4)] max-w-lg">
      <StatCard label="Default" value="100" icon="cube" />
      <StatCard label="Success" value="89" icon="check" variant="success" trend="up" trendValue="+12" />
      <StatCard label="Warning" value="5" icon="exclamation" variant="warning" trend="flat" trendValue="No change" />
      <StatCard label="Danger" value="3" icon="trash" variant="danger" trend="down" trendValue="-2" />
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**
  ```
  feat(ui): rebuild StatCard molecule with icon, variant, and trend indicator

  Adds icon prop (string name or node), variant prop for colored icon bg,
  loading skeleton, and proper trend arrow with value display.
  ```

---

### Task 6: Create DatePicker (NEW)

**Files:**
- Create: `demo-ui/src/molecules/DatePicker.jsx`
- Create: `demo-ui/src/molecules/DatePicker.stories.jsx`

- [ ] **Step: Create DatePicker molecule — native date input with FormField styling**

Create `demo-ui/src/molecules/DatePicker.jsx`:

```jsx
import { useId } from 'react'

export function DatePicker({
  label,
  value,
  onChange,
  min,
  max,
  error,
  disabled = false,
  helperText,
  required = false,
  className = '',
}) {
  const id = useId()

  return (
    <div className={className}>
      {label && (
        <label
          htmlFor={id}
          className="block text-[length:var(--text-sm)] font-[var(--font-medium)] text-[var(--color-text-secondary)] mb-1"
        >
          {label}
          {required && <span className="text-[var(--color-danger)] ml-0.5">*</span>}
        </label>
      )}
      <input
        id={id}
        type="date"
        value={value}
        onChange={onChange}
        min={min}
        max={max}
        disabled={disabled}
        className={`w-full px-[var(--space-3)] py-2 text-[length:var(--text-sm)] rounded-[var(--radius-md)] border bg-[var(--color-surface)] text-[var(--color-text-primary)] outline-none transition-colors focus:border-[var(--color-border-focus)] focus:ring-2 focus:ring-[var(--color-primary)]/20 disabled:opacity-50 disabled:cursor-not-allowed ${
          error
            ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)] focus:ring-[var(--color-danger)]/20'
            : 'border-[var(--color-border)]'
        }`}
      />
      {error && (
        <p className="text-[length:var(--text-xs)] text-[var(--color-danger)] mt-1" role="alert">
          {error}
        </p>
      )}
      {helperText && !error && (
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mt-1">
          {helperText}
        </p>
      )}
    </div>
  )
}
```

- [ ] **Step: Create DatePicker Storybook story**

Create `demo-ui/src/molecules/DatePicker.stories.jsx`:

```jsx
import { useState } from 'react'
import { DatePicker } from './DatePicker'

export default {
  title: 'Molecules/DatePicker',
  component: DatePicker,
  argTypes: {
    label: { control: 'text' },
    value: { control: 'date' },
    min: { control: 'date' },
    max: { control: 'date' },
    error: { control: 'text' },
    disabled: { control: 'boolean' },
    helperText: { control: 'text' },
    required: { control: 'boolean' },
  },
}

export const Default = {
  args: {
    label: 'Expiry Date',
    onChange: (e) => console.log(e.target.value),
  },
}

export const WithValue = {
  args: {
    label: 'Manufacture Date',
    value: '2026-01-15',
    onChange: (e) => console.log(e.target.value),
  },
}

export const WithMinMax = {
  args: {
    label: 'Delivery Date',
    min: '2026-05-17',
    max: '2026-12-31',
    helperText: 'Must be within the current year',
    onChange: (e) => console.log(e.target.value),
  },
}

export const WithError = {
  args: {
    label: 'Expiry Date',
    required: true,
    error: 'Expiry date is required',
    onChange: (e) => console.log(e.target.value),
  },
}

export const Disabled = {
  args: {
    label: 'Created Date',
    value: '2026-03-10',
    disabled: true,
    helperText: 'Auto-set on creation',
    onChange: () => {},
  },
}

export const Interactive = {
  render: () => {
    const [value, setValue] = useState('')
    return (
      <DatePicker
        label="Select a date"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        min="2026-01-01"
        max="2026-12-31"
        helperText={value ? `Selected: ${value}` : 'Pick any date in 2026'}
      />
    )
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-6 max-w-xs">
      <DatePicker label="Default" onChange={() => {}} />
      <DatePicker label="With value" value="2026-06-15" onChange={() => {}} />
      <DatePicker label="Required" required onChange={() => {}} />
      <DatePicker label="With error" required error="Date is required" onChange={() => {}} />
      <DatePicker label="Disabled" value="2026-01-01" disabled onChange={() => {}} />
      <DatePicker label="With helper" helperText="Select a date between today and Dec 31" onChange={() => {}} />
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**
  ```
  feat(ui): add DatePicker molecule for native date input with form styling

  Wraps HTML date input with label, error, helperText, min/max constraints.
  Supports disabled state and React Hook Form compatible onChange.
  ```

---

### Task 7: Create FileUploader (NEW)

**Files:**
- Create: `demo-ui/src/molecules/FileUploader.jsx`
- Create: `demo-ui/src/molecules/FileUploader.stories.jsx`

- [ ] **Step: Create FileUploader molecule — drag-and-drop + click to browse**

Create `demo-ui/src/molecules/FileUploader.jsx`:

```jsx
import { useState, useRef, useCallback } from 'react'
import { Icon } from '../atoms/Icon'
import { Spinner } from '../atoms/Spinner'

const MAX_DEFAULT_SIZE = 10 * 1024 * 1024 // 10 MB

function formatFileSize(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function FileUploader({
  accept,
  onFile,
  maxSize = MAX_DEFAULT_SIZE,
  loading = false,
  error,
  label = 'Upload File',
  className = '',
}) {
  const inputRef = useRef(null)
  const [isDragging, setIsDragging] = useState(false)
  const [selectedFile, setSelectedFile] = useState(null)
  const [sizeError, setSizeError] = useState('')

  const displayError = error || sizeError

  const processFile = useCallback(
    (file) => {
      setSizeError('')
      if (file && file.size > maxSize) {
        setSizeError(`File too large. Maximum size is ${formatFileSize(maxSize)}.`)
        return
      }
      setSelectedFile(file)
      if (file) {
        onFile?.(file)
      }
    },
    [maxSize, onFile]
  )

  const handleDrop = useCallback(
    (e) => {
      e.preventDefault()
      setIsDragging(false)
      const file = e.dataTransfer.files[0]
      processFile(file)
    },
    [processFile]
  )

  const handleDragOver = useCallback((e) => {
    e.preventDefault()
    setIsDragging(true)
  }, [])

  const handleDragLeave = useCallback((e) => {
    e.preventDefault()
    setIsDragging(false)
  }, [])

  const handleClick = useCallback(() => {
    inputRef.current?.click()
  }, [])

  const handleInputChange = useCallback(
    (e) => {
      const file = e.target.files[0]
      processFile(file)
    },
    [processFile]
  )

  return (
    <div className={className}>
      <div
        role="button"
        tabIndex={0}
        onClick={loading ? undefined : handleClick}
        onDrop={loading ? undefined : handleDrop}
        onDragOver={loading ? undefined : handleDragOver}
        onDragLeave={loading ? undefined : handleDragLeave}
        onKeyDown={(e) => {
          if ((e.key === 'Enter' || e.key === ' ') && !loading) {
            e.preventDefault()
            handleClick()
          }
        }}
        className={`flex flex-col items-center justify-center gap-[var(--space-2)] py-[var(--space-8)] px-[var(--space-4)] border-2 border-dashed rounded-[var(--radius-lg)] transition-colors cursor-pointer ${
          displayError
            ? 'border-[var(--color-danger)] bg-[var(--color-danger-subtle)]'
            : isDragging
              ? 'border-[var(--color-primary)] bg-[var(--color-primary-subtle)]'
              : 'border-[var(--color-border)] hover:border-[var(--color-primary)] hover:bg-[var(--color-primary-subtle)]'
        } ${loading ? 'opacity-50 cursor-wait' : ''}`}
      >
        {loading ? (
          <>
            <Spinner size="md" />
            <span className="text-[length:var(--text-sm)] text-[var(--color-text-secondary)]">Uploading...</span>
          </>
        ) : selectedFile && !displayError ? (
          <>
            <Icon name="check" className="w-8 h-8 text-[var(--color-success)]" />
            <span className="text-[length:var(--text-sm)] font-[var(--font-medium)] text-[var(--color-text-primary)]">
              {selectedFile.name}
            </span>
            <span className="text-[length:var(--text-xs)] text-[var(--color-text-muted)]">
              {formatFileSize(selectedFile.size)}
            </span>
          </>
        ) : (
          <>
            <Icon name="arrow-up-tray" className="w-8 h-8 text-[var(--color-text-muted)]" />
            <span className="text-[length:var(--text-sm)] text-[var(--color-text-secondary)]">
              <span className="text-[var(--color-primary)] font-[var(--font-medium)]">Click to upload</span>
              {' '}or drag and drop
            </span>
            {accept && (
              <span className="text-[length:var(--text-xs)] text-[var(--color-text-muted)]">
                Accepted: {accept}
              </span>
            )}
          </>
        )}
      </div>
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        onChange={handleInputChange}
        className="hidden"
        aria-label={label}
      />
      {displayError && (
        <p className="text-[length:var(--text-xs)] text-[var(--color-danger)] mt-1" role="alert">
          {displayError}
        </p>
      )}
    </div>
  )
}
```

- [ ] **Step: Create FileUploader Storybook story**

Create `demo-ui/src/molecules/FileUploader.stories.jsx`:

```jsx
import { FileUploader } from './FileUploader'

export default {
  title: 'Molecules/FileUploader',
  component: FileUploader,
  argTypes: {
    accept: { control: 'text' },
    maxSize: { control: 'number' },
    loading: { control: 'boolean' },
    error: { control: 'text' },
    label: { control: 'text' },
    onFile: { action: 'file' },
  },
}

export const Default = {
  args: {
    onFile: (file) => console.log('File:', file.name),
  },
}

export const CsvOnly = {
  args: {
    accept: '.csv',
    label: 'Upload CSV',
    onFile: (file) => console.log('CSV:', file.name),
  },
}

export const WithSizeLimit = {
  args: {
    accept: '.csv,.xlsx',
    maxSize: 5 * 1024 * 1024,
    onFile: (file) => console.log('File:', file.name),
  },
}

export const Loading = {
  args: {
    loading: true,
    onFile: () => {},
  },
}

export const WithError = {
  args: {
    error: 'Upload failed. Please try again.',
    onFile: () => {},
  },
}

export const MultipleFormats = {
  args: {
    accept: '.csv,.json,.xml',
    onFile: (file) => console.log('File:', file.name),
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-8 max-w-md">
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Default</p>
        <FileUploader onFile={(f) => console.log(f)} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">CSV only</p>
        <FileUploader accept=".csv" onFile={(f) => console.log(f)} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Loading</p>
        <FileUploader loading onFile={() => {}} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Error state</p>
        <FileUploader error="Invalid file format. Please upload a CSV file." onFile={() => {}} />
      </div>
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**
  ```
  feat(ui): add FileUploader molecule with drag-and-drop and file validation

  Supports click and drag-and-drop, shows filename + size after selection,
  validates max file size, and shows loading state during upload.
  ```

---

### Task 8: Create ConfirmationDialog (NEW)

**Files:**
- Create: `demo-ui/src/molecules/ConfirmationDialog.jsx`
- Create: `demo-ui/src/molecules/ConfirmationDialog.stories.jsx`

- [ ] **Step: Create ConfirmationDialog molecule using Headless UI Dialog**

Create `demo-ui/src/molecules/ConfirmationDialog.jsx`:

```jsx
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
  onCancel,
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
            <DialogTitle className="text-[length:var(--text-lg)] font-[var(--font-semibold)] text-[var(--color-text-primary)]">
              {title}
            </DialogTitle>
          )}
          {message && (
            <p className="text-[length:var(--text-sm)] text-[var(--color-text-secondary)] mt-[var(--space-2)]">
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
```

- [ ] **Step: Create ConfirmationDialog Storybook story**

Create `demo-ui/src/molecules/ConfirmationDialog.stories.jsx`:

```jsx
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
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Info variant</p>
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
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**
  ```
  feat(ui): add ConfirmationDialog molecule using Headless UI Dialog

  Supports info/warning/danger variants with matching button colors.
  Includes loading state for async confirmations, custom button labels,
  and backdrop overlay with semantic token colors.
  ```

---

### Task 9: Final Verification

- [ ] **Step: Run full build**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Verify all molecule files exist**
  Run: `ls demo-ui/src/molecules/*.jsx`

  Expected output should include all 16 files (8 components + 8 stories):
  - ConfirmationDialog.jsx, ConfirmationDialog.stories.jsx
  - DatePicker.jsx, DatePicker.stories.jsx
  - FileUploader.jsx, FileUploader.stories.jsx
  - FormField.jsx, FormField.stories.jsx
  - PageHeader.jsx, PageHeader.stories.jsx
  - Pagination.jsx, Pagination.stories.jsx
  - SearchBar.jsx, SearchBar.stories.jsx
  - StatCard.jsx, StatCard.stories.jsx

- [ ] **Step: Verify Storybook loads**
  Run: `cd demo-ui && npm run build-storybook`

---

## Summary

| Task | Component | Status | Type |
|------|-----------|--------|------|
| 1 | FormField | Rebuild | Form wrapper with label, error, helper text |
| 2 | PageHeader | Rebuild | Page title + subtitle + actions + breadcrumbs |
| 3 | Pagination | Rebuild | Smart page range, page size selector, total count |
| 4 | SearchBar | Rebuild | Debounced input with clear button |
| 5 | StatCard | Rebuild | Dashboard stat with icon, trend, variant |
| 6 | DatePicker | New | Native date input with form styling |
| 7 | FileUploader | New | Drag-and-drop + click, file size validation |
| 8 | ConfirmationDialog | New | Modal dialog using @headlessui/react Dialog |
| 9 | Final verification | - | Build + Storybook check |

**All molecules use semantic design tokens exclusively** (no primitive color references). Dark mode support comes free through the token layer defined in Plan 1.
