package com.csi.medical.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.application.service.MedicalService;
import com.csi.medical.config.AuthenticatedUser;
import com.csi.medical.domain.model.ConsultationStatus;
import com.csi.medical.domain.model.DoctorType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {
    private static final Set<String> SORTS = Set.of("startedAt", "insuranceNumber", "doctorMatricule", "status", "cost", "createdAt");
    private final MedicalService medicalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ResponseEntity<ApiResponse<ConsultationResponse>> create(@Valid @RequestBody CreateConsultationRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Consultation creee", medicalService.createConsultation(request, actor)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<Page<ConsultationResponse>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startedAt") String sort, @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search, @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String doctorId, @RequestParam(required = false) DoctorType doctorType,
            @RequestParam(required = false) ConsultationStatus status, @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Consultations recuperees", medicalService.listConsultations(search, patientId, doctorId,
                doctorType, status, startDate, endDate, pageable(page, size, sort, direction, SORTS), actor));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<ConsultationResponse> get(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Consultation recuperee", medicalService.getConsultation(id, actor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<ConsultationResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateConsultationRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Consultation modifiee", medicalService.updateConsultation(id, request, actor));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<ConsultationResponse> patch(@PathVariable UUID id, @Valid @RequestBody UpdateConsultationRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Consultation modifiee", medicalService.updateConsultation(id, request, actor));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<ConsultationResponse> cancel(@PathVariable UUID id, @Valid @RequestBody VersionRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Consultation annulee", medicalService.changeConsultationStatus(id, ConsultationStatus.CANCELLED, request.version(), actor));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<ConsultationResponse> archive(@PathVariable UUID id, @Valid @RequestBody VersionRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Consultation archivee", medicalService.changeConsultationStatus(id, ConsultationStatus.ARCHIVED, request.version(), actor));
    }

    @GetMapping("/{id}/prescriptions")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<List<PrescriptionResponse>> prescriptions(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Ordonnances de la consultation", medicalService.getConsultationPrescriptions(id, actor));
    }

    @GetMapping("/{id}/disease-sheet")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DiseaseSheetResponse> diseaseSheet(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuille de la consultation", medicalService.getDiseaseSheetByConsultation(id, actor));
    }

    public record VersionRequest(@NotNull Long version) {}

    static Pageable pageable(int page, int size, String sort, String direction, Set<String> allowed) {
        String field = allowed.contains(sort) ? sort : allowed.iterator().next();
        Sort.Direction order = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(order, field));
    }
}
