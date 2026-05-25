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
