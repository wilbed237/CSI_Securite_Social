package com.csi.auth.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @Entity
@Table(name = "audit_events")
public class AuthAuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "actor_user_id") private UUID actorUserId;
    @Column(name = "actor_role", length = 80) private String actorRole;
    @Column(nullable = false, length = 100) private String action;
    @Column(name = "resource_type", nullable = false, length = 80) private String resourceType;
    @Column(name = "resource_id", nullable = false, length = 100) private String resourceId;
    @Column(nullable = false) @Builder.Default private Instant timestamp = Instant.now();
    @Column(nullable = false, length = 30) private String result;
    @Column(length = 1000) private String metadata;
}
