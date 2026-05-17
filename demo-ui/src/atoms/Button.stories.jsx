import { Button } from './Button'

export default {
  title: 'Atoms/Button',
  component: Button,
  argTypes: {
    variant: { control: 'select', options: ['primary', 'secondary', 'danger', 'ghost'] },
    size: { control: 'select', options: ['sm', 'md', 'lg'] },
    onClick: { action: 'clicked' },
  },
}

export const Default = { args: { children: 'Click me' } }

export const Variants = {
  render: () => (
    <div className="flex flex-wrap gap-3">
      <Button variant="primary">Primary</Button>
      <Button variant="secondary">Secondary</Button>
      <Button variant="danger">Danger</Button>
      <Button variant="ghost">Ghost</Button>
    </div>
  ),
}

export const Sizes = {
  render: () => (
    <div className="flex items-center gap-3">
      <Button size="sm">Small</Button>
      <Button size="md">Medium</Button>
      <Button size="lg">Large</Button>
    </div>
  ),
}

export const States = {
  render: () => (
    <div className="flex items-center gap-3">
      <Button>Normal</Button>
      <Button disabled>Disabled</Button>
      <Button loading>Loading</Button>
    </div>
  ),
}
