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
