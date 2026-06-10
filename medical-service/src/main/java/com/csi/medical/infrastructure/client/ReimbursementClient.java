package com.csi.medical.infrastructure.client;

import com.csi.common.domain.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Client REST vers reimbursement-service pour verifier qu un paiement a ete execute.
 */
@Component
@RequiredArgsConstructor
public class ReimbursementClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${clients.reimbursement-service-url:http://localhost:8084}")
    private String reimbursementServiceUrl;

    public ReimbursementData getReimbursement(String reimbursementNumber) {
        try {
            ReimbursementEnvelope envelope = webClientBuilder.build().get()
                    .uri(reimbursementServiceUrl + "/api/v1/reimbursements/{number}", reimbursementNumber)
                    .headers(this::currentAuthorizationHeader)
                    .retrieve()
                    .bodyToMono(ReimbursementEnvelope.class)
                    .block();
            if (envelope == null || envelope.data() == null) {
                throw new BusinessException("REIMBURSEMENT_NOT_FOUND", "Remboursement introuvable : " + reimbursementNumber);
            }
            return envelope.data();
        } catch (WebClientResponseException.NotFound e) {
            throw new BusinessException("REIMBURSEMENT_NOT_FOUND", "Remboursement introuvable : " + reimbursementNumber);
        }
    }

    private void currentAuthorizationHeader(HttpHeaders headers) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String value = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (value != null) headers.set(HttpHeaders.AUTHORIZATION, value);
        }
    }

    public record ReimbursementEnvelope(boolean success, String message, ReimbursementData data, String timestamp) {}
    public record ReimbursementData(
            UUID id,
            String reimbursementNumber,
            String sheetNumber,
            String status,
            BigDecimal baseAmount,
            BigDecimal eligibleAmount,
            BigDecimal rate,
            BigDecimal reimbursedAmount,
            String paymentType,
            String paymentReference,
            UUID approvedByUserId,
            Instant approvedAt,
            UUID processedByUserId,
            Instant processedAt,
            String ruleCode,
            String rejectionReason) {}
}
