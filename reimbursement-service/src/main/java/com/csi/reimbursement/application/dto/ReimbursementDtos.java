package com.csi.reimbursement.application.dto;

import com.csi.reimbursement.domain.model.PaymentType;
import com.csi.reimbursement.domain.model.ReimbursementStatus;
import com.csi.reimbursement.domain.model.ReimbursementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO utilises par les endpoints de remboursement.
 */
public final class ReimbursementDtos {
    private ReimbursementDtos() {}

    public record CreateReimbursementRequest(@NotBlank String sheetNumber, @NotNull PaymentType paymentType,
            @Size(max = 80) String bankIban, ReimbursementType reimbursementType, @Size(max = 100) String idempotencyKey) {
        public CreateReimbursementRequest(String sheetNumber, PaymentType paymentType, String bankIban, ReimbursementType reimbursementType) {
            this(sheetNumber, paymentType, bankIban, reimbursementType, null);
        }
    }
    public record RejectReimbursementRequest(@NotBlank @Size(max = 1000) String reason) {}
    public record ReimbursementResponse(UUID id, String reimbursementNumber, String sheetNumber, LocalDate date,
            ReimbursementType reimbursementType, PaymentType paymentType, String bankAccountMasked,
            BigDecimal baseAmount, BigDecimal eligibleAmount, BigDecimal rate, BigDecimal reimbursedAmount,
            String ruleCode, ReimbursementStatus status, UUID approvedByUserId, Instant approvedAt,
            UUID processedByUserId, Instant processedAt, String paymentReference, String rejectionReason) {}
}
