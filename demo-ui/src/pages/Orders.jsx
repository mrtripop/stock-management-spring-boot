import { useState } from 'react'
import { DataTable } from '../organisms/DataTable'
import { PageHeader } from '../molecules/PageHeader'
import { Badge } from '../atoms/Badge'
import { useOrderList } from '../lib/hooks/useOrders'
import { useAuth } from '../lib/auth'

export default function Orders() {
  const { user } = useAuth()
  const [page, setPage] = useState(1)
  const { items, totalPages, loading } = useOrderList(user?.id, { page, size: 20 })

  return (
    <div className="space-y-4">
      <PageHeader title="Orders" subtitle="Order tracking and management" />
      <DataTable
        columns={[
          { key: 'id', label: 'Order ID' },
          { key: 'status', label: 'Status', render: (r) => <Badge variant="info">{r.status || 'Placed'}</Badge> },
          { key: 'createdAt', label: 'Date', render: (r) => new Date(r.createdAt).toLocaleDateString() },
        ]}
        data={items || []}
        loading={loading}
        currentPage={page}
        totalPages={totalPages}
        onPageChange={setPage}
        emptyMessage="No orders found"
      />
    </div>
  )
}
