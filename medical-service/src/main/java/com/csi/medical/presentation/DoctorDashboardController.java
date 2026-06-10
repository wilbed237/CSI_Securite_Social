package com.csi.medical.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.medical.application.dto.DashboardDtos.*;
import com.csi.medical.application.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints statistiques du dashboard medecin.
 */
@RestController
@RequestMapping("/api/v1/dashboard/doctor")
@RequiredArgsConstructor
public class DoctorDashboardController {
    private final DashboardService dashboardService;

    @Operation(summary = "Synthese dashboard medecin")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DoctorDashboardSummaryResponse> summary(@RequestParam(required = false) String doctorMatricule) {
        return ApiResponse.success("Synthese dashboard medecin", dashboardService.summary(doctorMatricule));
    }

    @GetMapping("/patients-stats")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DoctorPatientsStatsResponse> patientsStats(@RequestParam(required = false) String doctorMatricule) {
        return ApiResponse.success("Statistiques patients medecin", dashboardService.patientsStats(doctorMatricule));
    }

    @GetMapping("/consultations-stats")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DoctorConsultationsStatsResponse> consultationsStats(@RequestParam(required = false) String doctorMatricule) {
        return ApiResponse.success("Statistiques consultations", dashboardService.consultationsStats(doctorMatricule));
    }

    @GetMapping("/disease-sheets-stats")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DoctorDiseaseSheetsStatsResponse> diseaseSheetsStats(@RequestParam(required = false) String doctorMatricule) {
        return ApiResponse.success("Statistiques feuilles maladie", dashboardService.diseaseSheetsStats(doctorMatricule));
    }

    @GetMapping("/recommendations-stats")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DoctorRecommendationsStatsResponse> recommendationsStats(@RequestParam(required = false) String doctorMatricule) {
        return ApiResponse.success("Statistiques recommandations", dashboardService.recommendationsStats(doctorMatricule));
    }

    @GetMapping("/recent-activities")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DoctorRecentActivitiesResponse> recentActivities() {
        return ApiResponse.success("Activites recentes medecin", dashboardService.recentActivities());
    }
}
