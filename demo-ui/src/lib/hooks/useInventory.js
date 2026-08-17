import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api';

const BATCHES_KEY = ['inventory', 'batches'];
const STORE_STOCK_KEY = ['inventory', 'store-stock'];
const TASKS_KEY = ['inventory', 'tasks'];
const CONVERSIONS_KEY = ['inventory', 'conversions'];

function parsePaginatedResponse(payload) {
  const items = Array.isArray(payload) ? payload : (payload?.content ?? []);
  const totalPages = payload?.totalPages ?? 1;
  const totalElements = payload?.totalElements ?? items.length;
  return { items, totalPages, totalElements };
}

// ── Batch hooks ───────────────────────────────────────────────────

export function useBatchList(params = {}) {
  const { brandId, page = 1, size = 10, orderBy = 'ASC' } = params;
  const queryParams = new URLSearchParams({ page, size, orderBy });
  if (brandId) queryParams.set('brandId', brandId);

  const result = useQuery({
    queryKey: [...BATCHES_KEY, params],
    queryFn: async () => (await api.get(`/inventory/batches?${queryParams.toString()}`)).data,
    placeholderData: (prev) => prev,
  });

  const { items, totalPages, totalElements } = parsePaginatedResponse(result.data);

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
    queryFn: async () => (await api.get(`/inventory/stores/${storeId}/stock?${queryParams.toString()}`)).data,
    placeholderData: (prev) => prev,
    enabled: !!storeId,
  });

  const { items, totalPages, totalElements } = parsePaginatedResponse(result.data);

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
    queryFn: async () => (await api.get(`/inventory/tasks?${queryParams.toString()}`)).data,
    placeholderData: (prev) => prev,
  });

  const { items, totalPages, totalElements } = parsePaginatedResponse(result.data);

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

// ── Admin hooks ───────────────────────────────────────────────────────

export function useTriggerReconcile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.post('/inventory/admin/reconcile', {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TASKS_KEY });
    },
  });
}

export function useReconcileStatus() {
  return useQuery({
    queryKey: ['inventory', 'reconcile-status'],
    queryFn: async () => {
      const res = await api.get('/inventory/admin/reconcile/status');
      return res.data;
    },
    refetchInterval: (query) => {
      const status = query?.state?.data?.status;
      return status === 'PROCESSING' ? 10000 : false;
    },
  });
}
