# Plan 5 of 6: Auth Context, Enhanced API Client, and Domain Hooks

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a centralized auth context provider, enhance the API client with typed errors and file handling, and create domain-organized React Query hooks for every backend endpoint. This plan has no dependency on Plans 1-4 (component library) and can be executed in parallel.

**Architecture:** `AuthProvider` wraps the app, persists tokens in localStorage, manages login/MFA/store-selection flow. The enhanced `api.js` adds typed error classes, file upload/download, and retry logic. Domain hooks in `src/lib/hooks/` use React Query with automatic cache invalidation.

**Tech Stack:** React 19, TanStack React Query 5, Sonner (toasts), React Router 7. No new dependencies needed.

---

## File Structure

| Action | File | Responsibility |
|--------|------|---------------|
| Create | `demo-ui/src/lib/auth.jsx` | AuthProvider context, useAuth/useHasRole/useStoreId hooks, RequireRole/StoreSelectionGate components |
| Modify | `demo-ui/src/lib/api.js` | Enhanced API client with typed errors, file upload/download, retry logic |
| Create | `demo-ui/src/lib/hooks/useAuth.js` | Auth mutation hooks: useLogin, useVerifyMfa, useRegister, useSetupMfa, useSelectStore, useCurrentUser |
| Create | `demo-ui/src/lib/hooks/useProducts.js` | Product CRUD + upload/export/history hooks |
| Create | `demo-ui/src/lib/hooks/useInventory.js` | Batch, stock, task, conversion, recall hooks |
| Create | `demo-ui/src/lib/hooks/useClinical.js` | Molecule, brand, store, store-product hooks |
| Create | `demo-ui/src/lib/hooks/useTransactions.js` | Invoice, dispense, receipt, reconciliation hooks |
| Create | `demo-ui/src/lib/hooks/useOrders.js` | Order list/detail hooks |
| Create | `demo-ui/src/lib/hooks/useLocations.js` | Address + warehouse CRUD hooks |
| Create | `demo-ui/src/lib/hooks/useUsers.js` | User CRUD hooks |
| Create | `demo-ui/src/lib/hooks/useMesh.js` | Mesh stock search hook |

---

## API Endpoints Covered

| Domain | Endpoints |
|--------|-----------|
| Auth | `POST /auth/login`, `POST /auth/verify-mfa`, `POST /auth/register`, `POST /auth/setup-mfa`, `POST /auth/select-store`, `GET /auth/me` |
| Products | `GET/POST/PUT/DELETE /products`, `POST /products/upload`, `GET /products/export`, `GET /products/histories` |
| Inventory | `GET/POST /inventory/batches/*`, `POST /inventory/stock/deduct`, `GET /inventory/barcode/resolve`, `GET /inventory/stores/{id}/stock`, `GET/PATCH /inventory/tasks/*`, `POST /inventory/tasks/scan`, `GET/POST/DELETE /inventory/conversions/*`, `POST /inventory/compliance/recall` |
| Clinical | `GET/POST/PATCH /clinical/catalog/molecules/*`, `GET/POST /clinical/catalog/brands/*`, `GET/POST/PATCH/DELETE /clinical/stores/*`, `GET/POST/PATCH/DELETE /clinical/catalog/stores/{id}/products/*` |
| Transactions | `GET/POST /transaction/invoices/*`, `POST .../complete`, `POST .../void`, `POST .../dispense`, `GET .../daily-summary`, `GET .../receipt`, `POST /transaction/reports/reconciliation` |
| Orders | `GET /orders/users/{userId}`, `GET /orders/users/{userId}/{orderId}` |
| Locations | `GET/POST/PUT/DELETE /addresses`, `GET/POST /warehouses` |
| Users | `GET/POST/PUT/DELETE /users` |
| Mesh | `GET /mesh/stock/search` |

---

### Task 1: Enhanced API Client (`src/lib/api.js`)

**File:** `demo-ui/src/lib/api.js`

Replace the entire file with the enhanced version. Key changes: typed error classes, upload/download methods, retry-once for GET on network error, no retry for mutations.

- [ ] **Step 1: Write the enhanced API client**

```javascript
const API_BASE = '/api/v1';

// ── Key conversion utilities ──────────────────────────────────────

function toSnakeCase(str) {
  return str.replace(/[A-Z]/g, c => `_${c.toLowerCase()}`);
}

function toCamelCase(str) {
  return str.replace(/_([a-z])/g, (_, c) => c.toUpperCase());
}

function convertKeys(obj, fn) {
  if (obj === null || obj === undefined || typeof obj !== 'object') return obj;
  if (obj instanceof File) return obj;
  if (Array.isArray(obj)) return obj.map(v => convertKeys(v, fn));
  return Object.fromEntries(
    Object.entries(obj).map(([k, v]) => [fn(k), convertKeys(v, fn)])
  );
}

// ── Typed error classes ───────────────────────────────────────────

export class ApiError extends Error {
  constructor(status, code, message, details = {}) {
    super(message || `Request failed with status ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export class AuthError extends ApiError {
  constructor(message = 'Authentication required') {
    super(401, 'UNAUTHORIZED', message);
    this.name = 'AuthError';
  }
}

