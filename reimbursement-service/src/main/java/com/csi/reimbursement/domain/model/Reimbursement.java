package com.csi.reimbursement.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * Entite JPA representant le paiement effectue pour une feuille de maladie.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reimbursements", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reimbursement_number", columnNames = "reimbursement_number"),
        @UniqueConstraint(name = "uk_reimbursement_sheet", columnNames = "sheet_number")
})
public class Reimbursement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reimbursement_number", nullable = false, length = 80)
    private String reimbursementNumber;

    @Column(name = "sheet_number", nullable = false, length = 80)
    private String sheetNumber;

    @Column(nullable = false)
    @Builder.Default
    private LocalDate date = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "reimbursement_type", nullable = false, length = 40)
    @Builder.Default
    private ReimbursementType reimbursementType = ReimbursementType.CONSULTATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    private PaymentType paymentType;

    @Column(name = "bank_iban_encrypted", length = 600)
    private String bankIbanEncrypted;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal reimbursedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReimbursementStatus status = ReimbursementStatus.PENDING;

    @Column(name = "eligible_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal eligibleAmount;

    @Column(name = "rule_code", nullable = false, length = 80)
    private String ruleCode;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "approved_by_user_id") private UUID approvedByUserId;
    @Column(name = "approved_at") private Instant approvedAt;
    @Column(name = "rejection_reason", length = 1000) private String rejectionReason;
    @Column(name = "payment_reference", length = 120) private String paymentReference;
    @Column(name = "idempotency_key", length = 100, unique = true) private String idempotencyKey;

    @Column(name = "processed_by_user_id")
    private UUID processedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;
}
