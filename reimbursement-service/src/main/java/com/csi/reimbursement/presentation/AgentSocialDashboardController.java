package com.csi.reimbursement.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.reimbursement.application.dto.DashboardDtos.ReimbursementStatsResponse;
import com.csi.reimbursement.application.dto.DashboardPeriod;
import com.csi.reimbursement.application.service.DashboardService;
import com.csi.reimbursement.config.AuthenticatedUser;
import com.csi.reimbursement.domain.model.ReimbursementStatus;
import com.csi.reimbursement.domain.model.ReimbursementType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDate;

/**
 * Endpoints dashboard agent social portes par reimbursement-service.
 */
@RestController
@RequestMapping("/api/v1/dashboard/agent-social")
@RequiredArgsConstructor
public class AgentSocialDashboardController {
    private final DashboardService dashboardService;

    @Operation(summary = "Statistiques remboursements")
    @GetMapping("/reimbursements-stats")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<ReimbursementStatsResponse> reimbursementsStats(
            @RequestParam(defaultValue = "MONTH") DashboardPeriod period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) ReimbursementType type,
            @RequestParam(required = false) ReimbursementStatus status,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success("Statistiques remboursements", dashboardService.reimbursementStats(period, startDate, endDate, type, status, user.userId()));
    }
}