export class ForbiddenError extends ApiError {
  constructor(message = 'Access denied') {
    super(403, 'FORBIDDEN', message);
    this.name = 'ForbiddenError';
  }
}

export class NotFoundError extends ApiError {
  constructor(message = 'Resource not found') {
    super(404, 'NOT_FOUND', message);
    this.name = 'NotFoundError';
  }
}

export class ValidationError extends ApiError {
  constructor(message = 'Validation failed', details = {}) {
    super(400, 'BAD_REQUEST', message, details);
    this.name = 'ValidationError';
  }
}

export class ServerError extends ApiError {
  constructor(status, message = 'Server error') {
    super(status, 'SERVER_ERROR', message);
    this.name = 'ServerError';
  }
}

// ── Error factory ─────────────────────────────────────────────────

function createErrorFromResponse(status, body) {
  const message = body?.message || `Request failed: ${status}`;
  switch (status) {
    case 400: return new ValidationError(message, body);
    case 401: return new AuthError(message);
    case 403: return new ForbiddenError(message);
    case 404: return new NotFoundError(message);
    default:
      if (status >= 500) return new ServerError(status, message);
      return new ApiError(status, body?.code, message, body);
  }
}

// ── API Client ────────────────────────────────────────────────────

class ApiClient {
  constructor() {
    this.token = localStorage.getItem('jwt_token');
  }

  setToken(token) {
    this.token = token;
    localStorage.setItem('jwt_token', token);
  }

  clearToken() {
    this.token = null;
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('store_id');
  }

  getToken() {
    return this.token;
  }

  async request(path, options = {}) {
    const headers = { ...options.headers };
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    let body = options.body;
    const isJson = headers['Content-Type'] !== false;
    if (body && isJson) {
      if (!headers['Content-Type']) headers['Content-Type'] = 'application/json';
      const toSend = typeof body === 'string' ? JSON.parse(body) : body;
      body = JSON.stringify(convertKeys(toSend, toSnakeCase));
    }

    const res = await fetch(`${API_BASE}${path}`, { ...options, headers, body });

    if (res.status === 401) {
      this.clearToken();
      throw new AuthError();
    }

    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw createErrorFromResponse(res.status, errBody);
    }

    const contentType = res.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
      return res;
    }

    const json = await res.json();
    const normalized = Array.isArray(json) ? { data: json } : json;
    return convertKeys(normalized, toCamelCase);
  }

  async get(path, retry = true) {
    try {
      return await this.request(path);
    } catch (err) {
      if (retry && (err instanceof TypeError)) {
        // Network error: retry once
        return await this.request(path);
      }
      throw err;
    }
  }

  post(path, body) {
    return this.request(path, { method: 'POST', body });
  }

  put(path, body) {
    return this.request(path, { method: 'PUT', body });
  }

  patch(path, body) {
    return this.request(path, { method: 'PATCH', body });
  }

  del(path) {
    return this.request(path, { method: 'DELETE' });
  }

  async upload(path, file) {
    const formData = new FormData();
    formData.append('file', file);

    const headers = {};
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }
    // Do NOT set Content-Type; browser sets multipart boundary automatically

    const res = await fetch(`${API_BASE}${path}`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw createErrorFromResponse(res.status, errBody);
    }

    const contentType = res.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
      return res;
    }

    const json = await res.json();
    const normalized = Array.isArray(json) ? { data: json } : json;
    return convertKeys(normalized, toCamelCase);
  }

  async download(path) {
    const headers = {};
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    const res = await fetch(`${API_BASE}${path}`, { headers });

    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw createErrorFromResponse(res.status, errBody);
    }

    const contentDisposition = res.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    const filename = filenameMatch ? filenameMatch[1].replace(/['"]/g, '') : 'download';

    const blob = await res.blob();
    return { blob, filename };
  }
}

const api = new ApiClient();
export default api;
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 2: Auth Context Provider (`src/lib/auth.jsx`)

**File:** `demo-ui/src/lib/auth.jsx`

Create the centralized auth context. State machine: idle -> loading -> (authenticated | mfa_required -> verifying -> authenticated). Persists token + storeId in localStorage, user in memory only.

- [ ] **Step 1: Write the AuthProvider**

