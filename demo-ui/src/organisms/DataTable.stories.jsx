import { useState } from 'react'
import { DataTable } from './DataTable'
import { Badge } from '../atoms/Badge'

const sampleColumns = [
  { key: 'name', label: 'Name', sortable: true },
  { key: 'category', label: 'Category', sortable: true },
  { key: 'status', label: 'Status', render: (row) => <Badge variant={row.status === 'Active' ? 'success' : 'danger'}>{row.status}</Badge> },
  { key: 'quantity', label: 'Qty', sortable: true },
]

const sampleData = Array.from({ length: 25 }, (_, i) => ({
  id: i + 1,
  name: `Product ${i + 1}`,
  category: ['Tablet', 'Capsule', 'Syrup'][i % 3],
  status: i % 3 === 0 ? 'Active' : 'Inactive',
  quantity: Math.floor(Math.random() * 500),
}))

export default {
  title: 'Organisms/DataTable',
  component: DataTable,
  argTypes: {
    onSort: { action: 'sort' },
    onPageChange: { action: 'pageChange' },
    onSelectionChange: { action: 'selectionChange' },
  },
}

export const Default = {
  args: {
    columns: sampleColumns,
    data: sampleData.slice(0, 10),
    currentPage: 1,
    totalPages: 3,
    totalElements: 25,
  },
}

export const Loading = {
  args: {
    columns: sampleColumns,
    data: [],
    loading: true,
  },
}

export const Empty = {
  args: {
    columns: sampleColumns,
    data: [],
    emptyMessage: 'No products found',
  },
}

export const Selectable = {
  render: (args) => {
    const [selected, setSelected] = useState([])
    return <DataTable {...args} selectedKeys={selected} onSelectionChange={setSelected} />
  },
  args: {
    columns: sampleColumns,
    data: sampleData.slice(0, 10),
    selectable: true,
  },
}

export const Sortable = {
  render: (args) => {
    const [sortKey, setSortKey] = useState(null)
    const [sortDir, setSortDir] = useState('asc')
    const sorted = [...sampleData.slice(0, 10)].sort((a, b) => {
      if (!sortKey) return 0
      const valA = a[sortKey]
      const valB = b[sortKey]
      return sortDir === 'asc' ? (valA > valB ? 1 : -1) : (valA < valB ? 1 : -1)
    })
    return <DataTable {...args} data={sorted} sortKey={sortKey} sortDir={sortDir} onSort={(k, d) => { setSortKey(k); setSortDir(d) }} />
  },
  args: {
    columns: sampleColumns,
  },
}
