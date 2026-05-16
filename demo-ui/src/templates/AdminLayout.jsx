import { Outlet, useLocation } from 'react-router-dom'
import { Sidebar } from '../organisms/Sidebar'
import { TopBar } from '../organisms/TopBar'

const PAGE_TITLES = {
  '/': { title: 'Dashboard', subtitle: '' },
  '/products': { title: 'Products', subtitle: 'Manage your product catalog' },
  '/inventory': { title: 'Inventory', subtitle: 'Batch management and stock operations' },
  '/clinical': { title: 'Clinical', subtitle: 'Stores, molecules, and brands' },
  '/orders': { title: 'Orders', subtitle: 'Order tracking and management' },
  '/transactions': { title: 'Transactions', subtitle: 'Stock movement audit log' },
  '/locations': { title: 'Locations', subtitle: 'Warehouse and store locations' },
  '/users': { title: 'Users', subtitle: 'User accounts and roles' },
}

export default function AdminLayout() {
  const location = useLocation()
  const page = PAGE_TITLES[location.pathname] || { title: 'Dashboard', subtitle: '' }

  return (
    <div className="flex h-screen bg-[var(--color-background)]">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <TopBar title={page.title} subtitle={page.subtitle} />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
