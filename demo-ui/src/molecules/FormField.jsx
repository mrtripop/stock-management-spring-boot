import { useId } from 'react'

export function FormField({ label, error, helperText, required, children, className = '' }) {
  const generatedId = useId()
  // Allow children to provide their own id; fall back to generated one
  const childId = children?.props?.id || generatedId

  return (
    <div className={className}>
      {label && (
        <label
          htmlFor={childId}
          className="block text-sm font-medium text-[var(--color-text-secondary)] mb-1"
        >
          {label}
          {required && <span className="text-[var(--color-danger)] ml-0.5">*</span>}
        </label>
      )}
      {children}
      {error && (
        <p className="text-xs text-[var(--color-danger)] mt-1" role="alert">
          {error}
        </p>
      )}
      {helperText && !error && (
        <p className="text-xs text-[var(--color-text-muted)] mt-1">
          {helperText}
        </p>
      )}
    </div>
  )
}
