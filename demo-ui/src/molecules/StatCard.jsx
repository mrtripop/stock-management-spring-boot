import { Icon } from '../atoms/Icon'
import { Spinner } from '../atoms/Spinner'

const variantColors = {
  default: {
    icon: 'bg-[var(--color-primary-subtle)] text-[var(--color-primary)]',
    border: 'border-[var(--color-primary)]',
  },
  success: {
    icon: 'bg-[var(--color-success-subtle)] text-[var(--color-success)]',
    border: 'border-[var(--color-success)]',
  },
  danger: {
    icon: 'bg-[var(--color-danger-subtle)] text-[var(--color-danger)]',
    border: 'border-[var(--color-danger)]',
  },
  warning: {
    icon: 'bg-[var(--color-warning-subtle)] text-[var(--color-warning)]',
    border: 'border-[var(--color-warning)]',
  },
}

const trendIcons = {
  up: 'arrow-up',
  down: 'arrow-down',
}

const trendColors = {
  up: 'text-[var(--color-success)]',
  down: 'text-[var(--color-danger)]',
  flat: 'text-[var(--color-text-secondary)]',
}

export function StatCard({ icon, value, label, trend, trendValue, variant = 'default', loading = false, className = '' }) {
  const colors = variantColors[variant] || variantColors.default

  return (
    <div className={`bg-[var(--color-surface)] rounded-[var(--radius-lg)] p-[var(--space-5)] shadow-[var(--shadow-sm)] border border-[var(--color-border)] transition-all duration-200 hover:shadow-[var(--shadow-md)] hover:-translate-y-0.5 ${className}`}>
      <div className="flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <div className={`text-[var(--font-size-xs)] font-[var(--font-weight-medium)] text-[var(--color-text-muted)] uppercase tracking-wider truncate`}>
            {label}
          </div>
          {loading ? (
            <div className={`mt-[var(--space-2)] h-[var(--space-8)] w-20 bg-[var(--color-background)] rounded-[var(--radius-sm)] animate-pulse`} />
          ) : (
            <div className={`text-3xl font-[var(--font-weight-bold)] text-[var(--color-text-primary)] mt-[var(--space-1)] leading-[var(--line-height-tight)]`}>
              {value}
            </div>
          )}
          {trendValue && !loading && (
            <div className={`flex items-center gap-[var(--space-1)] text-[var(--font-size-xs)] mt-[var(--space-1)] ${trendColors[trend] || trendColors.flat}`}>
              {trend && trendIcons[trend] && (
                <Icon name={trendIcons[trend]} className="w-[var(--space-3)] h-[var(--space-3)]" />
              )}
              <span>{trendValue}</span>
            </div>
          )}
        </div>
        {icon && (
          <div className={`flex items-center justify-center w-10 h-10 rounded-[var(--radius-lg)] ${colors.icon}`}>
            {typeof icon === 'string' ? <Icon name={icon} /> : icon}
          </div>
        )}
      </div>
    </div>
  )
}
