import { StatCard } from './StatCard'

export default {
  title: 'Molecules/StatCard',
  component: StatCard,
  argTypes: {
    title: { control: 'text' },
    value: { control: 'text' },
    change: { control: 'text' },
    trend: { control: 'select', options: ['up', 'down', undefined] },
    accentColor: { control: 'color' },
  },
}

export const Default = { args: { title: 'Total Products', value: '1,234' } }

export const WithTrendUp = { args: { title: 'Revenue', value: '$45,678', change: '+12.5%', trend: 'up' } }

export const WithTrendDown = { args: { title: 'Expired Items', value: '23', change: '-8.3%', trend: 'down' } }

export const Dashboard = {
  render: () => (
    <div className="grid grid-cols-4 gap-4">
      <StatCard title="Total Products" value="1,234" accentColor="#0d9488" />
      <StatCard title="Low Stock" value="18" change="+3 today" trend="up" accentColor="#f59e0b" />
      <StatCard title="Expired" value="5" change="-2 this week" trend="down" accentColor="#ef4444" />
      <StatCard title="In Transit" value="42" accentColor="#8b5cf6" />
    </div>
  ),
}
