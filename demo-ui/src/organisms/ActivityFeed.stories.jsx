import { ActivityFeed } from './ActivityFeed'

export default {
  title: 'Organisms/ActivityFeed',
  component: ActivityFeed,
}

const MOCK_ITEMS = [
  { type: 'STOCK_IN', title: 'Stock In — Paracetamol', description: 'Batch BN-2026-0042 · 500 units', time: '2 min ago' },
  { type: 'DEDUCT', title: 'Deducted — Amoxicillin', description: 'Store A · 20 units dispensed', time: '15 min ago' },
  { type: 'CREATE', title: 'New Product Added', description: 'Metformin 850mg registered', time: '1 hr ago' },
  { type: 'LOW_STOCK', title: 'Low Stock Alert', description: 'Omeprazole 20mg — 5 units remaining', time: '3 hr ago' },
  { type: 'STOCK_IN', title: 'Stock In — Lisinopril', description: 'Batch BN-2026-0039 · 200 units', time: '5 hr ago' },
]

export const WithData = { args: { items: MOCK_ITEMS } }

export const Empty = { args: { items: [] } }

export const SingleItem = {
  args: {
    items: [{ type: 'STOCK_IN', title: 'Stock In — Ibuprofen', description: 'Batch BN-2026-0050 · 1000 units', time: 'Just now' }],
  },
}
