import { forwardRef } from 'react'

export const Select = forwardRef(function Select(
  {
    label,
    error,
    helperText,
    options = [],
    placeholder,
    disabled = false,
    className = '',
    children,
    ...props
  },
  ref
) {
  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label className="text-sm font-medium text-[var(--color-text-primary)]">
          {label}
        </label>
      )}
      <select
        ref={ref}
        disabled={disabled}
        className={`w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border outline-none transition-colors bg-[var(--color-surface)] appearance-none bg-[length:16px_16px] bg-[right_8px_center] bg-no-repeat
          ${error
            ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)] focus-visible:ring-2 focus-visible:ring-[var(--color-danger)]/20'
            : 'border-[var(--color-border)] focus:border-[var(--color-border-focus)] focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/20'
          }
          disabled:bg-[var(--color-background)] disabled:cursor-not-allowed disabled:opacity-60
          ${className}`}
        style={{
          backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3E%3Cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3E%3C/svg%3E")`,
        }}
        aria-invalid={!!error}
        aria-describedby={error ? `${props.id}-error` : helperText ? `${props.id}-helper` : undefined}
        {...props}
      >
        {placeholder && (
          <option value="" disabled>
            {placeholder}
          </option>
        )}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
        {children}
      </select>
      {error && (
        <p id={`${props.id}-error`} className="text-xs text-[var(--color-danger)]">
          {error}
        </p>
      )}
      {helperText && !error && (
        <p id={`${props.id}-helper`} className="text-xs text-[var(--color-text-muted)]">
          {helperText}
        </p>
      )}
    </div>
  )
})
