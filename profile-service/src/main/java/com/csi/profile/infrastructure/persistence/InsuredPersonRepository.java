package com.csi.profile.infrastructure.persistence;

import com.csi.profile.domain.model.InsuredPerson;
import com.csi.profile.domain.model.InsuredStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository Spring Data pour les assures identifies par numero d assurance.
 */
public interface InsuredPersonRepository extends JpaRepository<InsuredPerson, UUID>, JpaSpecificationExecutor<InsuredPerson> {
    interface MonthlyCount {
        String getMonth();
        long getCount();
    }

    Optional<InsuredPerson> findByInsuranceNumberIgnoreCase(String insuranceNumber);
    boolean existsByInsuranceNumberIgnoreCase(String insuranceNumber);
    long countByStatus(InsuredStatus status);
    long countByCreatedAtGreaterThanEqual(Instant since);
    long countByCreatedAtBetween(Instant start, Instant end);
    List<InsuredPerson> findTop5ByOrderByCreatedAtDesc();
    Page<InsuredPerson> findByTreatingDoctorAuthUserId(UUID authUserId, Pageable pageable);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', created_at), 'YYYY-MM') AS month, COUNT(*) AS count
            FROM insured_persons
            WHERE created_at >= :start AND created_at < :end
            GROUP BY DATE_TRUNC('month', created_at)
            ORDER BY DATE_TRUNC('month', created_at)
            """, nativeQuery = true)
    List<MonthlyCount> countByMonth(@Param("start") Instant start, @Param("end") Instant end);
}
