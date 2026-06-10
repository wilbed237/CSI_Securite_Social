package com.csi.medical.application.service;

import com.csi.common.domain.BusinessException;
import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.application.mapper.MedicalMapper;
import com.csi.medical.config.AuthenticatedUser;
import com.csi.medical.domain.model.*;
import com.csi.medical.infrastructure.client.ProfileClient;
import com.csi.medical.infrastructure.client.ReimbursementClient;
import com.csi.medical.infrastructure.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MedicalRulesTest {
    private ConsultationRepository consultations;
    private PrescriptionRepository prescriptions;
    private DiseaseSheetRepository diseaseSheets;
    private MedicalAuditEventRepository auditEvents;
    private ProfileClient profileClient;
    private MedicalService service;
    private AuthenticatedUser doctor;

    @BeforeEach
    void setUp() {
        consultations = mock(ConsultationRepository.class);
        prescriptions = mock(PrescriptionRepository.class);
        diseaseSheets = mock(DiseaseSheetRepository.class);
        auditEvents = mock(MedicalAuditEventRepository.class);
        profileClient = mock(ProfileClient.class);
        service = new MedicalService(consultations, prescriptions, diseaseSheets, mock(SpecialistReferralRepository.class),
                auditEvents, Mappers.getMapper(MedicalMapper.class), profileClient, mock(ReimbursementClient.class),
                mock(DiseaseSheetPdfService.class));
        doctor = new AuthenticatedUser(UUID.randomUUID(), "doctor", List.of("DOCTOR", "GENERALIST"));
    }

    @Test
    void consultationCreationPersistsAuditedDraft() {
        when(profileClient.currentDoctor()).thenReturn(new ProfileClient.DoctorData(UUID.randomUUID(), doctor.userId(),
                "MED-1", "Jean", "Kamga", "GENERALIST", null, null, null, true));
        when(consultations.save(any())).thenAnswer(invocation -> {
            Consultation value = invocation.getArgument(0);
            value.setId(UUID.randomUUID());
            return value;
        });
        LocalDateTime start = LocalDateTime.now();
        ConsultationResponse response = service.createConsultation(new CreateConsultationRequest(
                "ASS-1", null, null, start, start.plusMinutes(30), BigDecimal.TEN,
                "GENERAL", "Controle", null, null, null, null, "key-1"), doctor);

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(ConsultationStatus.DRAFT);
        verify(consultations).save(any(Consultation.class));
        verify(auditEvents).save(argThat(event -> "CONSULTATION_CREATED".equals(event.getAction())));
    }

    @Test
    void consultationEndMustBeAfterStart() {
        LocalDateTime now = LocalDateTime.now();
        var request = new CreateConsultationRequest("ASS-1", "MED-1", DoctorType.GENERALIST, now, now.minusMinutes(1), BigDecimal.TEN);
        assertThatThrownBy(() -> service.createConsultation(request, doctor)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("fin de consultation");
    }

    @Test
    void consultationUpdateRejectsAnotherDoctorAndVersionConflict() {
        Consultation consultation = consultation(doctor.userId());
        when(consultations.findById(consultation.getId())).thenReturn(Optional.of(consultation));
        AuthenticatedUser other = new AuthenticatedUser(UUID.randomUUID(), "other", List.of("DOCTOR"));
        UpdateConsultationRequest request = new UpdateConsultationRequest(null, null, null, null, "Motif", null, null, null, null, 0L);
        assertThatThrownBy(() -> service.updateConsultation(consultation.getId(), request, other))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Acces interdit");
        assertThatThrownBy(() -> service.updateConsultation(consultation.getId(), new UpdateConsultationRequest(
                null, null, null, null, "Motif", null, null, null, null, 4L), doctor))
                .isInstanceOf(BusinessException.class).hasMessageContaining("modifiee par un autre");
    }

    @Test
    void prescriptionPersistsMultipleMedicationLines() {
        Consultation consultation = consultation(doctor.userId());
        when(consultations.findById(consultation.getId())).thenReturn(Optional.of(consultation));
        when(prescriptions.save(any())).thenAnswer(invocation -> {
            Prescription value = invocation.getArgument(0);
            value.setId(UUID.randomUUID());
            value.getMedications().forEach(item -> item.setId(UUID.randomUUID()));
            return value;
        });
        PrescriptionResponse response = service.prescribeMedication(new CreateMedicationPrescriptionRequest(
                consultation.getId(), "Traitement", List.of(new MedicationItemRequest("A", "1/j"), new MedicationItemRequest("B", "2/j"))), doctor);
        assertThat(response.medications()).hasSize(2);
        assertThat(response.medications()).allMatch(item -> item.id() != null);
        verify(prescriptions).save(argThat(value -> value.getMedications().stream().allMatch(item -> item.getPrescription() == value)));
    }

    @Test
    void finalizedPrescriptionCannotBeUpdated() {
        Consultation consultation = consultation(doctor.userId());
        Prescription prescription = Prescription.builder().id(UUID.randomUUID()).prescriptionNumber("PM-1")
                .type(PrescriptionType.MEDICATION).consultation(consultation).status(PrescriptionStatus.FINALIZED).build();
        when(prescriptions.findDetailedById(prescription.getId())).thenReturn(Optional.of(prescription));
        when(consultations.findById(consultation.getId())).thenReturn(Optional.of(consultation));
        assertThatThrownBy(() -> service.updatePrescription(prescription.getId(),
                new UpdatePrescriptionRequest(null, null, null, null, 0L), doctor))
                .isInstanceOf(BusinessException.class).hasMessageContaining("finalisee");
    }

    @Test
    void diseaseSheetIsUniqueAndReimbursedSheetIsProtected() {
        Consultation consultation = consultation(doctor.userId());
        DiseaseSheet existing = sheet(consultation);
        when(consultations.findById(consultation.getId())).thenReturn(Optional.of(consultation));
        when(diseaseSheets.findByConsultationId(consultation.getId())).thenReturn(Optional.of(existing));
        DiseaseSheetResponse response = service.createDiseaseSheet(new CreateDiseaseSheetRequest(
                consultation.getId(), null, "Diagnostic", null, null), doctor);
        assertThat(response.id()).isEqualTo(existing.getId());
        verify(diseaseSheets, never()).save(any());

        existing.setReimbursementNumber("REM-1");
        when(diseaseSheets.findById(existing.getId())).thenReturn(Optional.of(existing));
        assertThatThrownBy(() -> service.updateDiseaseSheet(existing.getId(), new UpdateDiseaseSheetRequest(
                null, "Nouveau", null, null, null, null, null, null, null, 0L), doctor))
                .isInstanceOf(BusinessException.class).hasMessageContaining("remboursee");
    }

    private Consultation consultation(UUID owner) {
        return Consultation.builder().id(UUID.randomUUID()).insuranceNumber("ASS-1").doctorMatricule("MED-1")
                .doctorType(DoctorType.GENERALIST).startedAt(LocalDateTime.now()).endedAt(LocalDateTime.now().plusMinutes(30))
                .cost(BigDecimal.TEN).status(ConsultationStatus.DRAFT).createdByUserId(owner).build();
    }

    private DiseaseSheet sheet(Consultation consultation) {
        return DiseaseSheet.builder().id(UUID.randomUUID()).sheetNumber("FM-1").consultation(consultation)
                .patientId(consultation.getInsuranceNumber()).doctorId(consultation.getDoctorMatricule())
                .doctorType(consultation.getDoctorType()).consultationAmount(consultation.getCost())
                .consultationDate(consultation.getStartedAt()).registrationDate(java.time.LocalDate.now())
                .diagnosis("Diagnostic").status(DiseaseSheetStatus.ISSUED).createdByUserId(doctor.userId()).build();
    }
}
