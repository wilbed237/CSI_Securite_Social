import { apiClient } from './httpClient';
import type { ApiResponse, PageResponse, PrescriptionCreateRequest, PrescriptionFilters, PrescriptionResponse, PrescriptionUpdateRequest } from '../types/api';

export const prescriptionApi = {
  async createPrescription(payload: PrescriptionCreateRequest) {
    const { data } = await apiClient.post<ApiResponse<PrescriptionResponse>>('/api/v1/prescriptions', payload);
    return data.data;
  },
  async getPrescriptions(filters: PrescriptionFilters = {}) {
    const { data } = await apiClient.get<ApiResponse<PageResponse<PrescriptionResponse>>>('/api/v1/prescriptions', { params: filters });
    return data.data;
  },
  async getPrescriptionById(id: string) {
    const { data } = await apiClient.get<ApiResponse<PrescriptionResponse>>(`/api/v1/prescriptions/${id}`);
    return data.data;
  },
  async updatePrescription(id: string, payload: PrescriptionUpdateRequest) {
    const { data } = await apiClient.put<ApiResponse<PrescriptionResponse>>(`/api/v1/prescriptions/${id}`, payload);
    return data.data;
  },
};