```jsx
import { createContext, useContext, useState, useCallback, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import api from './api';

const AuthContext = createContext(null);

const TOKEN_KEY = 'jwt_token';
const STORE_ID_KEY = 'store_id';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const [storeId, setStoreIdState] = useState(() => localStorage.getItem(STORE_ID_KEY));
  const [token, setTokenState] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [isMfaRequired, setIsMfaRequired] = useState(false);
  const [tempToken, setTempToken] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const setToken = useCallback((newToken) => {
    setTokenState(newToken);
    if (newToken) {
      api.setToken(newToken);
      localStorage.setItem(TOKEN_KEY, newToken);
    } else {
      api.clearToken();
      localStorage.removeItem(TOKEN_KEY);
    }
  }, []);

  const setStoreId = useCallback((id) => {
    setStoreIdState(id);
    if (id) {
      localStorage.setItem(STORE_ID_KEY, id);
    } else {
      localStorage.removeItem(STORE_ID_KEY);
    }
  }, []);

  const clearAuth = useCallback(() => {
    setUser(null);
    setRole(null);
    setStoreId(null);
    setToken(null);
    setIsMfaRequired(false);
    setTempToken(null);
    setError(null);
  }, [setToken, setStoreId]);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });
      const payload = res.data;

      if (payload?.mfaRequired || payload?.tempToken) {
        setIsMfaRequired(true);
        setTempToken(payload.tempToken);
        return { mfaRequired: true };
      }

      if (payload?.accessToken) {
        setToken(payload.accessToken);
        setRole(payload.role || null);
        setStoreId(payload.storeId || null);
        return { success: true };
      }

      setError('Unexpected response format');
      return { success: false };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, [setToken, setStoreId]);

  const verifyMfa = useCallback(async (mfaTempToken, totpCode) => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.request('/auth/verify-mfa', {
        method: 'POST',
        body: JSON.stringify({ tempToken: mfaTempToken, totpCode }),
      });
      const payload = res.data;

      if (payload?.accessToken) {
        setToken(payload.accessToken);
        setRole(payload.role || null);
        setStoreId(payload.storeId || null);
        setIsMfaRequired(false);
        setTempToken(null);
        return { success: true };
      }

      setError('Unexpected MFA response');
      return { success: false };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, [setToken, setStoreId]);

  const register = useCallback(async (username, password) => {
    setLoading(true);
    setError(null);
    try {
      await api.request('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });
      return { success: true };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    clearAuth();
  }, [clearAuth]);

  const selectStore = useCallback(async (selectedStoreId) => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.post('/auth/select-store', { storeId: selectedStoreId });
      const payload = res.data;
      setStoreId(payload.storeId || selectedStoreId);
      setRole(payload.role || role);
      return { success: true };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, [setStoreId, role]);

  const setupMfa = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.post('/auth/setup-mfa', {});
      return { success: true, data: res.data };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, []);

  const refreshProfile = useCallback(async () => {
    if (!token) return;
    try {
      const res = await api.get('/auth/me');
      const profile = res.data;
      setUser(profile);
      setRole(profile.role || role);
      return profile;
    } catch {
      // Token might be expired; clear auth
      clearAuth();
      return null;
    }
  }, [token, role, clearAuth]);

  // On mount, if we have a token, fetch the profile
  useEffect(() => {
    if (token) {
      api.setToken(token);
      refreshProfile();
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const value = useMemo(() => ({
    user,
    role,
    storeId,
    token,
    isMfaRequired,
    tempToken,
    loading,
    error,
    isAuthenticated: !!token,
    login,
    verifyMfa,
    register,
    logout,
    selectStore,
    setupMfa,
    refreshProfile,
  }), [
    user, role, storeId, token, isMfaRequired, tempToken, loading, error,
    login, verifyMfa, register, logout, selectStore, setupMfa, refreshProfile,
  ]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}

export function useHasRole(roles) {
  const { role } = useAuth();
  if (!role) return false;
  const roleArray = Array.isArray(roles) ? roles : [roles];
  return roleArray.includes(role);
}

export function useStoreId() {
  const { storeId } = useAuth();
  return storeId;
}

export function RequireRole({ roles, children, fallback = null }) {
  const hasRole = useHasRole(roles);
  if (!hasRole) return fallback;
  return children;
}

export function StoreSelectionGate({ children }) {
  const { storeId, isAuthenticated } = useAuth();
  if (!isAuthenticated) return null;
  if (!storeId) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        backgroundColor: 'var(--color-background)',
      }}>
        <div style={{
          textAlign: 'center',
          padding: 'var(--space-8)',
          backgroundColor: 'var(--color-surface)',
          borderRadius: 'var(--radius-lg)',
          boxShadow: 'var(--shadow-md)',
        }}>
          <h2 style={{ fontSize: 'var(--text-xl)', fontWeight: 'var(--font-semibold)', marginBottom: 'var(--space-4)', color: 'var(--color-text-primary)' }}>
            Select a Store
          </h2>
          <p style={{ fontSize: 'var(--text-sm)', color: 'var(--color-text-secondary)', marginBottom: 'var(--space-6)' }}>
            Please select a store to continue.
          </p>
          <StorePicker />
        </div>
      </div>
    );
  }
  return children;
}

function StorePicker() {
  const { selectStore } = useAuth();
  const [stores, setStores] = useState([]);
  const [loadingStores, setLoadingStores] = useState(true);

  useEffect(() => {
    async function loadStores() {
      try {
        const res = await api.get('/clinical/stores?page=1&size=100');
        const raw = res.data;
        const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
        setStores(items);
      } catch {
        // ignore
      } finally {
        setLoadingStores(false);
      }
    }
    loadStores();
  }, []);

  const handleSelect = async (e) => {
    const id = e.target.value;
    if (id) {
      await selectStore(id);
    }
  };

  if (loadingStores) return <p>Loading stores...</p>;

  return (
    <select
      onChange={handleSelect}
      defaultValue=""
      style={{
        padding: 'var(--space-2) var(--space-4)',
        borderRadius: 'var(--radius-md)',
        border: '1px solid var(--color-border)',
        fontSize: 'var(--text-sm)',
        minWidth: '200px',
      }}
    >
      <option value="" disabled>Choose a store...</option>
      {stores.map((s) => (
        <option key={s.id} value={s.id}>{s.name}</option>
      ))}
    </select>
  );
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 3: Auth Hooks (`src/lib/hooks/useAuth.js`)

**File:** `demo-ui/src/lib/hooks/useAuth.js`

React Query hooks for auth operations. These hooks wrap the AuthProvider actions for use in components that need React Query integration (loading states, error handling, cache invalidation).

- [ ] **Step 1: Write auth hooks**

```javascript
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../auth';
import api from '../api';

