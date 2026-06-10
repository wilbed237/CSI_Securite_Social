package com.csi.medical.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entite JPA qui trace une consultation entre un assure et un medecin.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "consultations")
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "insurance_number", nullable = false, length = 60)
    private String insuranceNumber;

    @Column(name = "doctor_matricule", nullable = false, length = 60)
    private String doctorMatricule;

    @Enumerated(EnumType.STRING)
    @Column(name = "doctor_type", nullable = false, length = 30)
    private DoctorType doctorType;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(name = "consultation_type", nullable = false, length = 80)
    @Builder.Default
    private String consultationType = "GENERAL";

    @Column(nullable = false, length = 240)
    @Builder.Default
    private String reason = "CONSULTATION";

    @Column(length = 2000)
    private String observations;

    @Column(length = 2000)
    private String diagnosis;

    @Column(length = 2000)
    private String conclusion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.DRAFT;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "updated_by_user_id")
    private UUID updatedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Version
    private long version;

    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
