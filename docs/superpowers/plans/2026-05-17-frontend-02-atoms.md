# Atom Components Rebuild Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans.

**Goal:** Rebuild all 6 atom components (Button, Input, Select, Badge, Icon, Spinner) against the new semantic design tokens, adding missing props and forwardRef support for React Hook Form.
**Architecture:** Each atom is a single JSX file co-located with its Storybook story. Components consume semantic CSS custom properties (e.g. `var(--color-primary)`) from the token layer. forwardRef is applied to form elements for React Hook Form `register()` compatibility.
**Tech Stack:** React 19, Tailwind CSS, Heroicons, Headless UI, Storybook

**Dependency:** Plan 1 (design tokens) must be completed first. This plan assumes `tokens.css` with all semantic tokens from spec section 1.3 is already in place at `src/styles/tokens.css`.

---

### Task 1: Rebuild Button

**Files:**
- Modify: `demo-ui/src/atoms/Button.jsx`
- Modify: `demo-ui/src/atoms/Button.stories.jsx`

- [ ] **Step: Replace Button.jsx with token-driven component including outline variant, fullWidth, and type props**

```jsx
import { forwardRef } from 'react'
import { Spinner } from './Spinner'

const variants = {
  primary:
    'bg-[var(--color-primary)] text-[var(--color-text-inverse)] hover:bg-[var(--color-primary-hover)] active:bg-[var(--color-primary-active)] shadow-sm',
  secondary:
    'bg-[var(--color-surface)] text-[var(--color-text-secondary)] border border-[var(--color-border)] hover:border-[var(--color-primary)] hover:text-[var(--color-primary)]',
  outline:
    'bg-transparent text-[var(--color-primary)] border border-[var(--color-primary)] hover:bg-[var(--color-primary-subtle)]',
  danger:
    'bg-[var(--color-danger)] text-[var(--color-text-inverse)] hover:opacity-90',
  ghost:
    'bg-transparent text-[var(--color-primary)] hover:bg-[var(--color-primary-subtle)]',
}

const sizes = {
  sm: 'h-7 px-2.5 text-[var(--text-xs)] gap-1',
  md: 'h-9 px-4 text-[var(--text-sm)] gap-1.5',
  lg: 'h-11 px-5 text-[var(--text-base)] gap-2',
}

export const Button = forwardRef(function Button(
  {
    variant = 'primary',
    size = 'md',
    type = 'button',
    disabled = false,
    loading = false,
    icon: Icon,
    fullWidth = false,
    children,
    className = '',
    ...props
  },
  ref
) {
  return (
    <button
      ref={ref}
      type={type}
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center font-medium rounded-[var(--radius-md)] transition-all cursor-pointer active:scale-[0.97] disabled:opacity-50 disabled:cursor-not-allowed ${fullWidth ? 'w-full' : ''} ${variants[variant]} ${sizes[size]} ${className}`}
      {...props}
    >
      {loading ? <Spinner size="sm" /> : Icon ? <Icon className="w-4 h-4 shrink-0" /> : null}
      {children}
    </button>
  )
})
```

- [ ] **Step: Replace Button.stories.jsx with complete story including all props, variant matrix, and playground**

```jsx
import { Button } from './Button'
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline'

export default {
  title: 'Atoms/Button',
  component: Button,
  argTypes: {
    variant: {
      control: 'select',
      options: ['primary', 'secondary', 'outline', 'danger', 'ghost'],
      description: 'Visual style variant',
      table: { defaultValue: { summary: 'primary' } },
    },
    size: {
      control: 'select',
      options: ['sm', 'md', 'lg'],
      description: 'Button height and padding',
      table: { defaultValue: { summary: 'md' } },
    },
    type: {
      control: 'select',
      options: ['button', 'submit', 'reset'],
      description: 'HTML button type attribute',
      table: { defaultValue: { summary: 'button' } },
    },
    disabled: {
      control: 'boolean',
      description: 'Disables the button',
      table: { defaultValue: { summary: 'false' } },
    },
    loading: {
      control: 'boolean',
      description: 'Shows spinner, disables button',
      table: { defaultValue: { summary: 'false' } },
    },
    fullWidth: {
      control: 'boolean',
      description: 'Stretches to fill container width',
      table: { defaultValue: { summary: 'false' } },
    },
    icon: {
      description: 'Heroicons component rendered before children',
    },
    children: {
      control: 'text',
      description: 'Button label content',
    },
    onClick: { action: 'clicked' },
  },
  args: {
    children: 'Click me',
    variant: 'primary',
    size: 'md',
    type: 'button',
    disabled: false,
    loading: false,
    fullWidth: false,
  },
}

