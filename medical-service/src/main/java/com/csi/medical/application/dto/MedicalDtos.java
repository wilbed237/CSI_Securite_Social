package com.csi.medical.application.dto;

import com.csi.medical.domain.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class MedicalDtos {
    private MedicalDtos() {}

    public record CreateConsultationRequest(
            @NotBlank String insuranceNumber,
            String doctorMatricule,
            DoctorType doctorType,
            @NotNull LocalDateTime startedAt,
            @NotNull LocalDateTime endedAt,
            @NotNull @DecimalMin("0.01") BigDecimal cost,
            @Size(max = 80) String consultationType,
            @Size(max = 240) String reason,
            @Size(max = 2000) String observations,
            @Size(max = 2000) String diagnosis,
            @Size(max = 2000) String conclusion,
            ConsultationStatus status,
            @Size(max = 100) String idempotencyKey) {
        public CreateConsultationRequest(String insuranceNumber, String doctorMatricule, DoctorType doctorType,
                                         LocalDateTime startedAt, LocalDateTime endedAt, BigDecimal cost) {
            this(insuranceNumber, doctorMatricule, doctorType, startedAt, endedAt, cost,
                    null, null, null, null, null, null, null);
        }
    }

    public record UpdateConsultationRequest(
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            @DecimalMin("0.01") BigDecimal cost,
            @Size(max = 80) String consultationType,
            @Size(max = 240) String reason,
            @Size(max = 2000) String observations,
            @Size(max = 2000) String diagnosis,
            @Size(max = 2000) String conclusion,
            ConsultationStatus status,
            @NotNull Long version) {}

    public record MedicationItemRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 240) String posology,
            @Size(max = 120) String frequency,
            @Size(max = 120) String duration,
            @Positive Integer quantity,
            @Size(max = 120) String administrationRoute,
            @Size(max = 500) String instructions) {
        public MedicationItemRequest(String name, String posology) {
            this(name, posology, null, null, null, null, null);
        }
    }

    public record CreateMedicationPrescriptionRequest(
            @NotNull UUID consultationId,
            @Size(max = 2000) String notes,
            @NotEmpty List<@Valid MedicationItemRequest> medications) {
        public CreateMedicationPrescriptionRequest(UUID consultationId, List<MedicationItemRequest> medications) {
            this(consultationId, null, medications);
        }
    }

    public record UpdatePrescriptionRequest(
            LocalDate prescriptionDate,
            @Size(max = 2000) String notes,
            PrescriptionStatus status,
            List<@Valid MedicationItemRequest> medications,
            @NotNull Long version) {}

    public record CreateSpecialistReferralRequest(@NotNull UUID consultationId, @NotBlank @Size(max = 120) String requiredSpecialty, @Size(max = 500) String factors) {}

    public record CreateDiseaseSheetRequest(
            @NotNull UUID consultationId,
            UUID prescriptionId,
            @NotBlank @Size(max = 1000) String diagnosis,
            @Size(max = 2000) String medicalConclusion,
            @Size(max = 120) String specialty) {}

    public record UpdateDiseaseSheetRequest(
            UUID prescriptionId,
            @Size(max = 1000) String diagnosis,
            @Size(max = 2000) String medicalConclusion,
            @Size(max = 120) String specialty,
            DiseaseSheetStatus status,
            UUID reimbursementId,
            @Size(max = 80) String reimbursementNumber,
            @Pattern(regexp = "CASH|BANK_TRANSFER") String paymentType,
            @Size(max = 1000) String controlComment,
            @NotNull Long version) {}

    public record CreateReferralRequest(
            @NotNull UUID consultationId,
            @NotBlank @Size(max = 120) String specialty,
            @NotBlank @Size(max = 500) String reason,
            @NotNull ReferralPriority priority,
            @NotEmpty List<@NotBlank String> specialistMatricules) {}
    public record UpdateReferralStatusRequest(@NotNull ReferralStatus status) {}
    public record CompleteDiseaseSheetRequest(@NotBlank @Pattern(regexp = "CASH|BANK_TRANSFER") String paymentType, @Size(max = 1000) String controlComment) {}
    public record FinalizeDiseaseSheetRequest(@NotBlank @Size(max = 80) String reimbursementNumber, @Size(max = 1000) String completionComment) {}

    public record ConsultationResponse(
            UUID id, String insuranceNumber, String doctorMatricule, DoctorType doctorType,
            LocalDateTime startedAt, LocalDateTime endedAt, BigDecimal cost, String consultationType,
            String reason, String observations, String diagnosis, String conclusion,
            ConsultationStatus status, Instant createdAt, Instant updatedAt,
            UUID createdByUserId, UUID updatedByUserId, long version) {}

    public record MedicationResponse(
            UUID id, String name, String posology, String frequency, String duration,
            Integer quantity, String administrationRoute, String instructions) {}

    public record PrescriptionResponse(
            UUID id, String prescriptionNumber, PrescriptionType type, LocalDate prescriptionDate,
            UUID consultationId, UUID diseaseSheetId, String insuranceNumber, String doctorMatricule,
            String requiredSpecialty, String factors, String notes, PrescriptionStatus status,
            List<MedicationResponse> medications, int medicationCount, Instant createdAt, Instant updatedAt,
            UUID createdByUserId, UUID updatedByUserId, long version) {}

    public record DiseaseSheetResponse(
            UUID id, String sheetNumber, LocalDate date, String diagnosis, String medicalConclusion,
            DiseaseSheetStatus status, UUID consultationId, UUID prescriptionId,
            String insuranceNumber, String doctorMatricule, DoctorType doctorType, String specialty,
            BigDecimal consultationCost, LocalDateTime consultationDate, LocalDate registrationDate,
            Instant receivedAt, String paymentType, String controlComment, UUID completedByUserId,
            Instant completedAt, UUID reimbursementId, String reimbursementNumber,
            Instant createdAt, Instant updatedAt, UUID createdByUserId, UUID updatedByUserId, long version) {}

    public record ReferralResponse(UUID id, String referralNumber, UUID consultationId, String insuranceNumber,
                                   String specialty, String reason, ReferralPriority priority, ReferralStatus status,
                                   List<String> specialistMatricules, Instant createdAt) {}
}
