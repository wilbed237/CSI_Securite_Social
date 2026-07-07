package com.csi.auth.application.service;

import com.csi.auth.application.dto.AuthDtos.*;
import com.csi.auth.application.mapper.UserMapper;
import com.csi.auth.domain.model.RefreshToken;
import com.csi.auth.domain.model.RoleName;
import com.csi.auth.domain.model.UserAccount;
import com.csi.auth.infrastructure.client.ProfileServiceClient;
import com.csi.auth.infrastructure.persistence.RefreshTokenRepository;
import com.csi.auth.infrastructure.persistence.UserAccountRepository;
import com.csi.auth.infrastructure.persistence.AuthAuditEventRepository;
import com.csi.auth.domain.model.AuthAuditEvent;
import com.csi.common.domain.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Orchestre les cas d utilisation d authentification, de renouvellement et d inscription utilisateur.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final ProfileServiceClient profileServiceClient;
    private final AuthAuditEventRepository auditEvents;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${security.jwt.refresh-token-expiration-seconds:604800}")
    private long refreshExpirationSeconds;

    /**
     * Verifie les identifiants puis emet un access token et un refresh token.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCaseOrPhoneNumberOrUsernameIgnoreCase(request.identifier(), request.identifier(), request.identifier())
                .filter(UserAccount::isEnabled)
                .orElseThrow(() -> new BusinessException("BAD_CREDENTIALS", "Identifiants invalides"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("BAD_CREDENTIALS", "Identifiants invalides");
        }
        user.setLastLoginAt(Instant.now());
        users.save(user);
        audit(user, "USER_LOGIN", "SUCCESS");
        return issueTokens(user);
    }

    /**
     * Valide un refresh token actif et genere une nouvelle paire de tokens.
     */
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken token = refreshTokens.findByToken(request.refreshToken())
                .filter(t -> t.isActive(Instant.now()))
                .orElseThrow(() -> new BusinessException("REFRESH_TOKEN_INVALID", "Refresh token invalide ou expire"));
        return issueTokens(token.getUser());
    }

    /**
     * Cree un compte applicatif avec mot de passe chiffre et roles explicites.
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        UserAccount user = users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur introuvable"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("BAD_CREDENTIALS", "Mot de passe actuel invalide");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        users.save(user);
        audit(user, "PASSWORD_CHANGED", "SUCCESS");
    }

    @Transactional
    public void updateAccount(String username, UpdateAccountRequest request) {
        UserAccount user = users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur introuvable"));
        if (users.existsByEmailIgnoreCaseAndIdNot(request.email(), user.getId())) {
            throw new BusinessException("USER_ALREADY_EXISTS", "Un utilisateur existe deja avec cet email");
        }
        if (users.existsByUsernameIgnoreCaseAndIdNot(request.username(), user.getId())) {
            throw new BusinessException("USER_ALREADY_EXISTS", "Un utilisateur existe deja avec ce nom d'utilisateur");
        }
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        users.save(user);
        audit(user, "ACCOUNT_UPDATED", "SUCCESS");
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (users.existsByEmailIgnoreCase(request.email()) || users.existsByUsernameIgnoreCase(request.username())) {
            throw new BusinessException("USER_ALREADY_EXISTS", "Un utilisateur existe deja avec cet email ou ce nom d'utilisateur");
        }
        Set<RoleName> roles = normalizeRoles(request.roles());
        validateAgentProvisionedRegistration(roles, request);
        UserAccount user = UserAccount.builder()
                .username(request.username())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(roles)
                .enabled(true)
                .build();
        UserAccount saved = users.save(user);
        syncActorProfile(saved, request, roles);
        audit(saved, "USER_REGISTERED", "SUCCESS");
        return userMapper.toResponse(saved);
    }

    private Set<RoleName> normalizeRoles(Set<RoleName> requestedRoles) {
        EnumSet<RoleName> roles = requestedRoles.isEmpty() ? EnumSet.noneOf(RoleName.class) : EnumSet.copyOf(requestedRoles);
        if (roles.contains(RoleName.SOCIAL_AGENT) || roles.contains(RoleName.AGENT_SOCIAL) || roles.contains(RoleName.SECURITY_AGENT) || roles.contains(RoleName.ADMIN)) {
            roles.add(RoleName.AGENT);
        }
        if (roles.contains(RoleName.GENERALIST) || roles.contains(RoleName.SPECIALIST)) {
            roles.add(RoleName.DOCTOR);
        }
        return roles;
    }

    private void validateAgentProvisionedRegistration(Set<RoleName> roles, RegisterUserRequest request) {
        if (!(roles.contains(RoleName.DOCTOR) || roles.contains(RoleName.GENERALIST) || roles.contains(RoleName.SPECIALIST))) {
            throw new BusinessException("ROLE_NOT_ALLOWED", "Seuls les comptes medecins peuvent etre crees via cette route");
        }
        if (roles.contains(RoleName.AGENT) || roles.contains(RoleName.SOCIAL_AGENT) || roles.contains(RoleName.AGENT_SOCIAL)
                || roles.contains(RoleName.SECURITY_AGENT) || roles.contains(RoleName.ADMIN)) {
            throw new BusinessException("ROLE_NOT_ALLOWED", "La creation de comptes agent/admin est interdite via cette route");
        }
        String actorType = normalize(request.actorType());
        if (!actorType.isBlank() && !"DOCTOR".equals(actorType)) {
            throw new BusinessException("ACTOR_TYPE_INVALID", "Le type d'acteur doit etre DOCTOR");
        }
        String doctorType = doctorType(request, roles);
        if (doctorType == null || doctorType.isBlank()) {
            throw new BusinessException("DOCTOR_TYPE_REQUIRED", "Le type de medecin (GENERALIST ou SPECIALIST) est obligatoire");
        }
    }

    private void syncActorProfile(UserAccount user, RegisterUserRequest request, Set<RoleName> roles) {
        String actorType = actorType(request, roles);
        if (actorType == null) {
            return;
        }
        String firstName = fallbackName(request.firstName(), user.getUsername());
        String lastName = fallbackName(request.lastName(), user.getUsername());
        profileServiceClient.createActorProfile(new ProfileServiceClient.CreateActorProfilePayload(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                firstName,
                lastName,
                actorType,
                doctorType(request, roles),
                request.specialty()));
    }

    private String actorType(RegisterUserRequest request, Set<RoleName> roles) {
        String actorType = normalize(request.actorType());
        if (!actorType.isBlank()) {
            return actorType;
        }
        if (roles.contains(RoleName.DOCTOR) || roles.contains(RoleName.GENERALIST) || roles.contains(RoleName.SPECIALIST)) {
            return "DOCTOR";
        }
        if (roles.contains(RoleName.AGENT) || roles.contains(RoleName.SOCIAL_AGENT) || roles.contains(RoleName.AGENT_SOCIAL)
                || roles.contains(RoleName.SECURITY_AGENT) || roles.contains(RoleName.ADMIN)) {
            return "SOCIAL_AGENT";
        }
        return null;
    }

    private String doctorType(RegisterUserRequest request, Set<RoleName> roles) {
        String doctorType = normalize(request.doctorType());
        if ("GENERALISTE".equals(doctorType)) {
            return "GENERALIST";
        }
        if ("SPECIALISTE".equals(doctorType)) {
            return "SPECIALIST";
        }
        if (!doctorType.isBlank()) {
            return doctorType;
        }
        if (roles.contains(RoleName.GENERALIST)) {
            return "GENERALIST";
        }
        if (roles.contains(RoleName.SPECIALIST)) {
            return "SPECIALIST";
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String fallbackName(String value, String username) {
        return value == null || value.isBlank() ? username : value.trim();
    }

    private AuthResponse issueTokens(UserAccount user) {
        String access = jwtService.createAccessToken(user);
        String refresh = randomToken();
        refreshTokens.save(RefreshToken.builder()
                .user(user)
                .token(refresh)
                .expiresAt(Instant.now().plusSeconds(refreshExpirationSeconds))
                .build());
        return new AuthResponse(access, refresh, "Bearer", jwtService.expirationSeconds(), userMapper.toResponse(user));
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void audit(UserAccount user, String action, String result) {
        String role = user.getRoles().stream().findFirst().map(Enum::name).orElse("UNKNOWN");
        auditEvents.save(AuthAuditEvent.builder().actorUserId(user.getId()).actorRole(role).action(action)
                .resourceType("USER_ACCOUNT").resourceId(user.getId().toString()).result(result).build());
    }
}
