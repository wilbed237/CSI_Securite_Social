package com.csi.profile.application.service;

import com.csi.common.domain.BusinessException;
import com.csi.profile.application.dto.ProfileDtos.*;
import com.csi.profile.application.mapper.ProfileMapper;
import com.csi.profile.domain.model.Doctor;
import com.csi.profile.domain.model.DoctorType;
import com.csi.profile.domain.model.InsuredStatus;
import com.csi.profile.infrastructure.persistence.DoctorRepository;
import com.csi.profile.infrastructure.persistence.InsuredPersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contient les cas d utilisation de gestion des profils et applique les regles d integrite metier.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final InsuredPersonRepository insuredRepository;
    private final DoctorRepository doctorRepository;
    private final ProfileMapper mapper;

    /**
     * Inscrit un nouvel assure apres controle d unicite du numero d assurance.
     */
    @Transactional
    public InsuredResponse registerInsured(CreateInsuredRequest request) {
        if (insuredRepository.existsByInsuranceNumberIgnoreCase(request.insuranceNumber())) {
            throw new BusinessException("INSURED_ALREADY_EXISTS", "L'assure existe deja");
        }
        return mapper.toInsuredResponse(insuredRepository.save(mapper.toEntity(request)));
    }

    /**
     * Enregistre un medecin en respectant l exclusivite generaliste/specialiste.
     */
    @Transactional
    public DoctorResponse registerDoctor(CreateDoctorRequest request) {
        if (doctorRepository.existsByMatriculeIgnoreCase(request.matricule())) {
            throw new BusinessException("DOCTOR_ALREADY_EXISTS", "Le medecin existe deja");
        }
        if (request.type() == DoctorType.SPECIALIST && (request.specialty() == null || request.specialty().isBlank())) {
            throw new BusinessException("SPECIALTY_REQUIRED", "La specialite est obligatoire pour un specialiste");
        }
        if (request.type() == DoctorType.GENERALIST && request.specialty() != null && !request.specialty().isBlank()) {
            throw new BusinessException("GENERALIST_SPECIALTY_FORBIDDEN", "Un generaliste ne peut pas porter une specialite");
        }
        return mapper.toDoctorResponse(doctorRepository.save(mapper.toEntity(request)));
    }

    /**
     * Associe un medecin traitant generaliste a un assure existant.
     */
    @Transactional
    public InsuredResponse assignTreatingDoctor(String insuranceNumber, AssignTreatingDoctorRequest request) {
        var insured = insuredRepository.findByInsuranceNumberIgnoreCase(insuranceNumber)
                .orElseThrow(() -> new BusinessException("INSURED_NOT_FOUND", "Assure introuvable"));
        Doctor doctor = doctorRepository.findByMatriculeIgnoreCase(request.doctorMatricule())
                .orElseThrow(() -> new BusinessException("DOCTOR_NOT_FOUND", "Medecin introuvable"));
        if (doctor.getType() != DoctorType.GENERALIST) {
            throw new BusinessException("TREATING_DOCTOR_MUST_BE_GENERALIST", "Le medecin traitant doit etre generaliste");
        }
        insured.setTreatingDoctor(doctor);
        return mapper.toInsuredResponse(insured);
    }

    @Transactional(readOnly = true)
    public InsuredResponse getInsured(String insuranceNumber) {
        return insuredRepository.findByInsuranceNumberIgnoreCase(insuranceNumber)
                .map(mapper::toInsuredResponse)
                .orElseThrow(() -> new BusinessException("INSURED_NOT_FOUND", "Assure introuvable"));
    }

    @Transactional(readOnly = true)
    public InsuredStatusResponse getInsuredStatus(String insuranceNumber) {
        return insuredRepository.findByInsuranceNumberIgnoreCase(insuranceNumber)
                .map(i -> new InsuredStatusResponse(i.getInsuranceNumber(), i.getStatus() == InsuredStatus.ACTIVE, i.getStatus()))
                .orElse(new InsuredStatusResponse(insuranceNumber, false, null));
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctor(String matricule) {
        return doctorRepository.findByMatriculeIgnoreCase(matricule)
                .map(mapper::toDoctorResponse)
                .orElseThrow(() -> new BusinessException("DOCTOR_NOT_FOUND", "Medecin introuvable"));
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> findDoctors(DoctorType type, Pageable pageable) {
        Page<Doctor> page = type == null ? doctorRepository.findAll(pageable) : doctorRepository.findByType(type, pageable);
        return page.map(mapper::toDoctorResponse);
    }
}
