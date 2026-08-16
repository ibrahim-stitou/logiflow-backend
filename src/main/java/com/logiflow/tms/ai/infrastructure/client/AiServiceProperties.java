package com.logiflow.tms.ai.infrastructure.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration de connexion au service IA externe (application Flask), préfixe {@code
 * logiflow.ai-service.*}. Voir docs/integration-ia.md pour le détail du contrat.
 */
@ConfigurationProperties(prefix = "logiflow.ai-service")
@Validated
public record AiServiceProperties(
    @NotBlank String baseUrl,
    @NotBlank String apiKey,
    @NotNull Duration connectTimeout,
    @NotNull Duration readTimeout) {}
