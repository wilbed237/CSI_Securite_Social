package com.csi.reimbursement.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
    @Column(name = "payment_type", nullable = false, length = 30)
    private PaymentType paymentType;

    @Column(name = "bank_iban", length = 80)
    private String bankIban;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal reimbursedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReimbursementStatus status = ReimbursementStatus.EXECUTED;
}
