const sizes = {
  sm: 'w-4 h-4 border-2',
  md: 'w-6 h-6 border-2',
  lg: 'w-8 h-8 border-[3px]',
}

export function Spinner({ size = 'md', color, className = '' }) {
  const borderColor = color || 'var(--color-primary)'
  return (
    <div
      className={`rounded-full border-transparent animate-spin ${sizes[size]} ${className}`}
      style={{
        borderColor: 'var(--color-border)',
        borderTopColor: borderColor,
      }}
      role="status"
      aria-label="Loading"
    />
  )
}
