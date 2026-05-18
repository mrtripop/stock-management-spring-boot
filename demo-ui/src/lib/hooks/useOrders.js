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
