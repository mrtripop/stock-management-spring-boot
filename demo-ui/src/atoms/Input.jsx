export function Input({ error, className = '', ...props }) {
  return (
    <input
      className={`w-full px-3 py-2 text-sm rounded-[var(--radius-md)] border outline-none transition-colors
        ${error
          ? 'border-[var(--color-danger)] focus:ring-2 focus:ring-red-200'
          : 'border-[var(--color-border)] focus:border-[var(--color-primary)] focus:ring-2 focus:ring-teal-100'
        } ${className}`}
      {...props}
    />
  )
}