const AUTH_KEY = ['auth'];

export function useCurrentUser() {
  const { token } = useAuth();

  return useQuery({
    queryKey: [...AUTH_KEY, 'me'],
    queryFn: async () => {
      const res = await api.get('/auth/me');
      return res.data;
    },
    enabled: !!token,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

export function useLogin() {
  const { login } = useAuth();

  return {
    mutateAsync: async ({ username, password }) => {
      return login(username, password);
    },
    mutate: async ({ username, password }, options) => {
      const result = await login(username, password);
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false, // AuthProvider manages its own loading state
    error: null,
  };
}

export function useVerifyMfa() {
  const { verifyMfa } = useAuth();

  return {
    mutateAsync: async ({ tempToken, totpCode }) => {
      return verifyMfa(tempToken, totpCode);
    },
    mutate: async ({ tempToken, totpCode }, options) => {
      const result = await verifyMfa(tempToken, totpCode);
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false,
    error: null,
  };
}

export function useRegister() {
  const { register } = useAuth();

  return {
    mutateAsync: async ({ username, password }) => {
      return register(username, password);
    },
    mutate: async ({ username, password }, options) => {
      const result = await register(username, password);
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false,
    error: null,
  };
}

export function useSetupMfa() {
  const { setupMfa } = useAuth();

  return {
    mutateAsync: async () => {
      return setupMfa();
    },
    mutate: async (_vars, options) => {
      const result = await setupMfa();
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false,
    error: null,
  };
}

export function useSelectStore() {
  const { selectStore } = useAuth();
  const queryClient = useQueryClient();

  return {
    mutateAsync: async (storeId) => {
      const result = await selectStore(storeId);
      if (result.success) {
        queryClient.invalidateQueries();
      }
      return result;
    },
    mutate: async (storeId, options) => {
      const result = await selectStore(storeId);
      if (result.success) {
        queryClient.invalidateQueries();
      }
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false,
    error: null,
  };
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 4: Product Hooks (`src/lib/hooks/useProducts.js`)

**File:** `demo-ui/src/lib/hooks/useProducts.js`

- [ ] **Step 1: Write product hooks**

```javascript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api';

const PRODUCTS_KEY = ['products'];
const HISTORIES_KEY = ['product-histories'];

export function useProductList(params = {}) {
  const { page = 1, size = 10, orderBy = 'ASC', ...rest } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });
  Object.entries(rest).forEach(([k, v]) => {
    if (v != null && v !== '') queryParams.set(k, v);
  });

  const result = useQuery({
    queryKey: [...PRODUCTS_KEY, params],
    queryFn: () => api.get(`/products?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return {
    ...result,
    loading: result.isLoading,
    items,
    totalPages,
    totalElements,
  };
}

export function useProductDetail(productId) {
  return useQuery({
    queryKey: [...PRODUCTS_KEY, productId],
    queryFn: async () => {
      const res = await api.get(`/products/${productId}`);
      return res.data;
    },
    enabled: !!productId,
  });
}

export function useCreateProduct() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/products', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PRODUCTS_KEY });
    },
  });
}

export function useUpdateProduct() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...body }) => api.put(`/products/${id}`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PRODUCTS_KEY });
    },
  });
}

