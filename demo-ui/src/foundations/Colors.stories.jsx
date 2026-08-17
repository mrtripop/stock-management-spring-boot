import { useState } from 'react'
import { theme } from './theme'

const SCALE_NAMES = ['teal', 'slate', 'emerald', 'red', 'amber', 'blue', 'purple', 'orange']
const SCALE_STEPS = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950]

const SEMANTIC_GROUPS = [
  {
    title: 'Primary',
    tokens: [
      { name: '--color-primary', cssVar: 'color-primary' },
      { name: '--color-primary-hover', cssVar: 'color-primary-hover' },
      { name: '--color-primary-active', cssVar: 'color-primary-active' },
      { name: '--color-primary-subtle', cssVar: 'color-primary-subtle' },
      { name: '--color-primary-text', cssVar: 'color-primary-text' },
    ],
  },
  {
    title: 'Success',
    tokens: [
      { name: '--color-success', cssVar: 'color-success' },
      { name: '--color-success-subtle', cssVar: 'color-success-subtle' },
      { name: '--color-success-text', cssVar: 'color-success-text' },
    ],
  },
  {
    title: 'Danger',
    tokens: [
      { name: '--color-danger', cssVar: 'color-danger' },
      { name: '--color-danger-subtle', cssVar: 'color-danger-subtle' },
      { name: '--color-danger-text', cssVar: 'color-danger-text' },
    ],
  },
  {
    title: 'Warning',
    tokens: [
      { name: '--color-warning', cssVar: 'color-warning' },
      { name: '--color-warning-subtle', cssVar: 'color-warning-subtle' },
      { name: '--color-warning-text', cssVar: 'color-warning-text' },
    ],
  },
  {
    title: 'Info',
    tokens: [
      { name: '--color-info', cssVar: 'color-info' },
      { name: '--color-info-subtle', cssVar: 'color-info-subtle' },
      { name: '--color-info-text', cssVar: 'color-info-text' },
    ],
  },
  {
    title: 'Surfaces',
    tokens: [
      { name: '--color-background', cssVar: 'color-background' },
      { name: '--color-surface', cssVar: 'color-surface' },
      { name: '--color-surface-raised', cssVar: 'color-surface-raised' },
      { name: '--color-overlay', cssVar: 'color-overlay' },
    ],
  },
  {
    title: 'Text',
    tokens: [
      { name: '--color-text-primary', cssVar: 'color-text-primary' },
      { name: '--color-text-secondary', cssVar: 'color-text-secondary' },
      { name: '--color-text-muted', cssVar: 'color-text-muted' },
      { name: '--color-text-inverse', cssVar: 'color-text-inverse' },
    ],
  },
  {
    title: 'Borders',
    tokens: [
      { name: '--color-border', cssVar: 'color-border' },
      { name: '--color-border-light', cssVar: 'color-border-light' },
      { name: '--color-border-focus', cssVar: 'color-border-focus' },
    ],
  },
  {
    title: 'Sidebar',
    tokens: [
      { name: '--color-sidebar-bg', cssVar: 'color-sidebar-bg' },
      { name: '--color-sidebar-active', cssVar: 'color-sidebar-active' },
      { name: '--color-sidebar-text', cssVar: 'color-sidebar-text' },
      { name: '--color-sidebar-text-active', cssVar: 'color-sidebar-text-active' },
    ],
  },
  {
    title: 'Role Badges',
    tokens: [
      { name: '--color-badge-admin', cssVar: 'color-badge-admin' },
      { name: '--color-badge-admin-subtle', cssVar: 'color-badge-admin-subtle' },
      { name: '--color-badge-pharmacist', cssVar: 'color-badge-pharmacist' },
      { name: '--color-badge-pharmacist-subtle', cssVar: 'color-badge-pharmacist-subtle' },
      { name: '--color-badge-manager', cssVar: 'color-badge-manager' },
      { name: '--color-badge-manager-subtle', cssVar: 'color-badge-manager-subtle' },
      { name: '--color-badge-employee', cssVar: 'color-badge-employee' },
      { name: '--color-badge-employee-subtle', cssVar: 'color-badge-employee-subtle' },
    ],
  },
  {
    title: 'Task Types',
    tokens: [
      { name: '--color-task-reorder', cssVar: 'color-task-reorder' },
      { name: '--color-task-reorder-subtle', cssVar: 'color-task-reorder-subtle' },
    ],
  },
]

function ColorSwatch({ color, label, hex }) {
  return (
    <div className="flex flex-col items-center gap-1">
      <div
        className="w-14 h-14 rounded-[var(--radius-md)] border border-[var(--color-border)]"
        style={{ backgroundColor: color }}
      />
      <span className="text-[0.625rem] font-medium text-[var(--color-text-secondary)]">{label}</span>
      {hex && (
        <span className="text-[0.625rem] text-[var(--color-text-muted)]">{hex}</span>
      )}
    </div>
  )
}

function SemanticSwatch({ cssVar, name }) {
  return (
    <div className="flex items-center gap-3 p-2 rounded-[var(--radius-md)] bg-[var(--color-surface)]">
      <div
        className="w-10 h-10 rounded-[var(--radius-md)] border border-[var(--color-border)] shrink-0"
        style={{ backgroundColor: `var(--${cssVar})` }}
      />
      <div className="min-w-0">
        <div className="text-xs font-medium text-[var(--color-text-primary)] truncate">{name}</div>
        <div className="text-[0.625rem] text-[var(--color-text-muted)]">--{cssVar}</div>
      </div>
    </div>
  )
}

export default {
  title: 'Foundations/Colors',
  parameters: {
    layout: 'padded',
  },
}

export function PrimitiveScales() {
  return (
    <div className="space-y-8">
      <h2 className="text-xl font-semibold text-[var(--color-text-primary)]">Primitive Color Scales</h2>
      {SCALE_NAMES.map((scaleName) => (
        <div key={scaleName}>
          <h3 className="text-sm font-semibold text-[var(--color-text-secondary)] mb-3 capitalize">{scaleName}</h3>
          <div className="flex flex-wrap gap-2">
            {SCALE_STEPS.map((step) => {
              const hex = theme.primitives[scaleName][step]
              return (
                <ColorSwatch
                  key={step}
                  color={hex}
                  label={String(step)}
                  hex={hex}
                />
              )
            })}
          </div>
        </div>
      ))}
    </div>
  )
}

export function SemanticTokens() {
  const [isDark, setIsDark] = useState(false)

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-[var(--color-text-primary)]">Semantic Token Mappings</h2>
        <button
          onClick={() => setIsDark(!isDark)}
          className="px-3 py-1.5 text-xs font-medium rounded-[var(--radius-md)] border border-[var(--color-border)] text-[var(--color-text-secondary)] hover:bg-[var(--color-surface-raised)] cursor-pointer transition-colors"
        >
          {isDark ? 'Light Mode' : 'Dark Mode'}
        </button>
      </div>
      <div data-theme={isDark ? 'dark' : undefined} className="space-y-6 rounded-[var(--radius-lg)] p-4" style={{ backgroundColor: isDark ? '#020617' : '#f8fafc' }}>
        {SEMANTIC_GROUPS.map((group) => (
          <div key={group.title}>
            <h3 className="text-sm font-semibold text-[var(--color-text-secondary)] mb-2">{group.title}</h3>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2">
              {group.tokens.map((token) => (
                <SemanticSwatch key={token.cssVar} cssVar={token.cssVar} name={token.name} />
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
