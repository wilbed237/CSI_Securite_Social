package com.csi.profile.infrastructure.persistence;

import com.csi.profile.domain.model.InsuredPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data pour les assures identifies par numero d assurance.
 */
public interface InsuredPersonRepository extends JpaRepository<InsuredPerson, UUID> {
    Optional<InsuredPerson> findByInsuranceNumberIgnoreCase(String insuranceNumber);
    boolean existsByInsuranceNumberIgnoreCase(String insuranceNumber);
}
