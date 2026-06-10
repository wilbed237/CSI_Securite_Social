import { apiClient } from './httpClient';
import type { ApiResponse } from '../types/api';
import type { AgentRecentActivities, AgentSocialSummary, DashboardFilters, DoctorConsultedStats, DoctorDashboardSummary, ReimbursementStats } from '../types/dashboard';

const dashboardParams = (filters: DashboardFilters) => ({
  period: filters.period,
  startDate: filters.startDate,
  endDate: filters.endDate,
  type: filters.reimbursementType,
  status: filters.reimbursementStatus,
  doctorType: filters.doctorType,
  specialty: filters.specialty,
});

export const dashboardApi = {
  async getAgentSummary(filters: DashboardFilters) {
    const { data } = await apiClient.get<ApiResponse<AgentSocialSummary>>('/api/v1/dashboard/agent-social/summary', { params: dashboardParams(filters) });
    return data.data;
  },
  async getAgentReimbursementsStats(filters: DashboardFilters) {
    const { data } = await apiClient.get<ApiResponse<ReimbursementStats>>('/api/v1/dashboard/agent-social/reimbursements-stats', { params: dashboardParams(filters) });
    return data.data;
  },
  async getAgentRecentActivities() {
    const { data } = await apiClient.get<ApiResponse<AgentRecentActivities>>('/api/v1/dashboard/agent-social/recent-activities');
    return data.data;
  },
  async getDoctorsConsulted(filters: DashboardFilters) {
    const { data } = await apiClient.get<ApiResponse<DoctorConsultedStats>>('/api/v1/dashboard/agent-social/doctors-consulted', { params: dashboardParams(filters) });
    return data.data;
  },
  async getDoctorSummary(doctorMatricule?: string) {
    const { data } = await apiClient.get<ApiResponse<DoctorDashboardSummary>>('/api/v1/dashboard/doctor/summary', { params: { doctorMatricule } });
    return data.data;
  },
};
