import { forwardRef } from 'react'
import { Spinner } from './Spinner'

const variants = {
  primary:
    'bg-[var(--color-primary)] text-[var(--color-text-inverse)] hover:bg-[var(--color-primary-hover)] active:bg-[var(--color-primary-active)] shadow-sm',
  secondary:
    'bg-[var(--color-surface)] text-[var(--color-text-secondary)] border border-[var(--color-border)] hover:border-[var(--color-primary)] hover:text-[var(--color-primary)]',
  outline:
    'bg-transparent text-[var(--color-primary)] border border-[var(--color-primary)] hover:bg-[var(--color-primary-subtle)]',
  danger:
    'bg-[var(--color-danger)] text-[var(--color-text-inverse)] hover:opacity-90',
  ghost:
    'bg-transparent text-[var(--color-primary)] hover:bg-[var(--color-primary-subtle)]',
}

const sizes = {
  sm: 'h-7 px-2.5 text-[var(--text-xs)] gap-1',
  md: 'h-9 px-4 text-[var(--text-sm)] gap-1.5',
  lg: 'h-11 px-5 text-[var(--text-base)] gap-2',
}

export const Button = forwardRef(function Button(
  {
    variant = 'primary',
    size = 'md',
    type = 'button',
    disabled = false,
    loading = false,
    icon: Icon,
    fullWidth = false,
    children,
    className = '',
    ...props
  },
  ref
) {
  return (
    <button
      ref={ref}
      type={type}
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center font-medium rounded-[var(--radius-md)] transition-all cursor-pointer active:scale-[0.97] disabled:opacity-50 disabled:cursor-not-allowed ${fullWidth ? 'w-full' : ''} ${variants[variant]} ${sizes[size]} ${className}`}
      {...props}
    >
      {loading ? <Spinner size="sm" /> : Icon ? <Icon className="w-4 h-4 shrink-0" /> : null}
      {children}
    </button>
  )
})
