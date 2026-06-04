package com.csi.medical.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prescriptions", uniqueConstraints = @UniqueConstraint(name = "uk_prescription_number", columnNames = "prescription_number"))
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "prescription_number", nullable = false, length = 60)
    private String prescriptionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PrescriptionType type;

    @Column(name = "prescription_date", nullable = false)
    @Builder.Default
    private LocalDate prescriptionDate = LocalDate.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Column(name = "required_specialty", length = 120)
    private String requiredSpecialty;

    @Column(length = 500)
    private String factors;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Medication> medications = new ArrayList<>();
}
