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

export interface CreateDoctorAccountPayload {
  username: string;
  email: string;
  phoneNumber?: string;
  password: string;
  doctorType: 'GENERALIST' | 'SPECIALIST';
  specialty?: string;
  firstName: string;
  lastName: string;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

export interface UpdateAccountPayload {
  username: string;
  email: string;
  phoneNumber?: string;
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
  async changePassword(payload: ChangePasswordPayload) {
    const { data } = await apiClient.post<ApiResponse<void>>('/api/v1/auth/change-password', payload);
    return data.data;
  },
  async updateAccount(payload: UpdateAccountPayload) {
    const { data } = await apiClient.put<ApiResponse<void>>('/api/v1/auth/account', payload);
    return data.data;
  },
  async createDoctorAccount(payload: CreateDoctorAccountPayload) {
    const roles: RoleName[] = payload.doctorType === 'GENERALIST' ? ['DOCTOR', 'GENERALIST'] : ['DOCTOR', 'SPECIALIST'];
    return this.register({
      username: payload.username,
      email: payload.email,
      phoneNumber: payload.phoneNumber,
      password: payload.password,
      roles,
      actorType: 'DOCTOR',
      doctorType: payload.doctorType,
      specialty: payload.doctorType === 'SPECIALIST' ? payload.specialty : undefined,
      firstName: payload.firstName,
      lastName: payload.lastName,
    });
  },
};
