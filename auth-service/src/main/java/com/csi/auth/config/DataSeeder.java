package com.csi.auth.config;

import com.csi.auth.domain.model.RoleName;
import com.csi.auth.domain.model.UserAccount;
import com.csi.auth.infrastructure.persistence.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsers(UserAccountRepository users) {
        return args -> {
            createIfMissing(users, "agent.csi", "agent@csi.local", "+221770000001", Set.of(RoleName.AGENT, RoleName.ADMIN));
            createIfMissing(users, "dr.generaliste", "generaliste@csi.local", "+221770000002", Set.of(RoleName.DOCTOR, RoleName.GENERALIST));
            createIfMissing(users, "dr.specialiste", "specialiste@csi.local", "+221770000003", Set.of(RoleName.DOCTOR, RoleName.SPECIALIST));
        };
    }

    private void createIfMissing(UserAccountRepository users, String username, String email, String phone, Set<RoleName> roles) {
        if (!users.existsByUsernameIgnoreCase(username)) {
            users.save(UserAccount.builder()
                    .username(username)
                    .email(email)
                    .phoneNumber(phone)
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .roles(roles)
                    .enabled(true)
                    .build());
        }
    }
}
