package com.csi.profile.infrastructure.persistence;

import com.csi.profile.domain.model.SocialAgent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

/**
 * Repository Spring Data pour les agents sociaux.
 */
public interface SocialAgentRepository extends JpaRepository<SocialAgent, UUID> {
    boolean existsByAuthUserId(UUID authUserId);
    Optional<SocialAgent> findByAuthUserId(UUID authUserId);
    long countByActiveTrue();
}
