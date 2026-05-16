export function FormField({ label, required, error, hint, children, className = '' }) {
  return (
    <div className={`mb-3 ${className}`}>
      <label className="block text-xs font-medium text-[var(--color-text-secondary)] mb-1">
        {label}
        {required && <span className="text-[var(--color-danger)] ml-0.5">*</span>}
      </label>
      {children}
      {error && <p className="text-xs text-[var(--color-danger)] mt-1">{error}</p>}
      {hint && !error && <p className="text-xs text-[var(--color-text-muted)] mt-1">{hint}</p>}
    </div>
  )
}