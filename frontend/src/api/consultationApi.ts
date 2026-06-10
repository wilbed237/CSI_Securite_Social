import { apiClient } from './httpClient';
import type { ApiResponse, ConsultationCreateRequest, ConsultationFilters, ConsultationResponse, ConsultationUpdateRequest, PageResponse } from '../types/api';

export const consultationApi = {
  async createConsultation(payload: ConsultationCreateRequest) {
    const { data } = await apiClient.post<ApiResponse<ConsultationResponse>>('/api/v1/consultations', payload);
    return data.data;
  },
  async getConsultations(filters: ConsultationFilters = {}) {
    const { data } = await apiClient.get<ApiResponse<PageResponse<ConsultationResponse>>>('/api/v1/consultations', { params: filters });
    return data.data;
  },
  async getConsultationById(id: string) {
    const { data } = await apiClient.get<ApiResponse<ConsultationResponse>>(`/api/v1/consultations/${id}`);
    return data.data;
  },
  async updateConsultation(id: string, payload: ConsultationUpdateRequest) {
    const { data } = await apiClient.put<ApiResponse<ConsultationResponse>>(`/api/v1/consultations/${id}`, payload);
    return data.data;
  },
};
