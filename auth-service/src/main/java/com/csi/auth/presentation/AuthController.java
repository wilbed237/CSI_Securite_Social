package com.csi.auth.presentation;

import com.csi.auth.application.dto.AuthDtos.*;
import com.csi.auth.application.service.AuthService;
import com.csi.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Creer un utilisateur applicatif")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Utilisateur cree", authService.register(request)));
    }
}