export const Playground = {
  args: {
    children: 'Click me',
  },
}

export const Variants = {
  render: () => (
    <div className="flex flex-wrap gap-3">
      <Button variant="primary">Primary</Button>
      <Button variant="secondary">Secondary</Button>
      <Button variant="outline">Outline</Button>
      <Button variant="danger">Danger</Button>
      <Button variant="ghost">Ghost</Button>
    </div>
  ),
}

export const Sizes = {
  render: () => (
    <div className="flex items-center gap-3">
      <Button size="sm">Small</Button>
      <Button size="md">Medium</Button>
      <Button size="lg">Large</Button>
    </div>
  ),
}

export const States = {
  render: () => (
    <div className="flex flex-wrap items-center gap-3">
      <Button>Normal</Button>
      <Button disabled>Disabled</Button>
      <Button loading>Loading</Button>
    </div>
  ),
}

export const WithIcon = {
  render: () => (
    <div className="flex flex-wrap items-center gap-3">
      <Button icon={MagnifyingGlassIcon}>Search</Button>
      <Button variant="outline" icon={MagnifyingGlassIcon}>Search</Button>
      <Button size="sm" icon={MagnifyingGlassIcon}>Search</Button>
    </div>
  ),
}

export const FullWidth = {
  render: () => (
    <div className="flex flex-col gap-3 max-w-xs">
      <Button fullWidth>Full Width</Button>
      <Button fullWidth variant="outline">Outline Full Width</Button>
    </div>
  ),
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-4">
      {['primary', 'secondary', 'outline', 'danger', 'ghost'].map((variant) => (
        <div key={variant} className="flex flex-wrap items-center gap-3">
          <span className="w-24 text-sm text-[var(--color-text-secondary)] font-medium">{variant}</span>
          <Button variant={variant} size="sm">Small</Button>
          <Button variant={variant} size="md">Medium</Button>
          <Button variant={variant} size="lg">Large</Button>
          <Button variant={variant} disabled>Disabled</Button>
          <Button variant={variant} loading>Loading</Button>
        </div>
      ))}
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**

---

### Task 2: Rebuild Input

**Files:**
- Modify: `demo-ui/src/atoms/Input.jsx`
- Modify: `demo-ui/src/atoms/Input.stories.jsx`

- [ ] **Step: Replace Input.jsx with forwardRef component including error, helperText, and icon slots**

```jsx
import { forwardRef } from 'react'

export const Input = forwardRef(function Input(
  {
    label,
    error,
    helperText,
    leftIcon: LeftIcon,
    rightIcon: RightIcon,
    disabled = false,
    className = '',
    ...props
  },
  ref
) {
  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label className="text-[var(--text-sm)] font-medium text-[var(--color-text-primary)]">
          {label}
        </label>
      )}
      <div className="relative">
        {LeftIcon && (
          <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[var(--color-text-muted)]">
            <LeftIcon className="w-4 h-4" />
          </span>
        )}
        <input
          ref={ref}
          disabled={disabled}
          className={`w-full px-3 py-2 text-[var(--text-sm)] rounded-[var(--radius-md)] border outline-none transition-colors bg-[var(--color-surface)]
            ${LeftIcon ? 'pl-9' : ''}
            ${RightIcon ? 'pr-9' : ''}
            ${error
              ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)] focus-visible:ring-2 focus-visible:ring-[var(--color-danger)]/20'
              : 'border-[var(--color-border)] focus:border-[var(--color-border-focus)] focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/20'
            }
            disabled:bg-[var(--color-background)] disabled:cursor-not-allowed disabled:opacity-60
            ${className}`}
          aria-invalid={!!error}
          aria-describedby={error ? `${props.id}-error` : helperText ? `${props.id}-helper` : undefined}
          {...props}
        />
        {RightIcon && (
          <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-text-muted)]">
            <RightIcon className="w-4 h-4" />
          </span>
        )}
      </div>
      {error && (
        <p id={`${props.id}-error`} className="text-[var(--text-xs)] text-[var(--color-danger)]">
          {error}
        </p>
      )}
      {helperText && !error && (
        <p id={`${props.id}-helper`} className="text-[var(--text-xs)] text-[var(--color-text-muted)]">
          {helperText}
        </p>
      )}
    </div>
  )
})
```

