import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { useContext } from 'react'
import { AuthContext } from '../lib/auth'
import Users from './Users'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
})

function MockAdminAuth({ children }) {
  const ctx = useContext(AuthContext)
  return (
    <AuthContext.Provider value={{ ...ctx, role: 'ADMIN', token: 'mock-token', isAuthenticated: true }}>
      {children}
    </AuthContext.Provider>
  )
}

export default {
  title: 'Pages/Users',
  component: Users,
  decorators: [
    (Story) => (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/users']}>
          <MockAdminAuth>
            <Routes>
              <Route path="/users" element={<Story />} />
            </Routes>
          </MockAdminAuth>
        </MemoryRouter>
      </QueryClientProvider>
    ),
  ],
}

export const Default = {}
