package com.csi.auth.application.service;

import com.csi.auth.domain.model.RoleName;
import com.csi.auth.domain.model.UserAccount;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    @Test
    void createsBearerToken() {
        JwtService service = new JwtService("test-secret-test-secret-test-secret-test-secret", 3600);
        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .username("agent")
                .email("agent@csi.local")
                .roles(Set.of(RoleName.AGENT))
                .build();

        String token = service.createAccessToken(user);

        assertThat(token).contains(".");
        assertThat(service.expirationSeconds()).isEqualTo(3600);
    }
}
