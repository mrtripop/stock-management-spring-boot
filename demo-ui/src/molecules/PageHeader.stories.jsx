import { PageHeader } from './PageHeader'
import { Button } from '../atoms/Button'

export default {
  title: 'Molecules/PageHeader',
  component: PageHeader,
}

export const Default = { args: { title: 'Products' } }

export const WithSubtitle = { args: { title: 'Inventory', subtitle: 'Manage your stock and batches' } }

export const WithActions = {
  render: () => (
    <PageHeader
      title="Products"
      subtitle="Manage your product catalog"
      actions={<Button>Add Product</Button>}
    />
  ),
}
