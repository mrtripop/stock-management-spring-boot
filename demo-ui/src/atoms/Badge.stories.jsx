import { Badge } from './Badge'

export default {
  title: 'Atoms/Badge',
  component: Badge,
  argTypes: {
    variant: {
      control: 'select',
      options: ['success', 'danger', 'warning', 'info', 'neutral', 'teal', 'purple', 'orange'],
      description: 'Color variant',
      table: { defaultValue: { summary: 'neutral' } },
    },
    dot: {
      control: 'boolean',
      description: 'Show a small colored dot before the text for status indicators',
      table: { defaultValue: { summary: 'false' } },
    },
    children: {
      control: 'text',
      description: 'Badge content',
    },
  },
  args: {
    children: 'Badge',
    variant: 'neutral',
    dot: false,
  },
}

export const Playground = {
  args: {
    children: 'Active',
    variant: 'success',
  },
}

export const AllVariants = {
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="success">Success</Badge>
      <Badge variant="danger">Danger</Badge>
      <Badge variant="warning">Warning</Badge>
      <Badge variant="info">Info</Badge>
      <Badge variant="neutral">Neutral</Badge>
      <Badge variant="teal">Teal</Badge>
      <Badge variant="purple">Purple</Badge>
      <Badge variant="orange">Orange</Badge>
    </div>
  ),
}

export const WithDot = {
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="success" dot>Active</Badge>
      <Badge variant="danger" dot>Expired</Badge>
      <Badge variant="warning" dot>Pending</Badge>
      <Badge variant="info" dot>Acknowledged</Badge>
      <Badge variant="neutral" dot>Inactive</Badge>
      <Badge variant="teal" dot>Pharmacist</Badge>
      <Badge variant="purple" dot>Admin</Badge>
      <Badge variant="orange" dot>Reorder Needed</Badge>
    </div>
  ),
}

export const BatchStatus = {
  name: 'Pharmacy Enum — BatchStatus',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="success" dot>AVAILABLE</Badge>
      <Badge variant="danger" dot>RECALLED</Badge>
      <Badge variant="warning" dot>QUARANTINED</Badge>
    </div>
  ),
}

export const InvoiceStatus = {
  name: 'Pharmacy Enum — InvoiceStatus',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="warning" dot>PENDING</Badge>
      <Badge variant="success" dot>COMPLETED</Badge>
      <Badge variant="danger" dot>VOIDED</Badge>
    </div>
  ),
}

export const TaskStatus = {
  name: 'Pharmacy Enum — TaskStatus',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="warning" dot>PENDING</Badge>
      <Badge variant="info" dot>ACKNOWLEDGED</Badge>
      <Badge variant="success" dot>RESOLVED</Badge>
    </div>
  ),
}

export const UserRole = {
  name: 'Pharmacy Enum — UserRole',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="purple">ADMIN</Badge>
      <Badge variant="teal">MANAGER</Badge>
      <Badge variant="info">PHARMACIST</Badge>
      <Badge variant="neutral">EMPLOYEE</Badge>
    </div>
  ),
}

export const StoreType = {
  name: 'Pharmacy Enum — StoreType',
  render: () => (
    <div className="flex flex-wrap gap-2">
      <Badge variant="teal">PHYSICAL</Badge>
      <Badge variant="info">HUB</Badge>
      <Badge variant="neutral">LOGICAL</Badge>
    </div>
  ),
}
