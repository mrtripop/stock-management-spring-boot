import { theme } from './theme'

const SHADOW_ENTRIES = [
  { key: 'xs', use: 'Subtle elevation' },
  { key: 'sm', use: 'Cards, dropdowns' },
  { key: 'md', use: 'Popovers, modals' },
  { key: 'lg', use: 'Drawers, dialogs' },
  { key: 'xl', use: 'Full-screen overlays' },
]

const RADIUS_ENTRIES = [
  { key: 'sm', value: '4px', use: 'Badges, small elements' },
  { key: 'md', value: '6px', use: 'Buttons, inputs' },
  { key: 'lg', value: '8px', use: 'Cards, panels' },
  { key: 'xl', value: '12px', use: 'Modals, drawers' },
  { key: '2xl', value: '16px', use: 'Large containers' },
  { key: 'full', value: '9999px', use: 'Avatars, pills' },
]

export default {
  title: 'Foundations/Shadows',
  parameters: {
    layout: 'padded',
  },
}

export function ShadowScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Shadow Scale</h2>
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
        {SHADOW_ENTRIES.map(({ key, use }) => (
          <div key={key} className="flex flex-col items-center gap-3">
            <div
              className="w-28 h-28 bg-[var(--color-surface)] rounded-[var(--radius-lg)] flex items-center justify-center"
              style={{ boxShadow: `var(--shadow-${key})` }}
            >
              <div className="text-center">
                <div className="text-[var(--text-sm)] font-semibold text-[var(--color-text-primary)]">{key}</div>
                <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{use}</div>
              </div>
            </div>
            <code className="text-[var(--text-2xs)] text-[var(--color-text-muted)] font-mono max-w-28 break-all text-center">
              {theme.shadow[key]}
            </code>
          </div>
        ))}
      </div>
    </div>
  )
}

export function RadiusScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Border Radius Scale</h2>
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
        {RADIUS_ENTRIES.map(({ key, value, use }) => (
          <div key={key} className="flex flex-col items-center gap-3">
            <div
              className="w-20 h-20 bg-[var(--color-primary-subtle)] border-2 border-[var(--color-primary)] flex items-center justify-center"
              style={{ borderRadius: `var(--radius-${key})` }}
            >
              <div className="text-[var(--text-xs)] font-semibold text-[var(--color-primary-text)]">{value}</div>
            </div>
            <div className="text-center">
              <div className="text-[var(--text-xs)] font-medium text-[var(--color-text-primary)]">--radius-{key}</div>
              <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{use}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
