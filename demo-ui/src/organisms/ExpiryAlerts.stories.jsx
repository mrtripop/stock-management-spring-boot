import { ExpiryAlerts } from './ExpiryAlerts'

const sampleTasks = [
  { id: 1, taskType: 'EXPIRY_WARNING', brandName: 'Amoxicillin 500mg', batchNumber: 'BN-001', daysUntilExpiry: 3, currentQuantity: 50, status: 'PENDING' },
  { id: 2, taskType: 'RECALL_ALERT', brandName: 'Paracetamol 250mg', batchNumber: 'BN-045', currentQuantity: 200, status: 'PENDING' },
  { id: 3, taskType: 'EXPIRY_WARNING', brandName: 'Ibuprofen 200mg', batchNumber: 'BN-012', daysUntilExpiry: 18, currentQuantity: 100, status: 'ACKNOWLEDGED' },
  { id: 4, taskType: 'REORDER_NEEDED', brandName: 'Cetirizine 10mg', batchNumber: 'BN-078', currentQuantity: 5, status: 'PENDING' },
  { id: 5, taskType: 'EXPIRY_WARNING', brandName: 'Omeprazole 20mg', batchNumber: 'BN-023', daysUntilExpiry: 60, currentQuantity: 75, status: 'RESOLVED' },
]

export default {
  title: 'Organisms/ExpiryAlerts',
  component: ExpiryAlerts,
}

export const MixedAlerts = {
  args: {
    tasks: sampleTasks,
    onAcknowledge: (id) => console.log('Acknowledge', id),
    onResolve: (id) => console.log('Resolve', id),
  },
}

export const Loading = { args: { loading: true } }
export const Empty = { args: { tasks: [] } }
