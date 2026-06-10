package com.csi.profile.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entite JPA representant un medecin generaliste ou specialiste du systeme CSI.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctors", uniqueConstraints = {
        @UniqueConstraint(name = "uk_doctor_matricule", columnNames = "matricule"),
        @UniqueConstraint(name = "uk_doctor_email", columnNames = "email")
})
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auth_user_id")
    private UUID authUserId;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, length = 60)
    private String matricule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DoctorType type;

    @Column(length = 120)
    private String specialty;

    @Column(length = 40)
    private String phoneNumber;

    @Column(length = 160)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
