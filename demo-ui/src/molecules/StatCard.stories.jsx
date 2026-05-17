import { StatCard } from './StatCard'

export default {
  title: 'Molecules/StatCard',
  component: StatCard,
  argTypes: {
    label: { control: 'text' },
    value: { control: 'text' },
    trend: { control: 'select', options: ['up', 'down', 'flat', undefined] },
    trendValue: { control: 'text' },
    variant: { control: 'select', options: ['default', 'success', 'danger', 'warning'] },
    loading: { control: 'boolean' },
  },
}

export const Default = {
  args: {
    label: 'Total Products',
    value: '1,234',
    icon: 'cube',
  },
}

export const WithTrendUp = {
  args: {
    label: 'Revenue',
    value: '$45,678',
    icon: 'credit-card',
    trend: 'up',
    trendValue: '+12.5%',
  },
}

export const WithTrendDown = {
  args: {
    label: 'Expired Items',
    value: '23',
    icon: 'exclamation',
    trend: 'down',
    trendValue: '-8.3%',
    variant: 'danger',
  },
}

export const Loading = {
  args: {
    label: 'Loading Stat',
    loading: true,
    icon: 'cube',
  },
}

export const NoIcon = {
  args: {
    label: 'Active Batches',
    value: '89',
    trend: 'up',
    trendValue: '+5 today',
    variant: 'success',
  },
}

export const Dashboard = {
  render: () => (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[var(--space-4)]">
      <StatCard
        label="Total Products"
        value="1,234"
        icon="cube"
        trend="up"
        trendValue="+48 this month"
      />
      <StatCard
        label="Active Batches"
        value="89"
        icon="archive"
        trend="flat"
        trendValue="No change"
        variant="success"
      />
      <StatCard
        label="Daily Revenue"
        value="$12,450"
        icon="credit-card"
        trend="up"
        trendValue="+12.5%"
      />
      <StatCard
        label="Expiring Soon"
        value="7"
        icon="exclamation"
        trend="down"
        trendValue="-3 resolved"
        variant="danger"
      />
    </div>
  ),
}

export const VariantMatrix = {
  render: () => (
    <div className="grid grid-cols-2 gap-[var(--space-4)] max-w-lg">
      <StatCard label="Default" value="100" icon="cube" />
      <StatCard label="Success" value="89" icon="check" variant="success" trend="up" trendValue="+12" />
      <StatCard label="Warning" value="5" icon="exclamation" variant="warning" trend="flat" trendValue="No change" />
      <StatCard label="Danger" value="3" icon="trash" variant="danger" trend="down" trendValue="-2" />
    </div>
  ),
}
