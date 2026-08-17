import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import AdminLayout from './AdminLayout'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
})

export default {
  title: 'Templates/AdminLayout',
  component: AdminLayout,
  decorators: [
    (Story) => (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/']}>
          <Routes>
            <Route element={<Story />}>
              <Route index element={<DummyPage title="Dashboard" />} />
              <Route path="products" element={<DummyPage title="Products" />} />
              <Route path="inventory" element={<DummyPage title="Inventory" />} />
            </Route>
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    ),
  ],
}

function DummyPage({ title }) {
  return (
    <div className="p-4">
      <p className="text-sm text-[var(--color-text-secondary)]">Content area for: <strong>{title}</strong></p>
    </div>
  )
}

export const Default = {}
