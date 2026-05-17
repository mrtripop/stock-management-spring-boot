export function StatCard({ title, value, change, trend, accentColor = 'var(--color-primary)', className = '' }) {
  return (
    <div className={`bg-white rounded-[var(--radius-lg)] p-4 shadow-[var(--shadow-sm)] border-t-3 ${className}`}
      style={{ borderTopColor: accentColor }}
    >
      <div className="text-xs font-semibold text-[var(--color-text-muted)] uppercase tracking-wide">
        {title}
      </div>
      <div className="text-2xl font-bold text-[var(--color-text-primary)] mt-1">
        {value}
      </div>
      {change && (
        <div className={`text-xs mt-1 ${trend === 'up' ? 'text-[var(--color-success)]' : trend === 'down' ? 'text-[var(--color-danger)]' : 'text-[var(--color-text-secondary)]'}`}>
          {trend === 'up' ? '↑' : trend === 'down' ? '↓' : ''} {change}
        </div>
      )}
    </div>
  )
}