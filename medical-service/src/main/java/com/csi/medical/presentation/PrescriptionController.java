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
 * API REST pour creer les prescriptions de medicaments et les orientations specialistes.
 */
@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {
    private final MedicalService medicalService;

    @Operation(summary = "Prescrire des medicaments")
    @PostMapping("/medications")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> medications(@Valid @RequestBody CreateMedicationPrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Prescription medicamenteuse creee", medicalService.prescribeMedication(request)));
    }

    @Operation(summary = "Prescrire une consultation chez un specialiste")
    @PostMapping("/specialist-consultations")
    @PreAuthorize("hasRole('GENERALIST')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> specialist(@Valid @RequestBody CreateSpecialistReferralRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Orientation specialiste creee", medicalService.prescribeSpecialistConsultation(request)));
    }
}
