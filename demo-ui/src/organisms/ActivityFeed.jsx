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
        <h3 className="text-xs font-semibold text-[var(--color-text-primary)]">Recent Activity</h3>
        <button className="text-[11px] text-[var(--color-primary)] hover:underline cursor-pointer">View all</button>
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
                <div className="text-xs font-medium text-[var(--color-text-primary)]">{item.title}</div>
                <div className="text-[11px] text-[var(--color-text-muted)] truncate">{item.description}</div>
              </div>
              <div className="text-[11px] text-[var(--color-text-muted)] whitespace-nowrap shrink-0">{item.time}</div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
