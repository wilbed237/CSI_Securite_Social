package com.csi.profile.application.dto;

import com.csi.profile.application.dto.ProfileDtos.DoctorResponse;
import com.csi.profile.application.dto.ProfileDtos.InsuredResponse;
import com.csi.profile.domain.model.DoctorType;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO exposes par les dashboards agent social.
 */
public final class DashboardDtos {
    private DashboardDtos() {}

    public record AgentSocialSummaryResponse(
            long totalInsuredPatients,
            long activeInsuredPatients,
            long inactiveInsuredPatients,
            long newInsuredPatientsInPeriod,
            long newInsuredPatientsThisMonth,
            long totalDoctors,
            long activeDoctors,
            long inactiveDoctors,
            long generalistDoctors,
            long specialistDoctors,
            long totalAgents,
            long activeAgents,
            List<MonthlyCountResponse> insuredPatientsMonthlyEvolution,
            List<DoctorTypeCountResponse> doctorsByType) {}

    public record PatientsStatsResponse(long totalPatients, long activePatients, long inactivePatients, long newPatientsInPeriod, long newPatientsThisMonth, List<MonthlyCountResponse> monthlyEvolution) {}
    public record DoctorsStatsResponse(long totalDoctors, long activeDoctors, long inactiveDoctors, long generalists, long specialists, List<DoctorTypeCountResponse> byType) {}
    public record RecentActivitiesResponse(List<InsuredResponse> latestPatients, List<DoctorResponse> recentlyActiveDoctors) {}
    public record MonthlyCountResponse(String month, long count) {}
    public record DoctorTypeCountResponse(DoctorType type, long count) {}
    public record ActivityItemResponse(String type, String label, LocalDate date, String status) {}
}
