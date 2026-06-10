package com.csi.medical.infrastructure.persistence;
import com.csi.medical.domain.model.ReferralStatus;
import com.csi.medical.domain.model.SpecialistReferral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface SpecialistReferralRepository extends JpaRepository<SpecialistReferral, UUID> {
    Optional<SpecialistReferral> findByReferralNumberIgnoreCase(String referralNumber);
    Page<SpecialistReferral> findByTargetDoctorMatriculesContaining(String matricule, Pageable pageable);
    Page<SpecialistReferral> findByStatus(ReferralStatus status, Pageable pageable);
}
