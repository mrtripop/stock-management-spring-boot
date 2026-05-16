import { useState } from 'react'

export function TopBar({ title, subtitle }) {
  const [searchFocused, setSearchFocused] = useState(false)

  return (
    <div className="h-14 bg-white border-b border-[var(--color-border)] flex items-center justify-between px-6 shrink-0">
      {/* Left: Page info */}
      <div className="flex items-center gap-3">
        <span className="text-sm font-semibold text-[var(--color-text-primary)]">{title}</span>
        {subtitle && <span className="text-xs text-[var(--color-text-secondary)]">{subtitle}</span>}
      </div>

      {/* Right: Search + notifications + avatar */}
      <div className="flex items-center gap-3">
        <div className={`flex items-center gap-2 px-3 py-1.5 rounded-[var(--radius-md)] border transition-colors ${searchFocused ? 'border-[var(--color-primary)]' : 'border-[var(--color-border)]'}`}>
          <svg className="w-3.5 h-3.5 text-[var(--color-text-muted)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            placeholder="Search..."
            className="bg-transparent text-xs outline-none w-40 text-[var(--color-text-secondary)]"
            onFocus={() => setSearchFocused(true)}
            onBlur={() => setSearchFocused(false)}
          />
        </div>

        {/* Notification bell */}
        <div className="relative">
          <div className="w-8 h-8 rounded-[var(--radius-md)] border border-[var(--color-border)] flex items-center justify-center text-sm cursor-pointer hover:bg-[var(--color-background)] transition-colors">
            🔔
          </div>
          <div className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-[var(--color-danger)] rounded-full border-2 border-white" />
        </div>

        {/* Avatar */}
        <div className="w-8 h-8 bg-[var(--color-primary)] rounded-[var(--radius-md)] flex items-center justify-center text-white text-xs font-semibold">
          PS
        </div>
      </div>
    </div>
  )
}
