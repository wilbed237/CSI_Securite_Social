package com.csi.medical.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.medical.application.dto.AgentDashboardDtos.DoctorConsultedStatsResponse;
import com.csi.medical.application.service.AgentDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard/agent-social")
@RequiredArgsConstructor
public class AgentDashboardController {
    private final AgentDashboardService dashboardService;

    @Operation(summary = "Consultations de medecins par type sur une periode")
    @GetMapping("/doctors-consulted")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<DoctorConsultedStatsResponse> doctorsConsulted(@RequestParam(defaultValue = "MONTH") String period,
                                                                      @RequestParam(required = false) LocalDate startDate,
                                                                      @RequestParam(required = false) LocalDate endDate) {
        return ApiResponse.success("Medecins consultes", dashboardService.doctorsConsulted(period, startDate, endDate));
    }
}
