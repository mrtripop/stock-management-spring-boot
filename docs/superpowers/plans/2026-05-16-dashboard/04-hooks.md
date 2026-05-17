### Task 4: React Query Hooks

**Files:**
- Rewrite: `demo-ui/src/lib/hooks.js`

- [ ] **Step 1: Rewrite hooks.js with React Query wrappers**

Replace `demo-ui/src/lib/hooks.js`:

```jsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import api from './api'

/**
 * Paginated list query. Returns { data, loading, error, items, totalPages, totalElements }.
 * @param {string[]} queryKey - e.g. ['products', { search, page }]
 * @param {string} url - e.g. '/products'
 * @param {object} params - { page, size, orderBy, search, ...filters }
 * @param {object} options - useQuery options, e.g. { enabled: false }
 */
export function useQueryList(queryKey, url, params = {}, options = {}) {
  const { page = 1, size = 10, orderBy = 'DESC', ...rest } = params
  const queryParams = new URLSearchParams({ page, size, orderBy })
  Object.entries(rest).forEach(([k, v]) => { if (v != null && v !== '') queryParams.set(k, v) })
  const fullUrl = `${url}?${queryParams.toString()}`

  const result = useQuery({
    queryKey: [...queryKey, params],
    queryFn: () => api.get(fullUrl),
    placeholderData: (prev) => prev,
    ...options,
  })

  const raw = result.data?.data
  const items = Array.isArray(raw) ? raw : (raw?.content ?? [])
  const totalPages = raw?.totalPages ?? 1
  const totalElements = raw?.totalElements ?? items.length

  return {
    ...result,
    loading: result.isLoading,
    items,
    totalPages,
    totalElements,
  }
}

/**
 * Single item query.
 * @param {string[]} queryKey - e.g. ['products', id]
 * @param {string} url - e.g. '/products'
 * @param {string|number} id
 */
export function useQueryDetail(queryKey, url, id) {
  return useQuery({
    queryKey: [...queryKey, id],
    queryFn: () => api.get(`${url}/${id}`),
    enabled: !!id,
  })
}

/**
 * POST mutation with automatic cache invalidation.
 * @param {string[]} queryKey - base key to invalidate, e.g. ['products']
 * @param {string} url - e.g. '/products'
 */
export function useCreateMutation(queryKey, url) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body) => api.post(url, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey })
    },
  })
}

/**
 * PUT mutation with automatic cache invalidation.
 * @param {string[]} queryKey - base key to invalidate
 * @param {string} url - e.g. '/products'
 */
export function useUpdateMutation(queryKey, url) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, ...body }) => api.put(`${url}/${id}`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey })
    },
  })
}

/**
 * DELETE mutation with automatic cache invalidation.
 * @param {string[]} queryKey - base key to invalidate
 * @param {string} url - e.g. '/products'
 */
export function useDeleteMutation(queryKey, url) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id) => api.del(`${url}/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey })
    },
  })
}

/**
 * Generic POST mutation (for custom endpoints like stock-in, deduct).
 * @param {string[]} queryKey - base key to invalidate
 * @param {string} url - full endpoint URL
 */
export function usePostMutation(queryKey, url) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body) => api.post(url, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey })
    },
  })
}

export { api }
```

- [ ] **Step 2: Verify no import errors**

Run: `cd demo-ui && npm run build`
Expected: Build succeeds. (Pages still reference old useApi — will be fixed in later tasks.)

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/lib/hooks.js
git commit -m "feat(demo-ui): replace useApi hook with React Query wrappers

Add useQueryList, useQueryDetail, useCreateMutation, useUpdateMutation,
useDeleteMutation, and usePostMutation hooks. All mutations auto-invalidate
related query caches. Keeps existing api.js client unchanged."
```
