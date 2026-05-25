import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import Products from './Products'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
})

export default {
  title: 'Pages/Products',
  component: Products,
  decorators: [
    (Story) => (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/products']}>
          <Routes>
            <Route path="/products" element={<Story />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    ),
  ],
}

export const Default = {}
