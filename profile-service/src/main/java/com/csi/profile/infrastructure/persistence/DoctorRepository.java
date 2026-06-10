package com.csi.profile.infrastructure.persistence;

import com.csi.profile.domain.model.Doctor;
import com.csi.profile.domain.model.DoctorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data pour les medecins et leurs recherches metier.
 */
public interface DoctorRepository extends JpaRepository<Doctor, UUID>, JpaSpecificationExecutor<Doctor> {
    Optional<Doctor> findByMatriculeIgnoreCase(String matricule);
    boolean existsByMatriculeIgnoreCase(String matricule);
    boolean existsByAuthUserId(UUID authUserId);
    Optional<Doctor> findByAuthUserId(UUID authUserId);
    Optional<Doctor> findByEmailIgnoreCase(String email);
    Page<Doctor> findByType(DoctorType type, Pageable pageable);
    long countByType(DoctorType type);
    long countByActiveTrue();
    List<Doctor> findTop5ByOrderByCreatedAtDesc();
}
