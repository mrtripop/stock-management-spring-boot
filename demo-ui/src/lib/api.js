const API_BASE = '/api/v1';

// ── Key conversion utilities ──────────────────────────────────────

function toSnakeCase(str) {
  return str.replace(/[A-Z]/g, c => `_${c.toLowerCase()}`);
}

function toCamelCase(str) {
  return str.replace(/_([a-z])/g, (_, c) => c.toUpperCase());
}

function convertKeys(obj, fn) {
  if (obj === null || obj === undefined || typeof obj !== 'object') return obj;
  if (obj instanceof File) return obj;
  if (Array.isArray(obj)) return obj.map(v => convertKeys(v, fn));
  return Object.fromEntries(
    Object.entries(obj).map(([k, v]) => [fn(k), convertKeys(v, fn)])
  );
}

// ── Typed error classes ───────────────────────────────────────────

export class ApiError extends Error {
  constructor(status, code, message, details = {}) {
    super(message || `Request failed with status ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export class AuthError extends ApiError {
  constructor(message = 'Authentication required') {
    super(401, 'UNAUTHORIZED', message);
    this.name = 'AuthError';
  }
}

export class ForbiddenError extends ApiError {
  constructor(message = 'Access denied') {
    super(403, 'FORBIDDEN', message);
    this.name = 'ForbiddenError';
  }
}

export class NotFoundError extends ApiError {
  constructor(message = 'Resource not found') {
    super(404, 'NOT_FOUND', message);
    this.name = 'NotFoundError';
  }
}

export class ValidationError extends ApiError {
  constructor(message = 'Validation failed', details = {}) {
    super(400, 'BAD_REQUEST', message, details);
    this.name = 'ValidationError';
  }
}

export class ServerError extends ApiError {
  constructor(status, message = 'Server error') {
    super(status, 'SERVER_ERROR', message);
    this.name = 'ServerError';
  }
}

// ── Error factory ─────────────────────────────────────────────────

function createErrorFromResponse(status, body) {
  const message = body?.message || `Request failed: ${status}`;
  switch (status) {
    case 400: return new ValidationError(message, body);
    case 401: return new AuthError(message);
    case 403: return new ForbiddenError(message);
    case 404: return new NotFoundError(message);
    default:
      if (status >= 500) return new ServerError(status, message);
      return new ApiError(status, body?.code, message, body);
  }
}

// ── API Client ────────────────────────────────────────────────────

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
    localStorage.removeItem('store_id');
  }

  getToken() {
    return this.token;
  }

  async request(path, options = {}) {
    const headers = { ...options.headers };
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    let body = options.body;
    const isJson = headers['Content-Type'] !== false;
    if (body && isJson) {
      if (!headers['Content-Type']) headers['Content-Type'] = 'application/json';
      const toSend = typeof body === 'string' ? JSON.parse(body) : body;
      body = JSON.stringify(convertKeys(toSend, toSnakeCase));
    }

    const res = await fetch(`${API_BASE}${path}`, { ...options, headers, body });

    if (res.status === 401) {
      this.clearToken();
      throw new AuthError();
    }

    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw createErrorFromResponse(res.status, errBody);
    }

    const contentType = res.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
      return res;
    }

    const json = await res.json();
    const normalized = Array.isArray(json) ? { data: json } : json;
    return convertKeys(normalized, toCamelCase);
  }

  async get(path, retry = true) {
    try {
      return await this.request(path);
    } catch (err) {
      if (retry && (err instanceof TypeError)) {
        // Network error: retry once
        return await this.request(path);
      }
      throw err;
    }
  }

  post(path, body) {
    return this.request(path, { method: 'POST', body });
  }

  put(path, body) {
    return this.request(path, { method: 'PUT', body });
  }

  patch(path, body) {
    return this.request(path, { method: 'PATCH', body });
  }

  del(path) {
    return this.request(path, { method: 'DELETE' });
  }

  async upload(path, file) {
    const formData = new FormData();
    formData.append('file', file);

    const headers = {};
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }
    // Do NOT set Content-Type; browser sets multipart boundary automatically

    const res = await fetch(`${API_BASE}${path}`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw createErrorFromResponse(res.status, errBody);
    }

    const contentType = res.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
      return res;
    }

    const json = await res.json();
    const normalized = Array.isArray(json) ? { data: json } : json;
    return convertKeys(normalized, toCamelCase);
  }

  async download(path) {
    const headers = {};
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    const res = await fetch(`${API_BASE}${path}`, { headers });

    if (!res.ok) {
      const errBody = await res.json().catch(() => ({}));
      throw createErrorFromResponse(res.status, errBody);
    }

    const contentDisposition = res.headers.get('content-disposition') || '';
    const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    const filename = filenameMatch ? filenameMatch[1].replace(/['"]/g, '') : 'download';

    const blob = await res.blob();
    return { blob, filename };
  }
}

const api = new ApiClient();
export default api;
