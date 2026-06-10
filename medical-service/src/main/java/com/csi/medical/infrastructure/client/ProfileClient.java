package com.csi.medical.infrastructure.client;

import com.csi.common.domain.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

/**
 * Client REST vers profile-service pour verifier qu un patient est un assure actif.
 */
@Component
@RequiredArgsConstructor
public class ProfileClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${clients.profile-service-url:http://localhost:8082}")
    private String profileServiceUrl;

    public void ensureActiveInsured(String insuranceNumber) {
        ProfileStatusEnvelope envelope = webClientBuilder.build()
                .get()
                .uri(profileServiceUrl + "/api/v1/insured/{insuranceNumber}/status", insuranceNumber)
                .headers(headers -> currentAuthorizationHeader(headers))
                .retrieve()
                .bodyToMono(ProfileStatusEnvelope.class)
                .block();
        if (envelope == null || envelope.data() == null || !envelope.data().insured()) {
            throw new BusinessException("INSURED_NOT_ACTIVE", "Le patient n'est pas un assure actif");
        }
    }

    public DoctorData currentDoctor() {
        DoctorEnvelope envelope = webClientBuilder.build().get()
                .uri(profileServiceUrl + "/api/v1/doctors/me")
                .headers(this::currentAuthorizationHeader)
                .retrieve().bodyToMono(DoctorEnvelope.class).block();
        if (envelope == null || envelope.data() == null || !envelope.data().active()) {
            throw new BusinessException("DOCTOR_PROFILE_NOT_ACTIVE", "Le profil medecin actif est introuvable");
        }
        return envelope.data();
    }

    public DoctorData doctor(String matricule) {
        DoctorEnvelope envelope = webClientBuilder.build().get()
                .uri(profileServiceUrl + "/api/v1/doctors/{matricule}", matricule)
                .headers(this::currentAuthorizationHeader)
                .retrieve().bodyToMono(DoctorEnvelope.class).block();
        if (envelope == null || envelope.data() == null) {
            throw new BusinessException("DOCTOR_NOT_FOUND", "Medecin introuvable");
        }
        return envelope.data();
    }

    private void currentAuthorizationHeader(HttpHeaders headers) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String value = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (value != null) {
                headers.set(HttpHeaders.AUTHORIZATION, value);
            }
        }
    }

    public record ProfileStatusEnvelope(boolean success, String message, InsuredStatusData data, String timestamp) {}
    public record InsuredStatusData(String insuranceNumber, boolean insured, String status) {}
    public record DoctorEnvelope(boolean success, String message, DoctorData data, String timestamp) {}
    public record DoctorData(UUID id, UUID authUserId, String matricule, String firstName, String lastName,
                             String type, String specialty, String phoneNumber, String email, boolean active) {}
}
