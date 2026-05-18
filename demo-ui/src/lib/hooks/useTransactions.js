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
