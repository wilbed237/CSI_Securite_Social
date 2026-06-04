package com.csi.profile.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.profile.application.dto.ProfileDtos.*;
import com.csi.profile.application.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API REST dediee aux assures : inscription, consultation, statut et medecin traitant.
 */
@RestController
@RequestMapping("/api/v1/insured")
@RequiredArgsConstructor
public class InsuredController {
    private final ProfileService profileService;

    @Operation(summary = "Inscrire un assure")
    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<InsuredResponse>> create(@Valid @RequestBody CreateInsuredRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Assure inscrit", profileService.registerInsured(request)));
    }

    @Operation(summary = "Consulter un assure par numero d'assurance")
    @GetMapping("/{insuranceNumber}")
    @PreAuthorize("hasAnyRole('AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<InsuredResponse> get(@PathVariable String insuranceNumber) {
        return ApiResponse.success("Assure trouve", profileService.getInsured(insuranceNumber));
    }

    @Operation(summary = "Verifier le statut d'assure pour les autres microservices")
    @GetMapping("/{insuranceNumber}/status")
    @PreAuthorize("hasAnyRole('AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<InsuredStatusResponse> status(@PathVariable String insuranceNumber) {
        return ApiResponse.success("Statut assure", profileService.getInsuredStatus(insuranceNumber));
    }

    @Operation(summary = "Associer un medecin traitant generaliste a un assure")
    @PutMapping("/{insuranceNumber}/treating-doctor")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<InsuredResponse> assignTreatingDoctor(@PathVariable String insuranceNumber, @Valid @RequestBody AssignTreatingDoctorRequest request) {
        return ApiResponse.success("Medecin traitant associe", profileService.assignTreatingDoctor(insuranceNumber, request));
    }
}
