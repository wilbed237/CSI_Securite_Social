import { apiClient } from './httpClient';
import type { ApiResponse, PageResponse, PaymentType, ReimbursementResponse, ReimbursementStatus, ReimbursementType } from '../types/api';

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
  async calculate(reimbursementNumber: string) {
    const { data } = await apiClient.post<ApiResponse<ReimbursementResponse>>(`/api/v1/reimbursements/${reimbursementNumber}/calculate`);
    return data.data;
  },
  async approve(reimbursementNumber: string) {
    const { data } = await apiClient.patch<ApiResponse<ReimbursementResponse>>(`/api/v1/reimbursements/${reimbursementNumber}/approve`);
    return data.data;
  },
  async reject(reimbursementNumber: string, reason: string) {
    const { data } = await apiClient.patch<ApiResponse<ReimbursementResponse>>(`/api/v1/reimbursements/${reimbursementNumber}/reject`, { reason });
    return data.data;
  },
  async execute(reimbursementNumber: string) {
    const { data } = await apiClient.post<ApiResponse<ReimbursementResponse>>(`/api/v1/reimbursements/${reimbursementNumber}/execute`);
    return data.data;
  },
  async list(params: { status?: ReimbursementStatus; type?: ReimbursementType; startDate?: string; endDate?: string; processedByCurrentUser?: boolean; page?: number; size?: number }) {
    const { data } = await apiClient.get<ApiResponse<PageResponse<ReimbursementResponse>>>('/api/v1/reimbursements', { params });
    return data.data;
  },
  async downloadReceipt(reimbursementNumber: string) {
    const response = await apiClient.get<Blob>(`/api/v1/reimbursements/${reimbursementNumber}/receipt`, { responseType: 'blob' });
    return response.data;
  },
};
