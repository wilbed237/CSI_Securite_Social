package com.csi.profile.infrastructure.persistence;

import com.csi.profile.domain.model.Doctor;
import com.csi.profile.domain.model.DoctorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    Optional<Doctor> findByMatriculeIgnoreCase(String matricule);
    boolean existsByMatriculeIgnoreCase(String matricule);
    Page<Doctor> findByType(DoctorType type, Pageable pageable);
}
