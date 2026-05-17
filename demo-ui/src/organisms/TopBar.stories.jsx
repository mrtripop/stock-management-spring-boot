import { TopBar } from './TopBar'

export default {
  title: 'Organisms/TopBar',
  component: TopBar,
}

export const Default = {
  args: {
    breadcrumb: [{ label: 'Dashboard' }],
    notificationCount: 3,
    userAvatar: 'JD',
  },
}

export const WithBreadcrumb = {
  args: {
    breadcrumb: [{ label: 'Clinical', to: '/clinical' }, { label: 'Molecules' }],
    notificationCount: 0,
    userAvatar: 'PS',
  },
}

export const WithNotifications = {
  args: {
    breadcrumb: [{ label: 'Inventory' }],
    notificationCount: 12,
    userAvatar: 'AB',
  },
}
