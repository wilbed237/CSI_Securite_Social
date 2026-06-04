package com.csi.reimbursement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Fournit un WebClient reutilisable pour consulter les donnees medicales.
 */
@Configuration
public class WebClientConfig {
    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
