const variants = {
  success: 'bg-[var(--color-success-subtle)] text-[var(--color-success-text)]',
  danger: 'bg-[var(--color-danger-subtle)] text-[var(--color-danger-text)]',
  warning: 'bg-[var(--color-warning-subtle)] text-[var(--color-warning-text)]',
  info: 'bg-[var(--color-info-subtle)] text-[var(--color-info-text)]',
  neutral: 'bg-[var(--color-background)] text-[var(--color-text-secondary)]',
  teal: 'bg-[var(--color-primary-subtle)] text-[var(--color-primary-text)]',
  purple: 'bg-purple-100 text-purple-800',
  orange: 'bg-orange-100 text-orange-800',
}

const dotColors = {
  success: 'bg-[var(--color-success)]',
  danger: 'bg-[var(--color-danger)]',
  warning: 'bg-[var(--color-warning)]',
  info: 'bg-[var(--color-info)]',
  neutral: 'bg-[var(--color-text-muted)]',
  teal: 'bg-[var(--color-primary)]',
  purple: 'bg-purple-500',
  orange: 'bg-orange-500',
}

export function Badge({ variant = 'neutral', dot = false, children, className = '' }) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-[var(--radius-full)] text-xs font-medium ${variants[variant]} ${className}`}
    >
      {dot && <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${dotColors[variant]}`} />}
      {children}
    </span>
  )
}
