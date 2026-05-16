export function PageHeader({ title, subtitle, actions, className = '' }) {
  return (
    <div className={`flex justify-between items-center mb-6 ${className}`}>
      <div>
        <h1 className="text-xl font-bold text-[var(--color-text-primary)]">{title}</h1>
        {subtitle && <p className="text-sm text-[var(--color-text-secondary)] mt-0.5">{subtitle}</p>}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  )
}