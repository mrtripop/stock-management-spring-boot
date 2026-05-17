import { ActivityFeed } from './ActivityFeed'

const sampleItems = [
  { id: 1, type: 'transaction', message: 'Invoice #42 completed — Amoxicillin 500mg x10', timestamp: '2 minutes ago' },
  { id: 2, type: 'task', message: 'Expiry warning: Paracetamol 500mg expires in 5 days', timestamp: '15 minutes ago' },
  { id: 3, type: 'stock', message: 'Stock-in: Ibuprofen 200mg — 200 units added', timestamp: '1 hour ago' },
  { id: 4, type: 'alert', message: 'Batch BN-2026-003 recalled by manufacturer', timestamp: '2 hours ago' },
  { id: 5, type: 'transaction', message: 'Invoice #41 voided by admin', timestamp: '3 hours ago' },
]

export default {
  title: 'Organisms/ActivityFeed',
  component: ActivityFeed,
}

export const Default = { args: { items: sampleItems } }
export const Loading = { args: { loading: true } }
export const Empty = { args: { items: [] } }
