import { Badge } from './Badge'

export default {
  title: 'Atoms/Badge',
  component: Badge,
  argTypes: {
    variant: { control: 'select', options: ['success', 'danger', 'warning', 'info', 'neutral', 'teal', 'purple', 'orange'] },
  },
}

export const Default = { args: { children: 'Badge', variant: 'neutral' } }

export const AllVariants = {
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="success">Active</Badge>
      <Badge variant="danger">Expired</Badge>
      <Badge variant="warning">Warning</Badge>
      <Badge variant="info">Info</Badge>
      <Badge variant="neutral">Neutral</Badge>
      <Badge variant="teal">Teal</Badge>
      <Badge variant="purple">Purple</Badge>
      <Badge variant="orange">Orange</Badge>
    </div>
  ),
}
