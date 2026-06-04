package com.csi.medical.infrastructure.persistence;

import com.csi.medical.domain.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {}
