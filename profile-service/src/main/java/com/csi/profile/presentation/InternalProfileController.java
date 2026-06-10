package com.csi.profile.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.profile.application.dto.ProfileDtos.*;
import com.csi.profile.application.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * API interne appelee par auth-service apres creation d'un compte.
 */
@RestController
@RequestMapping("/api/v1/internal/profiles")
@RequiredArgsConstructor
public class InternalProfileController {
    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final ProfileService profileService;

    @Value("${internal.auth-service-secret:dev-internal-secret}")
    private String internalSecret;

    @PostMapping("/actors")
    public ApiResponse<ActorProfileResponse> createActor(
            @RequestHeader(value = INTERNAL_SECRET_HEADER, required = false) String secret,
            @Valid @RequestBody CreateActorProfileRequest request) {
        if (!internalSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal secret");
        }
        return ApiResponse.success("Profil metier cree", profileService.createActorProfile(request));
    }
}