- [ ] **Step: Replace Input.stories.jsx with complete story documenting all props, states, and icon slots**

```jsx
import { Input } from './Input'
import { MagnifyingGlassIcon, ExclamationCircleIcon } from '@heroicons/react/24/outline'

export default {
  title: 'Atoms/Input',
  component: Input,
  argTypes: {
    label: {
      control: 'text',
      description: 'Label text above the input',
    },
    error: {
      control: 'text',
      description: 'Error message displayed below the input',
    },
    helperText: {
      control: 'text',
      description: 'Helper text displayed below the input (hidden when error is set)',
    },
    placeholder: {
      control: 'text',
      description: 'Placeholder text',
    },
    disabled: {
      control: 'boolean',
      description: 'Disables the input',
      table: { defaultValue: { summary: 'false' } },
    },
    leftIcon: {
      description: 'Heroicons component rendered inside the left side',
    },
    rightIcon: {
      description: 'Heroicons component rendered inside the right side',
    },
  },
  args: {
    placeholder: 'Enter text...',
  },
}

export const Playground = {
  args: {
    placeholder: 'Type something...',
    label: 'Label',
  },
}

export const Default = {
  args: { placeholder: 'Enter text...' },
}

export const WithLabel = {
  args: {
    label: 'Username',
    placeholder: 'Enter your username',
    id: 'username',
  },
}

export const WithHelperText = {
  args: {
    label: 'Email',
    placeholder: 'you@example.com',
    helperText: 'We will never share your email.',
    id: 'email',
  },
}

export const WithError = {
  args: {
    label: 'Password',
    placeholder: 'Enter password',
    error: 'Password must be at least 8 characters',
    id: 'password',
  },
}

export const Disabled = {
  args: {
    label: 'Disabled Field',
    placeholder: 'Cannot edit',
    disabled: true,
    id: 'disabled',
  },
}

export const WithLeftIcon = {
  render: () => (
    <Input
      label="Search"
      placeholder="Search products..."
      leftIcon={MagnifyingGlassIcon}
      id="search"
    />
  ),
}

export const WithRightIcon = {
  render: () => (
    <Input
      label="Website"
      placeholder="https://example.com"
      rightIcon={ExclamationCircleIcon}
      id="website"
    />
  ),
}

export const WithBothIcons = {
  render: () => (
    <Input
      label="Search products"
      placeholder="Type to search..."
      leftIcon={MagnifyingGlassIcon}
      rightIcon={ExclamationCircleIcon}
      id="search-both"
    />
  ),
}

export const InputTypes = {
  render: () => (
    <div className="flex flex-col gap-4 max-w-sm">
      <Input label="Text" placeholder="Text input" type="text" id="text" />
      <Input label="Password" placeholder="Password" type="password" id="password" />
      <Input label="Email" placeholder="email@example.com" type="email" id="email" />
      <Input label="Number" placeholder="123" type="number" id="number" />
      <Input label="Date" type="date" id="date" />
    </div>
  ),
}

export const StateMatrix = {
  render: () => (
    <div className="flex flex-col gap-4 max-w-sm">
      <Input label="Default" placeholder="Default state" id="default" />
      <Input label="With helper" placeholder="With helper" helperText="Helper text here" id="helper" />
      <Input label="With error" placeholder="Error state" error="This field is required" id="error" />
      <Input label="Disabled" placeholder="Disabled" disabled id="disabled" />
      <Input label="Error + icon" placeholder="Error with icon" error="Invalid search" leftIcon={MagnifyingGlassIcon} id="error-icon" />
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**

---

### Task 3: Rebuild Select

**Files:**
- Modify: `demo-ui/src/atoms/Select.jsx`
- Modify: `demo-ui/src/atoms/Select.stories.jsx`

- [ ] **Step: Replace Select.jsx with forwardRef component including options array prop, error state, and placeholder**

```jsx
import { forwardRef } from 'react'

