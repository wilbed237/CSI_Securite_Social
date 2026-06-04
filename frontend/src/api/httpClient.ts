import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import toast from 'react-hot-toast';
import { env } from '../config/env';
import { useAuthStore } from '../store/authStore';
import type { ApiErrorResponse, ApiResponse, AuthResponse } from '../types/api';

export const apiClient = axios.create({
  baseURL: env.apiGatewayUrl,
  headers: { 'Content-Type': 'application/json' },
});

let refreshPromise: Promise<AuthResponse> | null = null;

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiErrorResponse>) => {
    const originalRequest = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    const status = error.response?.status;

    if (status === 401 && originalRequest && !originalRequest._retry && useAuthStore.getState().refreshToken) {
      originalRequest._retry = true;
      try {
        refreshPromise ??= apiClient
          .post<ApiResponse<AuthResponse>>('/api/v1/auth/refresh', { refreshToken: useAuthStore.getState().refreshToken })
          .then((response) => response.data.data)
          .finally(() => {
            refreshPromise = null;
          });
        const refreshed = await refreshPromise;
        useAuthStore.getState().setSession(refreshed);
        originalRequest.headers.Authorization = `Bearer ${refreshed.accessToken}`;
        return apiClient(originalRequest);
      } catch {
        useAuthStore.getState().clearSession();
        toast.error('Session expirée. Veuillez vous reconnecter.');
      }
    }

    if (status === 403) toast.error('Accès refusé pour votre rôle.');
    if (status === 404) toast.error('Ressource introuvable.');
    if (status && status >= 500) toast.error('Erreur serveur. Vérifiez que le backend est lancé.');

    return Promise.reject(error);
  },
);

export function extractApiError(error: unknown): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const details = error.response?.data?.details;
    const firstDetail = details ? Object.values(details)[0] : undefined;
    return firstDetail ?? error.response?.data?.message ?? error.message;
  }
  return 'Une erreur inattendue est survenue.';
}
