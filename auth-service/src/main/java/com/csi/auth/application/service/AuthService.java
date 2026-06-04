package com.csi.auth.application.service;

import com.csi.auth.application.dto.AuthDtos.*;
import com.csi.auth.application.mapper.UserMapper;
import com.csi.auth.domain.model.RefreshToken;
import com.csi.auth.domain.model.UserAccount;
import com.csi.auth.infrastructure.persistence.RefreshTokenRepository;
import com.csi.auth.infrastructure.persistence.UserAccountRepository;
import com.csi.common.domain.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${security.jwt.refresh-token-expiration-seconds:604800}")
    private long refreshExpirationSeconds;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCaseOrPhoneNumberOrUsernameIgnoreCase(request.identifier(), request.identifier(), request.identifier())
                .filter(UserAccount::isEnabled)
                .orElseThrow(() -> new BusinessException("BAD_CREDENTIALS", "Identifiants invalides"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("BAD_CREDENTIALS", "Identifiants invalides");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken token = refreshTokens.findByToken(request.refreshToken())
                .filter(t -> t.isActive(Instant.now()))
                .orElseThrow(() -> new BusinessException("REFRESH_TOKEN_INVALID", "Refresh token invalide ou expire"));
        return issueTokens(token.getUser());
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (users.existsByEmailIgnoreCase(request.email()) || users.existsByUsernameIgnoreCase(request.username())) {
            throw new BusinessException("USER_ALREADY_EXISTS", "Un utilisateur existe deja avec cet email ou ce nom d'utilisateur");
        }
        UserAccount user = UserAccount.builder()
                .username(request.username())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(request.roles())
                .enabled(true)
                .build();
        return userMapper.toResponse(users.save(user));
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
}
