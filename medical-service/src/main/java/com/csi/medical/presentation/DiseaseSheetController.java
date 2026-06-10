package com.csi.medical.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.common.domain.BusinessException;
import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.application.service.MedicalService;
import com.csi.medical.config.AuthenticatedUser;
import com.csi.medical.domain.model.DiseaseSheetStatus;
import com.csi.medical.domain.model.DoctorType;
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
@RequestMapping("/api/v1/disease-sheets")
@RequiredArgsConstructor
public class DiseaseSheetController {
    private static final Set<String> SORTS = Set.of("consultationDate", "patientId", "doctorId", "doctorType", "consultationAmount", "status", "createdAt", "reimbursementNumber");
    private final MedicalService medicalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ResponseEntity<ApiResponse<DiseaseSheetResponse>> create(@Valid @RequestBody CreateDiseaseSheetRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Feuille de maladie enregistree", medicalService.createDiseaseSheet(request, actor)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<Page<DiseaseSheetResponse>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "consultationDate") String sort, @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search, @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String doctorId, @RequestParam(required = false) DoctorType doctorType,
            @RequestParam(required = false) DiseaseSheetStatus status, @RequestParam(required = false) Boolean hasReimbursement,
            @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuilles de maladie recuperees", medicalService.listDiseaseSheets(search, patientId, doctorId,
                doctorType, status, hasReimbursement, startDate, endDate,
                ConsultationController.pageable(page, size, sort, direction, SORTS), actor));
    }

    @GetMapping("/{reference}")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DiseaseSheetResponse> get(@PathVariable String reference, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuille de maladie", medicalService.getDiseaseSheet(reference, actor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DiseaseSheetResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateDiseaseSheetRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuille de maladie modifiee", medicalService.updateDiseaseSheet(id, request, actor));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DiseaseSheetResponse> patch(@PathVariable UUID id, @Valid @RequestBody UpdateDiseaseSheetRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuille de maladie modifiee", medicalService.updateDiseaseSheet(id, request, actor));
    }

    @GetMapping("/{id}/prescription")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<PrescriptionResponse> prescription(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) {
        DiseaseSheetResponse sheet = medicalService.getDiseaseSheet(id, actor);
        if (sheet.prescriptionId() == null) throw new BusinessException("PRESCRIPTION_NOT_FOUND", "Aucune ordonnance n'est associee a cette feuille");
        return ApiResponse.success("Ordonnance de la feuille", medicalService.getPrescription(sheet.prescriptionId(), actor));
    }

    @PatchMapping("/{reference}/submit")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DiseaseSheetResponse> submit(@PathVariable String reference, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuille soumise", medicalService.submitDiseaseSheet(reference, actor));
    }

    @PatchMapping("/{reference}/complete")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT')")
    public ApiResponse<DiseaseSheetResponse> complete(@PathVariable String reference, @Valid @RequestBody CompleteDiseaseSheetRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuille prise en controle", medicalService.completeDiseaseSheet(reference, request, actor));
    }

    @PatchMapping("/{reference}/finalize")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT')")
    public ApiResponse<DiseaseSheetResponse> finalize(@PathVariable String reference, @Valid @RequestBody FinalizeDiseaseSheetRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuille de maladie completee", medicalService.finalizeDiseaseSheet(reference, request, actor));
    }

    @GetMapping(value = "/{reference}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ResponseEntity<byte[]> pdf(@PathVariable String reference, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=feuille-maladie-" + reference + ".pdf")
                .body(medicalService.diseaseSheetPdf(reference, actor));
    }
}
