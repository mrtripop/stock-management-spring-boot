import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { DataTable } from '../organisms/DataTable'
import { FormDrawer } from '../organisms/FormDrawer'
import { PageHeader } from '../molecules/PageHeader'
import { SearchBar } from '../molecules/SearchBar'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'
import { Badge } from '../atoms/Badge'
import { Button } from '../atoms/Button'
import { useProductList, useCreateProduct, useUpdateProduct, useDeleteProduct } from '../lib/hooks/useProducts'

const productSchema = z.object({
  code: z.string().min(1, 'Code is required'),
  barcode: z.string().min(1, 'Barcode is required'),
  name: z.string().min(1, 'Name is required'),
  description: z.string().max(300, 'Max 300 characters'),
  category: z.string().min(1, 'Category is required'),
  reorderQuantity: z.number().min(0, 'Must be >= 0'),
  packedWeight: z.number().min(0),
  packedHeight: z.number().min(0),
  packedWidth: z.number().min(0),
  packedDepth: z.number().min(0),
  isActive: z.boolean(),
})

const columns = [
  { key: 'code', label: 'Code', sortable: true },
  { key: 'name', label: 'Name', sortable: true },
  { key: 'category', label: 'Category', sortable: true },
  { key: 'isActive', label: 'Status', render: (row) => <Badge variant={row.isActive ? 'success' : 'neutral'}>{row.isActive ? 'Active' : 'Inactive'}</Badge> },
]

const DEFAULT_VALUES = { isActive: true, reorderQuantity: 0, packedWeight: 0, packedHeight: 0, packedWidth: 0, packedDepth: 0 }

export default function Products() {
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editing, setEditing] = useState(null)

  const { items, totalPages, totalElements, loading } = useProductList({ page, size: 20 })
  const createProduct = useCreateProduct()
  const updateProduct = useUpdateProduct()
  const deleteProduct = useDeleteProduct()

  const form = useForm({ resolver: zodResolver(productSchema), defaultValues: DEFAULT_VALUES })

  const filtered = (items || []).filter((p) => !search || p.name?.toLowerCase().includes(search.toLowerCase()) || p.code?.toLowerCase().includes(search.toLowerCase()))

  const openCreate = () => { setEditing(null); form.reset(DEFAULT_VALUES); setDrawerOpen(true) }
  const openEdit = (row) => { setEditing(row); form.reset(row); setDrawerOpen(true) }

  const onSubmit = async (data) => {
    if (editing) { await updateProduct.mutateAsync({ id: editing.id, ...data }) }
    else { await createProduct.mutateAsync(data) }
    setDrawerOpen(false)
  }

  const actionColumn = { key: 'actions', label: '', width: '80px', render: (row) => (
    <div className="flex gap-1">
      <Button size="sm" variant="ghost" onClick={() => openEdit(row)}>Edit</Button>
      <Button size="sm" variant="ghost" onClick={() => { if (confirm('Delete?')) deleteProduct.mutate(row.id) }}>Del</Button>
    </div>
  )}

  return (
    <div className="space-y-4">
      <PageHeader title="Products" subtitle="Manage your product catalog" actions={<Button onClick={openCreate}>Add Product</Button>} />
      <SearchBar value={search} onChange={setSearch} placeholder="Search products..." />
      <DataTable columns={[...columns, actionColumn]} data={filtered} loading={loading} currentPage={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} emptyMessage="No products found" />
      <FormDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title={editing ? 'Edit Product' : 'Create Product'} onSubmit={form.handleSubmit(onSubmit)} loading={createProduct.isPending || updateProduct.isPending}>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Code" required error={form.formState.errors.code?.message}><Input {...form.register('code')} /></FormField>
            <FormField label="Barcode" required error={form.formState.errors.barcode?.message}><Input {...form.register('barcode')} /></FormField>
          </div>
          <FormField label="Name" required error={form.formState.errors.name?.message}><Input {...form.register('name')} /></FormField>
          <FormField label="Description" error={form.formState.errors.description?.message}><Input {...form.register('description')} /></FormField>
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Category" required error={form.formState.errors.category?.message}><Input {...form.register('category')} /></FormField>
            <FormField label="Reorder Qty" error={form.formState.errors.reorderQuantity?.message}><Input type="number" {...form.register('reorderQuantity', { valueAsNumber: true })} /></FormField>
          </div>
          <div className="grid grid-cols-4 gap-3">
            <FormField label="Weight" error={form.formState.errors.packedWeight?.message}><Input type="number" step="0.01" {...form.register('packedWeight', { valueAsNumber: true })} /></FormField>
            <FormField label="Height" error={form.formState.errors.packedHeight?.message}><Input type="number" step="0.01" {...form.register('packedHeight', { valueAsNumber: true })} /></FormField>
            <FormField label="Width" error={form.formState.errors.packedWidth?.message}><Input type="number" step="0.01" {...form.register('packedWidth', { valueAsNumber: true })} /></FormField>
            <FormField label="Depth" error={form.formState.errors.packedDepth?.message}><Input type="number" step="0.01" {...form.register('packedDepth', { valueAsNumber: true })} /></FormField>
          </div>
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" {...form.register('isActive')} className="rounded" /> Active</label>
        </div>
      </FormDrawer>
    </div>
  )
}
