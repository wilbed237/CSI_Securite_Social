package com.csi.reimbursement.infrastructure.client;

import com.csi.common.domain.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Client REST vers medical-service pour recuperer la feuille de maladie a rembourser.
 */
@Component
@RequiredArgsConstructor
public class MedicalClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${clients.medical-service-url:http://localhost:8083}")
    private String medicalServiceUrl;

    public DiseaseSheetData getDiseaseSheet(String sheetNumber) {
        DiseaseSheetEnvelope envelope = webClientBuilder.build()
                .get()
                .uri(medicalServiceUrl + "/api/v1/disease-sheets/{sheetNumber}", sheetNumber)
                .headers(this::currentAuthorizationHeader)
                .retrieve()
                .bodyToMono(DiseaseSheetEnvelope.class)
                .block();
        if (envelope == null || envelope.data() == null) {
            throw new BusinessException("DISEASE_SHEET_NOT_FOUND", "Feuille de maladie introuvable");
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

    public record DiseaseSheetEnvelope(boolean success, String message, DiseaseSheetData data, String timestamp) {}
    public record DiseaseSheetData(UUID id, String sheetNumber, String date, String diagnosis, String status, UUID consultationId, String insuranceNumber, String doctorMatricule, String doctorType, BigDecimal consultationCost) {}
}
