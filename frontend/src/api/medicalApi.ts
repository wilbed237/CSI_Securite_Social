import { apiClient } from './httpClient';
import type { ApiResponse, ConsultationResponse, DiseaseSheetResponse, PrescriptionResponse, ReferralPriority, ReferralResponse, ReferralStatus } from '../types/api';

export interface CreateConsultationPayload { insuranceNumber: string; startedAt: string; endedAt: string; cost: number }
export interface CreateMedicationPrescriptionPayload { consultationId: string; medications: { name: string; posology: string }[] }
export interface CreateSpecialistReferralPayload { consultationId: string; requiredSpecialty: string; factors?: string }
export interface CreateDiseaseSheetPayload { consultationId: string; diagnosis: string }
export interface CreateReferralPayload { consultationId: string; specialty: string; reason: string; priority: ReferralPriority; specialistMatricules: string[] }

export const medicalApi = {
  async createConsultation(payload: CreateConsultationPayload) {
    const { data } = await apiClient.post<ApiResponse<ConsultationResponse>>('/api/v1/consultations', payload);
    return data.data;
  },
  async prescribeMedications(payload: CreateMedicationPrescriptionPayload) {
    const { data } = await apiClient.post<ApiResponse<PrescriptionResponse>>('/api/v1/prescriptions/medications', payload);
    return data.data;
  },
  async prescribeSpecialistConsultation(payload: CreateSpecialistReferralPayload) {
    const { data } = await apiClient.post<ApiResponse<PrescriptionResponse>>('/api/v1/prescriptions/specialist-consultations', payload);
    return data.data;
  },
  async createDiseaseSheet(payload: CreateDiseaseSheetPayload) {
    const { data } = await apiClient.post<ApiResponse<DiseaseSheetResponse>>('/api/v1/disease-sheets', payload);
    return data.data;
  },
  async createReferral(payload: CreateReferralPayload) {
    const { data } = await apiClient.post<ApiResponse<ReferralResponse>>(`/api/v1/consultations/${payload.consultationId}/referrals`, payload);
    return data.data;
  },
  async getReferral(referralNumber: string) {
    const { data } = await apiClient.get<ApiResponse<ReferralResponse>>(`/api/v1/referrals/${referralNumber}`);
    return data.data;
  },
  async updateReferralStatus(referralNumber: string, status: ReferralStatus) {
    const { data } = await apiClient.patch<ApiResponse<ReferralResponse>>(`/api/v1/referrals/${referralNumber}/status`, { status });
    return data.data;
  },
  async submitDiseaseSheet(sheetNumber: string) {
    const { data } = await apiClient.patch<ApiResponse<DiseaseSheetResponse>>(`/api/v1/disease-sheets/${sheetNumber}/submit`);
    return data.data;
  },
  async completeDiseaseSheet(sheetNumber: string, paymentType: 'CASH' | 'BANK_TRANSFER', controlComment?: string) {
    const { data } = await apiClient.patch<ApiResponse<DiseaseSheetResponse>>(`/api/v1/disease-sheets/${sheetNumber}/complete`, { paymentType, controlComment });
    return data.data;
  },
  async downloadDiseaseSheetPdf(sheetNumber: string) {
    const response = await apiClient.get<Blob>(`/api/v1/disease-sheets/${sheetNumber}/pdf`, { responseType: 'blob' });
    return response.data;
  },
  async getDiseaseSheet(sheetNumber: string) {
    const { data } = await apiClient.get<ApiResponse<DiseaseSheetResponse>>(`/api/v1/disease-sheets/${sheetNumber}`);
    return data.data;
  },
  async finalizeDiseaseSheet(sheetNumber: string, reimbursementNumber: string, completionComment?: string) {
    const { data } = await apiClient.patch<ApiResponse<DiseaseSheetResponse>>(
      `/api/v1/disease-sheets/${sheetNumber}/finalize`,
      { reimbursementNumber, completionComment },
    );
    return data.data;
  },
};
