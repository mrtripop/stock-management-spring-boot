import { Spinner } from './Spinner'

const variants = {
  primary: 'bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-hover)] shadow-sm',
  secondary: 'bg-white text-[var(--color-text-secondary)] border border-[var(--color-border)] hover:border-[var(--color-primary)] hover:text-[var(--color-primary)]',
  danger: 'bg-[var(--color-danger)] text-white hover:bg-[var(--color-danger-hover)]',
  ghost: 'bg-transparent text-[var(--color-primary)] hover:bg-[var(--color-background)]',
}

const sizes = {
  sm: 'h-7 px-2.5 text-xs gap-1',
  md: 'h-9 px-4 text-sm gap-1.5',
  lg: 'h-11 px-5 text-base gap-2',
}

export function Button({
  variant = 'primary',
  size = 'md',
  disabled = false,
  loading = false,
  icon: Icon,
  children,
  className = '',
  ...props
}) {
  return (
    <button
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center font-medium rounded-[var(--radius-md)] transition-all cursor-pointer active:scale-[0.97] disabled:opacity-50 disabled:cursor-not-allowed ${variants[variant]} ${sizes[size]} ${className}`}
      {...props}
    >
      {loading ? <Spinner size="sm" /> : Icon ? <Icon className="w-4 h-4" /> : null}
      {children}
    </button>
  )
}