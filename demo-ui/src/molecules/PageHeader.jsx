import { Icon } from '../atoms/Icon'

export function PageHeader({ title, subtitle, actions, breadcrumb, className = '' }) {
  return (
    <div className={className}>
      {breadcrumb && breadcrumb.length > 0 && (
        <nav aria-label="Breadcrumb" className="flex items-center gap-1 text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">
          {breadcrumb.map((item, index) => (
            <span key={index} className="contents">
              {index > 0 && (
                <Icon name="chevron-right" className="w-3 h-3 text-[var(--color-text-muted)]" />
              )}
              {item.href ? (
                <a
                  href={item.href}
                  className="hover:text-[var(--color-primary)] transition-colors"
                >
                  {item.label}
                </a>
              ) : (
                <span className={index === breadcrumb.length - 1 ? 'text-[var(--color-text-primary)] font-[var(--font-medium)]' : ''}>
                  {item.label}
                </span>
              )}
            </span>
          ))}
        </nav>
      )}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-[length:var(--text-2xl)] font-[var(--font-semibold)] text-[var(--color-text-primary)]">
            {title}
          </h1>
          {subtitle && (
            <p className="text-[length:var(--text-sm)] text-[var(--color-text-secondary)] mt-0.5">
              {subtitle}
            </p>
          )}
        </div>
        {actions && <div className="flex items-center gap-[var(--space-2)]">{actions}</div>}
      </div>
    </div>
  )
}
