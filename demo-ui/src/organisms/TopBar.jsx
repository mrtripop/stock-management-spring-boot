import { Icon } from '../atoms/Icon'
import { Badge } from '../atoms/Badge'

export function TopBar({ breadcrumb = [], onSearchClick, notificationCount = 0, userAvatar }) {
  return (
    <header className="h-14 bg-[var(--color-surface)] border-b border-[var(--color-border)] flex items-center justify-between px-6 shrink-0">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-1 text-sm">
        {breadcrumb.map((item, i) => (
          <span key={i} className="flex items-center gap-1">
            {i > 0 && <Icon name="chevron-right" className="w-3 h-3 text-[var(--color-text-muted)]" />}
            {item.to ? (
              <a href={item.to} className="text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] transition-colors">{item.label}</a>
            ) : (
              <span className="text-[var(--color-text-primary)] font-medium">{item.label}</span>
            )}
          </span>
        ))}
      </nav>

      {/* Right actions */}
      <div className="flex items-center gap-3">
        {onSearchClick && (
          <button onClick={onSearchClick} className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] transition-colors cursor-pointer" title="Search">
            <Icon name="search" className="w-5 h-5" />
          </button>
        )}
        <button className="relative text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] transition-colors cursor-pointer" title="Notifications">
          <Icon name="bell" className="w-5 h-5" />
          {notificationCount > 0 && (
            <span className="absolute -top-1 -right-1 w-4 h-4 bg-[var(--color-danger)] text-white text-[10px] font-bold rounded-full flex items-center justify-center">
              {notificationCount > 9 ? '9+' : notificationCount}
            </span>
          )}
        </button>
        <div className="w-8 h-8 rounded-full bg-[var(--color-primary)] flex items-center justify-center text-white text-xs font-bold cursor-pointer" title={userAvatar}>
          {userAvatar || 'U'}
        </div>
      </div>
    </header>
  )
}
