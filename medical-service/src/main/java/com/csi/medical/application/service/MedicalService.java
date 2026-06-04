package com.csi.medical.application.service;

import com.csi.common.domain.BusinessException;
import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.application.mapper.MedicalMapper;
import com.csi.medical.domain.model.*;
import com.csi.medical.infrastructure.client.ProfileClient;
import com.csi.medical.infrastructure.persistence.ConsultationRepository;
import com.csi.medical.infrastructure.persistence.DiseaseSheetRepository;
import com.csi.medical.infrastructure.persistence.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Orchestre les actes medicaux et garantit que le patient est assure avant toute operation.
 */
@Service
@RequiredArgsConstructor
public class MedicalService {
    private final ConsultationRepository consultations;
    private final PrescriptionRepository prescriptions;
    private final DiseaseSheetRepository diseaseSheets;
    private final MedicalMapper mapper;
    private final ProfileClient profileClient;

    /**
     * Cree une consultation uniquement si la periode est valide et le patient assure actif.
     */
    @Transactional
    public ConsultationResponse createConsultation(CreateConsultationRequest request) {
        if (!request.endedAt().isAfter(request.startedAt())) {
            throw new BusinessException("CONSULTATION_PERIOD_INVALID", "La fin de consultation doit etre apres le debut");
        }
        profileClient.ensureActiveInsured(request.insuranceNumber());
        Consultation consultation = Consultation.builder()
                .insuranceNumber(request.insuranceNumber())
                .doctorMatricule(request.doctorMatricule())
                .doctorType(request.doctorType())
                .startedAt(request.startedAt())
                .endedAt(request.endedAt())
                .cost(request.cost())
                .build();
        return mapper.toResponse(consultations.save(consultation));
    }

    /**
     * Enregistre une prescription de medicaments rattachee a une consultation existante.
     */
    @Transactional
    public PrescriptionResponse prescribeMedication(CreateMedicationPrescriptionRequest request) {
        Consultation consultation = loadConsultation(request.consultationId());
        profileClient.ensureActiveInsured(consultation.getInsuranceNumber());
        Prescription prescription = Prescription.builder()
                .prescriptionNumber("PM-" + UUID.randomUUID())
                .type(PrescriptionType.MEDICATION)
                .consultation(consultation)
                .build();
        request.medications().forEach(item -> prescription.getMedications().add(Medication.builder()
                .name(item.name())
                .posology(item.posology())
                .prescription(prescription)
                .build()));
        return mapper.toResponse(prescriptions.save(prescription));
    }

    /**
     * Cree une orientation vers specialiste, reservee aux consultations de generaliste.
     */
    @Transactional
    public PrescriptionResponse prescribeSpecialistConsultation(CreateSpecialistReferralRequest request) {
        Consultation consultation = loadConsultation(request.consultationId());
        if (consultation.getDoctorType() != DoctorType.GENERALIST) {
            throw new BusinessException("ONLY_GENERALIST_CAN_REFER", "Seul un generaliste peut orienter vers un specialiste");
        }
        profileClient.ensureActiveInsured(consultation.getInsuranceNumber());
        Prescription prescription = Prescription.builder()
                .prescriptionNumber("PC-" + UUID.randomUUID())
                .type(PrescriptionType.SPECIALIST_CONSULTATION)
                .consultation(consultation)
                .requiredSpecialty(request.requiredSpecialty())
                .factors(request.factors())
                .build();
        return mapper.toResponse(prescriptions.save(prescription));
    }

    /**
     * Genere la feuille de maladie qui servira de reference au remboursement.
     */
    @Transactional
    public DiseaseSheetResponse createDiseaseSheet(CreateDiseaseSheetRequest request) {
        Consultation consultation = loadConsultation(request.consultationId());
        profileClient.ensureActiveInsured(consultation.getInsuranceNumber());
        DiseaseSheet sheet = DiseaseSheet.builder()
                .sheetNumber("FM-" + LocalDate.now().getYear() + "-" + UUID.randomUUID())
                .consultation(consultation)
                .diagnosis(request.diagnosis())
                .status(DiseaseSheetStatus.COMPLETED)
                .build();
        return mapper.toResponse(diseaseSheets.save(sheet));
    }

    @Transactional(readOnly = true)
    public DiseaseSheetResponse getDiseaseSheet(String sheetNumber) {
        return diseaseSheets.findBySheetNumberIgnoreCase(sheetNumber)
                .map(mapper::toResponse)
                .orElseThrow(() -> new BusinessException("DISEASE_SHEET_NOT_FOUND", "Feuille de maladie introuvable"));
    }

    private Consultation loadConsultation(UUID consultationId) {
        return consultations.findById(consultationId)
                .orElseThrow(() -> new BusinessException("CONSULTATION_NOT_FOUND", "Consultation introuvable"));
    }
}
