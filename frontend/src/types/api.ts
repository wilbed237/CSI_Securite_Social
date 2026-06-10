export type RoleName = 'AGENT' | 'SOCIAL_AGENT' | 'AGENT_SOCIAL' | 'SECURITY_AGENT' | 'DOCTOR' | 'GENERALIST' | 'SPECIALIST' | 'ADMIN';
export type DoctorType = 'GENERALIST' | 'SPECIALIST';
export type InsuredStatus = 'ACTIVE' | 'SUSPENDED';
export type PrescriptionType = 'MEDICATION' | 'SPECIALIST_CONSULTATION';
export type ConsultationStatus = 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'ARCHIVED';
export type PrescriptionStatus = 'DRAFT' | 'ACTIVE' | 'FINALIZED' | 'CANCELLED';
export type DiseaseSheetStatus = 'DRAFT' | 'ISSUED' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'PAID' | 'COMPLETED' | 'CANCELLED';
export type PaymentType = 'CASH' | 'BANK_TRANSFER';
export type ReimbursementStatus = 'PENDING' | 'APPROVED' | 'EXECUTED' | 'REJECTED';
export type ReferralPriority = 'ROUTINE' | 'URGENT' | 'EMERGENCY';
export type ReferralStatus = 'PENDING' | 'ACCEPTED' | 'COMPLETED' | 'CANCELLED';
export type ReimbursementType = 'CONSULTATION' | 'MEDICATION' | 'HOSPITALIZATION' | 'MEDICAL_EXAM' | 'IMAGING' | 'SURGERY' | 'SPECIALIZED_CARE' | 'OTHER';

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
  lastLoginAt?: string;
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
  authUserId?: string;
  matricule: string;
  firstName: string;
  lastName: string;
  type: DoctorType;
  specialty?: string;
  phoneNumber?: string;
  email?: string;
  active?: boolean;
}

export interface SocialAgentResponse {
  id: string;
  authUserId: string;
  username: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  email?: string;
  active: boolean;
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
  countryCode?: string;
  preferredPaymentType?: PaymentType;
  bankAccountMasked?: string;
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
  consultationType: string;
  reason?: string;
  observations?: string;
  diagnosis?: string;
  conclusion?: string;
  status: ConsultationStatus;
  createdAt: string;
  updatedAt: string;
  createdByUserId?: string;
  updatedByUserId?: string;
  version: number;
}

export interface ConsultationCreateRequest {
  insuranceNumber: string;
  startedAt: string;
  endedAt: string;
  cost: number;
  consultationType?: string;
  reason?: string;
  observations?: string;
  diagnosis?: string;
  conclusion?: string;
  status?: ConsultationStatus;
  idempotencyKey?: string;
}

export interface ConsultationUpdateRequest extends Partial<Omit<ConsultationCreateRequest, 'insuranceNumber' | 'idempotencyKey'>> { version: number }
export interface ConsultationFilters { page?: number; size?: number; sort?: string; direction?: 'asc' | 'desc'; search?: string; patientId?: string; doctorId?: string; doctorType?: DoctorType; status?: ConsultationStatus; startDate?: string; endDate?: string }

export interface MedicationResponse {
  id: string;
  name: string;
  posology: string;
  frequency?: string;
  duration?: string;
  quantity?: number;
  administrationRoute?: string;
  instructions?: string;
}

export interface PrescriptionResponse {
  id: string;
  prescriptionNumber: string;
  type: PrescriptionType;
  prescriptionDate: string;
  consultationId: string;
  diseaseSheetId?: string;
  insuranceNumber: string;
  doctorMatricule: string;
  requiredSpecialty?: string;
  factors?: string;
  notes?: string;
  status: PrescriptionStatus;
  medications: MedicationResponse[];
  medicationCount: number;
  createdAt: string;
  updatedAt: string;
  createdByUserId?: string;
  updatedByUserId?: string;
  version: number;
}

export interface PrescriptionMedicationRequest { name: string; posology: string; frequency?: string; duration?: string; quantity?: number; administrationRoute?: string; instructions?: string }
export interface PrescriptionCreateRequest { consultationId: string; notes?: string; medications: PrescriptionMedicationRequest[] }
export interface PrescriptionUpdateRequest { prescriptionDate?: string; notes?: string; status?: PrescriptionStatus; medications?: PrescriptionMedicationRequest[]; version: number }
export interface PrescriptionFilters { page?: number; size?: number; sort?: string; direction?: 'asc' | 'desc'; search?: string; patientId?: string; doctorId?: string; consultationId?: string; diseaseSheetId?: string; status?: PrescriptionStatus; startDate?: string; endDate?: string; medicationName?: string }

export interface DiseaseSheetResponse {
  id: string;
  sheetNumber: string;
  date: string;
  diagnosis: string;
  medicalConclusion?: string;
  status: DiseaseSheetStatus;
  consultationId: string;
  prescriptionId?: string;
  insuranceNumber: string;
  doctorMatricule: string;
  doctorType: DoctorType;
  consultationCost: number;
  consultationDate: string;
  registrationDate: string;
  specialty?: string;
  receivedAt?: string;
  paymentType?: PaymentType;
  controlComment?: string;
  completedByUserId?: string;
  completedAt?: string;
  reimbursementId?: string;
  reimbursementNumber?: string;
  createdAt: string;
  updatedAt: string;
  createdByUserId?: string;
  updatedByUserId?: string;
  version: number;
}

export interface DiseaseSheetCreateRequest { consultationId: string; prescriptionId?: string; diagnosis: string; medicalConclusion?: string; specialty?: string }
export interface DiseaseSheetUpdateRequest { prescriptionId?: string; diagnosis?: string; medicalConclusion?: string; specialty?: string; status?: DiseaseSheetStatus; reimbursementId?: string; reimbursementNumber?: string; paymentType?: PaymentType; controlComment?: string; version: number }
export interface DiseaseSheetFilters { page?: number; size?: number; sort?: string; direction?: 'asc' | 'desc'; search?: string; patientId?: string; doctorId?: string; doctorType?: DoctorType; status?: DiseaseSheetStatus; hasReimbursement?: boolean; startDate?: string; endDate?: string }

export interface ReferralResponse {
  id: string;
  referralNumber: string;
  consultationId: string;
  insuranceNumber: string;
  specialty: string;
  reason: string;
  priority: ReferralPriority;
  status: ReferralStatus;
  specialistMatricules: string[];
  createdAt: string;
}

export interface ReimbursementResponse {
  id: string;
  reimbursementNumber: string;
  sheetNumber: string;
  date: string;
  reimbursementType: ReimbursementType;
  paymentType: PaymentType;
  bankAccountMasked?: string;
  baseAmount: number;
  eligibleAmount: number;
  rate: number;
  reimbursedAmount: number;
  ruleCode: string;
  status: ReimbursementStatus;
  approvedByUserId?: string;
  approvedAt?: string;
  processedByUserId?: string;
  processedAt?: string;
  paymentReference?: string;
  rejectionReason?: string;
}
