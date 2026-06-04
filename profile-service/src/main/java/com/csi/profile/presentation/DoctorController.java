package com.csi.profile.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.profile.application.dto.ProfileDtos.*;
import com.csi.profile.application.service.ProfileService;
import com.csi.profile.domain.model.DoctorType;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API REST dediee aux medecins generalistes et specialistes.
 */
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final ProfileService profileService;

    @Operation(summary = "Enregistrer un medecin")
    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<DoctorResponse>> create(@Valid @RequestBody CreateDoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Medecin enregistre", profileService.registerDoctor(request)));
    }

    @Operation(summary = "Consulter un medecin par matricule")
    @GetMapping("/{matricule}")
    @PreAuthorize("hasAnyRole('AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<DoctorResponse> get(@PathVariable String matricule) {
        return ApiResponse.success("Medecin trouve", profileService.getDoctor(matricule));
    }

    @Operation(summary = "Lister les medecins avec pagination et filtre de type")
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<Page<DoctorResponse>> list(@RequestParam(required = false) DoctorType type, Pageable pageable) {
        return ApiResponse.success("Medecins", profileService.findDoctors(type, pageable));
    }
}
