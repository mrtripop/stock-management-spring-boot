import { useState } from 'react'
import { DataTable } from '../organisms/DataTable'
import { PageHeader } from '../molecules/PageHeader'
import { Badge } from '../atoms/Badge'
import { RequireRole } from '../lib/auth'
import { useUserList } from '../lib/hooks/useUsers'

const roleVariant = { ADMIN: 'purple', MANAGER: 'teal', PHARMACIST: 'info', EMPLOYEE: 'neutral' }

export default function Users() {
  const [page, setPage] = useState(1)
  const { items, totalPages, loading } = useUserList({ page, size: 20 })

  return (
    <RequireRole roles={['ADMIN']}>
      <div className="space-y-4">
        <PageHeader title="Users" subtitle="User accounts and roles" />
        <DataTable
          columns={[
            { key: 'username', label: 'Username', sortable: true },
            { key: 'role', label: 'Role', render: (r) => <Badge variant={roleVariant[r.role] || 'neutral'}>{r.role}</Badge> },
            { key: 'createdAt', label: 'Created', render: (r) => new Date(r.createdAt).toLocaleDateString() },
          ]}
          data={items || []}
          loading={loading}
          currentPage={page}
          totalPages={totalPages}
          onPageChange={setPage}
          emptyMessage="No users found"
        />
      </div>
    </RequireRole>
  )
}
