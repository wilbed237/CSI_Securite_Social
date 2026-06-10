package com.csi.profile.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.profile.application.dto.ProfileDtos.*;
import com.csi.profile.application.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.csi.profile.config.AuthenticatedUser;

import java.util.List;

/**
 * API REST dediee aux assures : inscription, consultation, statut et medecin traitant.
 */
@RestController
@RequestMapping("/api/v1/insured")
@RequiredArgsConstructor
public class InsuredController {
    private final ProfileService profileService;

    @Operation(summary = "Lister les assures avec pagination et filtres")
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<Page<InsuredResponse>> list(
            @RequestParam(required = false) com.csi.profile.domain.model.InsuredStatus status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ApiResponse.success("Assures", profileService.findInsured(status, search, pageable));
    }

    @Operation(summary = "Lister les assures affectes au medecin connecte")
    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<Page<InsuredResponse>> assignedToMe(@AuthenticationPrincipal AuthenticatedUser actor, Pageable pageable) {
        return ApiResponse.success("Assures affectes", profileService.findAssignedInsured(actor.userId(), pageable));
    }

    @Operation(summary = "Inscrire un assure")
    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<InsuredResponse>> create(@Valid @RequestBody CreateInsuredRequest request,
                                                                @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Assure inscrit", profileService.registerInsured(request, actor)));
    }

    @Operation(summary = "Modifier les informations administratives d'un assure")
    @PutMapping("/{insuranceNumber}")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<InsuredResponse> update(@PathVariable String insuranceNumber, @Valid @RequestBody UpdateInsuredRequest request,
                                                @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Assure modifie", profileService.updateInsured(insuranceNumber, request, actor));
    }

    @Operation(summary = "Activer ou suspendre un assure")
    @PatchMapping("/{insuranceNumber}/status")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<InsuredResponse> status(@PathVariable String insuranceNumber, @Valid @RequestBody UpdateStatusRequest request,
                                                @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Statut assure modifie", profileService.updateInsuredStatus(insuranceNumber, request.active(), actor));
    }

    @Operation(summary = "Consulter un assure par numero d'assurance")
    @GetMapping("/{insuranceNumber}")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN','DOCTOR','GENERALIST','SPECIALIST')")
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
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<InsuredResponse> assignTreatingDoctor(@PathVariable String insuranceNumber, @Valid @RequestBody AssignTreatingDoctorRequest request,
                                                              @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Medecin traitant associe", profileService.assignTreatingDoctor(insuranceNumber, request, actor));
    }

    @PostMapping("/{insuranceNumber}/primary-doctor")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<InsuredResponse> assignPrimaryDoctor(@PathVariable String insuranceNumber, @Valid @RequestBody AssignTreatingDoctorRequest request,
                                                            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Medecin traitant associe", profileService.assignTreatingDoctor(insuranceNumber, request, actor));
    }

    @GetMapping("/{insuranceNumber}/primary-doctor-history")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<List<PrimaryDoctorAssignmentResponse>> primaryDoctorHistory(@PathVariable String insuranceNumber) {
        return ApiResponse.success("Historique medecin traitant", profileService.primaryDoctorHistory(insuranceNumber));
    }
}
