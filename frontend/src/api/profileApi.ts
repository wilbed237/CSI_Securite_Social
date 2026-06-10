import { apiClient } from './httpClient';
import type { ApiResponse, DoctorResponse, DoctorType, InsuredResponse, InsuredStatus, PageResponse, SocialAgentResponse } from '../types/api';

export interface CreateInsuredPayload {
  insuranceNumber: string; firstName: string; lastName: string; birthDate: string; address: string; phoneNumber?: string; email?: string;
  countryCode?: string; preferredPaymentType?: 'CASH' | 'BANK_TRANSFER'; bankIban?: string;
}
export interface CreateDoctorPayload {
  matricule: string; firstName: string; lastName: string; type: DoctorType; specialty?: string; phoneNumber?: string; email?: string;
}

export const profileApi = {
  async createInsured(payload: CreateInsuredPayload) {
    const { data } = await apiClient.post<ApiResponse<InsuredResponse>>('/api/v1/insured', payload);
    return data.data;
  },
  async getInsured(insuranceNumber: string) {
    const { data } = await apiClient.get<ApiResponse<InsuredResponse>>(`/api/v1/insured/${insuranceNumber}`);
    return data.data;
  },
  async assignTreatingDoctor(insuranceNumber: string, doctorMatricule: string) {
    const { data } = await apiClient.put<ApiResponse<InsuredResponse>>(`/api/v1/insured/${insuranceNumber}/treating-doctor`, { doctorMatricule });
    return data.data;
  },
  async updateInsuredStatus(insuranceNumber: string, active: boolean) {
    const { data } = await apiClient.patch<ApiResponse<InsuredResponse>>(`/api/v1/insured/${insuranceNumber}/status`, { active });
    return data.data;
  },
  async primaryDoctorHistory(insuranceNumber: string) {
    const { data } = await apiClient.get<ApiResponse<Array<{ id: string; doctor: DoctorResponse; startedAt: string; endedAt?: string }>>>(`/api/v1/insured/${insuranceNumber}/primary-doctor-history`);
    return data.data;
  },
  async createDoctor(payload: CreateDoctorPayload) {
    const { data } = await apiClient.post<ApiResponse<DoctorResponse>>('/api/v1/doctors', payload);
    return data.data;
  },
  async getDoctor(matricule: string) {
    const { data } = await apiClient.get<ApiResponse<DoctorResponse>>(`/api/v1/doctors/${matricule}`);
    return data.data;
  },
  async listInsured(params: { status?: InsuredStatus; search?: string; page?: number; size?: number }) {
    const { data } = await apiClient.get<ApiResponse<PageResponse<InsuredResponse>>>('/api/v1/insured', { params });
    return data.data;
  },
  async listAssignedInsured(params: { page?: number; size?: number }) {
    const { data } = await apiClient.get<ApiResponse<PageResponse<InsuredResponse>>>('/api/v1/insured/assigned-to-me', { params });
    return data.data;
  },
  async listDoctors(params: { type?: DoctorType; active?: boolean; specialty?: string; page?: number; size?: number }) {
    const { data } = await apiClient.get<ApiResponse<PageResponse<DoctorResponse>>>('/api/v1/doctors', { params });
    return data.data;
  },
  async listAgents(params: { page?: number; size?: number }) {
    const { data } = await apiClient.get<ApiResponse<PageResponse<SocialAgentResponse>>>('/api/v1/agents', { params });
    return data.data;
  },
};