export function useDeleteProduct() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => api.del(`/products/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PRODUCTS_KEY });
    },
  });
}

export function useUploadProducts() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (file) => api.upload('/products/upload', file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PRODUCTS_KEY });
    },
  });
}

export function useExportProducts() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (fileType = 'csv') => {
      return api.download(`/products/export?fileType=${fileType}`);
    },
    onSuccess: ({ blob, filename }) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    },
  });
}

export function useProductHistories(params = {}) {
  const { productCode, page = 1, size = 20, orderBy = 'ASC' } = params;
  const basePath = productCode
    ? `/products/histories/${productCode}`
    : '/products/histories';
  const queryParams = new URLSearchParams({ page, size, orderBy });

  const result = useQuery({
    queryKey: [...HISTORIES_KEY, params],
    queryFn: () => api.get(`${basePath}?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return {
    ...result,
    loading: result.isLoading,
    items,
    totalPages,
    totalElements,
  };
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 5: Inventory Hooks (`src/lib/hooks/useInventory.js`)

**File:** `demo-ui/src/lib/hooks/useInventory.js`

- [ ] **Step 1: Write inventory hooks**

```javascript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api';

const BATCHES_KEY = ['inventory', 'batches'];
const STORE_STOCK_KEY = ['inventory', 'store-stock'];
const TASKS_KEY = ['inventory', 'tasks'];
const CONVERSIONS_KEY = ['inventory', 'conversions'];

// ── Batch hooks ───────────────────────────────────────────────────

export function useBatchList(params = {}) {
  const { brandId, page = 1, size = 10, orderBy = 'ASC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });
  if (brandId) queryParams.set('brandId', brandId);

  const result = useQuery({
    queryKey: [...BATCHES_KEY, params],
    queryFn: () => api.get(`/inventory/batches?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useBatchDetail(batchId) {
  return useQuery({
    queryKey: [...BATCHES_KEY, batchId],
    queryFn: async () => {
      const res = await api.get(`/inventory/batches/${batchId}`);
      return res.data;
    },
    enabled: !!batchId,
  });
}

export function useStockIn() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/inventory/batches/stock-in', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: BATCHES_KEY });
      queryClient.invalidateQueries({ queryKey: STORE_STOCK_KEY });
    },
  });
}

export function useStockDeduct() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/inventory/stock/deduct', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: BATCHES_KEY });
      queryClient.invalidateQueries({ queryKey: STORE_STOCK_KEY });
    },
  });
}

export function useBarcodeResolve(barcode) {
  return useQuery({
    queryKey: ['inventory', 'barcode', barcode],
    queryFn: async () => {
      const res = await api.get(`/inventory/barcode/resolve?barcode=${encodeURIComponent(barcode)}`);
      return res.data;
    },
    enabled: !!barcode,
    staleTime: 60_000,
  });
}

export function useStoreStock(storeId, params = {}) {
  const { page = 1, size = 20, orderBy = 'DESC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });

  const result = useQuery({
    queryKey: [...STORE_STOCK_KEY, storeId, params],
    queryFn: () => api.get(`/inventory/stores/${storeId}/stock?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
    enabled: !!storeId,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

// ── Task hooks ────────────────────────────────────────────────────

export function useTaskList(params = {}) {
  const { storeId, status, page = 0, size = 20 } = params;
  const queryParams = new URLSearchParams({ page, size });
  if (storeId) queryParams.set('storeId', storeId);
  if (status) queryParams.set('status', status);

  const result = useQuery({
    queryKey: [...TASKS_KEY, params],
    queryFn: () => api.get(`/inventory/tasks?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useTaskDetail(taskId) {
  return useQuery({
    queryKey: [...TASKS_KEY, taskId],
    queryFn: async () => {
      const res = await api.get(`/inventory/tasks/${taskId}`);
      return res.data;
    },
    enabled: !!taskId,
  });
}

export function useAcknowledgeTask() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => api.patch(`/inventory/tasks/${id}/acknowledge`, {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TASKS_KEY });
    },
  });
}

export function useResolveTask() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => api.patch(`/inventory/tasks/${id}/resolve`, {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TASKS_KEY });
    },
  });
}

export function useTriggerScan() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.post('/inventory/tasks/scan', {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TASKS_KEY });
    },
  });
}

// ── Unit Conversion hooks ─────────────────────────────────────────

export function useUnitConversions(brandId) {
  return useQuery({
    queryKey: [...CONVERSIONS_KEY, brandId],
    queryFn: async () => {
      const res = await api.get(`/inventory/conversions?brandId=${brandId}`);
      return res.data;
    },
    enabled: !!brandId,
  });
}

export function useCreateConversion() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/inventory/conversions', body),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: [...CONVERSIONS_KEY, variables.brandId] });
    },
  });
}

