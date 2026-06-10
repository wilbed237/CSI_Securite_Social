package com.csi.medical.application.dto;

import java.util.List;

/**
 * DTO des statistiques du dashboard medecin.
 */
public final class DashboardDtos {
    private DashboardDtos() {}

    public record DoctorDashboardSummaryResponse(
            long patientsToday,
            long patientsThisWeek,
            long patientsThisMonth,
            long consultations,
            long diseaseSheets,
            long medicationPrescriptions,
            long specialistRecommendations,
            long followedPatientRecords,
            long pendingConsultations,
            List<MonthlyCountResponse> consultationsByDay,
            List<MonthlyCountResponse> diseaseSheetsByMonth,
            List<CategoryCountResponse> prescriptionsByCategory) {}

    public record DoctorPatientsStatsResponse(long today, long thisWeek, long thisMonth) {}
    public record DoctorConsultationsStatsResponse(long total, List<MonthlyCountResponse> byDay) {}
    public record DoctorDiseaseSheetsStatsResponse(long total, List<MonthlyCountResponse> byMonth) {}
    public record DoctorRecommendationsStatsResponse(long totalSpecialistRecommendations) {}
    public record DoctorRecentActivitiesResponse(List<String> actions) {}
    public record MonthlyCountResponse(String label, long count) {}
    public record CategoryCountResponse(String category, long count) {}
}
