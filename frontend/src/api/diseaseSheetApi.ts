import { apiClient } from './httpClient';
import type { ApiResponse, DiseaseSheetCreateRequest, DiseaseSheetFilters, DiseaseSheetResponse, DiseaseSheetUpdateRequest, PageResponse } from '../types/api';

export const diseaseSheetApi = {
  async createDiseaseSheet(payload: DiseaseSheetCreateRequest) {
    const { data } = await apiClient.post<ApiResponse<DiseaseSheetResponse>>('/api/v1/disease-sheets', payload);
    return data.data;
  },
  async getDiseaseSheets(filters: DiseaseSheetFilters = {}) {
    const { data } = await apiClient.get<ApiResponse<PageResponse<DiseaseSheetResponse>>>('/api/v1/disease-sheets', { params: filters });
    return data.data;
  },
  async getDiseaseSheetById(id: string) {
    const { data } = await apiClient.get<ApiResponse<DiseaseSheetResponse>>(`/api/v1/disease-sheets/${id}`);
    return data.data;
  },
  async updateDiseaseSheet(id: string, payload: DiseaseSheetUpdateRequest) {
    const { data } = await apiClient.put<ApiResponse<DiseaseSheetResponse>>(`/api/v1/disease-sheets/${id}`, payload);
    return data.data;
  },
};
