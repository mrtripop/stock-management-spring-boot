import { theme } from './theme'

const SCALE = [
  { key: '2xs', token: '--text-2xs', use: 'Tiny labels' },
  { key: 'xs', token: '--text-xs', use: 'Badges, helper text' },
  { key: 'sm', token: '--text-sm', use: 'Table cells, form inputs' },
  { key: 'base', token: '--text-base', use: 'Body text' },
  { key: 'lg', token: '--text-lg', use: 'Section titles' },
  { key: 'xl', token: '--text-xl', use: 'Page subtitles' },
  { key: '2xl', token: '--text-2xl', use: 'Page titles' },
  { key: '3xl', token: '--text-3xl', use: 'Dashboard stats' },
]

const WEIGHTS = [
  { key: 'normal', token: '--font-normal', value: 400 },
  { key: 'medium', token: '--font-medium', value: 500 },
  { key: 'semibold', token: '--font-semibold', value: 600 },
  { key: 'bold', token: '--font-bold', value: 700 },
]

export default {
  title: 'Foundations/Typography',
  parameters: {
    layout: 'padded',
  },
}

export function SizeScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Typography Size Scale</h2>
      <p className="text-[var(--text-sm)] text-[var(--color-text-secondary)]">
        Font family: <code className="px-1.5 py-0.5 bg-[var(--color-surface)] rounded text-[var(--text-xs)] font-mono">{theme.typography.fontFamily}</code>
      </p>
      <div className="space-y-4">
        {SCALE.map(({ key, token, use }) => {
          const step = theme.typography[key]
          return (
            <div key={key} className="flex items-baseline gap-4 p-3 rounded-[var(--radius-md)] border border-[var(--color-border)]">
              <div className="w-12 text-right shrink-0">
                <span className="text-[var(--text-2xs)] font-mono text-[var(--color-text-muted)]">{key}</span>
              </div>
              <div className="flex-1 min-w-0" style={{ fontSize: `var(${token})`, lineHeight: step.lineHeight, fontWeight: step.weight }}>
                The quick brown fox jumps over the lazy dog
              </div>
              <div className="text-right shrink-0">
                <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{step.size}</div>
                <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">lh {step.lineHeight}</div>
                <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">w {step.weight}</div>
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

export function FontWeightScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Font Weights</h2>
      <div className="space-y-3">
        {WEIGHTS.map(({ key, token, value }) => (
          <div key={key} className="flex items-baseline gap-4 p-3 rounded-[var(--radius-md)] border border-[var(--color-border)]">
            <div className="w-24 text-right shrink-0">
              <span className="text-[var(--text-2xs)] font-mono text-[var(--color-text-muted)]">{token}</span>
            </div>
            <div className="flex-1 text-[var(--text-2xl)]" style={{ fontWeight: `var(${token})` }}>
              Pharmacy Stock Management
            </div>
            <div className="w-12 text-right shrink-0">
              <span className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{value}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