export const Select = forwardRef(function Select(
  {
    label,
    error,
    helperText,
    options = [],
    placeholder,
    disabled = false,
    className = '',
    ...props
  },
  ref
) {
  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label className="text-[var(--text-sm)] font-medium text-[var(--color-text-primary)]">
          {label}
        </label>
      )}
      <select
        ref={ref}
        disabled={disabled}
        className={`w-full px-3 py-2 text-[var(--text-sm)] rounded-[var(--radius-md)] border outline-none transition-colors bg-[var(--color-surface)] appearance-none bg-[length:16px_16px] bg-[right_8px_center] bg-no-repeat
          ${error
            ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)] focus-visible:ring-2 focus-visible:ring-[var(--color-danger)]/20'
            : 'border-[var(--color-border)] focus:border-[var(--color-border-focus)] focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/20'
          }
          disabled:bg-[var(--color-background)] disabled:cursor-not-allowed disabled:opacity-60
          ${className}`}
        style={{
          backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3E%3Cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3E%3C/svg%3E")`,
        }}
        aria-invalid={!!error}
        aria-describedby={error ? `${props.id}-error` : helperText ? `${props.id}-helper` : undefined}
        {...props}
      >
        {placeholder && (
          <option value="" disabled>
            {placeholder}
          </option>
        )}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error && (
        <p id={`${props.id}-error`} className="text-[var(--text-xs)] text-[var(--color-danger)]">
          {error}
        </p>
      )}
      {helperText && !error && (
        <p id={`${props.id}-helper`} className="text-[var(--text-xs)] text-[var(--color-text-muted)]">
          {helperText}
        </p>
      )}
    </div>
  )
})
```

- [ ] **Step: Replace Select.stories.jsx with complete story documenting options prop, error state, and pharmacy enum examples**

```jsx
import { Select } from './Select'

export default {
  title: 'Atoms/Select',
  component: Select,
  argTypes: {
    label: {
      control: 'text',
      description: 'Label text above the select',
    },
    error: {
      control: 'text',
      description: 'Error message displayed below the select',
    },
    helperText: {
      control: 'text',
      description: 'Helper text displayed below the select',
    },
    placeholder: {
      control: 'text',
      description: 'Placeholder option (disabled, empty value)',
    },
    options: {
      description: 'Array of { value, label } objects',
    },
    disabled: {
      control: 'boolean',
      description: 'Disables the select',
      table: { defaultValue: { summary: 'false' } },
    },
  },
  args: {
    placeholder: 'Select an option...',
    options: [
      { value: 'option1', label: 'Option 1' },
      { value: 'option2', label: 'Option 2' },
      { value: 'option3', label: 'Option 3' },
    ],
  },
}

export const Playground = {
  args: {
    label: 'Choose one',
    id: 'playground',
  },
}

export const Default = {
  args: {
    placeholder: 'Select an option...',
    options: [
      { value: '1', label: 'Option 1' },
      { value: '2', label: 'Option 2' },
      { value: '3', label: 'Option 3' },
    ],
  },
}

export const WithLabel = {
  args: {
    label: 'Category',
    placeholder: 'Choose a category...',
    options: [
      { value: 'otc', label: 'Over the Counter' },
      { value: 'prescription', label: 'Prescription' },
      { value: 'supplement', label: 'Supplement' },
    ],
    id: 'category',
  },
}

