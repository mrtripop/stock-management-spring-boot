import { useState, useEffect, useCallback } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import api from './api'

// Temporary backward-compatible exports — will be removed when pages migrate to React Query
export function useApi(fetchFn, deps = []) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const fetch = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await fetchFn()
      setData(result)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, deps)

  useEffect(() => { fetch() }, [fetch])

  return { data, loading, error, refetch: fetch }
}

export function paginatedUrl(base, page = 1, size = 10) {
  return `${base}?page=${page}&size=${size}&orderBy=DESC`
}

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
