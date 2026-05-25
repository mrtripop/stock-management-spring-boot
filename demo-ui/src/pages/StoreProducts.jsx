import { useParams, useNavigate } from 'react-router-dom'
import { DataTable } from '../organisms/DataTable'
import { PageHeader } from '../molecules/PageHeader'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useStoreProducts } from '../lib/hooks/useClinical'

export default function StoreProducts() {
  const { storeId } = useParams()
  const navigate = useNavigate()
  const { items, loading } = useStoreProducts(storeId, { page: 1, size: 50 })

  return (
    <div className="space-y-4">
      <PageHeader title="Store Products" subtitle={`Store: ${storeId}`} actions={<Button variant="secondary" onClick={() => navigate('/clinical')}>Back to Clinical</Button>} />
      <DataTable
        columns={[
          { key: 'brandName', label: 'Brand' },
          { key: 'strength', label: 'Strength' },
          { key: 'form', label: 'Form' },
          { key: 'price', label: 'Price' },
          { key: 'shelfLocation', label: 'Shelf' },
          { key: 'isActive', label: 'Active', render: (r) => <Badge variant={r.isActive ? 'success' : 'neutral'}>{r.isActive ? 'Yes' : 'No'}</Badge> },
        ]}
        data={items || []}
        loading={loading}
        emptyMessage="No products activated for this store"
      />
    </div>
  )
}
