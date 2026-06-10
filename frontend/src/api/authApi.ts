import { apiClient } from './httpClient';
import type { ApiResponse, AuthResponse, RoleName, UserResponse } from '../types/api';

export interface LoginPayload { identifier: string; password: string }
export interface RegisterPayload {
  username: string;
  email: string;
  phoneNumber?: string;
  password: string;
  roles: RoleName[];
  actorType?: 'SOCIAL_AGENT' | 'DOCTOR';
  doctorType?: 'GENERALIST' | 'SPECIALIST';
  specialty?: string;
  firstName?: string;
  lastName?: string;
}

export const authApi = {
  async login(payload: LoginPayload) {
    const { data } = await apiClient.post<ApiResponse<AuthResponse>>('/api/v1/auth/login', payload);
    return data.data;
  },
  async register(payload: RegisterPayload) {
    const { data } = await apiClient.post<ApiResponse<UserResponse>>('/api/v1/auth/register', payload);
    return data.data;
  },
};
