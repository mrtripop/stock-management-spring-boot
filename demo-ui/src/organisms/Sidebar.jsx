import { useState, useEffect } from 'react'
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

const STORAGE_KEY = 'sidebar-collapsed'

export function Sidebar() {
  const navigate = useNavigate()
  const [collapsed, setCollapsed] = useState(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    return stored !== null ? stored === 'true' : true
  })

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, String(collapsed))
  }, [collapsed])

  const handleLogout = () => {
    api.clearToken()
    navigate('/login')
  }

  return (
    <div className={`${collapsed ? 'w-14' : 'w-[200px]'} bg-[var(--color-sidebar-bg)] flex flex-col shrink-0 transition-all duration-200`}>
      {/* Logo */}
      <div className={collapsed ? 'flex justify-center py-3' : 'flex items-center gap-2.5 px-3 py-3'}>
        <div className="w-8 h-8 bg-[var(--color-primary)] rounded-[var(--radius-md)] flex items-center justify-center text-white text-sm font-bold shrink-0">
          P
        </div>
        {!collapsed && (
          <span className="text-white text-sm font-semibold">PharmStock</span>
        )}
      </div>

      {/* Nav items */}
      <nav className="flex flex-col gap-0.5 flex-1 px-1.5">
        {NAV_ITEMS.map(({ to, label, icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center transition-colors group relative ${
                isActive
                  ? 'bg-[var(--color-sidebar-active)] text-teal-300'
                  : 'text-slate-400 hover:text-slate-200'
              }`
            }
            title={label}
          >
            <Icon name={icon} className="w-5 h-5 shrink-0" />
            {!collapsed && (
              <span className="text-xs font-medium truncate">{label}</span>
            )}
            {collapsed && (
              <span className="absolute left-12 bg-slate-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50">
                {label}
              </span>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Bottom section: toggle + logout */}
      <div className="px-1.5 pb-3 flex flex-col gap-0.5">
        {/* Toggle button */}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-slate-400 hover:text-slate-200 transition-colors border border-slate-400/30 cursor-pointer`}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          <Icon name={collapsed ? 'chevron-right' : 'chevron-left'} className="w-4 h-4 shrink-0" />
          {!collapsed && (
            <span className="text-xs">Collapse</span>
          )}
        </button>

        {/* Logout */}
        <button
          onClick={handleLogout}
          className={`${collapsed ? 'w-9 h-9 justify-center mx-auto' : 'h-9 px-2.5 gap-2.5'} rounded-[var(--radius-md)] flex items-center text-slate-400 hover:text-slate-200 transition-colors group relative cursor-pointer`}
          title="Sign Out"
        >
          <Icon name="logout" className="w-5 h-5 shrink-0" />
          {!collapsed && (
            <span className="text-xs">Sign Out</span>
          )}
          {collapsed && (
            <span className="absolute left-12 bg-slate-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50">
              Sign Out
            </span>
          )}
        </button>
      </div>
    </div>
  )
}
