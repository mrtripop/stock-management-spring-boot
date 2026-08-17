import { Icon } from './Icon'

const allIconNames = [
  'home', 'cube', 'archive', 'beaker', 'cart', 'credit-card', 'map-pin',
  'users', 'logout', 'search', 'bell', 'plus', 'pencil', 'trash', 'funnel',
  'arrow-down', 'arrow-up', 'exclamation', 'check', 'x-mark',
  'chevron-left', 'chevron-right', 'chevron-down',
  'arrow-down-tray', 'arrow-up-tray', 'magnifying-glass', 'receipt',
]

export default {
  title: 'Atoms/Icon',
  component: Icon,
  argTypes: {
    name: {
      control: 'select',
      options: allIconNames,
      description: 'Icon name from the Heroicons map',
      table: { defaultValue: { summary: 'home' } },
    },
    size: {
      control: 'select',
      options: ['sm', 'md', 'lg'],
      description: 'Icon size: sm=16px, md=20px, lg=24px',
      table: { defaultValue: { summary: 'md' } },
    },
  },
  args: {
    name: 'home',
    size: 'md',
  },
}

export const Playground = {
  args: {
    name: 'cube',
    size: 'md',
  },
}

export const Sizes = {
  render: () => (
    <div className="flex items-center gap-6">
      <div className="flex flex-col items-center gap-1">
        <Icon name="cube" size="sm" />
        <span className="text-xs text-[var(--color-text-muted)]">sm (16px)</span>
      </div>
      <div className="flex flex-col items-center gap-1">
        <Icon name="cube" size="md" />
        <span className="text-xs text-[var(--color-text-muted)]">md (20px)</span>
      </div>
      <div className="flex flex-col items-center gap-1">
        <Icon name="cube" size="lg" />
        <span className="text-xs text-[var(--color-text-muted)]">lg (24px)</span>
      </div>
    </div>
  ),
}

export const AllIcons = {
  render: () => (
    <div className="grid grid-cols-6 gap-4">
      {allIconNames.map((name) => (
        <div key={name} className="flex flex-col items-center gap-1">
          <Icon name={name} size="md" />
          <span className="text-[0.625rem] text-[var(--color-text-muted)] text-center truncate w-full">{name}</span>
        </div>
      ))}
    </div>
  ),
}
