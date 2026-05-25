import { Button } from './Button'
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline'

export default {
  title: 'Atoms/Button',
  component: Button,
  argTypes: {
    variant: {
      control: 'select',
      options: ['primary', 'secondary', 'outline', 'danger', 'ghost'],
      description: 'Visual style variant',
      table: { defaultValue: { summary: 'primary' } },
    },
    size: {
      control: 'select',
      options: ['sm', 'md', 'lg'],
      description: 'Button height and padding',
      table: { defaultValue: { summary: 'md' } },
    },
    type: {
      control: 'select',
      options: ['button', 'submit', 'reset'],
      description: 'HTML button type attribute',
      table: { defaultValue: { summary: 'button' } },
    },
    disabled: {
      control: 'boolean',
      description: 'Disables the button',
      table: { defaultValue: { summary: 'false' } },
    },
    loading: {
      control: 'boolean',
      description: 'Shows spinner, disables button',
      table: { defaultValue: { summary: 'false' } },
    },
    fullWidth: {
      control: 'boolean',
      description: 'Stretches to fill container width',
      table: { defaultValue: { summary: 'false' } },
    },
    icon: {
      description: 'Heroicons component rendered before children',
    },
    children: {
      control: 'text',
      description: 'Button label content',
    },
    onClick: { action: 'clicked' },
  },
  args: {
    children: 'Click me',
    variant: 'primary',
    size: 'md',
    type: 'button',
    disabled: false,
    loading: false,
    fullWidth: false,
  },
}

export const Playground = {
  args: {
    children: 'Click me',
  },
}

export const Variants = {
  render: () => (
    <div className="flex flex-wrap gap-3">
      <Button variant="primary">Primary</Button>
      <Button variant="secondary">Secondary</Button>
      <Button variant="outline">Outline</Button>
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
    <div className="flex flex-wrap items-center gap-3">
      <Button>Normal</Button>
      <Button disabled>Disabled</Button>
      <Button loading>Loading</Button>
    </div>
  ),
}

export const WithIcon = {
  render: () => (
    <div className="flex flex-wrap items-center gap-3">
      <Button icon={MagnifyingGlassIcon}>Search</Button>
      <Button variant="outline" icon={MagnifyingGlassIcon}>Search</Button>
      <Button size="sm" icon={MagnifyingGlassIcon}>Search</Button>
    </div>
  ),
}

export const FullWidth = {
  render: () => (
    <div className="flex flex-col gap-3 max-w-xs">
      <Button fullWidth>Full Width</Button>
      <Button fullWidth variant="outline">Outline Full Width</Button>
    </div>
  ),
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-4">
      {['primary', 'secondary', 'outline', 'danger', 'ghost'].map((variant) => (
        <div key={variant} className="flex flex-wrap items-center gap-3">
          <span className="w-24 text-sm text-[var(--color-text-secondary)] font-medium">{variant}</span>
          <Button variant={variant} size="sm">Small</Button>
          <Button variant={variant} size="md">Medium</Button>
          <Button variant={variant} size="lg">Large</Button>
          <Button variant={variant} disabled>Disabled</Button>
          <Button variant={variant} loading>Loading</Button>
        </div>
      ))}
    </div>
  ),
}
