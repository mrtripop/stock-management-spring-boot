import { useNavigate } from 'react-router-dom'
import { useStoreId } from '../lib/auth'
import { useProductList } from '../lib/hooks/useProducts'
import { useBatchList, useTaskList } from '../lib/hooks/useInventory'
import { useInvoiceList, useDailySummary } from '../lib/hooks/useTransactions'
import { StatCard } from '../molecules/StatCard'
import { ActivityFeed } from '../organisms/ActivityFeed'
import { ExpiryAlerts } from '../organisms/ExpiryAlerts'
import { Icon } from '../atoms/Icon'

const QUICK_ACTIONS = [
  { label: 'Stock In', icon: 'archive', to: '/inventory', color: 'text-teal-600 bg-teal-50' },
  { label: 'Dispense', icon: 'receipt', to: '/dispensing', color: 'text-blue-600 bg-blue-50' },
  { label: 'Search', icon: 'search', to: '/products', color: 'text-purple-600 bg-purple-50' },
  { label: 'Reports', icon: 'chart', to: '/transactions', color: 'text-amber-600 bg-amber-50' },
]

export default function Dashboard() {
  const navigate = useNavigate()
  const storeId = useStoreId()
  const { totalElements: productCount } = useProductList({ size: 1 })
  const { totalElements: batchCount } = useBatchList({ size: 1 })
  const { data: summary } = useDailySummary(storeId)
  const { items: taskItems } = useTaskList({ storeId, status: 'PENDING', size: 10 })
  const { items: recentTx } = useInvoiceList({ storeId, size: 10, orderBy: 'DESC' })

  const feedItems = (recentTx || []).map((tx) => ({
    id: tx.id,
    type: 'transaction',
    message: `Invoice #${tx.id} — ${tx.status || 'Created'}`,
    timestamp: tx.createdAt ? new Date(tx.createdAt).toLocaleString() : '',
  }))

  return (
    <div className="space-y-6">
      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Products" value={productCount ?? 0} icon="cube" />
        <StatCard label="Active Batches" value={batchCount ?? 0} icon="archive" />
        <StatCard label="Today's Revenue" value={`$${(summary?.totalRevenue ?? 0).toFixed(2)}`} icon="credit-card" variant="success" />
        <StatCard label="Items Dispensed" value={summary?.totalItemsDispensed ?? 0} icon="receipt" />
      </div>

      {/* Alerts + Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] p-5">
          <h3 className="text-sm font-semibold text-[var(--color-text-primary)] mb-4">Active Alerts</h3>
          <ExpiryAlerts tasks={taskItems || []} onAcknowledge={() => {}} onResolve={() => {}} />
        </div>
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] p-5">
          <h3 className="text-sm font-semibold text-[var(--color-text-primary)] mb-4">Recent Activity</h3>
          <ActivityFeed items={feedItems} />
        </div>
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {QUICK_ACTIONS.map((action) => (
          <button key={action.label} onClick={() => navigate(action.to)} className="bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] p-4 flex flex-col items-center gap-2 hover:border-[var(--color-primary)] transition-colors cursor-pointer">
            <div className={`w-10 h-10 rounded-full ${action.color} flex items-center justify-center`}>
              <Icon name={action.icon} className="w-5 h-5" />
            </div>
            <span className="text-sm font-medium text-[var(--color-text-primary)]">{action.label}</span>
          </button>
        ))}
      </div>
    </div>
  )
}
