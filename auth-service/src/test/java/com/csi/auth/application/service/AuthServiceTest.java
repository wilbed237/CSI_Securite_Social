package com.csi.auth.application.service;

import com.csi.auth.application.dto.AuthDtos.ChangePasswordRequest;
import com.csi.auth.application.dto.AuthDtos.UpdateAccountRequest;
import com.csi.auth.application.mapper.UserMapper;
import com.csi.auth.domain.model.AuthAuditEvent;
import com.csi.auth.domain.model.RoleName;
import com.csi.auth.domain.model.UserAccount;
import com.csi.auth.infrastructure.client.ProfileServiceClient;
import com.csi.auth.infrastructure.persistence.AuthAuditEventRepository;
import com.csi.auth.infrastructure.persistence.RefreshTokenRepository;
import com.csi.auth.infrastructure.persistence.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserAccountRepository users;
    @Mock
    private RefreshTokenRepository refreshTokens;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ProfileServiceClient profileServiceClient;
    @Mock
    private AuthAuditEventRepository auditEvents;

    @InjectMocks
    private AuthService authService;

    @Test
    void updatesAccountDetailsForAuthenticatedUser() {
        UUID id = UUID.randomUUID();
        UserAccount user = UserAccount.builder()
                .id(id)
                .username("doctor")
                .email("doctor@csi.local")
                .phoneNumber("+237690000000")
                .passwordHash("hash")
                .roles(Set.of(RoleName.DOCTOR))
                .enabled(true)
                .build();

        when(users.findByUsernameIgnoreCase("doctor")).thenReturn(Optional.of(user));
        when(users.existsByEmailIgnoreCaseAndIdNot("new@csi.local", id)).thenReturn(false);
        when(users.existsByUsernameIgnoreCaseAndIdNot("new-doctor", id)).thenReturn(false);
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.updateAccount("doctor", new UpdateAccountRequest("new-doctor", "new@csi.local", "+237699999999"));

        verify(users).save(argThat(saved -> "new-doctor".equals(saved.getUsername()) && "new@csi.local".equals(saved.getEmail()) && "+237699999999".equals(saved.getPhoneNumber())));
        verify(auditEvents).save(any(AuthAuditEvent.class));
    }

    @Test
    void changesPasswordForAuthenticatedUser() {
        UUID id = UUID.randomUUID();
        UserAccount user = UserAccount.builder()
                .id(id)
                .username("doctor")
                .email("doctor@csi.local")
                .passwordHash("old-hash")
                .roles(Set.of(RoleName.DOCTOR))
                .enabled(true)
                .build();

        when(users.findByUsernameIgnoreCase("doctor")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-pass", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.changePassword("doctor", new ChangePasswordRequest("old-pass", "new-pass"));

        verify(users).save(argThat(saved -> "new-hash".equals(saved.getPasswordHash())));
        verify(auditEvents).save(any(AuthAuditEvent.class));
    }
}
