package com.csi.reimbursement.application.dto;

import com.csi.reimbursement.application.dto.ReimbursementDtos.ReimbursementResponse;
import com.csi.reimbursement.domain.model.ReimbursementStatus;
import com.csi.reimbursement.domain.model.ReimbursementType;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de statistiques remboursements pour le dashboard agent social.
 */
public final class DashboardDtos {
    private DashboardDtos() {}

    public record ReimbursementStatsResponse(
            long totalReimbursements,
            long executedReimbursements,
            long pendingReimbursements,
            long rejectedReimbursements,
            BigDecimal totalReimbursedAmount,
            BigDecimal averageReimbursedAmount,
            BigDecimal maximumReimbursedAmount,
            BigDecimal minimumReimbursedAmount,
            BigDecimal currentAgentReimbursedAmount,
            long currentAgentProcessedCount,
            double validationRate,
            double rejectionRate,
            double averageProcessingDelayDays,
            long overdueReimbursements,
            List<StatusCountResponse> byStatus,
            List<TypeCountResponse> byType,
            List<MonthlyAmountResponse> amountByMonth,
            List<MonthlyCountResponse> monthlyEvolution,
            List<ReimbursementResponse> latestSubmitted,
            List<ReimbursementResponse> latestValidated) {}

    public record StatusCountResponse(ReimbursementStatus status, long count) {}
    public record TypeCountResponse(ReimbursementType type, long count) {}
    public record MonthlyAmountResponse(String month, BigDecimal amount) {}
    public record MonthlyCountResponse(String month, long count) {}
}
