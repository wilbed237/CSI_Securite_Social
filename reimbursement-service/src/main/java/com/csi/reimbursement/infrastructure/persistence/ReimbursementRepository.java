package com.csi.reimbursement.infrastructure.persistence;

import com.csi.reimbursement.domain.model.Reimbursement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data garantissant notamment l unicite du remboursement par feuille.
 */
public interface ReimbursementRepository extends JpaRepository<Reimbursement, UUID> {
    boolean existsBySheetNumberIgnoreCase(String sheetNumber);
    Optional<Reimbursement> findByReimbursementNumberIgnoreCase(String reimbursementNumber);
}
