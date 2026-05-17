import { theme } from './theme'

const SPACING_ENTRIES = [
  { key: '0', use: 'Reset' },
  { key: '0.5', use: 'Tight gaps' },
  { key: '1', use: 'Icon gaps' },
  { key: '1.5', use: 'Badge padding' },
  { key: '2', use: 'Inline spacing' },
  { key: '3', use: 'Compact padding' },
  { key: '4', use: 'Standard padding' },
  { key: '5', use: 'Card padding' },
  { key: '6', use: 'Section gaps' },
  { key: '8', use: 'Page margins' },
  { key: '10', use: 'Large gaps' },
  { key: '12', use: 'Page sections' },
  { key: '16', use: 'Major sections' },
]

export default {
  title: 'Foundations/Spacing',
  parameters: {
    layout: 'padded',
  },
}

export function SpacingScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Spacing Scale</h2>
      <div className="space-y-2">
        {SPACING_ENTRIES.map(({ key, use }) => {
          const value = theme.spacing[key]
          // Parse rem to px for visualization (1rem = 16px)
          const pxValue = parseFloat(value) * 16
          return (
            <div key={key} className="flex items-center gap-4">
              <div className="w-8 text-right shrink-0">
                <span className="text-[var(--text-2xs)] font-mono text-[var(--color-text-muted)]">{key}</span>
              </div>
              <div className="w-20 text-right shrink-0">
                <span className="text-[var(--text-xs)] font-mono text-[var(--color-text-secondary)]">{value}</span>
              </div>
              <div className="flex-1">
                <div
                  className="h-4 rounded-[var(--radius-sm)]"
                  style={{
                    width: `${pxValue}px`,
                    backgroundColor: 'var(--color-primary)',
                    opacity: 0.6,
                  }}
                />
              </div>
              <div className="w-12 text-right shrink-0">
                <span className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{pxValue}px</span>
              </div>
              <div className="w-28 text-right shrink-0">
                <span className="text-[var(--text-2xs)] text-[var(--color-text-secondary)]">{use}</span>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
