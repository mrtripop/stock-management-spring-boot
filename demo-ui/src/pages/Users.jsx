import { useState } from 'react'
import { PageHeader } from '../molecules/PageHeader'
import { DataTable } from '../organisms/DataTable'
import { useQueryList } from '../lib/hooks'

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'username', label: 'Username' },
  { key: 'name', label: 'Name' },
  { key: 'email', label: 'Email' },
  { key: 'registered', label: 'Registered' },
]

export default function Users() {
  const [page, setPage] = useState(1)

  const { items, totalPages, totalElements, loading } = useQueryList(
    ['users'], '/users', { page, size: 10 }
  )

  return (
    <div>
      <PageHeader title="Users" subtitle="User accounts and roles" />
      <DataTable columns={COLUMNS} data={items} loading={loading}
        currentPage={page} totalPages={totalPages} totalElements={totalElements}
        pageSize={10} onPageChange={setPage} emptyMessage="No users found"
        renderRow={(u) => (
          <tr key={u.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50">
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{u.id}</td>
            <td className="px-4 py-2.5">
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-[var(--color-primary)] rounded-full flex items-center justify-center text-white text-[10px] font-semibold">
                  {(u.username || '?').substring(0, 2).toUpperCase()}
                </div>
                <span className="text-sm font-medium text-[var(--color-text-primary)]">{u.username}</span>
              </div>
            </td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{[u.firstName, u.lastName].filter(Boolean).join(' ') || '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{u.email || '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{u.registeredAt ? new Date(u.registeredAt).toLocaleDateString() : '-'}</td>
          </tr>
        )}
      />
    </div>
  )
}
