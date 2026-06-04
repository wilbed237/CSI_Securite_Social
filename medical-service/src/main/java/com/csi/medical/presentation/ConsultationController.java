package com.csi.medical.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.application.service.MedicalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API REST permettant aux medecins de declarer une consultation.
 */
@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {
    private final MedicalService medicalService;

    @Operation(summary = "Creer une consultation medicale")
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ResponseEntity<ApiResponse<ConsultationResponse>> create(@Valid @RequestBody CreateConsultationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Consultation creee", medicalService.createConsultation(request)));
    }
}
