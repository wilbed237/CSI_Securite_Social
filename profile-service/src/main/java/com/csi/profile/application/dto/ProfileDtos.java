package com.csi.profile.application.dto;

import com.csi.profile.domain.model.DoctorType;
import com.csi.profile.domain.model.InsuredStatus;
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
            @Email @Size(max = 160) String email) {}

    public record CreateDoctorRequest(
            @NotBlank @Size(max = 60) String matricule,
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotNull DoctorType type,
            @Size(max = 120) String specialty,
            @Size(max = 40) String phoneNumber,
            @Email @Size(max = 160) String email) {}

    public record AssignTreatingDoctorRequest(@NotBlank String doctorMatricule) {}
    public record DoctorResponse(UUID id, String matricule, String firstName, String lastName, DoctorType type, String specialty, String phoneNumber, String email) {}
    public record InsuredResponse(UUID id, String insuranceNumber, String firstName, String lastName, LocalDate birthDate, String address, String phoneNumber, String email, InsuredStatus status, DoctorResponse treatingDoctor) {}
    public record InsuredStatusResponse(String insuranceNumber, boolean insured, InsuredStatus status) {}
}
