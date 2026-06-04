package com.csi.medical.infrastructure.client;

import com.csi.common.domain.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

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
}
