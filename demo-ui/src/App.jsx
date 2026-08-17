import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'sonner'
import { AuthProvider, useAuth } from './lib/auth'
import AdminLayout from './templates/AdminLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Products from './pages/Products'
import Inventory from './pages/Inventory'
import Clinical from './pages/Clinical'
import StoreProducts from './pages/StoreProducts'
import Dispensing from './pages/Dispensing'
import Orders from './pages/Orders'
import Transactions from './pages/Transactions'
import Locations from './pages/Locations'
import Users from './pages/Users'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false },
    mutations: { retry: 0 },
  },
})

function ProtectedRoute({ children }) {
  const { token } = useAuth()
  if (!token) return <Navigate to="/login" />
  return children
}

function PublicRoute({ children }) {
  const { token } = useAuth()
  if (token) return <Navigate to="/" />
  return children
}

export default function App() {
  return (
    <AuthProvider>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
            <Route path="/" element={<ProtectedRoute><AdminLayout /></ProtectedRoute>}>
              <Route index element={<Dashboard />} />
              <Route path="products" element={<Products />} />
              <Route path="inventory" element={<Inventory />} />
              <Route path="clinical" element={<Clinical />} />
              <Route path="clinical/stores/:storeId/products" element={<StoreProducts />} />
              <Route path="dispensing" element={<Dispensing />} />
              <Route path="orders" element={<Orders />} />
              <Route path="transactions" element={<Transactions />} />
              <Route path="locations" element={<Locations />} />
              <Route path="users" element={<Users />} />
            </Route>
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </BrowserRouter>
        <Toaster position="top-right" richColors closeButton />
      </QueryClientProvider>
    </AuthProvider>
  )
}
