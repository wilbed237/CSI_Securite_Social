package com.csi.medical.infrastructure.persistence;

import com.csi.medical.domain.model.Prescription;
import com.csi.medical.domain.model.PrescriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

/**
 * Repository Spring Data des prescriptions.
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID>, JpaSpecificationExecutor<Prescription> {
    long countByType(PrescriptionType type);
    long countByConsultationDoctorMatriculeIgnoreCaseAndType(String doctorMatricule, PrescriptionType type);

    @Query("SELECT p FROM Prescription p LEFT JOIN FETCH p.medications WHERE p.consultation.id = :consultationId")
    List<Prescription> findByConsultationId(UUID consultationId);

    @EntityGraph(attributePaths = {"consultation", "medications"})
    @Query("SELECT p FROM Prescription p WHERE p.id = :id")
    Optional<Prescription> findDetailedById(UUID id);

    boolean existsByIdAndConsultationCreatedByUserId(UUID id, UUID userId);
}
