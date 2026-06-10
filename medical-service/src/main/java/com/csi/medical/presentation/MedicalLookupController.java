package com.csi.medical.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.medical.application.dto.MedicalDtos.DiseaseSheetResponse;
import com.csi.medical.application.service.MedicalService;
import com.csi.medical.config.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class MedicalLookupController {
    private final MedicalService medicalService;

    @GetMapping("/api/v1/patients/{patientId}/disease-sheets")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<Page<DiseaseSheetResponse>> patientSheets(@PathVariable String patientId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuilles du patient", medicalService.listDiseaseSheets(null, patientId, null, null,
                null, null, null, null, ConsultationController.pageable(page, size, "consultationDate", "desc", Set.of("consultationDate")), actor));
    }

    @GetMapping("/api/v1/doctors/{doctorId}/disease-sheets")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<Page<DiseaseSheetResponse>> doctorSheets(@PathVariable String doctorId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Feuilles du medecin", medicalService.listDiseaseSheets(null, null, doctorId, null,
                null, null, null, null, ConsultationController.pageable(page, size, "consultationDate", "desc", Set.of("consultationDate")), actor));
    }
}
