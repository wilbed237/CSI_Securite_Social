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
 * API REST pour creer et consulter les feuilles de maladie servant au remboursement.
 */
@RestController
@RequestMapping("/api/v1/disease-sheets")
@RequiredArgsConstructor
public class DiseaseSheetController {
    private final MedicalService medicalService;

    @Operation(summary = "Enregistrer une feuille de maladie")
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ResponseEntity<ApiResponse<DiseaseSheetResponse>> create(@Valid @RequestBody CreateDiseaseSheetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Feuille de maladie creee", medicalService.createDiseaseSheet(request)));
    }

    @Operation(summary = "Consulter une feuille de maladie par numero")
    @GetMapping("/{sheetNumber}")
    @PreAuthorize("hasAnyRole('AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DiseaseSheetResponse> get(@PathVariable String sheetNumber) {
        return ApiResponse.success("Feuille de maladie", medicalService.getDiseaseSheet(sheetNumber));
    }
}
