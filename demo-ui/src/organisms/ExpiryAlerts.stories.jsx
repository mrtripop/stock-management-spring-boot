import { MemoryRouter } from 'react-router-dom'
import { ExpiryAlerts } from './ExpiryAlerts'

export default {
  title: 'Organisms/ExpiryAlerts',
  component: ExpiryAlerts,
  decorators: [
    (Story) => <MemoryRouter><Story /></MemoryRouter>,
  ],
}

const MOCK_ITEMS = [
  { id: '1', productName: 'Amoxicillin 250mg', batchNumber: 'BN-2026-001', quantity: 45, expiryDate: '2026-05-20' },
  { id: '2', productName: 'Paracetamol 500mg', batchNumber: 'BN-2026-002', quantity: 200, expiryDate: '2026-06-10' },
  { id: '3', productName: 'Omeprazole 20mg', batchNumber: 'BN-2026-003', quantity: 12, expiryDate: '2026-08-15' },
  { id: '4', productName: 'Metformin 850mg', batchNumber: 'BN-2026-004', quantity: 80, expiryDate: '2027-01-20' },
]

export const WithData = { args: { items: MOCK_ITEMS } }

export const Empty = { args: { items: [] } }

export const UrgentOnly = {
  args: {
    items: [
      { id: '1', productName: 'Insulin Pen', batchNumber: 'BN-0099', quantity: 5, expiryDate: '2026-05-18' },
      { id: '2', productName: 'Epinephrine 1mg', batchNumber: 'BN-0100', quantity: 2, expiryDate: '2026-05-17' },
    ],
  },
}