export function useDeleteConversion() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => api.del(`/inventory/conversions/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CONVERSIONS_KEY });
    },
  });
}

// ── Recall hook ───────────────────────────────────────────────────

export function useRecallBatch() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (batchId) => api.post('/inventory/compliance/recall', { batchId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: BATCHES_KEY });
      queryClient.invalidateQueries({ queryKey: STORE_STOCK_KEY });
      queryClient.invalidateQueries({ queryKey: TASKS_KEY });
    },
  });
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 6: Clinical Hooks (`src/lib/hooks/useClinical.js`)

**File:** `demo-ui/src/lib/hooks/useClinical.js`

- [ ] **Step 1: Write clinical hooks**

```javascript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api';

const MOLECULES_KEY = ['clinical', 'molecules'];
const BRANDS_KEY = ['clinical', 'brands'];
const STORES_KEY = ['clinical', 'stores'];
const STORE_PRODUCTS_KEY = ['clinical', 'store-products'];

// ── Molecule hooks ────────────────────────────────────────────────

export function useMolecules(params = {}) {
  const { page = 1, size = 20, orderBy = 'ASC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });

  const result = useQuery({
    queryKey: [...MOLECULES_KEY, params],
    queryFn: () => api.get(`/clinical/catalog/molecules?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useMoleculeDetail(moleculeId) {
  return useQuery({
    queryKey: [...MOLECULES_KEY, moleculeId],
    queryFn: async () => {
      const res = await api.get(`/clinical/catalog/molecules/${moleculeId}`);
      return res.data;
    },
    enabled: !!moleculeId,
  });
}

export function useCreateMolecule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/clinical/catalog/molecules', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: MOLECULES_KEY });
    },
  });
}

export function useUpdateMolecule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...body }) => api.patch(`/clinical/catalog/molecules/${id}/metadata`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: MOLECULES_KEY });
    },
  });
}

export function useSearchMolecules(query) {
  return useQuery({
    queryKey: [...MOLECULES_KEY, 'search', query],
    queryFn: async () => {
      const res = await api.get(`/clinical/catalog/molecules/search?query=${encodeURIComponent(query)}`);
      return res.data;
    },
    enabled: !!query && query.length >= 2,
    staleTime: 30_000,
  });
}

// ── Brand hooks ───────────────────────────────────────────────────

export function useBrandsByMolecule(moleculeId) {
  return useQuery({
    queryKey: [...BRANDS_KEY, moleculeId],
    queryFn: async () => {
      const res = await api.get(`/clinical/catalog/molecules/${moleculeId}/brands`);
      return res.data;
    },
    enabled: !!moleculeId,
  });
}

export function useCreateBrand() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/clinical/catalog/brands', body),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: [...BRANDS_KEY, variables.moleculeId] });
    },
  });
}

// ── Store hooks ───────────────────────────────────────────────────

export function useStoreList(params = {}) {
  const { page = 1, size = 100, orderBy = 'ASC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });

  const result = useQuery({
    queryKey: [...STORES_KEY, params],
    queryFn: () => api.get(`/clinical/stores?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useStoreDetail(storeId) {
  return useQuery({
    queryKey: [...STORES_KEY, storeId],
    queryFn: async () => {
      const res = await api.get(`/clinical/stores/${storeId}`);
      return res.data;
    },
    enabled: !!storeId,
  });
}

export function useCreateStore() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/clinical/stores', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: STORES_KEY });
    },
  });
}

export function useUpdateStore() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...body }) => api.patch(`/clinical/stores/${id}`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: STORES_KEY });
    },
  });
}

export function useDeleteStore() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => api.del(`/clinical/stores/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: STORES_KEY });
    },
  });
}

// ── Store Product hooks ──────────────────────────────────────────

export function useStoreProducts(storeId, params = {}) {
  const { page = 1, size = 20, orderBy = 'ASC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });

  const result = useQuery({
    queryKey: [...STORE_PRODUCTS_KEY, storeId, params],
    queryFn: () => api.get(`/clinical/catalog/stores/${storeId}/products?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
    enabled: !!storeId,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useStoreProductDetail(storeId, productId) {
  return useQuery({
    queryKey: [...STORE_PRODUCTS_KEY, storeId, productId],
    queryFn: async () => {
      const res = await api.get(`/clinical/catalog/stores/${storeId}/products/${productId}`);
      return res.data;
    },
    enabled: !!storeId && !!productId,
  });
}

export function useActivateStoreProduct(storeId) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post(`/clinical/catalog/stores/${storeId}/products`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [...STORE_PRODUCTS_KEY, storeId] });
    },
  });
}

export function useUpdateStoreProduct(storeId) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ productId, ...body }) =>
      api.patch(`/clinical/catalog/stores/${storeId}/products/${productId}`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [...STORE_PRODUCTS_KEY, storeId] });
    },
  });
}

