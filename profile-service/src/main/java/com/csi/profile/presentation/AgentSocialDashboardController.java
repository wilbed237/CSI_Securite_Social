package com.csi.profile.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.profile.application.dto.DashboardDtos.*;
import com.csi.profile.application.dto.DashboardPeriod;
import com.csi.profile.application.service.DashboardService;
import com.csi.profile.domain.model.DoctorType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Endpoints du dashboard agent social portes par profile-service.
 */
@RestController
@RequestMapping("/api/v1/dashboard/agent-social")
@RequiredArgsConstructor
public class AgentSocialDashboardController {
    private final DashboardService dashboardService;

    @Operation(summary = "Synthese patients, medecins et agents")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<AgentSocialSummaryResponse> summary(
            @RequestParam(defaultValue = "MONTH") DashboardPeriod period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) DoctorType doctorType,
            @RequestParam(required = false) String specialty) {
        return ApiResponse.success("Synthese dashboard agent social", dashboardService.agentSocialSummary(period, startDate, endDate, doctorType, specialty));
    }

    @Operation(summary = "Statistiques patients assures")
    @GetMapping("/patients-stats")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<PatientsStatsResponse> patientsStats(
            @RequestParam(defaultValue = "MONTH") DashboardPeriod period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ApiResponse.success("Statistiques patients", dashboardService.patientsStats(period, startDate, endDate));
    }

    @Operation(summary = "Statistiques medecins")
    @GetMapping("/doctors-stats")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<DoctorsStatsResponse> doctorsStats() {
        return ApiResponse.success("Statistiques medecins", dashboardService.doctorsStats());
    }

    @Operation(summary = "Activites recentes du referentiel profil")
    @GetMapping("/recent-activities")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<RecentActivitiesResponse> recentActivities() {
        return ApiResponse.success("Activites recentes", dashboardService.recentActivities());
    }
}
