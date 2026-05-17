### Task 7: Dashboard Organisms — ExpiryAlerts + ActivityFeed

**Files:**
- Create: `demo-ui/src/organisms/ExpiryAlerts.jsx`
- Create: `demo-ui/src/organisms/ActivityFeed.jsx`

- [ ] **Step 1: Create ExpiryAlerts organism**

Create `demo-ui/src/organisms/ExpiryAlerts.jsx`:

```jsx
import { useNavigate } from 'react-router-dom'

export function ExpiryAlerts({ items = [], className = '' }) {
  const navigate = useNavigate()

  const getDaysUntil = (dateStr) => {
    if (!dateStr) return null
    const diff = new Date(dateStr) - new Date()
    return Math.ceil(diff / (1000 * 60 * 60 * 24))
  }

  return (
    <div className={`bg-white rounded-[var(--radius-lg)] p-4 shadow-[var(--shadow-sm)] ${className}`}>
      <div className="flex justify-between items-center mb-3">
        <h3 className="text-xs font-semibold text-[var(--color-text-primary)]">⚠️ Expiry Alerts</h3>
        <button onClick={() => navigate('/inventory')} className="text-[10px] text-[var(--color-primary)] hover:underline cursor-pointer">
          View all
        </button>
      </div>
      <div className="flex flex-col gap-1.5">
        {items.length === 0 ? (
          <p className="text-xs text-[var(--color-text-muted)] py-4 text-center">No expiring batches</p>
        ) : items.map((item) => {
          const days = getDaysUntil(item.expiryDate)
          const urgent = days !== null && days <= 7
          return (
            <div
              key={item.id || item.batchNumber}
              className={`flex justify-between items-center px-3 py-2 rounded-[var(--radius-sm)] border-l-3 ${
                urgent ? 'bg-red-50 border-l-[var(--color-danger)]' : 'bg-amber-50 border-l-[var(--color-warning)]'
              }`}
            >
              <div>
                <div className="text-[10px] font-semibold text-[var(--color-text-primary)]">{item.productName || item.barcode}</div>
                <div className="text-[8px] text-[var(--color-text-muted)]">
                  Batch: {item.batchNumber || '-'} · {item.quantity} units
                </div>
              </div>
              <div className="text-right">
                <div className={`text-[9px] font-semibold ${urgent ? 'text-[var(--color-danger)]' : 'text-[var(--color-warning)]'}`}>
                  {days !== null ? `${days} days` : '-'}
                </div>
                <div className="text-[7px] text-[var(--color-text-muted)]">
                  {item.expiryDate ? new Date(item.expiryDate).toLocaleDateString() : '-'}
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Create ActivityFeed organism**

Create `demo-ui/src/organisms/ActivityFeed.jsx`:

```jsx
const TYPE_CONFIG = {
  STOCK_IN: { icon: '📥', bg: 'bg-blue-50', label: 'Stock In' },
  DEDUCT: { icon: '📤', bg: 'bg-pink-50', label: 'Deducted' },
  CREATE: { icon: '✅', bg: 'bg-green-50', label: 'Created' },
  LOW_STOCK: { icon: '⚠️', bg: 'bg-amber-50', label: 'Low Stock' },
  default: { icon: '📋', bg: 'bg-slate-50', label: 'Activity' },
}

export function ActivityFeed({ items = [], className = '' }) {
  return (
    <div className={`bg-white rounded-[var(--radius-lg)] p-4 shadow-[var(--shadow-sm)] ${className}`}>
      <div className="flex justify-between items-center mb-3">
        <h3 className="text-xs font-semibold text-[var(--color-text-primary)]">📋 Recent Activity</h3>
        <button className="text-[10px] text-[var(--color-primary)] hover:underline cursor-pointer">View all</button>
      </div>
      <div className="flex flex-col gap-2">
        {items.length === 0 ? (
          <p className="text-xs text-[var(--color-text-muted)] py-4 text-center">No recent activity</p>
        ) : items.map((item, i) => {
          const config = TYPE_CONFIG[item.type] || TYPE_CONFIG.default
          return (
            <div key={i} className="flex gap-2.5 items-start">
              <div className={`w-7 h-7 ${config.bg} rounded-[var(--radius-sm)] flex items-center justify-center text-[11px] shrink-0`}>
                {config.icon}
              </div>
              <div className="flex-1 min-w-0">
                <div className="text-[10px] font-medium text-[var(--color-text-primary)]">{item.title}</div>
                <div className="text-[8px] text-[var(--color-text-muted)] truncate">{item.description}</div>
              </div>
              <div className="text-[8px] text-[var(--color-text-muted)] whitespace-nowrap shrink-0">{item.time}</div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
```

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/organisms/ExpiryAlerts.jsx demo-ui/src/organisms/ActivityFeed.jsx
git commit -m "feat(demo-ui): add ExpiryAlerts and ActivityFeed organisms

ExpiryAlerts: color-coded expiry list (red <7d, amber <30d) with navigation.
ActivityFeed: timeline of stock movements with typed icons and timestamps."
```
