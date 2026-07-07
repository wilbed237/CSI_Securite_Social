package com.csi.auth.presentation;

import com.csi.auth.application.dto.AuthDtos.*;
import com.csi.auth.application.service.AuthService;
import com.csi.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Expose les endpoints REST de connexion, renouvellement de token et creation de compte.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Authentifier un agent ou un medecin")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Authentification reussie", authService.login(request));
    }

    @Operation(summary = "Renouveler un access token via refresh token")
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success("Token renouvele", authService.refresh(request));
    }

    @Operation(summary = "Changer le mot de passe de l'utilisateur connecté")
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ApiResponse.success("Mot de passe mis a jour", null);
    }

    @Operation(summary = "Mettre a jour les informations de compte de l'utilisateur connecté")
    @PutMapping("/account")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> updateAccount(Authentication authentication, @Valid @RequestBody UpdateAccountRequest request) {
        authService.updateAccount(authentication.getName(), request);
        return ApiResponse.success("Compte mis a jour", null);
    }

    @Operation(summary = "Creer un utilisateur applicatif")
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Utilisateur cree", authService.register(request)));
    }
}
