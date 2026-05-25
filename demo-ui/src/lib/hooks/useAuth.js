import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../auth';
import api from '../api';

const AUTH_KEY = ['auth'];

export function useCurrentUser() {
  const { token } = useAuth();

  return useQuery({
    queryKey: [...AUTH_KEY, 'me'],
    queryFn: async () => {
      const res = await api.get('/auth/me');
      return res.data;
    },
    enabled: !!token,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

export function useLogin() {
  const { login } = useAuth();

  return {
    mutateAsync: async ({ username, password }) => {
      return login(username, password);
    },
    mutate: async ({ username, password }, options) => {
      const result = await login(username, password);
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false, // AuthProvider manages its own loading state
    error: null,
  };
}

export function useVerifyMfa() {
  const { verifyMfa } = useAuth();

  return {
    mutateAsync: async ({ tempToken, totpCode }) => {
      return verifyMfa(tempToken, totpCode);
    },
    mutate: async ({ tempToken, totpCode }, options) => {
      const result = await verifyMfa(tempToken, totpCode);
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false,
    error: null,
  };
}

export function useRegister() {
  const { register } = useAuth();

  return {
    mutateAsync: async ({ username, password }) => {
      return register(username, password);
    },
    mutate: async ({ username, password }, options) => {
      const result = await register(username, password);
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false,
    error: null,
  };
}

export function useSetupMfa() {
  const { setupMfa } = useAuth();

  return {
    mutateAsync: async () => {
      return setupMfa();
    },
    mutate: async (_vars, options) => {
      const result = await setupMfa();
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false,
    error: null,
  };
}

export function useSelectStore() {
  const { selectStore } = useAuth();
  const queryClient = useQueryClient();

  return {
    mutateAsync: async (storeId) => {
      const result = await selectStore(storeId);
      if (result.success) {
        queryClient.invalidateQueries();
      }
      return result;
    },
    mutate: async (storeId, options) => {
      const result = await selectStore(storeId);
      if (result.success) {
        queryClient.invalidateQueries();
      }
      options?.onSuccess?.(result);
      return result;
    },
    isPending: false,
    error: null,
  };
}
