package com.csi.medical.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.application.service.MedicalService;
import com.csi.medical.config.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReferralController {
    private final MedicalService medicalService;

    @PostMapping("/api/v1/consultations/{consultationId}/referrals")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST')")
    public ResponseEntity<ApiResponse<ReferralResponse>> create(@PathVariable java.util.UUID consultationId,
            @Valid @RequestBody CreateReferralRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        CreateReferralRequest normalized = new CreateReferralRequest(consultationId, request.specialty(), request.reason(),
                request.priority(), request.specialistMatricules());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Orientation creee", medicalService.createReferral(normalized, actor)));
    }

    @GetMapping("/api/v1/referrals/{referralNumber}")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN','DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<ReferralResponse> get(@PathVariable String referralNumber, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Orientation", medicalService.getReferral(referralNumber, actor));
    }

    @PatchMapping("/api/v1/referrals/{referralNumber}/status")
    @PreAuthorize("hasAnyRole('DOCTOR','GENERALIST','SPECIALIST')")
    public ApiResponse<ReferralResponse> updateStatus(@PathVariable String referralNumber,
            @Valid @RequestBody UpdateReferralStatusRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ApiResponse.success("Statut de l'orientation modifie", medicalService.updateReferralStatus(referralNumber, request, actor));
    }
}
