import { apiClient } from './httpClient';
import type { ApiResponse } from '../types/api';
import type { SettingsCategory, SettingsCategoryResponse, SettingResponse, UpsertSettingPayload } from '../types/settings';

export const settingsApi = {
  async listAll() {
    const { data } = await apiClient.get<ApiResponse<SettingsCategoryResponse[]>>('/api/v1/settings');
    return data.data;
  },
  async getCategory(category: SettingsCategory) {
    const { data } = await apiClient.get<ApiResponse<SettingsCategoryResponse>>(`/api/v1/settings/${category}`);
    return data.data;
  },
  async upsert(category: SettingsCategory, payload: UpsertSettingPayload) {
    const { data } = await apiClient.put<ApiResponse<SettingResponse>>(`/api/v1/settings/${category}`, payload);
    return data.data;
  },
};
