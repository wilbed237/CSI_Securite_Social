package com.csi.profile.application.service;

import com.csi.common.domain.BusinessException;
import com.csi.profile.application.dto.ProfileDtos.*;
import com.csi.profile.application.mapper.ProfileMapper;
import com.csi.profile.domain.model.Doctor;
import com.csi.profile.domain.model.DoctorType;
import com.csi.profile.domain.model.InsuredStatus;
import com.csi.profile.domain.model.InsuredPerson;
import com.csi.profile.domain.model.SocialAgent;
import com.csi.profile.infrastructure.persistence.DoctorRepository;
import com.csi.profile.infrastructure.persistence.InsuredPersonRepository;
import com.csi.profile.infrastructure.persistence.SocialAgentRepository;
import com.csi.profile.infrastructure.persistence.PrimaryDoctorAssignmentRepository;
import com.csi.profile.config.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contient les cas d utilisation de gestion des profils et applique les regles d integrite metier.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final InsuredPersonRepository insuredRepository;
    private final DoctorRepository doctorRepository;
    private final SocialAgentRepository socialAgentRepository;
    private final PrimaryDoctorAssignmentRepository assignmentRepository;
    private final ProfileMapper mapper;
    private final SensitiveDataCipher sensitiveDataCipher;
    private final AuditService auditService;

    /**
     * Inscrit un nouvel assure apres controle d unicite du numero d assurance.
     */
    @Transactional
    public InsuredResponse registerInsured(CreateInsuredRequest request, AuthenticatedUser actor) {
        if (insuredRepository.existsByInsuranceNumberIgnoreCase(request.insuranceNumber())) {
            throw new BusinessException("INSURED_ALREADY_EXISTS", "L'assure existe deja");
        }
        InsuredPerson insured = mapper.toEntity(request);
        insured.setCountryCode(defaultCountry(request.countryCode()));
        insured.setPreferredPaymentType(request.preferredPaymentType() == null ? com.csi.profile.domain.model.PaymentPreference.CASH : request.preferredPaymentType());
        insured.setBankAccountEncrypted(sensitiveDataCipher.encrypt(request.bankIban()));
        InsuredPerson saved = insuredRepository.save(insured);
        auditService.record(actor, "INSURED_CREATED", "INSURED", saved.getInsuranceNumber(), null);
        return toInsuredResponse(saved);
    }

    public InsuredResponse registerInsured(CreateInsuredRequest request) {
        return registerInsured(request, null);
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
     * Cree le profil metier correspondant a un compte nouvellement inscrit.
     */
    @Transactional
    public ActorProfileResponse createActorProfile(CreateActorProfileRequest request) {
        String actorType = normalize(request.actorType());
        if ("DOCTOR".equals(actorType)) {
            DoctorResponse doctor = createDoctorProfile(request);
            return new ActorProfileResponse("DOCTOR", doctor, null);
        }
        if (isAgentActor(actorType)) {
            SocialAgentResponse agent = createSocialAgentProfile(request);
            return new ActorProfileResponse("SOCIAL_AGENT", null, agent);
        }
        throw new BusinessException("ACTOR_TYPE_UNSUPPORTED", "Type d'acteur non supporte");
    }

    /**
     * Associe un medecin traitant generaliste a un assure existant.
     */
    @Transactional
    public InsuredResponse assignTreatingDoctor(String insuranceNumber, AssignTreatingDoctorRequest request) {
        return assignTreatingDoctor(insuranceNumber, request, null);
    }

    @Transactional
    public InsuredResponse assignTreatingDoctor(String insuranceNumber, AssignTreatingDoctorRequest request, AuthenticatedUser actor) {
        var insured = insuredRepository.findByInsuranceNumberIgnoreCase(insuranceNumber)
                .orElseThrow(() -> new BusinessException("INSURED_NOT_FOUND", "Assure introuvable"));
        Doctor doctor = doctorRepository.findByMatriculeIgnoreCase(request.doctorMatricule())
                .orElseThrow(() -> new BusinessException("DOCTOR_NOT_FOUND", "Medecin introuvable"));
        if (doctor.getType() != DoctorType.GENERALIST) {
            throw new BusinessException("TREATING_DOCTOR_MUST_BE_GENERALIST", "Le medecin traitant doit etre generaliste");
        }
        if (!doctor.isActive()) {
            throw new BusinessException("TREATING_DOCTOR_INACTIVE", "Le medecin traitant doit etre actif");
        }
        assignmentRepository.findByInsuredIdAndEndedAtIsNull(insured.getId()).ifPresent(current -> {
            current.setEndedAt(Instant.now());
            assignmentRepository.save(current);
        });
        assignmentRepository.save(com.csi.profile.domain.model.PrimaryDoctorAssignment.builder()
                .insured(insured).doctor(doctor).assignedByUserId(actor == null ? null : actor.userId()).build());
        insured.setTreatingDoctor(doctor);
        auditService.record(actor, "PRIMARY_DOCTOR_ASSIGNED", "INSURED", insuranceNumber, "doctor=" + doctor.getMatricule());
        return toInsuredResponse(insured);
    }

    @Transactional(readOnly = true)
    public List<PrimaryDoctorAssignmentResponse> primaryDoctorHistory(String insuranceNumber) {
        InsuredPerson insured = loadInsured(insuranceNumber);
        return assignmentRepository.findByInsuredIdOrderByStartedAtDesc(insured.getId()).stream()
                .map(item -> new PrimaryDoctorAssignmentResponse(item.getId(), mapper.toDoctorResponse(item.getDoctor()), item.getStartedAt(), item.getEndedAt(), item.getAssignedByUserId()))
                .toList();
    }

    @Transactional
    public InsuredResponse updateInsured(String insuranceNumber, UpdateInsuredRequest request, AuthenticatedUser actor) {
        InsuredPerson insured = loadInsured(insuranceNumber);
        insured.setFirstName(request.firstName());
        insured.setLastName(request.lastName());
        insured.setBirthDate(request.birthDate());
        insured.setAddress(request.address());
        insured.setPhoneNumber(request.phoneNumber());
        insured.setEmail(request.email());
        insured.setCountryCode(defaultCountry(request.countryCode()));
        if (request.preferredPaymentType() != null) insured.setPreferredPaymentType(request.preferredPaymentType());
        if (request.bankIban() != null) insured.setBankAccountEncrypted(sensitiveDataCipher.encrypt(request.bankIban()));
        auditService.record(actor, "INSURED_UPDATED", "INSURED", insuranceNumber, null);
        return toInsuredResponse(insured);
    }

    @Transactional
    public InsuredResponse updateInsuredStatus(String insuranceNumber, boolean active, AuthenticatedUser actor) {
        InsuredPerson insured = loadInsured(insuranceNumber);
        insured.setStatus(active ? InsuredStatus.ACTIVE : InsuredStatus.SUSPENDED);
        auditService.record(actor, "INSURED_STATUS_CHANGED", "INSURED", insuranceNumber, "active=" + active);
        return toInsuredResponse(insured);
    }

    @Transactional
    public DoctorResponse updateDoctorStatus(String matricule, boolean active, AuthenticatedUser actor) {
        Doctor doctor = doctorRepository.findByMatriculeIgnoreCase(matricule)
                .orElseThrow(() -> new BusinessException("DOCTOR_NOT_FOUND", "Medecin introuvable"));
        doctor.setActive(active);
        auditService.record(actor, "DOCTOR_STATUS_CHANGED", "DOCTOR", matricule, "active=" + active);
        return mapper.toDoctorResponse(doctor);
    }

    @Transactional(readOnly = true)
    public InsuredResponse getInsured(String insuranceNumber) {
        return insuredRepository.findByInsuranceNumberIgnoreCase(insuranceNumber)
                .map(this::toInsuredResponse)
                .orElseThrow(() -> new BusinessException("INSURED_NOT_FOUND", "Assure introuvable"));
    }

    @Transactional(readOnly = true)
    public Page<InsuredResponse> findInsured(InsuredStatus status, String search, Pageable pageable) {
        Specification<InsuredPerson> spec = Specification.where(null);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (search != null && !search.isBlank()) {
            String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("insuranceNumber")), term),
                    cb.like(cb.lower(root.get("firstName")), term),
                    cb.like(cb.lower(root.get("lastName")), term)));
        }
        return insuredRepository.findAll(spec, pageable).map(this::toInsuredResponse);
    }

    @Transactional(readOnly = true)
    public Page<InsuredResponse> findAssignedInsured(UUID doctorAuthUserId, Pageable pageable) {
        return insuredRepository.findByTreatingDoctorAuthUserId(doctorAuthUserId, pageable).map(this::toInsuredResponse);
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

    @Transactional
    public DoctorResponse getDoctorByAuthenticatedUser(UUID authUserId, String email) {
        var linkedDoctor = doctorRepository.findByAuthUserId(authUserId);
        if (linkedDoctor.isPresent()) {
            return mapper.toDoctorResponse(linkedDoctor.get());
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException("DOCTOR_PROFILE_NOT_FOUND", "Profil medecin introuvable pour ce compte");
        }
        Doctor doctor = doctorRepository.findByEmailIgnoreCase(email.trim())
                .filter(candidate -> candidate.getAuthUserId() == null)
                .orElseThrow(() -> new BusinessException("DOCTOR_PROFILE_NOT_FOUND", "Profil medecin introuvable pour ce compte"));
        doctor.setAuthUserId(authUserId);
        return mapper.toDoctorResponse(doctorRepository.save(doctor));
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> findDoctors(DoctorType type, Boolean active, String specialty, Pageable pageable) {
        Specification<Doctor> spec = Specification.where(null);
        if (type != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        if (active != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        if (specialty != null && !specialty.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("specialty")), specialty.trim().toUpperCase(Locale.ROOT)));
        }
        return doctorRepository.findAll(spec, pageable).map(mapper::toDoctorResponse);
    }

    @Transactional(readOnly = true)
    public Page<SocialAgentResponse> findAgents(Pageable pageable) {
        return socialAgentRepository.findAll(pageable).map(mapper::toSocialAgentResponse);
    }

    private DoctorResponse createDoctorProfile(CreateActorProfileRequest request) {
        var existing = doctorRepository.findByAuthUserId(request.authUserId());
        if (existing.isPresent()) return mapper.toDoctorResponse(existing.get());
        DoctorType doctorType = request.doctorType();
        if (doctorType == null) {
            throw new BusinessException("DOCTOR_TYPE_REQUIRED", "Le type de medecin est obligatoire");
        }
        if (doctorType == DoctorType.SPECIALIST && (request.specialty() == null || request.specialty().isBlank())) {
            throw new BusinessException("SPECIALTY_REQUIRED", "La specialite est obligatoire pour un specialiste");
        }
        if (doctorType == DoctorType.GENERALIST && request.specialty() != null && !request.specialty().isBlank()) {
            throw new BusinessException("GENERALIST_SPECIALTY_FORBIDDEN", "Un generaliste ne peut pas porter une specialite");
        }
        Doctor doctor = Doctor.builder()
                .authUserId(request.authUserId())
                .matricule(generateUniqueDoctorMatricule(doctorType, request.username()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .type(doctorType)
                .specialty(blankToNull(request.specialty()))
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .build();
        return mapper.toDoctorResponse(doctorRepository.save(doctor));
    }

    private SocialAgentResponse createSocialAgentProfile(CreateActorProfileRequest request) {
        var existing = socialAgentRepository.findByAuthUserId(request.authUserId());
        if (existing.isPresent()) return mapper.toSocialAgentResponse(existing.get());
        SocialAgent agent = SocialAgent.builder()
                .authUserId(request.authUserId())
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .build();
        return mapper.toSocialAgentResponse(socialAgentRepository.save(agent));
    }

    private boolean isAgentActor(String actorType) {
        return "AGENT".equals(actorType)
                || "SOCIAL_AGENT".equals(actorType)
                || "AGENT_SOCIAL".equals(actorType)
                || "SECURITY_AGENT".equals(actorType)
                || "ADMIN".equals(actorType);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private InsuredPerson loadInsured(String insuranceNumber) {
        return insuredRepository.findByInsuranceNumberIgnoreCase(insuranceNumber)
                .orElseThrow(() -> new BusinessException("INSURED_NOT_FOUND", "Assure introuvable"));
    }

    private String defaultCountry(String value) {
        return value == null || value.isBlank() ? "CM" : value.trim().toUpperCase(Locale.ROOT);
    }

    private InsuredResponse toInsuredResponse(InsuredPerson insured) {
        return new InsuredResponse(insured.getId(), insured.getInsuranceNumber(), insured.getFirstName(), insured.getLastName(), insured.getBirthDate(),
                insured.getAddress(), insured.getPhoneNumber(), insured.getEmail(), insured.getCountryCode(), insured.getPreferredPaymentType(),
                sensitiveDataCipher.maskEncrypted(insured.getBankAccountEncrypted()), insured.getStatus(),
                insured.getTreatingDoctor() == null ? null : mapper.toDoctorResponse(insured.getTreatingDoctor()));
    }

    private String generateUniqueDoctorMatricule(DoctorType doctorType, String username) {
        String prefix = doctorType == DoctorType.GENERALIST ? "MED-GEN" : "MED-SPE";
        String suffix = username.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (suffix.length() > 12) {
            suffix = suffix.substring(0, 12);
        }
        String base = prefix + "-" + suffix;
        String matricule = base;
        int index = 2;
        while (doctorRepository.existsByMatriculeIgnoreCase(matricule)) {
            matricule = base + "-" + index;
            index++;
        }
        return matricule;
    }
}
