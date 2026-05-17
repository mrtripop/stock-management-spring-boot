### Task 2: Atoms

**Files:**
- Create: `demo-ui/src/atoms/Button.jsx`
- Create: `demo-ui/src/atoms/Badge.jsx`
- Create: `demo-ui/src/atoms/Input.jsx`
- Create: `demo-ui/src/atoms/Select.jsx`
- Create: `demo-ui/src/atoms/Spinner.jsx`
- Create: `demo-ui/src/atoms/Icon.jsx`

- [ ] **Step 1: Create Button atom**

Create `demo-ui/src/atoms/Button.jsx`:

```jsx
import { Spinner } from './Spinner'

const variants = {
  primary: 'bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-hover)] shadow-sm',
  secondary: 'bg-white text-[var(--color-text-secondary)] border border-[var(--color-border)] hover:border-[var(--color-primary)] hover:text-[var(--color-primary)]',
  danger: 'bg-[var(--color-danger)] text-white hover:bg-[var(--color-danger-hover)]',
  ghost: 'bg-transparent text-[var(--color-primary)] hover:bg-[var(--color-background)]',
}

const sizes = {
  sm: 'h-7 px-2.5 text-xs gap-1',
  md: 'h-9 px-4 text-sm gap-1.5',
  lg: 'h-11 px-5 text-base gap-2',
}

export function Button({
  variant = 'primary',
  size = 'md',
  disabled = false,
  loading = false,
  icon: Icon,
  children,
  className = '',
  ...props
}) {
  return (
    <button
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center font-medium rounded-[var(--radius-md)] transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed ${variants[variant]} ${sizes[size]} ${className}`}
      {...props}
    >
      {loading ? <Spinner size="sm" /> : Icon ? <Icon className="w-4 h-4" /> : null}
      {children}
    </button>
  )
}
```

- [ ] **Step 2: Create Badge atom**

Create `demo-ui/src/atoms/Badge.jsx`:

```jsx
const variants = {
  success: 'bg-emerald-100 text-emerald-800',
  danger: 'bg-red-100 text-red-800',
  warning: 'bg-amber-100 text-amber-800',
  info: 'bg-blue-100 text-blue-800',
  neutral: 'bg-slate-100 text-slate-700',
  teal: 'bg-teal-100 text-teal-800',
  purple: 'bg-purple-100 text-purple-800',
  orange: 'bg-orange-100 text-orange-800',
}

export function Badge({ variant = 'neutral', children, className = '' }) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-[var(--radius-full)] text-xs font-medium ${variants[variant]} ${className}`}>
      {children}
    </span>
  )
}
```

- [ ] **Step 3: Create Input atom**

Create `demo-ui/src/atoms/Input.jsx`:

```jsx
export function Input({ error, className = '', ...props }) {
  return (
    <input
      className={`w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border outline-none transition-colors
        ${error
          ? 'border-[var(--color-danger)] focus:ring-2 focus:ring-red-200'
          : 'border-[var(--color-border)] focus:border-[var(--color-primary)] focus:ring-2 focus:ring-teal-100'
        } ${className}`}
      {...props}
    />
  )
}
```

- [ ] **Step 4: Create Select atom**

Create `demo-ui/src/atoms/Select.jsx`:

```jsx
export function Select({ error, className = '', children, ...props }) {
  return (
    <select
      className={`w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border outline-none transition-colors bg-white
        ${error
          ? 'border-[var(--color-danger)] focus:ring-2 focus:ring-red-200'
          : 'border-[var(--color-border)] focus:border-[var(--color-primary)] focus:ring-2 focus:ring-teal-100'
        } ${className}`}
      {...props}
    >
      {children}
    </select>
  )
}
```

- [ ] **Step 5: Create Spinner atom**

Create `demo-ui/src/atoms/Spinner.jsx`:

```jsx
const sizes = { sm: 'w-4 h-4 border-2', md: 'w-6 h-6 border-2', lg: 'w-8 h-8 border-3' }

export function Spinner({ size = 'md', className = '' }) {
  return (
    <div
      className={`rounded-full border-[var(--color-border)] border-t-[var(--color-primary)] animate-spin ${sizes[size]} ${className}`}
      role="status"
      aria-label="Loading"
    />
  )
}
```

- [ ] **Step 6: Create Icon atom**

Create `demo-ui/src/atoms/Icon.jsx`:

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
  'arrow-down-tray': Icons.ArrowDownTrayIcon,
  'arrow-up-tray': Icons.ArrowUpTrayIcon,
  'magnifying-glass': Icons.MagnifyingGlassIcon,
}

export function Icon({ name, className = '' }) {
  const Component = iconMap[name]
  if (!Component) return null
  return <Component className={`w-5 h-5 ${className}`} />
}
```

- [ ] **Step 7: Commit**

```bash
mkdir -p demo-ui/src/atoms
git add demo-ui/src/atoms/
git commit -m "feat(demo-ui): add atom components — Button, Badge, Input, Select, Spinner, Icon

All atoms use design tokens via CSS custom properties.
Button supports primary/secondary/danger/ghost variants with loading state.
Badge supports 8 color variants for status types.
Icon wraps @heroicons/react with a name-based lookup."
```
