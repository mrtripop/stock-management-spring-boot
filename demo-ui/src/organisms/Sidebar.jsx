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
