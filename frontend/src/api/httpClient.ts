import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import toast from 'react-hot-toast';
import { env } from '../config/env';
import { useAuthStore } from '../store/authStore';
import type { ApiErrorResponse, ApiResponse, AuthResponse } from '../types/api';
import i18n from '../i18n';

export const apiClient = axios.create({
  baseURL: env.apiGatewayUrl,
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
});

let refreshPromise: Promise<AuthResponse> | null = null;

function isRefreshRequest(config: InternalAxiosRequestConfig): boolean {
  return config.url?.endsWith('/api/v1/auth/refresh') ?? false;
}

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

    if (
      status === 401
      && originalRequest
      && !originalRequest._retry
      && !isRefreshRequest(originalRequest)
      && useAuthStore.getState().refreshToken
    ) {
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
        toast.error(i18n.t('errors.sessionExpired'));
      }
    }

    if (status === 403) toast.error(i18n.t('errors.forbidden'));
    if (status === 404) toast.error(i18n.t('errors.notFound'));
    if (status && status >= 500) toast.error(i18n.t('errors.server'));

    return Promise.reject(error);
  },
);

export function extractApiError(error: unknown): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const details = error.response?.data?.details;
    const firstDetail = details ? Object.entries(details)[0] : undefined;
    if (firstDetail) {
      return `${firstDetail[0]} : ${firstDetail[1]}`;
    }
    return error.response?.data?.message ?? error.message;
  }
  return i18n.t('errors.unexpected');
}
