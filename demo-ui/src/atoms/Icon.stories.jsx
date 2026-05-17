import { Icon } from './Icon'

export default {
  title: 'Atoms/Icon',
  component: Icon,
  argTypes: {
    name: {
      control: 'select',
      options: ['home', 'cube', 'archive', 'beaker', 'cart', 'credit-card', 'map-pin', 'users', 'search', 'bell', 'plus', 'pencil', 'trash', 'funnel', 'check', 'x-mark'],
    },
  },
}

export const Default = { args: { name: 'home' } }

export const AllIcons = {
  render: () => (
    <div className="grid grid-cols-6 gap-4">
      {['home', 'cube', 'archive', 'beaker', 'cart', 'credit-card', 'map-pin', 'users', 'search', 'bell', 'plus', 'pencil', 'trash', 'funnel', 'check', 'x-mark'].map((name) => (
        <div key={name} className="flex flex-col items-center gap-1">
          <Icon name={name} />
          <span className="text-xs text-slate-500">{name}</span>
        </div>
      ))}
    </div>
  ),
}
