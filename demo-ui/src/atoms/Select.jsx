export function Select({ error, className = '', children, ...props }) {
  return (
    <select
      className={`w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border outline-none transition-colors bg-white focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/30
  ${error
    ? 'border-[var(--color-danger)] focus:border-[var(--color-danger)]'
    : 'border-[var(--color-border)] focus:border-[var(--color-primary)]'
  } ${className}`}
      {...props}
    >
      {children}
    </select>
  )
}