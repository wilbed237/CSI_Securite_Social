package com.csi.medical.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
}
