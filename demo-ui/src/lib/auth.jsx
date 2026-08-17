import { createContext, useContext, useState, useCallback, useEffect, useMemo } from 'react';
import api from './api';

export const AuthContext = createContext(null);

const TOKEN_KEY = 'jwt_token';
const STORE_ID_KEY = 'store_id';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const [storeId, setStoreIdState] = useState(() => localStorage.getItem(STORE_ID_KEY));
  const [token, setTokenState] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [isMfaRequired, setIsMfaRequired] = useState(false);
  const [tempToken, setTempToken] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const setToken = useCallback((newToken) => {
    setTokenState(newToken);
    if (newToken) {
      api.setToken(newToken);
      localStorage.setItem(TOKEN_KEY, newToken);
    } else {
      api.clearToken();
      localStorage.removeItem(TOKEN_KEY);
    }
  }, []);

  const setStoreId = useCallback((id) => {
    setStoreIdState(id);
    if (id) {
      localStorage.setItem(STORE_ID_KEY, id);
    } else {
      localStorage.removeItem(STORE_ID_KEY);
    }
  }, []);

  const clearAuth = useCallback(() => {
    setUser(null);
    setRole(null);
    setStoreId(null);
    setToken(null);
    setIsMfaRequired(false);
    setTempToken(null);
    setError(null);
  }, [setToken, setStoreId]);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });
      const payload = res.data;

      if (payload?.mfaRequired) {
        setIsMfaRequired(true);
        setTempToken(payload.tempToken);
        return { mfaRequired: true, tempToken: payload.tempToken };
      }

      if (payload?.accessToken) {
        setToken(payload.accessToken);
        setRole(payload.role || null);
        setStoreId(payload.storeId || null);
        return { success: true };
      }

      setError('Unexpected response format');
      return { success: false };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, [setToken, setStoreId]);

  const verifyMfa = useCallback(async (mfaTempToken, totpCode) => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.request('/auth/verify-mfa', {
        method: 'POST',
        body: JSON.stringify({ tempToken: mfaTempToken, totpCode }),
      });
      const payload = res.data;

      if (payload?.accessToken) {
        setToken(payload.accessToken);
        setRole(payload.role || null);
        setStoreId(payload.storeId || null);
        setIsMfaRequired(false);
        setTempToken(null);
        return { success: true };
      }

      setError('Unexpected MFA response');
      return { success: false };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, [setToken, setStoreId]);

  const register = useCallback(async (username, password) => {
    setLoading(true);
    setError(null);
    try {
      await api.request('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });
      return { success: true };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    clearAuth();
  }, [clearAuth]);

  const selectStore = useCallback(async (selectedStoreId) => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.post('/auth/select-store', { storeId: selectedStoreId });
      const payload = res.data;
      setStoreId(payload.storeId || selectedStoreId);
      setRole(payload.role || role);
      return { success: true };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, [setStoreId, role]);

  const setupMfa = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.post('/auth/setup-mfa', {});
      return { success: true, data: res.data };
    } catch (err) {
      setError(err.message);
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, []);

  const refreshProfile = useCallback(async () => {
    if (!token) return;
    try {
      const res = await api.get('/auth/me');
      const profile = res.data;
      setUser(profile);
      setRole(profile.role || role);
      return profile;
    } catch {
      // Token might be expired; clear auth
      clearAuth();
      return null;
    }
  }, [token, role, clearAuth]);

  // On mount, if we have a token, fetch the profile
  useEffect(() => {
    if (token) {
      api.setToken(token);
      refreshProfile();
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const value = useMemo(() => ({
    user,
    role,
    storeId,
    token,
    isMfaRequired,
    tempToken,
    loading,
    error,
    isAuthenticated: !!token,
    login,
    verifyMfa,
    register,
    logout,
    selectStore,
    setupMfa,
    refreshProfile,
  }), [
    user, role, storeId, token, isMfaRequired, tempToken, loading, error,
    login, verifyMfa, register, logout, selectStore, setupMfa, refreshProfile,
  ]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}

export function useHasRole(roles) {
  const { role } = useAuth();
  if (!role) return false;
  const roleArray = Array.isArray(roles) ? roles : [roles];
  return roleArray.includes(role);
}

export function useStoreId() {
  const { storeId } = useAuth();
  return storeId;
}

export function RequireRole({ roles, children, fallback = null }) {
  const hasRole = useHasRole(roles);
  if (!hasRole) return fallback;
  return children;
}

export function StoreSelectionGate({ children }) {
  const { storeId, isAuthenticated } = useAuth();
  if (!isAuthenticated) return null;
  if (!storeId) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        backgroundColor: 'var(--color-background)',
      }}>
        <div style={{
          textAlign: 'center',
          padding: 'var(--space-8)',
          backgroundColor: 'var(--color-surface)',
          borderRadius: 'var(--radius-lg)',
          boxShadow: 'var(--shadow-md)',
        }}>
          <h2 style={{ fontSize: 'var(--text-xl)', fontWeight: 'var(--font-semibold)', marginBottom: 'var(--space-4)', color: 'var(--color-text-primary)' }}>
            Select a Store
          </h2>
          <p style={{ fontSize: 'var(--text-sm)', color: 'var(--color-text-secondary)', marginBottom: 'var(--space-6)' }}>
            Please select a store to continue.
          </p>
          <StorePicker />
        </div>
      </div>
    );
  }
  return children;
}

function StorePicker() {
  const { selectStore } = useAuth();
  const [stores, setStores] = useState([]);
  const [loadingStores, setLoadingStores] = useState(true);

  useEffect(() => {
    async function loadStores() {
      try {
        const res = await api.get('/clinical/stores?page=1&size=100');
        const raw = res.data;
        const items = Array.isArray(raw) ? raw : (raw?.content ?? []);
        setStores(items);
      } catch {
        // ignore
      } finally {
        setLoadingStores(false);
      }
    }
    loadStores();
  }, []);

  const handleSelect = async (e) => {
    const id = e.target.value;
    if (id) {
      await selectStore(id);
    }
  };

  if (loadingStores) return <p>Loading stores...</p>;

  return (
    <select
      onChange={handleSelect}
      defaultValue=""
      style={{
        padding: 'var(--space-2) var(--space-4)',
        borderRadius: 'var(--radius-md)',
        border: '1px solid var(--color-border)',
        fontSize: 'var(--text-sm)',
        minWidth: '200px',
      }}
    >
      <option value="" disabled>Choose a store...</option>
      {stores.map((s) => (
        <option key={s.id} value={s.id}>{s.name}</option>
      ))}
    </select>
  );
}
