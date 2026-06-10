package com.csi.medical.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Feuille de maladie documentant une consultation et declenchant le remboursement.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "disease_sheets", uniqueConstraints = @UniqueConstraint(name = "uk_sheet_number", columnNames = "sheet_number"))
public class DiseaseSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sheet_number", nullable = false, length = 60)
    private String sheetNumber;

    @Column(nullable = false)
    @Builder.Default
    private LocalDate date = LocalDate.now();

    @Column(nullable = false, length = 1000)
    private String diagnosis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DiseaseSheetStatus status = DiseaseSheetStatus.ISSUED;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    @Column(name = "prescription_id") private UUID prescriptionId;
    @Column(name = "patient_id", nullable = false, length = 60) private String patientId;
    @Column(name = "doctor_id", nullable = false, length = 60) private String doctorId;
    @Enumerated(EnumType.STRING) @Column(name = "doctor_type", nullable = false, length = 30) private DoctorType doctorType;
    @Column(length = 120) private String specialty;
    @Column(name = "consultation_amount", nullable = false, precision = 12, scale = 2) private BigDecimal consultationAmount;
    @Column(name = "consultation_date", nullable = false) private LocalDateTime consultationDate;
    @Column(name = "registration_date", nullable = false) private LocalDate registrationDate;
    @Column(name = "medical_conclusion", length = 2000) private String medicalConclusion;

    @Column(name = "received_at") private java.time.Instant receivedAt;
    @Column(name = "payment_type", length = 30) private String paymentType;
    @Column(name = "control_comment", length = 1000) private String controlComment;
    @Column(name = "completed_by_user_id") private UUID completedByUserId;
    @Column(name = "completed_at") private java.time.Instant completedAt;
    @Column(name = "reimbursement_number", length = 80) private String reimbursementNumber;
    @Column(name = "reimbursement_id") private UUID reimbursementId;
    @Column(name = "completion_comment", length = 1000) private String completionComment;

    @Column(name = "created_by_user_id") private UUID createdByUserId;
    @Column(name = "updated_by_user_id") private UUID updatedByUserId;
    @Column(name = "created_at", nullable = false, updatable = false) @Builder.Default private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) @Builder.Default private Instant updatedAt = Instant.now();
    @Version private long version;

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
