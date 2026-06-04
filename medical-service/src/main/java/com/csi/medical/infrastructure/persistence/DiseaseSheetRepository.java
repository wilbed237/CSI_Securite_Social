package com.csi.medical.infrastructure.persistence;

import com.csi.medical.domain.model.DiseaseSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiseaseSheetRepository extends JpaRepository<DiseaseSheet, UUID> {
    Optional<DiseaseSheet> findBySheetNumberIgnoreCase(String sheetNumber);
}
