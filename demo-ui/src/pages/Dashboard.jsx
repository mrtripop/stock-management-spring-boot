import { useNavigate } from 'react-router-dom'
import { Button } from '../atoms/Button'
import { StatCard } from '../molecules/StatCard'
import { ExpiryAlerts } from '../organisms/ExpiryAlerts'
import { ActivityFeed } from '../organisms/ActivityFeed'
import { useQueryList } from '../lib/hooks'

export default function Dashboard() {
  const navigate = useNavigate()

  const { items: productData, totalElements: productCount } = useQueryList(
    ['products'], '/products', { page: 1, size: 1 }
  )

  const { items: batchData, totalElements: batchCount } = useQueryList(
    ['batches'], '/inventory/batches', { page: 1, size: 1 }
  )

  const { items: expiringBatches } = useQueryList(
    ['batches-expiring'], '/inventory/batches', { page: 1, size: 5 }
  )

  const { items: recentTransactions } = useQueryList(
    ['transactions-recent'], '/transactions', { page: 1, size: 5 }
  )

  const stats = [
    { title: 'Products', value: productCount ?? '-', change: '', trend: null, accentColor: 'var(--color-primary)' },
    { title: 'Expiring Soon', value: '-', change: '', trend: null, accentColor: 'var(--color-warning)' },
    { title: 'Batches', value: batchCount ?? '-', change: 'Active batches', trend: null, accentColor: 'var(--color-purple)' },
    { title: 'Low Stock', value: '-', change: 'Below reorder', trend: null, accentColor: 'var(--color-danger)' },
  ]

  const activities = recentTransactions.map((t) => ({
    type: t.type === 0 ? 'STOCK_IN' : t.type === 1 ? 'DEDUCT' : 'default',
    title: `Transaction #${t.id}`,
    description: t.code || '-',
    time: t.createdAt ? new Date(t.createdAt).toLocaleTimeString() : '-',
  }))

  return (
    <div>
      {/* Quick Actions Bar */}
      <div className="flex gap-2 mb-5">
        <Button icon={ArrowDownIcon} onClick={() => navigate('/inventory')}>Stock In</Button>
        <Button variant="secondary" onClick={() => navigate('/inventory')}>Deduct Stock</Button>
        <Button variant="secondary" onClick={() => navigate('/products')}>Search Product</Button>
        <Button variant="secondary">Run Report</Button>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-4 gap-3 mb-5">
        {stats.map((stat) => (
          <StatCard key={stat.title} {...stat} />
        ))}
      </div>

      {/* Expiry Alerts + Activity Feed */}
      <div className="grid grid-cols-2 gap-4">
        <ExpiryAlerts items={expiringBatches.map((b) => ({
          id: b.id,
          productName: b.barcode || 'Unknown',
          batchNumber: b.batchNumber,
          quantity: b.quantity,
          expiryDate: b.expiryDate,
        }))} />
        <ActivityFeed items={activities} />
      </div>
    </div>
  )
}

function ArrowDownIcon({ className }) {
  return (
    <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
    </svg>
  )
}
