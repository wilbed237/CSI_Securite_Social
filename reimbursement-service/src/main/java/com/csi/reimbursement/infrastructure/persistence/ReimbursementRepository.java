package com.csi.reimbursement.infrastructure.persistence;

import com.csi.reimbursement.domain.model.Reimbursement;
import com.csi.reimbursement.domain.model.ReimbursementStatus;
import com.csi.reimbursement.domain.model.ReimbursementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data garantissant notamment l unicite du remboursement par feuille.
 */
public interface ReimbursementRepository extends JpaRepository<Reimbursement, UUID>, JpaSpecificationExecutor<Reimbursement> {
    interface MonthlyCount {
        String getMonth();
        long getCount();
    }

    interface DashboardAggregate {
        long getTotal();
        long getExecuted();
        long getPending();
        long getRejected();
        BigDecimal getTotalAmount();
        BigDecimal getAverageAmount();
        BigDecimal getMaximumAmount();
        BigDecimal getMinimumAmount();
        BigDecimal getAgentAmount();
        long getAgentCount();
        double getAverageDelayDays();
        long getOverdue();
    }

    interface TypeAggregate {
        String getType();
        long getCount();
    }

    interface MonthlyAmount {
        String getMonth();
        BigDecimal getAmount();
    }

    boolean existsBySheetNumberIgnoreCase(String sheetNumber);
    Optional<Reimbursement> findByReimbursementNumberIgnoreCase(String reimbursementNumber);
    Optional<Reimbursement> findByIdempotencyKey(String idempotencyKey);
    long countByStatus(ReimbursementStatus status);
    long countByDateBetween(LocalDate start, LocalDate end);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', date), 'YYYY-MM') AS month, COUNT(*) AS count
            FROM reimbursements
            WHERE date >= :start AND date < :end
            GROUP BY DATE_TRUNC('month', date)
            ORDER BY DATE_TRUNC('month', date)
            """, nativeQuery = true)
    List<MonthlyCount> countByMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(value = """
            SELECT COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE status = 'EXECUTED') AS executed,
                   COUNT(*) FILTER (WHERE status = 'PENDING') AS pending,
                   COUNT(*) FILTER (WHERE status = 'REJECTED') AS rejected,
                   COALESCE(SUM(reimbursed_amount), 0) AS total_amount,
                   COALESCE(AVG(reimbursed_amount), 0) AS average_amount,
                   COALESCE(MAX(reimbursed_amount), 0) AS maximum_amount,
                   COALESCE(MIN(reimbursed_amount), 0) AS minimum_amount,
                   COALESCE(SUM(reimbursed_amount) FILTER (WHERE processed_by_user_id = :currentUserId), 0) AS agent_amount,
                   COUNT(*) FILTER (WHERE processed_by_user_id = :currentUserId) AS agent_count,
                   COALESCE(AVG(EXTRACT(EPOCH FROM (processed_at - created_at)) / 86400.0)
                       FILTER (WHERE processed_at IS NOT NULL), 0) AS average_delay_days,
                   COUNT(*) FILTER (WHERE status = 'PENDING' AND date < :overdueBefore) AS overdue
            FROM reimbursements
            WHERE date >= :start AND date <= :end
              AND (:type IS NULL OR reimbursement_type = :type)
              AND (:status IS NULL OR reimbursements.status = :status)
            """, nativeQuery = true)
    DashboardAggregate dashboardAggregate(@Param("start") LocalDate start,
                                          @Param("end") LocalDate end,
                                          @Param("type") String type,
                                          @Param("status") String status,
                                          @Param("currentUserId") UUID currentUserId,
                                          @Param("overdueBefore") LocalDate overdueBefore);

    @Query(value = """
            SELECT reimbursement_type AS type, COUNT(*) AS count
            FROM reimbursements
            WHERE date >= :start AND date <= :end
              AND (:type IS NULL OR reimbursement_type = :type)
              AND (:status IS NULL OR reimbursements.status = :status)
            GROUP BY reimbursement_type
            """, nativeQuery = true)
    List<TypeAggregate> countByType(@Param("start") LocalDate start,
                                    @Param("end") LocalDate end,
                                    @Param("type") String type,
                                    @Param("status") String status);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', date), 'YYYY-MM') AS month,
                   COALESCE(SUM(reimbursed_amount), 0) AS amount
            FROM reimbursements
            WHERE date >= :start AND date <= :end
              AND (:type IS NULL OR reimbursement_type = :type)
              AND (:status IS NULL OR reimbursements.status = :status)
            GROUP BY DATE_TRUNC('month', date)
            ORDER BY DATE_TRUNC('month', date)
            """, nativeQuery = true)
    List<MonthlyAmount> amountByMonth(@Param("start") LocalDate start,
                                      @Param("end") LocalDate end,
                                      @Param("type") String type,
                                      @Param("status") String status);

    @Query("""
            SELECT r FROM Reimbursement r
            WHERE r.date >= :start AND r.date <= :end
              AND (:type IS NULL OR r.reimbursementType = :type)
              AND (:status IS NULL OR r.status = :status)
            ORDER BY r.date DESC, r.createdAt DESC
            """)
    List<Reimbursement> findLatest(@Param("start") LocalDate start,
                                   @Param("end") LocalDate end,
                                   @Param("type") ReimbursementType type,
                                   @Param("status") ReimbursementStatus status,
                                   Pageable pageable);

    @Query("""
            SELECT r FROM Reimbursement r
            WHERE r.date >= :start AND r.date <= :end
              AND (:type IS NULL OR r.reimbursementType = :type)
              AND r.status = :executedStatus
            ORDER BY r.date DESC, r.createdAt DESC
            """)
    List<Reimbursement> findLatestExecuted(@Param("start") LocalDate start,
                                           @Param("end") LocalDate end,
                                           @Param("type") ReimbursementType type,
                                           @Param("executedStatus") ReimbursementStatus executedStatus,
                                           Pageable pageable);
}
