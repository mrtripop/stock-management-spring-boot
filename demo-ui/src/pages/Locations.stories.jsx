import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import Locations from './Locations'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
})

export default {
  title: 'Pages/Locations',
  component: Locations,
  decorators: [
    (Story) => (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/locations']}>
          <Routes>
            <Route path="/locations" element={<Story />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    ),
  ],
}

export const Default = {}
