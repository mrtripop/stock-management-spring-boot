import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import Clinical from './Clinical'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
})

export default {
  title: 'Pages/Clinical',
  component: Clinical,
  decorators: [
    (Story) => (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/clinical']}>
          <Routes>
            <Route path="/clinical" element={<Story />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    ),
  ],
}

export const Default = {}
