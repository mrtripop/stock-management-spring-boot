import { Outlet, useLocation, useParams } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { Sidebar } from '../organisms/Sidebar'
import { TopBar } from '../organisms/TopBar'
import { useAuth, useStoreId } from '../lib/auth'
import { useStoreList } from '../lib/hooks/useClinical'

const ROUTE_TITLES = {
  '/': 'Dashboard',
  '/products': 'Products',
  '/inventory': 'Inventory',
  '/clinical': 'Clinical',
  '/dispensing': 'Dispensing',
  '/orders': 'Orders',
  '/transactions': 'Transactions',
  '/locations': 'Locations',
  '/users': 'Users',
}

function buildBreadcrumb(pathname) {
  const segments = pathname.split('/').filter(Boolean)
  const crumbs = [{ label: 'Home', to: '/' }]
  let path = ''
  for (const seg of segments) {
    path += `/${seg}`
    const title = ROUTE_TITLES[path] || seg
    crumbs.push({ label: title.charAt(0).toUpperCase() + title.slice(1) })
    if (crumbs.length > 1) crumbs[crumbs.length - 2].to = path
  }
  return crumbs
}

export default function AdminLayout() {
  const location = useLocation()
  const { user, logout, storeId, selectStore } = useAuth()
  const [isDark, setIsDark] = useState(() => localStorage.getItem('theme') === 'dark')
  const { items: storeOptions } = useStoreList({ page: 1, size: 100 })

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light')
    localStorage.setItem('theme', isDark ? 'dark' : 'light')
  }, [isDark])

  const breadcrumb = buildBreadcrumb(location.pathname)

  return (
    <div className="flex h-screen bg-[var(--color-background)]">
      <Sidebar
        user={user ? { username: user.username, role: user.role } : null}
        storeOptions={storeOptions || []}
        selectedStore={storeId}
        onStoreChange={selectStore}
        onLogout={logout}
        onToggleTheme={() => setIsDark(!isDark)}
        isDarkMode={isDark}
      />
      <div className="flex-1 flex flex-col min-w-0">
        <TopBar breadcrumb={breadcrumb} notificationCount={0} userAvatar={user?.username?.[0]?.toUpperCase()} />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
