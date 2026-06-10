package com.csi.profile.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entite JPA representant un assure social et son eventuel medecin traitant.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insured_persons", uniqueConstraints = {
        @UniqueConstraint(name = "uk_insured_number", columnNames = "insurance_number"),
        @UniqueConstraint(name = "uk_insured_email", columnNames = "email")
})
public class InsuredPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "insurance_number", nullable = false, length = 60)
    private String insuranceNumber;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 240)
    private String address;

    @Column(length = 40)
    private String phoneNumber;

    @Column(length = 160)
    private String email;

    @Column(name = "country_code", nullable = false, length = 2)
    @Builder.Default
    private String countryCode = "CM";

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_payment_type", nullable = false, length = 30)
    @Builder.Default
    private PaymentPreference preferredPaymentType = PaymentPreference.CASH;

    @Column(name = "bank_account_encrypted", length = 1000)
    private String bankAccountEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private InsuredStatus status = InsuredStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treating_doctor_id")
    private Doctor treatingDoctor;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
