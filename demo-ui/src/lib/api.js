const API_BASE = '/api/v1';

function toSnakeCase(str) {
  return str.replace(/[A-Z]/g, c => `_${c.toLowerCase()}`);
}

function toCamelCase(str) {
  return str.replace(/_([a-z])/g, (_, c) => c.toUpperCase());
}

function convertKeys(obj, fn) {
  if (obj === null || obj === undefined || typeof obj !== 'object') return obj;
  if (Array.isArray(obj)) return obj.map(v => convertKeys(v, fn));
  return Object.fromEntries(
    Object.entries(obj).map(([k, v]) => [fn(k), convertKeys(v, fn)])
  );
}

class ApiClient {
  constructor() {
    this.token = localStorage.getItem('jwt_token');
  }

  setToken(token) {
    this.token = token;
    localStorage.setItem('jwt_token', token);
  }

  clearToken() {
    this.token = null;
    localStorage.removeItem('jwt_token');
  }

  async request(path, options = {}) {
    const headers = { 'Content-Type': 'application/json' };
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }
    let body = options.body;
    if (body) {
      body = JSON.stringify(convertKeys(JSON.parse(body), toSnakeCase));
    }
    const res = await fetch(`${API_BASE}${path}`, { ...options, headers, body });
    if (res.status === 401) {
      this.clearToken();
      window.location.href = '/login';
      throw new Error('Unauthorized');
    }
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || `Request failed: ${res.status}`);
    }
    const json = await res.json();
    const normalized = Array.isArray(json) ? { data: json } : json;
    return convertKeys(normalized, toCamelCase);
  }

  get(path) {
    return this.request(path);
  }

  post(path, body) {
    return this.request(path, { method: 'POST', body: JSON.stringify(body) });
  }

  put(path, body) {
    return this.request(path, { method: 'PUT', body: JSON.stringify(body) });
  }

  patch(path, body) {
    return this.request(path, { method: 'PATCH', body: JSON.stringify(body) });
  }

  del(path) {
    return this.request(path, { method: 'DELETE' });
  }
}

const api = new ApiClient();
export default api;
