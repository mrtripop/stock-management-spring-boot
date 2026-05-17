export function Input({ error, className = '', ...props }) {
  return (
    <input
      className={`w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border outline-none transition-colors focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/30
  ${error
    ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)]'
    : 'border-[var(--color-border)] focus:border-[var(--color-primary)]'
  } ${className}`}
      {...props}
    />
  )
}