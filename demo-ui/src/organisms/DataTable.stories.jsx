import { DataTable } from './DataTable'
import { Badge } from '../atoms/Badge'

export default {
  title: 'Organisms/DataTable',
  component: DataTable,
}

const COLUMNS = [
  { key: 'name', label: 'Name' },
  { key: 'category', label: 'Category' },
  { key: 'qty', label: 'Qty' },
  { key: 'status', label: 'Status' },
]

const MOCK_DATA = [
  { id: 1, name: 'Paracetamol 500mg', category: 'Analgesic', qty: 120, active: true },
  { id: 2, name: 'Amoxicillin 250mg', category: 'Antibiotic', qty: 45, active: true },
  { id: 3, name: 'Omeprazole 20mg', category: 'PPI', qty: 0, active: false },
  { id: 4, name: 'Metformin 500mg', category: 'Antidiabetic', qty: 200, active: true },
  { id: 5, name: 'Lisinopril 10mg', category: 'ACE Inhibitor', qty: 8, active: true },
]

export const WithData = {
  args: {
    columns: COLUMNS,
    data: MOCK_DATA,
    currentPage: 1,
    totalPages: 1,
    totalElements: 5,
    pageSize: 10,
    onPageChange: () => {},
    renderRow: (item) => (
      <tr key={item.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
        <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{item.name}</td>
        <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{item.category}</td>
        <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{item.qty}</td>
        <td className="px-4 py-2.5">
          <Badge variant={item.active ? 'success' : 'neutral'}>{item.active ? 'Active' : 'Inactive'}</Badge>
        </td>
      </tr>
    ),
  },
}

export const Loading = {
  args: {
    columns: COLUMNS,
    data: [],
    loading: true,
    renderRow: () => null,
  },
}

export const Empty = {
  args: {
    columns: COLUMNS,
    data: [],
    emptyMessage: 'No products found',
    renderRow: () => null,
  },
}

export const Paginated = {
  args: {
    columns: COLUMNS,
    data: MOCK_DATA,
    currentPage: 2,
    totalPages: 5,
    totalElements: 48,
    pageSize: 10,
    onPageChange: () => {},
    renderRow: (item) => (
      <tr key={item.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
        <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{item.name}</td>
        <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{item.category}</td>
        <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{item.qty}</td>
        <td className="px-4 py-2.5">
          <Badge variant={item.active ? 'success' : 'neutral'}>{item.active ? 'Active' : 'Inactive'}</Badge>
        </td>
      </tr>
    ),
  },
}
