import { useId } from 'react'

export function DatePicker({
  label,
  value,
  onChange,
  min,
  max,
  error,
  disabled = false,
  helperText,
  required = false,
  className = '',
}) {
  const id = useId()

  return (
    <div className={className}>
      {label && (
        <label
          htmlFor={id}
          className="block text-[length:var(--text-sm)] font-[var(--font-medium)] text-[var(--color-text-secondary)] mb-1"
        >
          {label}
          {required && <span className="text-[var(--color-danger)] ml-0.5">*</span>}
        </label>
      )}
      <input
        id={id}
        type="date"
        value={value}
        onChange={onChange}
        min={min}
        max={max}
        disabled={disabled}
        className={`w-full px-[var(--space-3)] py-2 text-[length:var(--text-sm)] rounded-[var(--radius-md)] border bg-[var(--color-surface)] text-[var(--color-text-primary)] outline-none transition-colors focus:border-[var(--color-border-focus)] focus:ring-2 focus:ring-[var(--color-primary)]/20 disabled:opacity-50 disabled:cursor-not-allowed ${
          error
            ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)] focus:ring-[var(--color-danger)]/20'
            : 'border-[var(--color-border)]'
        }`}
      />
      {error && (
        <p className="text-[length:var(--text-xs)] text-[var(--color-danger)] mt-1" role="alert">
          {error}
        </p>
      )}
      {helperText && !error && (
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mt-1">
          {helperText}
        </p>
      )}
    </div>
  )
}
