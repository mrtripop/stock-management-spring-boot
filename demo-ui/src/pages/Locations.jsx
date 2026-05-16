import { useState } from 'react'
import { Badge } from '../atoms/Badge'
import { PageHeader } from '../molecules/PageHeader'
import { DataTable } from '../organisms/DataTable'
import { useQueryList } from '../lib/hooks'

const COLUMNS = [
  { key: 'name', label: 'Name' },
  { key: 'line1', label: 'Address' },
  { key: 'city', label: 'City' },
  { key: 'province', label: 'Province' },
  { key: 'country', label: 'Country' },
  { key: 'postal', label: 'Postal' },
]

export default function Locations() {
  const [page, setPage] = useState(1)

  const { items, totalPages, totalElements, loading } = useQueryList(
    ['addresses'], '/addresses', { page, size: 10 }
  )

  return (
    <div>
      <PageHeader title="Locations" subtitle="Warehouse and store locations" />
      <DataTable columns={COLUMNS} data={items} loading={loading}
        currentPage={page} totalPages={totalPages} totalElements={totalElements}
        pageSize={10} onPageChange={setPage} emptyMessage="No locations found"
        renderRow={(a) => (
          <tr key={a.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
            <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{a.addressName}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.line1}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.city}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.province}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.country}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{a.postalCode}</td>
          </tr>
        )}
      />
    </div>
  )
}
