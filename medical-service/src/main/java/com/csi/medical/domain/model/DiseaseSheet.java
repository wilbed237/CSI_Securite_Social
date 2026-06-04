package com.csi.medical.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

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
    private DiseaseSheetStatus status = DiseaseSheetStatus.COMPLETED;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;
}
