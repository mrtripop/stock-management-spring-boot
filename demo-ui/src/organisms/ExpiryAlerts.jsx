import { Icon } from '../atoms/Icon'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { Spinner } from '../atoms/Spinner'

function getUrgency(task) {
  if (task.taskType === 'RECALL_ALERT') return 'critical'
  if (task.taskType === 'REORDER_NEEDED') return 'warning'
  if (task.daysUntilExpiry != null && task.daysUntilExpiry < 7) return 'critical'
  if (task.daysUntilExpiry != null && task.daysUntilExpiry < 30) return 'warning'
  return 'default'
}

const URGENCY_STYLES = {
  critical: { border: 'border-l-[var(--color-danger)]', bg: 'bg-[var(--color-danger-subtle)]', badge: 'danger' },
  warning: { border: 'border-l-[var(--color-warning)]', bg: 'bg-[var(--color-warning-subtle)]', badge: 'warning' },
  default: { border: 'border-l-[var(--color-info)]', bg: 'bg-[var(--color-info-subtle)]', badge: 'info' },
}

export function ExpiryAlerts({ tasks = [], onAcknowledge, onResolve, loading = false }) {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Spinner size="md" />
      </div>
    )
  }

  if (tasks.length === 0) {
    return <p className="text-sm text-[var(--color-text-muted)] text-center py-8">No active alerts</p>
  }

  const sorted = [...tasks].sort((a, b) => {
    const order = { critical: 0, warning: 1, default: 2 }
    return (order[getUrgency(a)] ?? 2) - (order[getUrgency(b)] ?? 2)
  })

  return (
    <div className="space-y-2">
      {sorted.map((task) => {
        const urgency = getUrgency(task)
        const style = URGENCY_STYLES[urgency]
        return (
          <div key={task.id} className={`flex items-center gap-3 p-3 rounded-[var(--radius-md)] border-l-4 ${style.border} ${style.bg}`}>
            <Icon name={task.taskType === 'RECALL_ALERT' ? 'exclamation' : 'clock'} className="w-5 h-5 shrink-0" />
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <p className="text-sm font-medium text-[var(--color-text-primary)] truncate">{task.brandName}</p>
                <Badge variant={style.badge}>{task.taskType?.replace('_', ' ')}</Badge>
              </div>
              <p className="text-xs text-[var(--color-text-secondary)] mt-0.5">
                Batch {task.batchNumber}
                {task.daysUntilExpiry != null && ` — ${task.daysUntilExpiry} days until expiry`}
                {task.currentQuantity != null && ` — Qty: ${task.currentQuantity}`}
              </p>
            </div>
            <div className="flex items-center gap-1.5 shrink-0">
              {task.status === 'PENDING' && onAcknowledge && (
                <Button size="sm" variant="secondary" onClick={() => onAcknowledge(task.id)}>Ack</Button>
              )}
              {task.status === 'ACKNOWLEDGED' && onResolve && (
                <Button size="sm" variant="primary" onClick={() => onResolve(task.id)}>Resolve</Button>
              )}
              {task.status === 'RESOLVED' && (
                <Badge variant="success">Resolved</Badge>
              )}
            </div>
          </div>
        )
      })}
    </div>
  )
}
