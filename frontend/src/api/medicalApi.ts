import { apiClient } from './httpClient';
import type { ApiResponse, ConsultationResponse, DiseaseSheetResponse, DoctorType, PrescriptionResponse } from '../types/api';

export interface CreateConsultationPayload { insuranceNumber: string; doctorMatricule: string; doctorType: DoctorType; startedAt: string; endedAt: string; cost: number }
export interface CreateMedicationPrescriptionPayload { consultationId: string; medications: { name: string; posology: string }[] }
export interface CreateSpecialistReferralPayload { consultationId: string; requiredSpecialty: string; factors?: string }
export interface CreateDiseaseSheetPayload { consultationId: string; diagnosis: string }

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
  async getDiseaseSheet(sheetNumber: string) {
    const { data } = await apiClient.get<ApiResponse<DiseaseSheetResponse>>(`/api/v1/disease-sheets/${sheetNumber}`);
    return data.data;
  },
};
