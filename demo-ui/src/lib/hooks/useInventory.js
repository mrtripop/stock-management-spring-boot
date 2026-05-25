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
