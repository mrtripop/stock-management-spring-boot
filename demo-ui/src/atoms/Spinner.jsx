const sizes = { sm: 'w-4 h-4 border-2', md: 'w-6 h-6 border-2', lg: 'w-8 h-8 border-3' }

export function Spinner({ size = 'md', className = '' }) {
  return (
    <div
      className={`rounded-full border-[var(--color-border)] border-t-[var(--color-primary)] animate-spin ${sizes[size]} ${className}`}
      role="status"
      aria-label="Loading"
    />
  )
}