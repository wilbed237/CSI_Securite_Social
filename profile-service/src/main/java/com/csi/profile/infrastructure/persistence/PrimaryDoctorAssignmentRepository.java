package com.csi.profile.infrastructure.persistence;

import com.csi.profile.domain.model.PrimaryDoctorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrimaryDoctorAssignmentRepository extends JpaRepository<PrimaryDoctorAssignment, UUID> {
    Optional<PrimaryDoctorAssignment> findByInsuredIdAndEndedAtIsNull(UUID insuredId);
    List<PrimaryDoctorAssignment> findByInsuredIdOrderByStartedAtDesc(UUID insuredId);
}
