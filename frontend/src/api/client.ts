import axios from 'axios';
import i18n from '../i18n';

let accessToken: string | null = null;
let refreshPromise: Promise<string> | null = null;

export function setAccessToken(token: string | null): void {
  accessToken = token;
}

export function getAccessToken(): string | null {
  return accessToken;
}

export const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  // Lokalisierte Server-Meldungen in der aktuellen UI-Sprache (ADR-018 Kapitel 8).
  config.headers['Accept-Language'] = i18n.resolvedLanguage ?? i18n.language ?? 'de';
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config as typeof error.config & { _retry?: boolean };
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      try {
        if (!refreshPromise) {
          refreshPromise = axios
            .post<{ accessToken: string }>('/api/auth/refresh', {}, { withCredentials: true })
            .then((res) => {
              accessToken = res.data.accessToken;
              refreshPromise = null;
              return res.data.accessToken;
            })
            .catch((err) => {
              refreshPromise = null;
              accessToken = null;
              window.dispatchEvent(new Event('auth:expired'));
              throw err;
            });
        }
        const newToken = await refreshPromise;
        original.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(original);
      } catch {
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  },
);
