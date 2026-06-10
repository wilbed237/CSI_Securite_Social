package com.csi.medical.application.service;

import com.csi.common.domain.BusinessException;
import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.application.mapper.MedicalMapper;
import com.csi.medical.config.AuthenticatedUser;
import com.csi.medical.domain.model.*;
import com.csi.medical.infrastructure.client.ProfileClient;
import com.csi.medical.infrastructure.client.ReimbursementClient;
import com.csi.medical.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalService {
    private final ConsultationRepository consultations;
    private final PrescriptionRepository prescriptions;
    private final DiseaseSheetRepository diseaseSheets;
    private final SpecialistReferralRepository referrals;
    private final MedicalAuditEventRepository auditEvents;
    private final MedicalMapper mapper;
    private final ProfileClient profileClient;
    private final ReimbursementClient reimbursementClient;
    private final DiseaseSheetPdfService pdfService;

    @Transactional
    public ConsultationResponse createConsultation(CreateConsultationRequest request, AuthenticatedUser actor) {
        if (!request.endedAt().isAfter(request.startedAt())) {
            throw new BusinessException("CONSULTATION_PERIOD_INVALID", "La fin de consultation doit etre apres le debut");
        }
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            var existing = consultations.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) return mapper.toResponse(existing.get());
        }
        profileClient.ensureActiveInsured(request.insuranceNumber());
        ProfileClient.DoctorData doctor = profileClient.currentDoctor();
        DoctorType doctorType = DoctorType.valueOf(doctor.type());
        Consultation consultation = Consultation.builder()
                .insuranceNumber(request.insuranceNumber())
                .doctorMatricule(doctor.matricule())
                .doctorType(doctorType)
                .startedAt(request.startedAt())
                .endedAt(request.endedAt())
                .cost(request.cost())
                .consultationType(blankToDefault(request.consultationType(), "GENERAL"))
                .reason(blankToDefault(request.reason(), "CONSULTATION"))
                .observations(request.observations())
                .diagnosis(request.diagnosis())
                .conclusion(request.conclusion())
                .status(request.status() == null ? ConsultationStatus.DRAFT : request.status())
                .createdByUserId(actor.userId())
                .idempotencyKey(request.idempotencyKey())
                .build();
        consultation = consultations.save(consultation);
        audit(actor, "CONSULTATION_CREATED", "CONSULTATION", consultation.getId().toString(), "SUCCESS", null);
        return mapper.toResponse(consultation);
    }

    @Transactional(readOnly = true)
    public Page<ConsultationResponse> listConsultations(String search, String patientId, String doctorId,
            DoctorType doctorType, ConsultationStatus status, LocalDate startDate, LocalDate endDate,
            Pageable pageable, AuthenticatedUser actor) {
        ensureMedicalReadRole(actor);
        Specification<Consultation> spec = Specification.where(null);
        if (!isMedicalAgent(actor)) spec = spec.and((root, query, cb) -> cb.equal(root.get("createdByUserId"), actor.userId()));
        if (patientId != null && !patientId.isBlank()) spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("insuranceNumber")), patientId.trim().toUpperCase(Locale.ROOT)));
        if (doctorId != null && !doctorId.isBlank()) spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("doctorMatricule")), doctorId.trim().toUpperCase(Locale.ROOT)));
        if (doctorType != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("doctorType"), doctorType));
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (startDate != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startedAt"), startDate.atStartOfDay()));
        if (endDate != null) spec = spec.and((root, query, cb) -> cb.lessThan(root.get("startedAt"), endDate.plusDays(1).atStartOfDay()));
        if (search != null && !search.isBlank()) {
            String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            UUID exactId = parseUuid(search);
            spec = spec.and((root, query, cb) -> {
                var values = new ArrayList<jakarta.persistence.criteria.Predicate>();
                values.add(cb.like(cb.lower(root.get("insuranceNumber")), term));
                values.add(cb.like(cb.lower(root.get("doctorMatricule")), term));
                values.add(cb.like(cb.lower(root.get("reason")), term));
                if (exactId != null) values.add(cb.equal(root.get("id"), exactId));
                return cb.or(values.toArray(jakarta.persistence.criteria.Predicate[]::new));
            });
        }
        return consultations.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getConsultation(UUID id, AuthenticatedUser actor) {
        Consultation consultation = loadConsultation(id);
        ensureConsultationAccess(consultation, actor);
        return mapper.toResponse(consultation);
    }

    @Transactional
    public ConsultationResponse updateConsultation(UUID id, UpdateConsultationRequest request, AuthenticatedUser actor) {
        Consultation consultation = ownedConsultation(id, actor);
        ensureVersion(request.version(), consultation.getVersion());
        if (!List.of(ConsultationStatus.DRAFT, ConsultationStatus.IN_PROGRESS).contains(consultation.getStatus())) {
            throw new BusinessException("CONSULTATION_NOT_EDITABLE", "Cette consultation n'est plus modifiable");
        }
        LocalDateTime start = request.startedAt() == null ? consultation.getStartedAt() : request.startedAt();
        LocalDateTime end = request.endedAt() == null ? consultation.getEndedAt() : request.endedAt();
        if (!end.isAfter(start)) throw new BusinessException("CONSULTATION_PERIOD_INVALID", "La fin de consultation doit etre apres le debut");
        if (request.startedAt() != null) consultation.setStartedAt(request.startedAt());
        if (request.endedAt() != null) consultation.setEndedAt(request.endedAt());
        if (request.cost() != null) consultation.setCost(request.cost());
        if (request.consultationType() != null) consultation.setConsultationType(request.consultationType());
        if (request.reason() != null) consultation.setReason(request.reason());
        if (request.observations() != null) consultation.setObservations(request.observations());
        if (request.diagnosis() != null) consultation.setDiagnosis(request.diagnosis());
        if (request.conclusion() != null) consultation.setConclusion(request.conclusion());
        if (request.status() != null) {
            if (!List.of(ConsultationStatus.DRAFT, ConsultationStatus.IN_PROGRESS, ConsultationStatus.COMPLETED).contains(request.status())) {
                throw new BusinessException("CONSULTATION_TRANSITION_INVALID", "Transition de statut interdite");
            }
            consultation.setStatus(request.status());
        }
        consultation.setUpdatedByUserId(actor.userId());
        audit(actor, "CONSULTATION_UPDATED", "CONSULTATION", id.toString(), "SUCCESS", "fields=medical-record");
        return mapper.toResponse(consultation);
    }

    @Transactional
    public ConsultationResponse changeConsultationStatus(UUID id, ConsultationStatus status, long version, AuthenticatedUser actor) {
        Consultation consultation = ownedConsultation(id, actor);
        ensureVersion(version, consultation.getVersion());
        if (consultation.getStatus() == ConsultationStatus.CANCELLED || consultation.getStatus() == ConsultationStatus.ARCHIVED) {
            throw new BusinessException("CONSULTATION_TRANSITION_INVALID", "Cette consultation est deja fermee");
        }
        if (diseaseSheets.findByConsultationId(id).map(this::isReimbursedSheet).orElse(false)) {
            throw new BusinessException("CONSULTATION_REIMBURSED", "Une consultation remboursee ne peut plus etre modifiee");
        }
        consultation.setStatus(status);
        consultation.setUpdatedByUserId(actor.userId());
        audit(actor, "CONSULTATION_UPDATED", "CONSULTATION", id.toString(), "SUCCESS", "status=" + status);
        return mapper.toResponse(consultation);
    }

    @Transactional
    public PrescriptionResponse prescribeMedication(CreateMedicationPrescriptionRequest request, AuthenticatedUser actor) {
        Consultation consultation = ownedConsultation(request.consultationId(), actor);
        Prescription prescription = Prescription.builder()
                .prescriptionNumber("PM-" + UUID.randomUUID())
                .type(PrescriptionType.MEDICATION)
                .consultation(consultation)
                .notes(request.notes())
                .status(PrescriptionStatus.ACTIVE)
                .createdByUserId(actor.userId())
                .build();
        request.medications().forEach(item -> prescription.getMedications().add(Medication.builder()
                .name(item.name()).posology(item.posology()).frequency(item.frequency()).duration(item.duration())
                .quantity(item.quantity()).administrationRoute(item.administrationRoute())
                .instructions(item.instructions()).prescription(prescription).build()));
        Prescription saved = prescriptions.save(prescription);
        audit(actor, "PRESCRIPTION_CREATED", "PRESCRIPTION", saved.getId().toString(), "SUCCESS", null);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> listPrescriptions(String search, String patientId, String doctorId,
            UUID consultationId, UUID diseaseSheetId, PrescriptionStatus status, LocalDate startDate,
            LocalDate endDate, String medicationName, Pageable pageable, AuthenticatedUser actor) {
        ensureMedicalReadRole(actor);
        Specification<Prescription> spec = Specification.where(null);
        if (!isMedicalAgent(actor)) spec = spec.and((root, query, cb) -> cb.equal(root.get("consultation").get("createdByUserId"), actor.userId()));
        if (patientId != null && !patientId.isBlank()) spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("consultation").get("insuranceNumber")), patientId.trim().toUpperCase(Locale.ROOT)));
        if (doctorId != null && !doctorId.isBlank()) spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("consultation").get("doctorMatricule")), doctorId.trim().toUpperCase(Locale.ROOT)));
        if (consultationId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("consultation").get("id"), consultationId));
        if (diseaseSheetId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("diseaseSheetId"), diseaseSheetId));
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (startDate != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("prescriptionDate"), startDate));
        if (endDate != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("prescriptionDate"), endDate));
        if (medicationName != null && !medicationName.isBlank()) {
            String term = "%" + medicationName.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                return cb.like(cb.lower(root.join("medications").get("name")), term);
            });
        }
        if (search != null && !search.isBlank()) {
            String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            UUID exactId = parseUuid(search);
            spec = spec.and((root, query, cb) -> {
                var values = new ArrayList<jakarta.persistence.criteria.Predicate>();
                values.add(cb.like(cb.lower(root.get("prescriptionNumber")), term));
                values.add(cb.like(cb.lower(root.get("consultation").get("insuranceNumber")), term));
                values.add(cb.like(cb.lower(root.get("consultation").get("doctorMatricule")), term));
                if (exactId != null) values.add(cb.equal(root.get("id"), exactId));
                return cb.or(values.toArray(jakarta.persistence.criteria.Predicate[]::new));
            });
        }
        return prescriptions.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescription(UUID id, AuthenticatedUser actor) {
        Prescription prescription = loadPrescription(id);
        ensureConsultationAccess(prescription.getConsultation(), actor);
        return mapper.toResponse(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getConsultationPrescriptions(UUID consultationId, AuthenticatedUser actor) {
        Consultation consultation = loadConsultation(consultationId);
        ensureConsultationAccess(consultation, actor);
        return prescriptions.findByConsultationId(consultationId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PrescriptionResponse updatePrescription(UUID id, UpdatePrescriptionRequest request, AuthenticatedUser actor) {
        Prescription prescription = loadPrescription(id);
        ownedConsultation(prescription.getConsultation().getId(), actor);
        ensureVersion(request.version(), prescription.getVersion());
        if (!List.of(PrescriptionStatus.DRAFT, PrescriptionStatus.ACTIVE).contains(prescription.getStatus())) {
            throw new BusinessException("PRESCRIPTION_NOT_EDITABLE", "Cette ordonnance est finalisee ou annulee");
        }
        if (prescription.getDiseaseSheetId() != null) {
            DiseaseSheet sheet = diseaseSheets.findById(prescription.getDiseaseSheetId()).orElse(null);
            if (sheet != null && !List.of(DiseaseSheetStatus.DRAFT, DiseaseSheetStatus.ISSUED).contains(sheet.getStatus())) {
                throw new BusinessException("PRESCRIPTION_LOCKED", "L'ordonnance est verrouillee par le traitement administratif");
            }
        }
        if (request.prescriptionDate() != null) prescription.setPrescriptionDate(request.prescriptionDate());
        if (request.notes() != null) prescription.setNotes(request.notes());
        if (request.status() != null) prescription.setStatus(request.status());
        if (request.medications() != null) {
            if (prescription.getType() != PrescriptionType.MEDICATION) throw new BusinessException("PRESCRIPTION_TYPE_INVALID", "Cette prescription ne contient pas de medicaments");
            prescription.getMedications().clear();
            request.medications().forEach(item -> prescription.getMedications().add(Medication.builder()
                    .name(item.name()).posology(item.posology()).frequency(item.frequency()).duration(item.duration())
                    .quantity(item.quantity()).administrationRoute(item.administrationRoute())
                    .instructions(item.instructions()).prescription(prescription).build()));
        }
        prescription.setUpdatedByUserId(actor.userId());
        audit(actor, "PRESCRIPTION_UPDATED", "PRESCRIPTION", id.toString(), "SUCCESS", "fields=prescription");
        return mapper.toResponse(prescription);
    }

    /** Contrat historique conserve pour les clients existants. */
    @Transactional
    public PrescriptionResponse prescribeSpecialistConsultation(CreateSpecialistReferralRequest request, AuthenticatedUser actor) {
        Consultation consultation = ownedConsultation(request.consultationId(), actor);
        requireGeneralist(consultation);
        Prescription prescription = Prescription.builder()
                .prescriptionNumber("PC-" + UUID.randomUUID())
                .type(PrescriptionType.SPECIALIST_CONSULTATION)
                .consultation(consultation)
                .requiredSpecialty(request.requiredSpecialty())
                .factors(request.factors())
                .status(PrescriptionStatus.ACTIVE)
                .createdByUserId(actor.userId())
                .build();
        prescription = prescriptions.save(prescription);
        audit(actor, "SPECIALIST_REFERRAL_CREATED", "PRESCRIPTION", prescription.getId().toString(), "SUCCESS", null);
        return mapper.toResponse(prescription);
    }

    @Transactional
    public ReferralResponse createReferral(CreateReferralRequest request, AuthenticatedUser actor) {
        Consultation consultation = ownedConsultation(request.consultationId(), actor);
        requireGeneralist(consultation);
        List<String> targetMatricules = request.specialistMatricules().stream().distinct().toList();
        for (String matricule : targetMatricules) {
            ProfileClient.DoctorData specialist = profileClient.doctor(matricule);
            if (!specialist.active() || !"SPECIALIST".equals(specialist.type())) {
                throw new BusinessException("SPECIALIST_NOT_ACTIVE", "Le medecin selectionne n'est pas un specialiste actif");
            }
            if (specialist.specialty() == null || !specialist.specialty().equalsIgnoreCase(request.specialty())) {
                throw new BusinessException("SPECIALTY_MISMATCH", "La specialite du medecin ne correspond pas a l'orientation");
            }
        }
        SpecialistReferral referral = SpecialistReferral.builder()
                .referralNumber("OR-" + LocalDate.now().getYear() + "-" + UUID.randomUUID())
                .consultation(consultation).specialty(request.specialty()).reason(request.reason())
                .priority(request.priority()).status(ReferralStatus.PENDING)
                .targetDoctorMatricules(targetMatricules).createdByUserId(actor.userId()).build();
        referral = referrals.save(referral);
        audit(actor, "SPECIALIST_REFERRAL_CREATED", "SPECIALIST_REFERRAL", referral.getId().toString(), "SUCCESS", null);
        return toReferralResponse(referral);
    }

    @Transactional(readOnly = true)
    public ReferralResponse getReferral(String referralNumber, AuthenticatedUser actor) {
        SpecialistReferral referral = referrals.findByReferralNumberIgnoreCase(referralNumber)
                .orElseThrow(() -> new BusinessException("REFERRAL_NOT_FOUND", "Orientation introuvable"));
        ensureReferralAccess(referral, actor);
        return toReferralResponse(referral);
    }

    @Transactional
    public ReferralResponse updateReferralStatus(String referralNumber, UpdateReferralStatusRequest request, AuthenticatedUser actor) {
        SpecialistReferral referral = referrals.findByReferralNumberIgnoreCase(referralNumber)
                .orElseThrow(() -> new BusinessException("REFERRAL_NOT_FOUND", "Orientation introuvable"));
        ensureReferralAccess(referral, actor);
        if (!isValidReferralTransition(referral.getStatus(), request.status())) {
            throw new BusinessException("REFERRAL_TRANSITION_INVALID", "Transition de statut d'orientation interdite");
        }
        referral.setStatus(request.status());
        referral = referrals.save(referral);
        audit(actor, "REFERRAL_STATUS_CHANGED", "SPECIALIST_REFERRAL", referral.getId().toString(), "SUCCESS", request.status().name());
        return toReferralResponse(referral);
    }

    @Transactional
    public DiseaseSheetResponse createDiseaseSheet(CreateDiseaseSheetRequest request, AuthenticatedUser actor) {
        Consultation consultation = ownedConsultation(request.consultationId(), actor);
        var existing = diseaseSheets.findByConsultationId(consultation.getId());
        if (existing.isPresent()) return mapper.toResponse(existing.get());
        DiseaseSheet sheet = DiseaseSheet.builder()
                .sheetNumber("FM-" + LocalDate.now().getYear() + "-" + UUID.randomUUID())
                .consultation(consultation)
                .prescriptionId(resolvePrescriptionId(request.prescriptionId(), consultation))
                .patientId(consultation.getInsuranceNumber())
                .doctorId(consultation.getDoctorMatricule())
                .doctorType(consultation.getDoctorType())
                .specialty(request.specialty())
                .consultationAmount(consultation.getCost())
                .consultationDate(consultation.getStartedAt())
                .registrationDate(LocalDate.now())
                .diagnosis(request.diagnosis())
                .medicalConclusion(blankToDefault(request.medicalConclusion(), consultation.getConclusion()))
                .status(DiseaseSheetStatus.ISSUED)
                .createdByUserId(actor.userId())
                .build();
        sheet = diseaseSheets.save(sheet);
        if (sheet.getPrescriptionId() != null) {
            Prescription linked = loadPrescription(sheet.getPrescriptionId());
            linked.setDiseaseSheetId(sheet.getId());
        }
        consultation.setStatus(ConsultationStatus.COMPLETED);
        consultation.setUpdatedByUserId(actor.userId());
        audit(actor, "DISEASE_SHEET_CREATED", "DISEASE_SHEET", sheet.getId().toString(), "SUCCESS", null);
        return mapper.toResponse(sheet);
    }

    @Transactional(readOnly = true)
    public Page<DiseaseSheetResponse> listDiseaseSheets(String search, String patientId, String doctorId,
            DoctorType doctorType, DiseaseSheetStatus status, Boolean hasReimbursement, LocalDate startDate,
            LocalDate endDate, Pageable pageable, AuthenticatedUser actor) {
        ensureMedicalReadRole(actor);
        Specification<DiseaseSheet> spec = Specification.where(null);
        if (!isMedicalAgent(actor)) spec = spec.and((root, query, cb) -> cb.equal(root.get("consultation").get("createdByUserId"), actor.userId()));
        if (patientId != null && !patientId.isBlank()) spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("patientId")), patientId.trim().toUpperCase(Locale.ROOT)));
        if (doctorId != null && !doctorId.isBlank()) spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("doctorId")), doctorId.trim().toUpperCase(Locale.ROOT)));
        if (doctorType != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("doctorType"), doctorType));
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (Boolean.TRUE.equals(hasReimbursement)) spec = spec.and((root, query, cb) -> cb.isNotNull(root.get("reimbursementNumber")));
        if (Boolean.FALSE.equals(hasReimbursement)) spec = spec.and((root, query, cb) -> cb.isNull(root.get("reimbursementNumber")));
        if (startDate != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("consultationDate"), startDate.atStartOfDay()));
        if (endDate != null) spec = spec.and((root, query, cb) -> cb.lessThan(root.get("consultationDate"), endDate.plusDays(1).atStartOfDay()));
        if (search != null && !search.isBlank()) {
            String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            UUID exactId = parseUuid(search);
            spec = spec.and((root, query, cb) -> {
                var values = new ArrayList<jakarta.persistence.criteria.Predicate>();
                values.add(cb.like(cb.lower(root.get("sheetNumber")), term));
                values.add(cb.like(cb.lower(root.get("patientId")), term));
                values.add(cb.like(cb.lower(root.get("doctorId")), term));
                if (exactId != null) values.add(cb.equal(root.get("id"), exactId));
                return cb.or(values.toArray(jakarta.persistence.criteria.Predicate[]::new));
            });
        }
        return diseaseSheets.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DiseaseSheetResponse getDiseaseSheet(UUID id, AuthenticatedUser actor) {
        DiseaseSheet sheet = diseaseSheets.findById(id)
                .orElseThrow(() -> new BusinessException("DISEASE_SHEET_NOT_FOUND", "Feuille de maladie introuvable"));
        ensureSheetAccess(sheet, actor);
        return mapper.toResponse(sheet);
    }

    @Transactional(readOnly = true)
    public DiseaseSheetResponse getDiseaseSheetByConsultation(UUID consultationId, AuthenticatedUser actor) {
        DiseaseSheet sheet = diseaseSheets.findByConsultationId(consultationId)
                .orElseThrow(() -> new BusinessException("DISEASE_SHEET_NOT_FOUND", "Feuille de maladie introuvable"));
        ensureSheetAccess(sheet, actor);
        return mapper.toResponse(sheet);
    }

    @Transactional(readOnly = true)
    public DiseaseSheetResponse getDiseaseSheetByPrescription(UUID prescriptionId, AuthenticatedUser actor) {
        DiseaseSheet sheet = diseaseSheets.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new BusinessException("DISEASE_SHEET_NOT_FOUND", "Feuille de maladie introuvable"));
        ensureSheetAccess(sheet, actor);
        return mapper.toResponse(sheet);
    }

    @Transactional
    public DiseaseSheetResponse updateDiseaseSheet(UUID id, UpdateDiseaseSheetRequest request, AuthenticatedUser actor) {
        DiseaseSheet sheet = diseaseSheets.findById(id)
                .orElseThrow(() -> new BusinessException("DISEASE_SHEET_NOT_FOUND", "Feuille de maladie introuvable"));
        ensureVersion(request.version(), sheet.getVersion());
        if (isReimbursedSheet(sheet) || List.of(DiseaseSheetStatus.PAID, DiseaseSheetStatus.COMPLETED, DiseaseSheetStatus.CANCELLED).contains(sheet.getStatus())) {
            throw new BusinessException("DISEASE_SHEET_LOCKED", "Une feuille finalisee ou remboursee ne peut plus etre modifiee");
        }
        if (isDoctor(actor)) {
            ensureSheetAccess(sheet, actor);
            if (!List.of(DiseaseSheetStatus.DRAFT, DiseaseSheetStatus.ISSUED).contains(sheet.getStatus())) {
                throw new BusinessException("DISEASE_SHEET_NOT_EDITABLE", "La partie medicale n'est plus modifiable");
            }
            if (request.diagnosis() != null) sheet.setDiagnosis(request.diagnosis());
            if (request.medicalConclusion() != null) sheet.setMedicalConclusion(request.medicalConclusion());
            if (request.specialty() != null) sheet.setSpecialty(request.specialty());
            if (request.prescriptionId() != null) sheet.setPrescriptionId(resolvePrescriptionId(request.prescriptionId(), sheet.getConsultation()));
            if (request.status() != null && List.of(DiseaseSheetStatus.DRAFT, DiseaseSheetStatus.ISSUED).contains(request.status())) sheet.setStatus(request.status());
        } else if (isMedicalAgent(actor)) {
            if (request.diagnosis() != null || request.medicalConclusion() != null || request.prescriptionId() != null) {
                throw new BusinessException("MEDICAL_FIELDS_READ_ONLY", "Un agent ne peut pas modifier les donnees medicales");
            }
            if (request.status() != null) sheet.setStatus(request.status());
            if (request.reimbursementId() != null) sheet.setReimbursementId(request.reimbursementId());
            if (request.reimbursementNumber() != null) sheet.setReimbursementNumber(request.reimbursementNumber());
            if (request.paymentType() != null) sheet.setPaymentType(request.paymentType());
            if (request.controlComment() != null) sheet.setControlComment(request.controlComment());
        } else {
            throw new BusinessException("MEDICAL_ACCESS_DENIED", "Acces interdit a cette feuille");
        }
        sheet.setUpdatedByUserId(actor.userId());
        audit(actor, "DISEASE_SHEET_UPDATED", "DISEASE_SHEET", id.toString(), "SUCCESS", "fields=authorized");
        return mapper.toResponse(sheet);
    }

    @Transactional(readOnly = true)
    public DiseaseSheetResponse getDiseaseSheet(String sheetNumber, AuthenticatedUser actor) {
        DiseaseSheet sheet = loadSheet(sheetNumber);
        ensureSheetAccess(sheet, actor);
        return mapper.toResponse(sheet);
    }

    @Transactional
    public DiseaseSheetResponse submitDiseaseSheet(String sheetNumber, AuthenticatedUser actor) {
        DiseaseSheet sheet = loadSheet(sheetNumber);
        ensureSheetAccess(sheet, actor);
        if (sheet.getStatus() != DiseaseSheetStatus.ISSUED) {
            throw new BusinessException("DISEASE_SHEET_TRANSITION_INVALID", "Seule une feuille emise peut etre soumise");
        }
        sheet.setStatus(DiseaseSheetStatus.SUBMITTED);
        sheet = diseaseSheets.save(sheet);
        audit(actor, "DISEASE_SHEET_SUBMITTED", "DISEASE_SHEET", sheet.getId().toString(), "SUCCESS", null);
        return mapper.toResponse(sheet);
    }

    /** Prise en contrôle par l'agent (SUBMITTED → UNDER_REVIEW). */
    @Transactional
    public DiseaseSheetResponse completeDiseaseSheet(String sheetNumber, CompleteDiseaseSheetRequest request, AuthenticatedUser actor) {
        requireAgent(actor);
        DiseaseSheet sheet = loadSheet(sheetNumber);
        if (sheet.getStatus() != DiseaseSheetStatus.SUBMITTED && sheet.getStatus() != DiseaseSheetStatus.UNDER_REVIEW) {
            throw new BusinessException("DISEASE_SHEET_TRANSITION_INVALID", "La feuille doit etre soumise avant son controle");
        }
        sheet.setStatus(DiseaseSheetStatus.UNDER_REVIEW);
        sheet.setReceivedAt(Instant.now());
        sheet.setPaymentType(request.paymentType());
        sheet.setControlComment(request.controlComment());
        sheet.setCompletedByUserId(actor.userId());
        sheet.setUpdatedByUserId(actor.userId());
        sheet = diseaseSheets.save(sheet);
        audit(actor, "DISEASE_SHEET_COMPLETED", "DISEASE_SHEET", sheet.getId().toString(), "SUCCESS", request.paymentType());
        return mapper.toResponse(sheet);
    }

    /**
     * Complétion définitive d'une feuille après vérification du paiement.
     * Transition: UNDER_REVIEW | APPROVED | PAID → COMPLETED.
     * Hypothèse: la vérification passe par reimbursement-service.
     */
    @Transactional
    public DiseaseSheetResponse finalizeDiseaseSheet(String sheetNumber, FinalizeDiseaseSheetRequest request, AuthenticatedUser actor) {
        requireAgent(actor);
        DiseaseSheet sheet = loadSheet(sheetNumber);

        if (sheet.getStatus() == DiseaseSheetStatus.COMPLETED) {
            throw new BusinessException("DISEASE_SHEET_ALREADY_COMPLETED", "Cette feuille de maladie a deja ete completee");
        }
        if (!List.of(DiseaseSheetStatus.UNDER_REVIEW, DiseaseSheetStatus.APPROVED, DiseaseSheetStatus.PAID).contains(sheet.getStatus())) {
            throw new BusinessException("DISEASE_SHEET_TRANSITION_INVALID",
                    "La feuille doit etre en cours de traitement pour etre completee (statut actuel : " + sheet.getStatus() + ")");
        }

        ReimbursementClient.ReimbursementData reimbursement = reimbursementClient.getReimbursement(request.reimbursementNumber());

        if (!sheet.getSheetNumber().equalsIgnoreCase(reimbursement.sheetNumber())) {
            throw new BusinessException("REIMBURSEMENT_SHEET_MISMATCH",
                    "Le remboursement ne correspond pas a cette feuille de maladie");
        }
        if (!"EXECUTED".equals(reimbursement.status())) {
            throw new BusinessException("REIMBURSEMENT_NOT_EXECUTED",
                    "Le remboursement n'a pas ete execute (statut actuel : " + reimbursement.status() + ")");
        }
        if (reimbursement.paymentReference() == null || reimbursement.paymentReference().isBlank()) {
            throw new BusinessException("PAYMENT_REFERENCE_MISSING", "La reference de paiement est absente");
        }

        sheet.setStatus(DiseaseSheetStatus.COMPLETED);
        sheet.setReimbursementId(reimbursement.id());
        sheet.setReimbursementNumber(request.reimbursementNumber());
        sheet.setCompletionComment(request.completionComment());
        sheet.setCompletedByUserId(actor.userId());
        sheet.setCompletedAt(Instant.now());
        sheet.setUpdatedByUserId(actor.userId());
        sheet = diseaseSheets.save(sheet);
        audit(actor, "DISEASE_SHEET_COMPLETED", "DISEASE_SHEET", sheet.getId().toString(), "SUCCESS",
                "reimbursement=" + request.reimbursementNumber());
        return mapper.toResponse(sheet);
    }

    @Transactional(readOnly = true)
    public byte[] diseaseSheetPdf(String sheetNumber, AuthenticatedUser actor) {
        DiseaseSheet sheet = loadSheet(sheetNumber);
        ensureSheetAccess(sheet, actor);
        List<Prescription> sheetPrescriptions = prescriptions.findByConsultationId(sheet.getConsultation().getId());
        audit(actor, "DISEASE_SHEET_PDF_GENERATED", "DISEASE_SHEET", sheet.getId().toString(), "SUCCESS", null);
        return pdfService.generate(sheet, sheetPrescriptions);
    }

    private Consultation ownedConsultation(UUID id, AuthenticatedUser actor) {
        Consultation consultation = loadConsultation(id);
        if (!isDoctor(actor) || !actor.userId().equals(consultation.getCreatedByUserId())) {
            throw new BusinessException("MEDICAL_ACCESS_DENIED", "Acces interdit a cette consultation");
        }
        return consultation;
    }

    private void ensureConsultationAccess(Consultation consultation, AuthenticatedUser actor) {
        ensureMedicalReadRole(actor);
        if (!isMedicalAgent(actor) && !actor.userId().equals(consultation.getCreatedByUserId())) {
            throw new BusinessException("MEDICAL_ACCESS_DENIED", "Acces interdit a cette consultation");
        }
    }

    private void ensureSheetAccess(DiseaseSheet sheet, AuthenticatedUser actor) {
        ensureMedicalReadRole(actor);
        if (!isMedicalAgent(actor) && !actor.userId().equals(sheet.getConsultation().getCreatedByUserId())) {
            throw new BusinessException("MEDICAL_ACCESS_DENIED", "Acces interdit a cette feuille");
        }
    }

    private void ensureReferralAccess(SpecialistReferral referral, AuthenticatedUser actor) {
        if (isMedicalAgent(actor) || actor.userId().equals(referral.getCreatedByUserId())) return;
        ProfileClient.DoctorData doctor = profileClient.currentDoctor();
        if (!referral.getTargetDoctorMatricules().contains(doctor.matricule())) {
            throw new BusinessException("MEDICAL_ACCESS_DENIED", "Acces interdit a cette orientation");
        }
    }

    private void requireGeneralist(Consultation consultation) {
        if (consultation.getDoctorType() != DoctorType.GENERALIST) {
            throw new BusinessException("ONLY_GENERALIST_CAN_REFER", "Seul un generaliste peut prescrire une consultation specialisee");
        }
    }

    private boolean isMedicalAgent(AuthenticatedUser actor) {
        return actor.roles().stream().anyMatch(role -> List.of("AGENT", "AGENT_SOCIAL", "SOCIAL_AGENT", "SECURITY_AGENT").contains(role));
    }

    private boolean isDoctor(AuthenticatedUser actor) {
        return actor.roles().stream().anyMatch(role -> List.of("DOCTOR", "GENERALIST", "SPECIALIST").contains(role));
    }

    private void ensureMedicalReadRole(AuthenticatedUser actor) {
        if (!isDoctor(actor) && !isMedicalAgent(actor)) {
            throw new BusinessException("MEDICAL_ACCESS_DENIED", "Acces medical non autorise");
        }
    }

    private void requireAgent(AuthenticatedUser actor) {
        if (!isMedicalAgent(actor)) throw new BusinessException("MEDICAL_ACCESS_DENIED", "Operation reservee a un agent autorise");
    }

    private boolean isValidReferralTransition(ReferralStatus current, ReferralStatus next) {
        return switch (current) {
            case PENDING -> next == ReferralStatus.ACCEPTED || next == ReferralStatus.CANCELLED;
            case ACCEPTED -> next == ReferralStatus.COMPLETED || next == ReferralStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private Consultation loadConsultation(UUID id) {
        return consultations.findById(id).orElseThrow(() -> new BusinessException("CONSULTATION_NOT_FOUND", "Consultation introuvable"));
    }

    private Prescription loadPrescription(UUID id) {
        return prescriptions.findDetailedById(id)
                .orElseThrow(() -> new BusinessException("PRESCRIPTION_NOT_FOUND", "Ordonnance introuvable"));
    }

    private DiseaseSheet loadSheet(String number) {
        UUID id = parseUuid(number);
        if (id != null) {
            return diseaseSheets.findById(id)
                    .orElseThrow(() -> new BusinessException("DISEASE_SHEET_NOT_FOUND", "Feuille de maladie introuvable"));
        }
        return diseaseSheets.findBySheetNumberIgnoreCase(number)
                .orElseThrow(() -> new BusinessException("DISEASE_SHEET_NOT_FOUND", "Feuille de maladie introuvable"));
    }

    private UUID resolvePrescriptionId(UUID requestedId, Consultation consultation) {
        if (requestedId == null) {
            return prescriptions.findByConsultationId(consultation.getId()).stream()
                    .filter(item -> item.getType() == PrescriptionType.MEDICATION)
                    .map(Prescription::getId).findFirst().orElse(null);
        }
        Prescription prescription = loadPrescription(requestedId);
        if (!Objects.equals(prescription.getConsultation().getId(), consultation.getId())) {
            throw new BusinessException("PRESCRIPTION_CONSULTATION_MISMATCH", "L'ordonnance ne correspond pas a la consultation");
        }
        return prescription.getId();
    }

    private boolean isReimbursedSheet(DiseaseSheet sheet) {
        return sheet.getReimbursementId() != null || (sheet.getReimbursementNumber() != null && !sheet.getReimbursementNumber().isBlank());
    }

    private void ensureVersion(Long requested, long current) {
        if (requested == null || requested != current) {
            throw new BusinessException("VERSION_CONFLICT", "La ressource a ete modifiee par un autre utilisateur");
        }
    }

    private UUID parseUuid(String value) {
        try {
            return value == null ? null : UUID.fromString(value.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private ReferralResponse toReferralResponse(SpecialistReferral referral) {
        return new ReferralResponse(referral.getId(), referral.getReferralNumber(), referral.getConsultation().getId(),
                referral.getConsultation().getInsuranceNumber(), referral.getSpecialty(), referral.getReason(),
                referral.getPriority(), referral.getStatus(), List.copyOf(referral.getTargetDoctorMatricules()), referral.getCreatedAt());
    }

    private void audit(AuthenticatedUser actor, String action, String type, String id, String result, String metadata) {
        auditEvents.save(MedicalAuditEvent.builder().actorUserId(actor.userId()).actorRole(actor.primaryRole())
                .action(action).resourceType(type).resourceId(id).result(result).metadata(metadata).build());
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
