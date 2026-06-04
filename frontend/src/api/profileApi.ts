import { apiClient } from './httpClient';
import type { ApiResponse, DoctorResponse, DoctorType, InsuredResponse, PageResponse } from '../types/api';

export interface CreateInsuredPayload {
  insuranceNumber: string; firstName: string; lastName: string; birthDate: string; address: string; phoneNumber?: string; email?: string;
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
  async createDoctor(payload: CreateDoctorPayload) {
    const { data } = await apiClient.post<ApiResponse<DoctorResponse>>('/api/v1/doctors', payload);
    return data.data;
  },
  async getDoctor(matricule: string) {
    const { data } = await apiClient.get<ApiResponse<DoctorResponse>>(`/api/v1/doctors/${matricule}`);
    return data.data;
  },
  async listDoctors(params: { type?: DoctorType; page?: number; size?: number }) {
    const { data } = await apiClient.get<ApiResponse<PageResponse<DoctorResponse>>>('/api/v1/doctors', { params });
    return data.data;
  },
};
