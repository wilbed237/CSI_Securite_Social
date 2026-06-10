package com.csi.auth.infrastructure.client;

import com.csi.common.domain.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

/**
 * Client REST interne vers profile-service pour creer le profil metier apres inscription.
 */
@Component
@RequiredArgsConstructor
public class ProfileServiceClient {
    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final WebClient.Builder webClientBuilder;

    @Value("${clients.profile-service-url:http://localhost:8082}")
    private String profileServiceUrl;

    @Value("${internal.profile-service-secret:dev-internal-secret}")
    private String internalSecret;

    public void createActorProfile(CreateActorProfilePayload payload) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(profileServiceUrl + "/api/v1/internal/profiles/actors")
                    .header(INTERNAL_SECRET_HEADER, internalSecret)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (RuntimeException ex) {
            throw new BusinessException("PROFILE_SYNC_FAILED", "La creation du profil metier a echoue");
        }
    }

    public record CreateActorProfilePayload(
            UUID authUserId,
            String username,
            String email,
            String phoneNumber,
            String firstName,
            String lastName,
            String actorType,
            String doctorType,
            String specialty) {}
}
