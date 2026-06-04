import { apiClient } from './httpClient';
import type { ApiResponse, PaymentType, ReimbursementResponse } from '../types/api';

export interface CreateReimbursementPayload { sheetNumber: string; paymentType: PaymentType; bankIban?: string }

export const reimbursementApi = {
  async create(payload: CreateReimbursementPayload) {
    const { data } = await apiClient.post<ApiResponse<ReimbursementResponse>>('/api/v1/reimbursements', payload);
    return data.data;
  },
  async get(reimbursementNumber: string) {
    const { data } = await apiClient.get<ApiResponse<ReimbursementResponse>>(`/api/v1/reimbursements/${reimbursementNumber}`);
    return data.data;
  },
};
