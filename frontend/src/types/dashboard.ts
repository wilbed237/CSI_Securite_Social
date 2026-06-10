import type { DoctorResponse, DoctorType, ReimbursementResponse, ReimbursementStatus, ReimbursementType } from './api';

export type DashboardPeriod = 'DAY' | 'WEEK' | 'MONTH' | 'QUARTER' | 'YEAR' | 'CUSTOM';
export interface DashboardFilters {
  period: DashboardPeriod;
  startDate?: string;
  endDate?: string;
  reimbursementType?: ReimbursementType;
  reimbursementStatus?: ReimbursementStatus;
  doctorType?: DoctorType;
  specialty?: string;
}

export interface MonthlyCount {
  month?: string;
  label?: string;
  count: number;
}

export interface AgentSocialSummary {
  totalInsuredPatients: number;
  activeInsuredPatients: number;
  inactiveInsuredPatients: number;
  newInsuredPatientsInPeriod: number;
  newInsuredPatientsThisMonth: number;
  totalDoctors: number;
  activeDoctors: number;
  inactiveDoctors: number;
  generalistDoctors: number;
  specialistDoctors: number;
  totalAgents: number;
  activeAgents: number;
  insuredPatientsMonthlyEvolution: MonthlyCount[];
  doctorsByType: { type: DoctorType; count: number }[];
}

export interface ReimbursementStats {
  totalReimbursements: number;
  executedReimbursements: number;
  pendingReimbursements: number;
  rejectedReimbursements: number;
  totalReimbursedAmount: number;
  averageReimbursedAmount: number;
  maximumReimbursedAmount: number;
  minimumReimbursedAmount: number;
  currentAgentReimbursedAmount: number;
  currentAgentProcessedCount: number;
  validationRate: number;
  rejectionRate: number;
  averageProcessingDelayDays: number;
  overdueReimbursements: number;
  byStatus: { status: ReimbursementStatus; count: number }[];
  byType: { type: ReimbursementType; count: number }[];
  amountByMonth: { month: string; amount: number }[];
  monthlyEvolution: MonthlyCount[];
  latestSubmitted: ReimbursementResponse[];
  latestValidated: ReimbursementResponse[];
}

export interface AgentRecentActivities {
  latestPatients: unknown[];
  recentlyActiveDoctors: DoctorResponse[];
}

export interface DoctorConsultedStats {
  totalConsultations: number;
  generalistConsultations: number;
  specialistConsultations: number;
  byType: { type: DoctorType; count: number }[];
}

export interface DoctorDashboardSummary {
  patientsToday: number;
  patientsThisWeek: number;
  patientsThisMonth: number;
  consultations: number;
  diseaseSheets: number;
  medicationPrescriptions: number;
  specialistRecommendations: number;
  followedPatientRecords: number;
  pendingConsultations: number;
  consultationsByDay: MonthlyCount[];
  diseaseSheetsByMonth: MonthlyCount[];
  prescriptionsByCategory: { category: string; count: number }[];
}
