package com.csi.profile.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Agent metier rattache a un compte applicatif auth-service.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "social_agents", uniqueConstraints = {
        @UniqueConstraint(name = "uk_social_agent_auth_user", columnNames = "auth_user_id"),
        @UniqueConstraint(name = "uk_social_agent_email", columnNames = "email")
})
public class SocialAgent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auth_user_id", nullable = false)
    private UUID authUserId;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, length = 80)
    private String username;

    @Column(length = 40)
    private String phoneNumber;

    @Column(length = 160)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
