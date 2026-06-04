package com.csi.medical.application.dto;

import com.csi.medical.domain.model.DiseaseSheetStatus;
import com.csi.medical.domain.model.DoctorType;
import com.csi.medical.domain.model.PrescriptionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class MedicalDtos {
    private MedicalDtos() {}

    public record CreateConsultationRequest(
            @NotBlank String insuranceNumber,
            @NotBlank String doctorMatricule,
            @NotNull DoctorType doctorType,
            @NotNull LocalDateTime startedAt,
            @NotNull LocalDateTime endedAt,
            @NotNull @DecimalMin("0.0") BigDecimal cost) {}

    public record MedicationItemRequest(@NotBlank @Size(max = 160) String name, @NotBlank @Size(max = 240) String posology) {}
    public record CreateMedicationPrescriptionRequest(@NotNull UUID consultationId, @NotEmpty List<@Valid MedicationItemRequest> medications) {}
    public record CreateSpecialistReferralRequest(@NotNull UUID consultationId, @NotBlank @Size(max = 120) String requiredSpecialty, @Size(max = 500) String factors) {}
    public record CreateDiseaseSheetRequest(@NotNull UUID consultationId, @NotBlank @Size(max = 1000) String diagnosis) {}

    public record ConsultationResponse(UUID id, String insuranceNumber, String doctorMatricule, DoctorType doctorType, LocalDateTime startedAt, LocalDateTime endedAt, BigDecimal cost) {}
    public record MedicationResponse(UUID id, String name, String posology) {}
    public record PrescriptionResponse(UUID id, String prescriptionNumber, PrescriptionType type, LocalDate prescriptionDate, UUID consultationId, String requiredSpecialty, String factors, List<MedicationResponse> medications) {}
    public record DiseaseSheetResponse(UUID id, String sheetNumber, LocalDate date, String diagnosis, DiseaseSheetStatus status, UUID consultationId, String insuranceNumber, String doctorMatricule, DoctorType doctorType, BigDecimal consultationCost) {}
}
