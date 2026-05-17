import { TopBar } from './TopBar'

export default {
  title: 'Organisms/TopBar',
  component: TopBar,
}

export const Default = { args: { title: 'Dashboard' } }

export const WithSubtitle = { args: { title: 'Products', subtitle: 'Manage your product catalog' } }

export const LongTitle = { args: { title: 'Inventory Management System', subtitle: 'Batch management and stock operations' } }
