package com.csi.medical.infrastructure.persistence;

import com.csi.medical.domain.model.DiseaseSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data des feuilles de maladie recherchees par numero metier.
 */
public interface DiseaseSheetRepository extends JpaRepository<DiseaseSheet, UUID>, JpaSpecificationExecutor<DiseaseSheet> {
    interface PeriodCount {
        String getLabel();
        long getCount();
    }

    Optional<DiseaseSheet> findBySheetNumberIgnoreCase(String sheetNumber);
    Optional<DiseaseSheet> findByConsultationId(UUID consultationId);
    Optional<DiseaseSheet> findByPrescriptionId(UUID prescriptionId);
    long countByConsultationDoctorMatriculeIgnoreCase(String doctorMatricule);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', ds.date), 'YYYY-MM') AS label, COUNT(*) AS count
            FROM disease_sheets ds
            JOIN consultations c ON c.id = ds.consultation_id
            WHERE ds.date >= :start AND ds.date < :end
              AND (:doctorMatricule IS NULL OR UPPER(c.doctor_matricule) = UPPER(:doctorMatricule))
            GROUP BY DATE_TRUNC('month', ds.date)
            ORDER BY DATE_TRUNC('month', ds.date)
            """, nativeQuery = true)
    List<PeriodCount> countByMonth(@Param("doctorMatricule") String doctorMatricule,
                                  @Param("start") LocalDate start,
                                  @Param("end") LocalDate end);
}
