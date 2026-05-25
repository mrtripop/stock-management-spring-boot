import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import Dashboard from './Dashboard'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
})

export default {
  title: 'Pages/Dashboard',
  component: Dashboard,
  decorators: [
    (Story) => (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/']}>
          <Story />
        </MemoryRouter>
      </QueryClientProvider>
    ),
  ],
}

export const Default = {}
