import { forwardRef } from 'react'
import { Spinner } from './Spinner'
import { tokens } from '../../foundations/theme'

const variants = {
  primary: {
    className: 'bg-[var(--color-primary)] text-[var(--color-text-inverse)] hover:bg-[var(--color-primary-hover)] active:bg-[var(--color-primary-active)]',
    style: { boxShadow: tokens.shadow.sm },
  },
  secondary: {
    className: 'bg-[var(--color-surface)] text-[var(--color-text-secondary)] border border-[var(--color-border)] hover:border-[var(--color-primary)] hover:text-[var(--color-primary)]',
    style: {},
  },
  outline: {
    className: 'bg-transparent text-[var(--color-primary)] border border-[var(--color-primary)] hover:bg-[var(--color-primary-subtle)]',
    style: {},
  },
  danger: {
    className: 'bg-[var(--color-danger)] text-[var(--color-text-inverse)] hover:opacity-90',
    style: {},
  },
  ghost: {
    className: 'bg-transparent text-[var(--color-primary)] hover:bg-[var(--color-primary-subtle)]',
    style: {},
  },
}

const sizes = {
  sm: {
    height: tokens.spacing[7],
    paddingLeft: tokens.spacing[2],
    paddingRight: tokens.spacing[2],
    fontSize: tokens.fontSize.xs,
    gap: tokens.spacing[1],
  },
  md: {
    height: '36px',
    paddingLeft: tokens.spacing[4],
    paddingRight: tokens.spacing[4],
    fontSize: tokens.fontSize.sm,
    gap: '6px',
  },
  lg: {
    height: '44px',
    paddingLeft: tokens.spacing[5],
    paddingRight: tokens.spacing[5],
    fontSize: tokens.fontSize.base,
    gap: tokens.spacing[2],
  },
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
      style={{
        borderRadius: tokens.radius.md,
        ...sizes[size],
        ...variants[variant].style,
      }}
      className={`inline-flex items-center justify-center font-medium transition-all cursor-pointer active:scale-[0.97] disabled:opacity-50 disabled:cursor-not-allowed ${fullWidth ? 'w-full' : ''} ${variants[variant].className} ${className}`}
      {...props}
    >
      {loading ? <Spinner size="sm" /> : Icon ? <Icon className="w-4 h-4 shrink-0" /> : null}
      {children}
    </button>
  )
})
