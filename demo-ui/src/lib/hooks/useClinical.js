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
