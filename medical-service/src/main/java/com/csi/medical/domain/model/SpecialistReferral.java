package com.csi.medical.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @Entity
@Table(name = "specialist_referrals", uniqueConstraints = @UniqueConstraint(name = "uk_referral_number", columnNames = "referral_number"))
public class SpecialistReferral {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "referral_number", nullable = false, length = 80) private String referralNumber;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "consultation_id", nullable = false) private Consultation consultation;
    @Column(nullable = false, length = 120) private String specialty;
    @Column(nullable = false, length = 500) private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ReferralPriority priority;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) @Builder.Default private ReferralStatus status = ReferralStatus.PENDING;
    @ElementCollection
    @CollectionTable(name = "specialist_referral_targets", joinColumns = @JoinColumn(name = "referral_id"))
    @Column(name = "doctor_matricule", nullable = false, length = 60)
    @Builder.Default private List<String> targetDoctorMatricules = new ArrayList<>();
    @Column(name = "created_by_user_id", nullable = false) private UUID createdByUserId;
    @Column(name = "created_at", nullable = false, updatable = false) @Builder.Default private Instant createdAt = Instant.now();
}