export function useDeactivateStoreProduct(storeId) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (productId) =>
      api.del(`/clinical/catalog/stores/${storeId}/products/${productId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [...STORE_PRODUCTS_KEY, storeId] });
    },
  });
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 7: Transaction Hooks (`src/lib/hooks/useTransactions.js`)

**File:** `demo-ui/src/lib/hooks/useTransactions.js`

- [ ] **Step 1: Write transaction hooks**

```javascript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api';

const INVOICES_KEY = ['transactions', 'invoices'];

export function useInvoiceList(params = {}) {
  const { storeId, page = 1, size = 20, orderBy = 'DESC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });
  if (storeId) queryParams.set('storeId', storeId);

  const result = useQuery({
    queryKey: [...INVOICES_KEY, params],
    queryFn: () => api.get(`/transaction/invoices?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useInvoiceDetail(invoiceId) {
  return useQuery({
    queryKey: [...INVOICES_KEY, invoiceId],
    queryFn: async () => {
      const res = await api.get(`/transaction/invoices/${invoiceId}`);
      return res.data;
    },
    enabled: !!invoiceId,
  });
}

export function useCreateInvoice() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/transaction/invoices', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: INVOICES_KEY });
    },
  });
}

export function useCompleteInvoice() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (invoiceId) => api.post(`/transaction/invoices/${invoiceId}/complete`, {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: INVOICES_KEY });
    },
  });
}

export function useVoidInvoice() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (invoiceId) => api.post(`/transaction/invoices/${invoiceId}/void`, {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: INVOICES_KEY });
      queryClient.invalidateQueries({ queryKey: ['inventory', 'batches'] });
      queryClient.invalidateQueries({ queryKey: ['inventory', 'store-stock'] });
    },
  });
}

export function useQuickDispense() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/transaction/invoices/dispense', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: INVOICES_KEY });
      queryClient.invalidateQueries({ queryKey: ['inventory', 'batches'] });
      queryClient.invalidateQueries({ queryKey: ['inventory', 'store-stock'] });
    },
  });
}

export function useDailySummary(storeId, date) {
  const params = new URLSearchParams();
  if (storeId) params.set('storeId', storeId);
  if (date) params.set('date', date);

  return useQuery({
    queryKey: [...INVOICES_KEY, 'daily-summary', storeId, date],
    queryFn: async () => {
      const res = await api.get(`/transaction/invoices/daily-summary?${params.toString()}`);
      return res.data;
    },
    enabled: !!storeId,
    staleTime: 60_000,
  });
}

export function useReceipt(invoiceId) {
  return useQuery({
    queryKey: [...INVOICES_KEY, invoiceId, 'receipt'],
    queryFn: async () => {
      const res = await api.get(`/transaction/invoices/${invoiceId}/receipt`);
      return res.data;
    },
    enabled: !!invoiceId,
  });
}

export function useReconciliation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/transaction/reports/reconciliation', body),
    // Reconciliation reports are not cached; no invalidation needed
  });
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 8: Order Hooks (`src/lib/hooks/useOrders.js`)

**File:** `demo-ui/src/lib/hooks/useOrders.js`

- [ ] **Step 1: Write order hooks**

```javascript
import { useQuery } from '@tanstack/react-query';
import api from '../api';

const ORDERS_KEY = ['orders'];

export function useOrderList(userId, params = {}) {
  const { page = 1, size = 20, orderBy = 'ASC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });

  const result = useQuery({
    queryKey: [...ORDERS_KEY, userId, params],
    queryFn: () => api.get(`/orders/users/${userId}?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
    enabled: !!userId,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useOrderDetail(userId, orderId) {
  return useQuery({
    queryKey: [...ORDERS_KEY, userId, orderId],
    queryFn: async () => {
      const res = await api.get(`/orders/users/${userId}/${orderId}`);
      return res.data;
    },
    enabled: !!userId && !!orderId,
  });
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 9: Location Hooks (`src/lib/hooks/useLocations.js`)

**File:** `demo-ui/src/lib/hooks/useLocations.js`

- [ ] **Step 1: Write location hooks**

```javascript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api';

const ADDRESSES_KEY = ['addresses'];
const WAREHOUSES_KEY = ['warehouses'];

// ── Address hooks ─────────────────────────────────────────────────

export function useAddressList(params = {}) {
  const { page = 1, size = 20, orderBy = 'ASC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });

  const result = useQuery({
    queryKey: [...ADDRESSES_KEY, params],
    queryFn: () => api.get(`/addresses?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useAddressDetail(addressId) {
  return useQuery({
    queryKey: [...ADDRESSES_KEY, addressId],
    queryFn: async () => {
      const res = await api.get(`/addresses/${addressId}`);
      return res.data;
    },
    enabled: !!addressId,
  });
}

export function useCreateAddress() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/addresses', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ADDRESSES_KEY });
    },
  });
}

export function useUpdateAddress() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...body }) => api.put(`/addresses/${id}`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ADDRESSES_KEY });
    },
  });
}

export function useDeleteAddress() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => api.del(`/addresses/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ADDRESSES_KEY });
    },
  });
}

// ── Warehouse hooks ───────────────────────────────────────────────

export function useWarehouseList(params = {}) {
  const { page = 1, size = 20, orderBy = 'ASC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });

  const result = useQuery({
    queryKey: [...WAREHOUSES_KEY, params],
    queryFn: () => api.get(`/warehouses?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useWarehouseDetail(warehouseId) {
  return useQuery({
    queryKey: [...WAREHOUSES_KEY, warehouseId],
    queryFn: async () => {
      const res = await api.get(`/warehouses/${warehouseId}`);
      return res.data;
    },
    enabled: !!warehouseId,
  });
}

export function useCreateWarehouse() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/warehouses', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: WAREHOUSES_KEY });
      queryClient.invalidateQueries({ queryKey: ADDRESSES_KEY });
    },
  });
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 10: User Hooks (`src/lib/hooks/useUsers.js`)

**File:** `demo-ui/src/lib/hooks/useUsers.js`

- [ ] **Step 1: Write user hooks**

```javascript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api';

const USERS_KEY = ['users'];

export function useUserList(params = {}) {
  const { page = 1, size = 20, orderBy = 'ASC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });

  const result = useQuery({
    queryKey: [...USERS_KEY, params],
    queryFn: () => api.get(`/users?${queryParams.toString()}`),
    placeholderData: (prev) => prev,
  });

  const raw = result.data?.data;
  const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
  const totalPages = raw?.totalPages ?? 1;
  const totalElements = raw?.totalElements ?? items.length;

  return { ...result, loading: result.isLoading, items, totalPages, totalElements };
}

export function useUserDetail(userId) {
  return useQuery({
    queryKey: [...USERS_KEY, userId],
    queryFn: async () => {
      const res = await api.get(`/users/${userId}`);
      return res.data;
    },
    enabled: !!userId,
  });
}

export function useCreateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body) => api.post('/users', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: USERS_KEY });
    },
  });
}

export function useUpdateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...body }) => api.put(`/users/${id}/general`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: USERS_KEY });
    },
  });
}

export function useDeleteUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => api.del(`/users/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: USERS_KEY });
    },
  });
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 11: Mesh Hook (`src/lib/hooks/useMesh.js`)

**File:** `demo-ui/src/lib/hooks/useMesh.js`

- [ ] **Step 1: Write mesh hook**

```javascript
import { useQuery } from '@tanstack/react-query';
import api from '../api';

export function useMeshStockSearch(params = {}) {
  const { moleculeId, genericName, requestingStoreId } = params;
  const queryParams = new URLSearchParams();
  if (moleculeId) queryParams.set('moleculeId', moleculeId);
  if (genericName) queryParams.set('genericName', genericName);
  if (requestingStoreId) queryParams.set('requestingStoreId', requestingStoreId);

  return useQuery({
    queryKey: ['mesh', 'stock', 'search', params],
    queryFn: async () => {
      const res = await api.get(`/mesh/stock/search?${queryParams.toString()}`);
      return res.data;
    },
    enabled: !!(moleculeId || genericName),
    staleTime: 30_000,
  });
}
```

**Verification:** `cd demo-ui && npm run build`

---

### Task 12: Create the hooks directory and index file

**File:** `demo-ui/src/lib/hooks/index.js` (optional barrel file)

This is a convenience re-export file so consumers can `import { useProductList } from '../lib/hooks'` if desired. Individual imports from the specific file are also valid.

- [ ] **Step 1: Write the barrel file**

```javascript
export * from './useAuth';
export * from './useProducts';
export * from './useInventory';
export * from './useClinical';
export * from './useTransactions';
export * from './useOrders';
export * from './useLocations';
export * from './useUsers';
export * from './useMesh';
```

**Verification:** `cd demo-ui && npm run build`

---

## Integration Notes

After all files are created, the consuming agent (Plans 5-6 for page rebuilds) should:

1. **Wrap the app with AuthProvider** in `App.jsx` or `main.jsx`:
   ```jsx
   import { AuthProvider } from './lib/auth';
   // ...
   <AuthProvider>
     <QueryClientProvider client={queryClient}>
       <App />
     </QueryClientProvider>
   </AuthProvider>
   ```

2. **Replace `api.token` checks** with `useAuth().isAuthenticated` from the AuthProvider.

3. **Replace the old `hooks.js` imports** with the new domain-specific imports:
   ```jsx
   // Old:
   import { useQueryList } from '../lib/hooks';
   // New:
   import { useProductList } from '../lib/hooks/useProducts';
   ```

4. **Keep `src/lib/hooks.js`** for backward compatibility until all pages are migrated, then delete it.

5. **StoreSelectionGate** should wrap the AdminLayout route so users must pick a store before accessing store-scoped data.

## Verification Summary

After every task, run:
```bash
cd demo-ui && npm run build
```

After all tasks are complete, verify:
- All 11 hook files exist in `demo-ui/src/lib/hooks/`
- `demo-ui/src/lib/auth.jsx` exists with AuthProvider export
- `demo-ui/src/lib/api.js` has been replaced with the enhanced version
- Build succeeds with zero errors
