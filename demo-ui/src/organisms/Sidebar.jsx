import { useState, useEffect } from 'react'
import { NavLink } from 'react-router-dom'
import { Icon } from '../atoms/Icon'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: 'home', end: true },
  { to: '/products', label: 'Products', icon: 'cube' },
  { to: '/inventory', label: 'Inventory', icon: 'archive' },
  { to: '/clinical', label: 'Clinical', icon: 'beaker' },
  { to: '/dispensing', label: 'Dispensing', icon: 'receipt' },
  { to: '/orders', label: 'Orders', icon: 'cart' },
  { to: '/transactions', label: 'Transactions', icon: 'credit-card' },
  { to: '/locations', label: 'Locations', icon: 'map-pin' },
  { to: '/users', label: 'Users', icon: 'users' },
]

const STORAGE_KEY = 'sidebar-collapsed'

export function Sidebar({
  collapsed: controlledCollapsed,
  onToggle,
  activeRoute,
  storeOptions = [],
  selectedStore,
  onStoreChange,
  user,
  onLogout,
  onToggleTheme,
  isDarkMode = false,
}) {
  const [internalCollapsed, setInternalCollapsed] = useState(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    return stored !== null ? stored === 'true' : true
  })

  const collapsed = controlledCollapsed ?? internalCollapsed

  const handleToggle = () => {
    if (onToggle) { onToggle() } else { setInternalCollapsed((c) => !c) }
  }

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, String(collapsed))
  }, [collapsed])

  return (
    <div className={`${collapsed ? 'w-14' : 'w-[200px]'} bg-[var(--color-sidebar-bg)] flex flex-col shrink-0 transition-all duration-200 h-screen`}>
      {/* Logo + Store selector */}
      <div className={collapsed ? 'flex justify-center py-3' : 'px-3 py-3'}>
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 bg-[var(--color-primary)] rounded-[var(--radius-md)] flex items-center justify-center text-white text-sm font-bold shrink-0">P</div>
          {!collapsed && <span className="text-white text-sm font-semibold">PharmStock</span>}
        </div>
      </div>

      {/* Store selector */}
      {storeOptions.length > 0 && !collapsed && (
        <div className="px-3 pb-2">
          <select
            value={selectedStore ?? ''}
            onChange={(e) => onStoreChange?.(e.target.value)}
            className="w-full bg-[var(--color-sidebar-bg)] border border-white/20 rounded-[var(--radius-md)] text-white text-xs px-2 py-1.5 focus:outline-none focus:border-[var(--color-primary)] cursor-pointer"
          >
            <option value="" disabled>Select store</option>
            {storeOptions.map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </div>
      )}

      {/* Nav items */}
      <nav className="flex flex-col gap-0.5 flex-1 px-1.5 mt-1">
        {NAV_ITEMS.map(({ to, label, icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center transition-colors group relative ${
                isActive ? 'bg-[var(--color-sidebar-active)] text-teal-300' : 'text-[var(--color-sidebar-text)] hover:text-[var(--color-sidebar-text-active)]'
              }`
            }
            title={label}
          >
            <Icon name={icon} className="w-5 h-5 shrink-0" />
            {!collapsed && <span className="text-xs font-medium truncate">{label}</span>}
            {collapsed && (
              <span className="absolute left-12 bg-slate-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50">{label}</span>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Bottom: user + theme + toggle */}
      <div className="px-1.5 pb-3 flex flex-col gap-0.5">
        {/* User info */}
        {!collapsed && user && (
          <div className="px-2.5 py-2 mb-1 rounded-[var(--radius-md)] bg-white/5">
            <p className="text-xs font-medium text-white truncate">{user.username}</p>
            <p className="text-xs text-[var(--color-sidebar-text)]">{user.role}</p>
          </div>
        )}

        {/* Theme toggle */}
        {onToggleTheme && (
          <button
            onClick={onToggleTheme}
            className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-[var(--color-sidebar-text)] hover:text-[var(--color-sidebar-text-active)] transition-colors cursor-pointer`}
            title={isDarkMode ? 'Switch to light mode' : 'Switch to dark mode'}
          >
            <Icon name={isDarkMode ? 'sun' : 'moon'} className="w-4 h-4 shrink-0" />
            {!collapsed && <span className="text-xs">{isDarkMode ? 'Light' : 'Dark'}</span>}
          </button>
        )}

        {/* Collapse toggle */}
        <button
          onClick={handleToggle}
          className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-[var(--color-sidebar-text)] hover:text-[var(--color-sidebar-text-active)] transition-colors border border-white/20 cursor-pointer`}
          title={collapsed ? 'Expand' : 'Collapse'}
        >
          <Icon name={collapsed ? 'chevron-right' : 'chevron-left'} className="w-4 h-4 shrink-0" />
          {!collapsed && <span className="text-xs">Collapse</span>}
        </button>

        {/* Logout */}
        {onLogout && (
          <button
            onClick={onLogout}
            className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-[var(--color-sidebar-text)] hover:text-[var(--color-sidebar-text-active)] transition-colors cursor-pointer`}
            title="Sign Out"
          >
            <Icon name="logout" className="w-5 h-5 shrink-0" />
            {!collapsed && <span className="text-xs">Sign Out</span>}
          </button>
        )}
      </div>
    </div>
  )
}
