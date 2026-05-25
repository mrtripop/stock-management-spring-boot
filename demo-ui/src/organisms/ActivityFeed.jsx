import { Icon } from '../atoms/Icon'
import { Spinner } from '../atoms/Spinner'

const TYPE_ICONS = {
  transaction: 'credit-card',
  task: 'exclamation',
  alert: 'bell',
  stock: 'archive',
  default: 'info',
}

export function ActivityFeed({ items = [], loading = false }) {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Spinner size="md" />
      </div>
    )
  }

  if (items.length === 0) {
    return <p className="text-sm text-[var(--color-text-muted)] text-center py-8">No recent activity</p>
  }

  return (
    <div className="space-y-0">
      {items.map((item) => (
        <div key={item.id} className="flex gap-3 py-3 border-b border-[var(--color-border)] last:border-0">
          <div className="w-8 h-8 rounded-full bg-[var(--color-background)] flex items-center justify-center shrink-0">
            <Icon name={TYPE_ICONS[item.type] || TYPE_ICONS.default} className="w-4 h-4 text-[var(--color-text-muted)]" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm text-[var(--color-text-primary)] truncate">{item.message}</p>
            <p className="text-xs text-[var(--color-text-muted)]">{item.timestamp}</p>
          </div>
        </div>
      ))}
    </div>
  )
}