export const WithHelperText = {
  args: {
    label: 'Sort by',
    helperText: 'Default sort is by name',
    options: [
      { value: 'name', label: 'Name' },
      { value: 'price', label: 'Price' },
      { value: 'date', label: 'Date Added' },
    ],
    id: 'sort',
  },
}

export const WithError = {
  args: {
    label: 'Role',
    error: 'Please select a role',
    placeholder: 'Select role...',
    options: [
      { value: 'admin', label: 'Admin' },
      { value: 'pharmacist', label: 'Pharmacist' },
      { value: 'employee', label: 'Employee' },
    ],
    id: 'role-error',
  },
}

export const Disabled = {
  args: {
    label: 'Disabled',
    disabled: true,
    options: [
      { value: '1', label: 'Option 1' },
    ],
    id: 'disabled',
  },
}

export const StoreType = {
  name: 'Pharmacy Enum — StoreType',
  render: () => (
    <Select
      label="Store Type"
      placeholder="Select store type..."
      options={[
        { value: 'PHYSICAL', label: 'Physical' },
        { value: 'HUB', label: 'Hub' },
        { value: 'LOGICAL', label: 'Logical' },
      ]}
      id="store-type"
    />
  ),
}

export const UserRole = {
  name: 'Pharmacy Enum — UserRole',
  render: () => (
    <Select
      label="User Role"
      placeholder="Select role..."
      options={[
        { value: 'ADMIN', label: 'Admin' },
        { value: 'MANAGER', label: 'Manager' },
        { value: 'PHARMACIST', label: 'Pharmacist' },
        { value: 'EMPLOYEE', label: 'Employee' },
      ]}
      id="user-role"
    />
  ),
}

export const BatchStatus = {
  name: 'Pharmacy Enum — BatchStatus',
  render: () => (
    <Select
      label="Batch Status"
      placeholder="Filter by status..."
      options={[
        { value: 'AVAILABLE', label: 'Available' },
        { value: 'RECALLED', label: 'Recalled' },
        { value: 'QUARANTINED', label: 'Quarantined' },
      ]}
      id="batch-status"
    />
  ),
}

export const InvoiceStatus = {
  name: 'Pharmacy Enum — InvoiceStatus',
  render: () => (
    <Select
      label="Invoice Status"
      placeholder="Filter by status..."
      options={[
        { value: 'PENDING', label: 'Pending' },
        { value: 'COMPLETED', label: 'Completed' },
        { value: 'VOIDED', label: 'Voided' },
      ]}
      id="invoice-status"
    />
  ),
}

