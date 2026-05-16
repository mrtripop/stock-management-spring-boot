const variants = {
  success: 'bg-emerald-100 text-emerald-800',
  danger: 'bg-red-100 text-red-800',
  warning: 'bg-amber-100 text-amber-800',
  info: 'bg-blue-100 text-blue-800',
  neutral: 'bg-slate-100 text-slate-700',
  teal: 'bg-teal-100 text-teal-800',
  purple: 'bg-purple-100 text-purple-800',
  orange: 'bg-orange-100 text-orange-800',
}

export function Badge({ variant = 'neutral', children, className = '' }) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-[var(--radius-full)] text-xs font-medium ${variants[variant]} ${className}`}>
      {children}
    </span>
  )
}