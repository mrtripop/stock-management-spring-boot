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
