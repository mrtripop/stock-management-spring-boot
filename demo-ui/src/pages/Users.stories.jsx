import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import Users from './Users'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
})

export default {
  title: 'Pages/Users',
  component: Users,
  decorators: [
    (Story) => (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/users']}>
          <Routes>
            <Route path="/users" element={<Story />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    ),
  ],
}

export const Default = {}
