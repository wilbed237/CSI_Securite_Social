package com.csi.profile.domain.model;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private InsuredStatus status = InsuredStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treating_doctor_id")
    private Doctor treatingDoctor;
}
