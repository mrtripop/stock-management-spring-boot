import { useQuery } from '@tanstack/react-query';
import api from '../api';

export function useMeshStockSearch(params = {}) {
  const { moleculeId, genericName, requestingStoreId } = params;
  const queryParams = new URLSearchParams();
  if (moleculeId) queryParams.set('moleculeId', moleculeId);
  if (genericName) queryParams.set('genericName', genericName);
  if (requestingStoreId) queryParams.set('requestingStoreId', requestingStoreId);

  return useQuery({
    queryKey: ['mesh', 'stock', 'search', params],
    queryFn: async () => {
      const res = await api.get(`/mesh/stock/search?${queryParams.toString()}`);
      return res.data;
    },
    enabled: !!(moleculeId || genericName),
    staleTime: 30_000,
  });
}
