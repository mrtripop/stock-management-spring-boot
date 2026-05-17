export function StatCard({ title, value, change, trend, accentColor = 'var(--color-primary)', loading = false, className = '' }) {
  return (
    <div className={`bg-white rounded-[var(--radius-lg)] p-4 shadow-[var(--shadow-sm)] border-t-3 transition-all duration-200 hover:shadow-md hover:-translate-y-0.5 ${className}`}
      style={{ borderTopColor: accentColor }}
    >
      <div className="text-xs font-semibold text-[var(--color-text-muted)] uppercase tracking-wide">
        {title}
      </div>
      {loading ? (
        <div className="mt-1.5 h-7 w-16 bg-slate-100 rounded animate-pulse" />
      ) : (
        <div className="text-2xl font-bold text-[var(--color-text-primary)] mt-1">
          {value}
        </div>
      )}
      {change && !loading && (
        <div className={`text-xs mt-1 ${trend === 'up' ? 'text-[var(--color-success)]' : trend === 'down' ? 'text-[var(--color-danger)]' : 'text-[var(--color-text-secondary)]'}`}>
          {trend === 'up' ? '↑' : trend === 'down' ? '↓' : ''} {change}
        </div>
      )}
    </div>
  )
}