export const StateMatrix = {
  render: () => (
    <div className="flex flex-col gap-4 max-w-sm">
      <Select
        label="Default"
        placeholder="Select..."
        options={[{ value: '1', label: 'Option 1' }]}
        id="default"
      />
      <Select
        label="With helper"
        placeholder="Select..."
        options={[{ value: '1', label: 'Option 1' }]}
        helperText="Pick one"
        id="helper"
      />
      <Select
        label="With error"
        placeholder="Select..."
        options={[{ value: '1', label: 'Option 1' }]}
        error="Required field"
        id="error"
      />
      <Select
        label="Disabled"
        placeholder="Select..."
        options={[{ value: '1', label: 'Option 1' }]}
        disabled
        id="disabled"
      />
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**

---

### Task 4: Rebuild Badge

**Files:**
- Modify: `demo-ui/src/atoms/Badge.jsx`
- Modify: `demo-ui/src/atoms/Badge.stories.jsx`

- [ ] **Step: Replace Badge.jsx with token-driven component including dot sub-variant**

```jsx
const variants = {
  success: 'bg-[var(--color-success-subtle)] text-[var(--color-success-text)]',
  danger: 'bg-[var(--color-danger-subtle)] text-[var(--color-danger-text)]',
  warning: 'bg-[var(--color-warning-subtle)] text-[var(--color-warning-text)]',
  info: 'bg-[var(--color-info-subtle)] text-[var(--color-info-text)]',
  neutral: 'bg-[var(--color-background)] text-[var(--color-text-secondary)]',
  teal: 'bg-[var(--color-primary-subtle)] text-[var(--color-primary-text)]',
  purple: 'bg-purple-100 text-purple-800',
  orange: 'bg-orange-100 text-orange-800',
}

const dotColors = {
  success: 'bg-[var(--color-success)]',
  danger: 'bg-[var(--color-danger)]',
  warning: 'bg-[var(--color-warning)]',
  info: 'bg-[var(--color-info)]',
  neutral: 'bg-[var(--color-text-muted)]',
  teal: 'bg-[var(--color-primary)]',
  purple: 'bg-purple-500',
  orange: 'bg-orange-500',
}

export function Badge({ variant = 'neutral', dot = false, children, className = '' }) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-[var(--radius-full)] text-[var(--text-xs)] font-medium ${variants[variant]} ${className}`}
    >
      {dot && <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${dotColors[variant]}`} />}
      {children}
    </span>
  )
}
```

- [ ] **Step: Replace Badge.stories.jsx with complete story including all variants, dot sub-variant, and pharmacy enum mappings**

```jsx
import { Badge } from './Badge'

export default {
  title: 'Atoms/Badge',
  component: Badge,
  argTypes: {
    variant: {
      control: 'select',
      options: ['success', 'danger', 'warning', 'info', 'neutral', 'teal', 'purple', 'orange'],
      description: 'Color variant',
      table: { defaultValue: { summary: 'neutral' } },
    },
    dot: {
      control: 'boolean',
      description: 'Show a small colored dot before the text for status indicators',
      table: { defaultValue: { summary: 'false' } },
    },
    children: {
      control: 'text',
      description: 'Badge content',
    },
  },
  args: {
    children: 'Badge',
    variant: 'neutral',
    dot: false,
  },
}

export const Playground = {
  args: {
    children: 'Active',
    variant: 'success',
  },
}

export const AllVariants = {
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="success">Success</Badge>
      <Badge variant="danger">Danger</Badge>
      <Badge variant="warning">Warning</Badge>
      <Badge variant="info">Info</Badge>
      <Badge variant="neutral">Neutral</Badge>
      <Badge variant="teal">Teal</Badge>
      <Badge variant="purple">Purple</Badge>
      <Badge variant="orange">Orange</Badge>
    </div>
  ),
}

export const WithDot = {
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="success" dot>Active</Badge>
      <Badge variant="danger" dot>Expired</Badge>
      <Badge variant="warning" dot>Pending</Badge>
      <Badge variant="info" dot>Acknowledged</Badge>
      <Badge variant="neutral" dot>Inactive</Badge>
      <Badge variant="teal" dot>Pharmacist</Badge>
      <Badge variant="purple" dot>Admin</Badge>
      <Badge variant="orange" dot>Reorder Needed</Badge>
    </div>
  ),
}

export const BatchStatus = {
  name: 'Pharmacy Enum — BatchStatus',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="success" dot>AVAILABLE</Badge>
      <Badge variant="danger" dot>RECALLED</Badge>
      <Badge variant="warning" dot>QUARANTINED</Badge>
    </div>
  ),
}

export const InvoiceStatus = {
  name: 'Pharmacy Enum — InvoiceStatus',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="warning" dot>PENDING</Badge>
      <Badge variant="success" dot>COMPLETED</Badge>
      <Badge variant="danger" dot>VOIDED</Badge>
    </div>
  ),
}

export const TaskStatus = {
  name: 'Pharmacy Enum — TaskStatus',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="warning" dot>PENDING</Badge>
      <Badge variant="info" dot>ACKNOWLEDGED</Badge>
      <Badge variant="success" dot>RESOLVED</Badge>
    </div>
  ),
}

export const UserRole = {
  name: 'Pharmacy Enum — UserRole',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="purple">ADMIN</Badge>
      <Badge variant="teal">MANAGER</Badge>
      <Badge variant="info">PHARMACIST</Badge>
      <Badge variant="neutral">EMPLOYEE</Badge>
    </div>
  ),
}

export const StoreType = {
  name: 'Pharmacy Enum — StoreType',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="teal">PHYSICAL</Badge>
      <Badge variant="info">HUB</Badge>
      <Badge variant="neutral">LOGICAL</Badge>
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**

---

### Task 5: Rebuild Icon

**Files:**
- Modify: `demo-ui/src/atoms/Icon.jsx`
- Modify: `demo-ui/src/atoms/Icon.stories.jsx`

- [ ] **Step: Replace Icon.jsx with explicit size prop mapped to pixel values**

```jsx
import * as Icons from '@heroicons/react/24/outline'

const iconMap = {
  'home': Icons.HomeIcon,
  'cube': Icons.CubeIcon,
  'archive': Icons.ArchiveBoxIcon,
  'beaker': Icons.BeakerIcon,
  'cart': Icons.ShoppingCartIcon,
  'credit-card': Icons.CreditCardIcon,
  'map-pin': Icons.MapPinIcon,
  'users': Icons.UserGroupIcon,
  'logout': Icons.ArrowRightOnRectangleIcon,
  'search': Icons.MagnifyingGlassIcon,
  'bell': Icons.BellIcon,
  'plus': Icons.PlusIcon,
  'pencil': Icons.PencilSquareIcon,
  'trash': Icons.TrashIcon,
  'funnel': Icons.FunnelIcon,
  'arrow-down': Icons.ArrowDownIcon,
  'arrow-up': Icons.ArrowUpIcon,
  'exclamation': Icons.ExclamationTriangleIcon,
  'check': Icons.CheckIcon,
  'x-mark': Icons.XMarkIcon,
  'chevron-left': Icons.ChevronLeftIcon,
  'chevron-right': Icons.ChevronRightIcon,
  'chevron-down': Icons.ChevronDownIcon,
  'arrow-down-tray': Icons.ArrowDownTrayIcon,
  'arrow-up-tray': Icons.ArrowUpTrayIcon,
  'magnifying-glass': Icons.MagnifyingGlassIcon,
  'receipt': Icons.DocumentTextIcon,
}

const sizeMap = {
  sm: 'w-4 h-4',
  md: 'w-5 h-5',
  lg: 'w-6 h-6',
}

export function Icon({ name, size = 'md', className = '' }) {
  const Component = iconMap[name]
  if (!Component) return null
  return <Component className={`${sizeMap[size]} ${className}`} />}
```

- [ ] **Step: Replace Icon.stories.jsx with complete story showing size prop and full icon catalog**

```jsx
import { Icon } from './Icon'

const allIconNames = [
  'home', 'cube', 'archive', 'beaker', 'cart', 'credit-card', 'map-pin',
  'users', 'logout', 'search', 'bell', 'plus', 'pencil', 'trash', 'funnel',
  'arrow-down', 'arrow-up', 'exclamation', 'check', 'x-mark',
  'chevron-left', 'chevron-right', 'chevron-down',
  'arrow-down-tray', 'arrow-up-tray', 'magnifying-glass', 'receipt',
]

export default {
  title: 'Atoms/Icon',
  component: Icon,
  argTypes: {
    name: {
      control: 'select',
      options: allIconNames,
      description: 'Icon name from the Heroicons map',
      table: { defaultValue: { summary: 'home' } },
    },
    size: {
      control: 'select',
      options: ['sm', 'md', 'lg'],
      description: 'Icon size: sm=16px, md=20px, lg=24px',
      table: { defaultValue: { summary: 'md' } },
    },
  },
  args: {
    name: 'home',
    size: 'md',
  },
}

export const Playground = {
  args: {
    name: 'cube',
    size: 'md',
  },
}

export const Sizes = {
  render: () => (
    <div className="flex items-center gap-6">
      <div className="flex flex-col items-center gap-1">
        <Icon name="cube" size="sm" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">sm (16px)</span>
      </div>
      <div className="flex flex-col items-center gap-1">
        <Icon name="cube" size="md" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">md (20px)</span>
      </div>
      <div className="flex flex-col items-center gap-1">
        <Icon name="cube" size="lg" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">lg (24px)</span>
      </div>
    </div>
  ),
}

export const AllIcons = {
  render: () => (
    <div className="grid grid-cols-6 gap-4">
      {allIconNames.map((name) => (
        <div key={name} className="flex flex-col items-center gap-1">
          <Icon name={name} size="md" />
          <span className="text-[var(--text-2xs)] text-[var(--color-text-muted)] text-center truncate w-full">{name}</span>
        </div>
      ))}
    </div>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**

---

### Task 6: Rebuild Spinner

**Files:**
- Modify: `demo-ui/src/atoms/Spinner.jsx`
- Modify: `demo-ui/src/atoms/Spinner.stories.jsx`

- [ ] **Step: Replace Spinner.jsx with color prop defaulting to primary semantic token**

```jsx
const sizes = {
  sm: 'w-4 h-4 border-2',
  md: 'w-6 h-6 border-2',
  lg: 'w-8 h-8 border-[3px]',
}

export function Spinner({ size = 'md', color, className = '' }) {
  const borderColor = color || 'var(--color-primary)'
  return (
    <div
      className={`rounded-full border-transparent animate-spin ${sizes[size]} ${className}`}
      style={{
        borderColor: 'var(--color-border)',
        borderTopColor: borderColor,
      }}
      role="status"
      aria-label="Loading"
    />
  )
}
```

- [ ] **Step: Replace Spinner.stories.jsx with complete story including size, color, and usage examples**

```jsx
import { Spinner } from './Spinner'

export default {
  title: 'Atoms/Spinner',
  component: Spinner,
  argTypes: {
    size: {
      control: 'select',
      options: ['sm', 'md', 'lg'],
      description: 'Spinner diameter: sm=16px, md=24px, lg=32px',
      table: { defaultValue: { summary: 'md' } },
    },
    color: {
      control: 'color',
      description: 'Border top color. Defaults to var(--color-primary)',
      table: { defaultValue: { summary: 'var(--color-primary)' } },
    },
  },
  args: {
    size: 'md',
  },
}

export const Playground = {
  args: {
    size: 'md',
  },
}

export const Sizes = {
  render: () => (
    <div className="flex items-center gap-6">
      <div className="flex flex-col items-center gap-2">
        <Spinner size="sm" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">sm</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">md</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="lg" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">lg</span>
      </div>
    </div>
  ),
}

export const CustomColors = {
  render: () => (
    <div className="flex items-center gap-6">
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">primary</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" color="var(--color-danger)" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">danger</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" color="var(--color-success)" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">success</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" color="var(--color-warning)" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">warning</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" color="var(--color-text-secondary)" />
        <span className="text-[var(--text-xs)] text-[var(--color-text-muted)]">muted</span>
      </div>
    </div>
  ),
}

export const InsideButton = {
  name: 'Usage — Inside Button',
  render: () => (
    <button
      disabled
      className="inline-flex items-center gap-2 px-4 h-9 text-[var(--text-sm)] font-medium rounded-[var(--radius-md)] bg-[var(--color-primary)] text-[var(--color-text-inverse)] opacity-75 cursor-not-allowed"
    >
      <Spinner size="sm" color="var(--color-text-inverse)" />
      Saving...
    </button>
  ),
}
```

- [ ] **Step: Verify**
  Run: `cd demo-ui && npm run build`

- [ ] **Step: Commit**

---

### Task 7: Final Verification

**Files:** None (verification only)

- [ ] **Step: Run full Storybook build to confirm all atom stories render**

  Run: `cd demo-ui && npm run build`

- [ ] **Step: Start Storybook and visually confirm all atoms**

  Run: `cd demo-ui && npm run storybook`

  Confirm all 6 stories appear under the `Atoms/` section:
  - Atoms/Button — all variants, sizes, states, icon, fullWidth, playground
  - Atoms/Input — label, error, helperText, leftIcon/rightIcon, disabled, state matrix
  - Atoms/Select — label, options, error, placeholder, pharmacy enum stories
  - Atoms/Badge — all variants, dot sub-variant, pharmacy enum mappings
  - Atoms/Icon — size prop (sm/md/lg), full icon catalog
  - Atoms/Spinner — size, custom colors, inside-button usage example

- [ ] **Step: Final commit (if any fixes were needed)**
