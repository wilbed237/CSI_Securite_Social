package com.csi.medical.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.application.service.MedicalService;
import com.csi.medical.config.AuthenticatedUser;
import com.csi.medical.domain.model.PrescriptionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {
    private static final Set<String> SORTS = Set.of("prescriptionDate", "status", "createdAt");
    private final MedicalService medicalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(@Valid @RequestBody CreateMedicationPrescriptionRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return medication(request, actor);
    }

    @PostMapping("/medications")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> medication(@Valid @RequestBody CreateMedicationPrescriptionRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Ordonnance creee", medicalService.prescribeMedication(request, actor)));
    }

    @PostMapping("/specialist-consultations")
    @PreAuthorize("hasAnyRole('GENERALIST')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> specialist(@Valid @RequestBody CreateSpecialistReferralRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Orientation specialiste creee", medicalService.prescribeSpecialistConsultation(request, actor)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<Page<PrescriptionResponse>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "prescriptionDate") String sort, @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search, @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String doctorId, @RequestParam(required = false) UUID consultationId,
            @RequestParam(required = false) UUID diseaseSheetId, @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String medicationName, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Ordonnances recuperees", medicalService.listPrescriptions(search, patientId, doctorId,
                consultationId, diseaseSheetId, status, startDate, endDate, medicationName,
                ConsultationController.pageable(page, size, sort, direction, SORTS), actor));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<PrescriptionResponse> get(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Ordonnance recuperee", medicalService.getPrescription(id, actor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<PrescriptionResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdatePrescriptionRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Ordonnance modifiee", medicalService.updatePrescription(id, request, actor));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<PrescriptionResponse> patch(@PathVariable UUID id, @Valid @RequestBody UpdatePrescriptionRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Ordonnance modifiee", medicalService.updatePrescription(id, request, actor));
    }
}
