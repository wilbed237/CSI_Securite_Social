package com.csi.medical.infrastructure.persistence;

import com.csi.medical.domain.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository Spring Data des medicaments prescrits.
 */
public interface MedicationRepository extends JpaRepository<Medication, UUID> {}
