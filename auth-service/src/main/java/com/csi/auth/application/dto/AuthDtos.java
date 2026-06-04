package com.csi.auth.application.dto;

import com.csi.auth.domain.model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

/**
 * Regroupe les DTO d entree et de sortie du service d authentification.
 */
public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record RegisterUserRequest(
            @NotBlank @Size(max = 80) String username,
            @NotBlank @Email @Size(max = 160) String email,
            @Size(max = 40) String phoneNumber,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotEmpty Set<RoleName> roles) {}

    public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds, UserResponse user) {}
    public record UserResponse(UUID id, String username, String email, String phoneNumber, Set<RoleName> roles, boolean enabled) {}
}
