package com.csi.reimbursement.application.dto;

import com.csi.reimbursement.domain.model.PaymentType;
import com.csi.reimbursement.domain.model.ReimbursementStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class ReimbursementDtos {
    private ReimbursementDtos() {}

    public record CreateReimbursementRequest(@NotBlank String sheetNumber, @NotNull PaymentType paymentType, @Size(max = 80) String bankIban) {}
    public record ReimbursementResponse(UUID id, String reimbursementNumber, String sheetNumber, LocalDate date, PaymentType paymentType, String bankIban, BigDecimal baseAmount, BigDecimal rate, BigDecimal reimbursedAmount, ReimbursementStatus status) {}
}
