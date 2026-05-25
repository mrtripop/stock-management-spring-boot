import { PageHeader } from './PageHeader'
import { Button } from '../atoms/Button'

export default {
  title: 'Molecules/PageHeader',
  component: PageHeader,
  argTypes: {
    title: { control: 'text' },
    subtitle: { control: 'text' },
  },
}

export const Default = {
  args: { title: 'Products' },
}

export const WithSubtitle = {
  args: { title: 'Inventory', subtitle: 'Manage your stock and batches' },
}

export const WithActions = {
  render: () => (
    <PageHeader
      title="Products"
      subtitle="Manage your product catalog"
      actions={<Button>Add Product</Button>}
    />
  ),
}

export const WithBreadcrumb = {
  args: {
    title: 'Batch #BN-2026-0042',
    subtitle: 'Paracetamol 500mg',
    breadcrumb: [
      { label: 'Dashboard', href: '/' },
      { label: 'Inventory', href: '/inventory' },
      { label: 'Batch #BN-2026-0042' },
    ],
  },
}

export const FullFeatured = {
  render: () => (
    <PageHeader
      title="Products"
      subtitle="Manage your product catalog"
      breadcrumb={[
        { label: 'Dashboard', href: '/' },
        { label: 'Products' },
      ]}
      actions={
        <div className="flex items-center gap-2">
          <Button variant="secondary">Export CSV</Button>
          <Button>Add Product</Button>
        </div>
      }
    />
  ),
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-8">
      <PageHeader title="Simple Title" />
      <PageHeader title="With Subtitle" subtitle="A brief description of this page" />
      <PageHeader
        title="With Actions"
        actions={<Button size="sm">Action</Button>}
      />
      <PageHeader
        title="Full Example"
        subtitle="Everything together"
        breadcrumb={[
          { label: 'Home', href: '/' },
          { label: 'Section', href: '/section' },
          { label: 'Current Page' },
        ]}
        actions={
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm">Cancel</Button>
            <Button size="sm">Save</Button>
          </div>
        }
      />
    </div>
  ),
}
