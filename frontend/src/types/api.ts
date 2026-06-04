export type RoleName = 'AGENT' | 'DOCTOR' | 'GENERALIST' | 'SPECIALIST' | 'ADMIN';
export type DoctorType = 'GENERALIST' | 'SPECIALIST';
export type InsuredStatus = 'ACTIVE' | 'SUSPENDED';
export type PrescriptionType = 'MEDICATION' | 'SPECIALIST_CONSULTATION';
export type DiseaseSheetStatus = 'CREATED' | 'COMPLETED';
export type PaymentType = 'CASH' | 'BANK_TRANSFER';
export type ReimbursementStatus = 'EXECUTED';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface ApiErrorResponse {
  code: string;
  message: string;
  details?: Record<string, string>;
  timestamp?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  phoneNumber?: string;
  roles: RoleName[];
  enabled: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresInSeconds: number;
  user: UserResponse;
}

export interface DoctorResponse {
  id: string;
  matricule: string;
  firstName: string;
  lastName: string;
  type: DoctorType;
  specialty?: string;
  phoneNumber?: string;
  email?: string;
}

export interface InsuredResponse {
  id: string;
  insuranceNumber: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  address: string;
  phoneNumber?: string;
  email?: string;
  status: InsuredStatus;
  treatingDoctor?: DoctorResponse;
}

export interface ConsultationResponse {
  id: string;
  insuranceNumber: string;
  doctorMatricule: string;
  doctorType: DoctorType;
  startedAt: string;
  endedAt: string;
  cost: number;
}

export interface MedicationResponse {
  id: string;
  name: string;
  posology: string;
}

export interface PrescriptionResponse {
  id: string;
  prescriptionNumber: string;
  type: PrescriptionType;
  prescriptionDate: string;
  consultationId: string;
  requiredSpecialty?: string;
  factors?: string;
  medications: MedicationResponse[];
}

export interface DiseaseSheetResponse {
  id: string;
  sheetNumber: string;
  date: string;
  diagnosis: string;
  status: DiseaseSheetStatus;
  consultationId: string;
  insuranceNumber: string;
  doctorMatricule: string;
  doctorType: DoctorType;
  consultationCost: number;
}

export interface ReimbursementResponse {
  id: string;
  reimbursementNumber: string;
  sheetNumber: string;
  date: string;
  paymentType: PaymentType;
  bankIban?: string;
  baseAmount: number;
  rate: number;
  reimbursedAmount: number;
  status: ReimbursementStatus;
}
