import { forwardRef } from 'react'

export const Input = forwardRef(function Input(
  {
    label,
    error,
    helperText,
    leftIcon: LeftIcon,
    rightIcon: RightIcon,
    disabled = false,
    className = '',
    ...props
  },
  ref
) {
  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label className="text-[var(--text-sm)] font-medium text-[var(--color-text-primary)]">
          {label}
        </label>
      )}
      <div className="relative">
        {LeftIcon && (
          <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[var(--color-text-muted)]">
            <LeftIcon className="w-4 h-4" />
          </span>
        )}
        <input
          ref={ref}
          disabled={disabled}
          className={`w-full px-3 py-2 text-[var(--text-sm)] rounded-[var(--radius-md)] border outline-none transition-colors bg-[var(--color-surface)]
            ${LeftIcon ? 'pl-9' : ''}
            ${RightIcon ? 'pr-9' : ''}
            ${error
              ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)] focus-visible:ring-2 focus-visible:ring-[var(--color-danger)]/20'
              : 'border-[var(--color-border)] focus:border-[var(--color-border-focus)] focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/20'
            }
            disabled:bg-[var(--color-background)] disabled:cursor-not-allowed disabled:opacity-60
            ${className}`}
          aria-invalid={!!error}
          aria-describedby={error ? `${props.id}-error` : helperText ? `${props.id}-helper` : undefined}
          {...props}
        />
        {RightIcon && (
          <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-text-muted)]">
            <RightIcon className="w-4 h-4" />
          </span>
        )}
      </div>
      {error && (
        <p id={`${props.id}-error`} className="text-[var(--text-xs)] text-[var(--color-danger)]">
          {error}
        </p>
      )}
      {helperText && !error && (
        <p id={`${props.id}-helper`} className="text-[var(--text-xs)] text-[var(--color-text-muted)]">
          {helperText}
        </p>
      )}
    </div>
  )
})
