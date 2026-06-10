package com.csi.profile.application.dto;

import com.csi.profile.domain.model.DoctorType;
import com.csi.profile.domain.model.InsuredStatus;
import com.csi.profile.domain.model.PaymentPreference;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO utilises par les API de gestion des assures et medecins.
 */
public final class ProfileDtos {
    private ProfileDtos() {}

    public record CreateInsuredRequest(
            @NotBlank @Size(max = 60) String insuranceNumber,
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotNull LocalDate birthDate,
            @NotBlank @Size(max = 240) String address,
            @Size(max = 40) String phoneNumber,
            @Email @Size(max = 160) String email,
            @Size(min = 2, max = 2) String countryCode,
            PaymentPreference preferredPaymentType,
            @Size(max = 80) String bankIban) {}

    public record UpdateInsuredRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotNull LocalDate birthDate,
            @NotBlank @Size(max = 240) String address,
            @Size(max = 40) String phoneNumber,
            @Email @Size(max = 160) String email,
            @Size(min = 2, max = 2) String countryCode,
            PaymentPreference preferredPaymentType,
            @Size(max = 80) String bankIban) {}

    public record UpdateStatusRequest(@NotNull Boolean active) {}

    public record CreateDoctorRequest(
            @NotBlank @Size(max = 60) String matricule,
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotNull DoctorType type,
            @Size(max = 120) String specialty,
            @Size(max = 40) String phoneNumber,
            @Email @Size(max = 160) String email) {}

    public record CreateActorProfileRequest(
            @NotNull UUID authUserId,
            @NotBlank @Size(max = 80) String username,
            @NotBlank @Size(max = 160) String email,
            @Size(max = 40) String phoneNumber,
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotBlank @Size(max = 40) String actorType,
            DoctorType doctorType,
            @Size(max = 120) String specialty) {}

    public record AssignTreatingDoctorRequest(@NotBlank String doctorMatricule) {}
    public record DoctorResponse(UUID id, UUID authUserId, String matricule, String firstName, String lastName, DoctorType type, String specialty, String phoneNumber, String email, boolean active) {}
    public record SocialAgentResponse(UUID id, UUID authUserId, String username, String firstName, String lastName, String phoneNumber, String email, boolean active) {}
    public record InsuredResponse(UUID id, String insuranceNumber, String firstName, String lastName, LocalDate birthDate, String address, String phoneNumber, String email, String countryCode, PaymentPreference preferredPaymentType, String bankAccountMasked, InsuredStatus status, DoctorResponse treatingDoctor) {}
    public record InsuredStatusResponse(String insuranceNumber, boolean insured, InsuredStatus status) {}
    public record ActorProfileResponse(String actorType, DoctorResponse doctor, SocialAgentResponse agent) {}
    public record PrimaryDoctorAssignmentResponse(UUID id, DoctorResponse doctor, java.time.Instant startedAt, java.time.Instant endedAt, UUID assignedByUserId) {}
    public record AuditEventResponse(UUID id, UUID actorUserId, String actorRole, String action, String resourceType, String resourceId, java.time.Instant timestamp, String result, String metadata) {}
}
