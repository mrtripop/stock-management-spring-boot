import { useState } from 'react'
import { toast } from 'sonner'
import { Button } from '../atoms/Button'
import { Badge } from '../atoms/Badge'
import { Input } from '../atoms/Input'
import { Select } from '../atoms/Select'
import { Icon } from '../atoms/Icon'
import { PageHeader } from '../molecules/PageHeader'
import { SearchBar } from '../molecules/SearchBar'
import { FormField } from '../molecules/FormField'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { AlertDialog } from '../organisms/AlertDialog'
import { TableRowActions } from '../molecules/TableRowActions'
import { useQueryList, useCreateMutation, useUpdateMutation, useDeleteMutation } from '../lib/hooks'

const EMPTY_PRODUCT = {
  code: '', barcode: '', name: '', description: '', category: '',
  reorderQuantity: 0, packedWeight: 0, packedHeight: 0,
  packedWidth: 0, packedDepth: 0, isActive: true,
}

const COLUMNS = [
  { key: 'code', label: 'Code' },
  { key: 'name', label: 'Name' },
  { key: 'category', label: 'Category' },
  { key: 'reorderQuantity', label: 'Reorder Qty' },
  { key: 'status', label: 'Status' },
  { key: 'actions', label: '', width: '120px' },
]

export default function Products() {
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editingProduct, setEditingProduct] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [form, setForm] = useState(EMPTY_PRODUCT)

  const { items: products, totalPages, totalElements, loading } = useQueryList(
    ['products'], '/products', { page, size: 10, search }
  )

  const createMutation = useCreateMutation(['products'], '/products')
  const updateMutation = useUpdateMutation(['products'], '/products')
  const deleteMutation = useDeleteMutation(['products'], '/products')

  const openCreate = () => {
    setForm(EMPTY_PRODUCT)
    setEditingProduct(null)
    setShowForm(true)
  }

  const openEdit = (product) => {
    setForm({
      code: product.code, barcode: product.barcode, name: product.name,
      description: product.description, category: product.category,
      reorderQuantity: product.reorderQuantity, packedWeight: product.packedWeight,
      packedHeight: product.packedHeight, packedWidth: product.packedWidth,
      packedDepth: product.packedDepth, isActive: product.isActive,
    })
    setEditingProduct(product)
    setShowForm(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (editingProduct) {
        await updateMutation.mutateAsync({ id: editingProduct.id, ...form })
        toast.success('Product updated')
      } else {
        await createMutation.mutateAsync(form)
        toast.success('Product created')
      }
      setShowForm(false)
    } catch (err) {
      toast.error(err.message)
    }
  }

  const handleDelete = async () => {
    try {
      await deleteMutation.mutateAsync(deleteTarget.id)
      toast.success('Product deleted')
      setDeleteTarget(null)
    } catch (err) {
      toast.error(err.message)
    }
  }

  const formLoading = createMutation.isPending || updateMutation.isPending

  return (
    <div>
      <PageHeader
        title="Products"
        subtitle="Manage your product catalog"
        actions={<Button onClick={openCreate}>+ Add Product</Button>}
      />

      <div className="mb-4">
        <SearchBar placeholder="Search products..." onSearch={setSearch} />
      </div>

      <DataTable
        columns={COLUMNS}
        data={products}
        loading={loading}
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={10}
        onPageChange={setPage}
        emptyMessage="No products found"
        renderRow={(p) => (
          <tr key={p.id} className="border-b border-[var(--color-border-light)] hover:bg-slate-50 transition-colors">
            <td className="px-4 py-2.5 text-sm font-medium text-[var(--color-text-primary)]">{p.code}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-primary)]">{p.name}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{p.category || '-'}</td>
            <td className="px-4 py-2.5 text-sm text-[var(--color-text-secondary)]">{p.reorderQuantity}</td>
            <td className="px-4 py-2.5">
              <Badge variant={p.isActive ? 'success' : 'neutral'}>{p.isActive ? 'Active' : 'Inactive'}</Badge>
            </td>
            <td className="px-4 py-2.5 text-right">
              <TableRowActions
                onEdit={() => openEdit(p)}
                onDelete={() => setDeleteTarget(p)}
              />
            </td>
          </tr>
        )}
      />

      {/* Create/Edit Drawer */}
      <FormDrawer
        open={showForm}
        onClose={() => setShowForm(false)}
        title={editingProduct ? 'Edit Product' : 'New Product'}
        onSubmit={handleSubmit}
        submitLabel={editingProduct ? 'Update' : 'Create'}
        loading={formLoading}
      >
        <FormField label="Product Code" required>
          <Input value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} required />
        </FormField>
        <FormField label="Barcode">
          <Input value={form.barcode} onChange={(e) => setForm({ ...form, barcode: e.target.value })} />
        </FormField>
        <FormField label="Product Name" required>
          <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </FormField>
        <FormField label="Description">
          <Input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </FormField>
        <FormField label="Category">
          <Input value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
        </FormField>
        <FormField label="Reorder Quantity" required>
          <Input type="number" min="0" value={form.reorderQuantity} onChange={(e) => setForm({ ...form, reorderQuantity: +e.target.value })} />
        </FormField>
        <FormField label="Packed Weight (kg)">
          <Input type="number" min="0" step="0.01" value={form.packedWeight} onChange={(e) => setForm({ ...form, packedWeight: +e.target.value })} />
        </FormField>
        <FormField label="Packed Height (cm)">
          <Input type="number" min="0" step="0.01" value={form.packedHeight} onChange={(e) => setForm({ ...form, packedHeight: +e.target.value })} />
        </FormField>
        <FormField label="Packed Width (cm)">
          <Input type="number" min="0" step="0.01" value={form.packedWidth} onChange={(e) => setForm({ ...form, packedWidth: +e.target.value })} />
        </FormField>
        <FormField label="Packed Depth (cm)">
          <Input type="number" min="0" step="0.01" value={form.packedDepth} onChange={(e) => setForm({ ...form, packedDepth: +e.target.value })} />
        </FormField>
        <FormField label="Active">
          <Select value={String(form.isActive)} onChange={(e) => setForm({ ...form, isActive: e.target.value === 'true' })}>
            <option value="true">Yes</option>
            <option value="false">No</option>
          </Select>
        </FormField>
      </FormDrawer>

      {/* Delete Confirmation */}
      <AlertDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Delete Product"
        message={deleteTarget ? `Are you sure you want to delete "${deleteTarget.name}"? This action cannot be undone.` : ''}
        confirmLabel="Delete"
        loading={deleteMutation.isPending}
      />
    </div>
  )
}
