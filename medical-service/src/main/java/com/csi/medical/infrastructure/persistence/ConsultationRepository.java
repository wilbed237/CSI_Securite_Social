package com.csi.medical.infrastructure.persistence;

import com.csi.medical.domain.model.Consultation;
import com.csi.medical.domain.model.DoctorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

/**
 * Repository Spring Data des consultations medicales.
 */
public interface ConsultationRepository extends JpaRepository<Consultation, UUID>, JpaSpecificationExecutor<Consultation> {
    interface DashboardCounts {
        long getTotal();
        long getToday();
        long getWeek();
        long getMonth();
    }

    interface PeriodCount {
        String getLabel();
        long getCount();
    }

    Optional<Consultation> findByIdempotencyKey(String idempotencyKey);
    long countByDoctorMatriculeIgnoreCase(String doctorMatricule);
    long countByDoctorMatriculeIgnoreCaseAndStartedAtBetween(String doctorMatricule, LocalDateTime start, LocalDateTime end);
    long countByStartedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByDoctorTypeAndStartedAtBetween(DoctorType doctorType, LocalDateTime start, LocalDateTime end);

    @Query(value = """
            SELECT COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE started_at >= :todayStart AND started_at < :todayEnd) AS today,
                   COUNT(*) FILTER (WHERE started_at >= :weekStart AND started_at < :weekEnd) AS week,
                   COUNT(*) FILTER (WHERE started_at >= :monthStart AND started_at < :monthEnd) AS month
            FROM consultations
            WHERE (:doctorMatricule IS NULL OR UPPER(doctor_matricule) = UPPER(:doctorMatricule))
            """, nativeQuery = true)
    DashboardCounts dashboardCounts(@Param("doctorMatricule") String doctorMatricule,
                                    @Param("todayStart") LocalDateTime todayStart,
                                    @Param("todayEnd") LocalDateTime todayEnd,
                                    @Param("weekStart") LocalDateTime weekStart,
                                    @Param("weekEnd") LocalDateTime weekEnd,
                                    @Param("monthStart") LocalDateTime monthStart,
                                    @Param("monthEnd") LocalDateTime monthEnd);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('day', started_at), 'MM-DD') AS label, COUNT(*) AS count
            FROM consultations
            WHERE started_at >= :start AND started_at < :end
              AND (:doctorMatricule IS NULL OR UPPER(doctor_matricule) = UPPER(:doctorMatricule))
            GROUP BY DATE_TRUNC('day', started_at)
            ORDER BY DATE_TRUNC('day', started_at)
            """, nativeQuery = true)
    List<PeriodCount> countByDay(@Param("doctorMatricule") String doctorMatricule,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
}
