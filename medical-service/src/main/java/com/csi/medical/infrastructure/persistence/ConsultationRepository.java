package com.csi.medical.infrastructure.persistence;

import com.csi.medical.domain.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository Spring Data des consultations medicales.
 */
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {}
