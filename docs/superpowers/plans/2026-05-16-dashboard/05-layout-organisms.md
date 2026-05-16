### Task 5: Layout Organisms — Sidebar + TopBar

**Files:**
- Create: `demo-ui/src/organisms/Sidebar.jsx`
- Create: `demo-ui/src/organisms/TopBar.jsx`

- [ ] **Step 1: Create Sidebar organism**

Create `demo-ui/src/organisms/Sidebar.jsx`:

```jsx
import { NavLink, useNavigate } from 'react-router-dom'
import { Icon } from '../atoms/Icon'
import api from '../lib/api'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: 'home', end: true },
  { to: '/products', label: 'Products', icon: 'cube' },
  { to: '/inventory', label: 'Inventory', icon: 'archive' },
  { to: '/clinical', label: 'Clinical', icon: 'beaker' },
  { to: '/orders', label: 'Orders', icon: 'cart' },
  { to: '/transactions', label: 'Transactions', icon: 'credit-card' },
  { to: '/locations', label: 'Locations', icon: 'map-pin' },
  { to: '/users', label: 'Users', icon: 'users' },
]

export function Sidebar() {
  const navigate = useNavigate()

  const handleLogout = () => {
    api.clearToken()
    navigate('/login')
  }

  return (
    <div className="w-14 bg-[var(--color-sidebar-bg)] flex flex-col items-center py-3 shrink-0">
      {/* Logo */}
      <div className="w-8 h-8 bg-[var(--color-primary)] rounded-[var(--radius-md)] flex items-center justify-center text-white text-sm font-bold mb-4">
        P
      </div>

      {/* Nav items */}
      <nav className="flex flex-col gap-1 flex-1">
        {NAV_ITEMS.map(({ to, label, icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `w-9 h-9 rounded-[var(--radius-md)] flex items-center justify-center transition-colors group relative ${
                isActive
                  ? 'bg-[var(--color-sidebar-active)] text-teal-300'
                  : 'text-slate-400 hover:text-slate-200'
              }`
            }
            title={label}
          >
            <Icon name={icon} />
            {/* Tooltip */}
            <span className="absolute left-12 bg-slate-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50">
              {label}
            </span>
          </NavLink>
        ))}
      </nav>

      {/* Sign out */}
      <button
        onClick={handleLogout}
        className="w-9 h-9 rounded-[var(--radius-md)] flex items-center justify-center text-slate-400 hover:text-slate-200 transition-colors group relative"
        title="Sign Out"
      >
        <Icon name="logout" />
        <span className="absolute left-12 bg-slate-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50">
          Sign Out
        </span>
      </button>
    </div>
  )
}
```

- [ ] **Step 2: Create TopBar organism**

Create `demo-ui/src/organisms/TopBar.jsx`:

```jsx
import { useState } from 'react'

export function TopBar({ title, subtitle }) {
  const [searchFocused, setSearchFocused] = useState(false)

  return (
    <div className="h-14 bg-white border-b border-[var(--color-border)] flex items-center justify-between px-6 shrink-0">
      {/* Left: Page info */}
      <div className="flex items-center gap-3">
        <span className="text-sm font-semibold text-[var(--color-text-primary)]">{title}</span>
        {subtitle && <span className="text-xs text-[var(--color-text-secondary)]">{subtitle}</span>}
      </div>

      {/* Right: Search + notifications + avatar */}
      <div className="flex items-center gap-3">
        <div className={`flex items-center gap-2 px-3 py-1.5 rounded-[var(--radius-md)] border transition-colors ${searchFocused ? 'border-[var(--color-primary)]' : 'border-[var(--color-border)]'}`}>
          <svg className="w-3.5 h-3.5 text-[var(--color-text-muted)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            placeholder="Search..."
            className="bg-transparent text-xs outline-none w-40 text-[var(--color-text-secondary)]"
            onFocus={() => setSearchFocused(true)}
            onBlur={() => setSearchFocused(false)}
          />
        </div>

        {/* Notification bell */}
        <div className="relative">
          <div className="w-8 h-8 rounded-[var(--radius-md)] border border-[var(--color-border)] flex items-center justify-center text-sm cursor-pointer hover:bg-[var(--color-background)] transition-colors">
            🔔
          </div>
          <div className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-[var(--color-danger)] rounded-full border-2 border-white" />
        </div>

        {/* Avatar */}
        <div className="w-8 h-8 bg-[var(--color-primary)] rounded-[var(--radius-md)] flex items-center justify-center text-white text-xs font-semibold">
          PS
        </div>
      </div>
    </div>
  )
}
```

- [ ] **Step 3: Commit**

```bash
mkdir -p demo-ui/src/organisms
git add demo-ui/src/organisms/Sidebar.jsx demo-ui/src/organisms/TopBar.jsx
git commit -m "feat(demo-ui): add Sidebar and TopBar organisms

Sidebar: 56px icon-only nav with tooltips, active state highlight, sign-out.
TopBar: 56px header with search input, notification bell, user avatar."
```
