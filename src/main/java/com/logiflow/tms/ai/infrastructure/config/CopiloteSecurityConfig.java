package com.logiflow.tms.ai.infrastructure.config;

import com.logiflow.tms.ai.domain.port.out.ContexteCopiloteStore;
import com.logiflow.tms.ai.infrastructure.client.AiServiceProperties;
import com.logiflow.tms.ai.infrastructure.security.CopiloteOutilsAuthFilter;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * Chaîne de sécurité dédiée aux rappels du service IA ({@code /internal/copilote/**}), évaluée
 * AVANT la chaîne principale (JWT) : ces routes ne sont jamais accessibles avec un JWT utilisateur,
 * seulement avec la clé de rappel + un jeton de contexte du copilote.
 */
@Configuration
public class CopiloteSecurityConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain copiloteOutilsSecurityFilterChain(
      HttpSecurity http, AiServiceProperties properties, ContexteCopiloteStore contexteStore)
      throws Exception {
    http.securityMatcher("/internal/copilote/**")
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(
            new CopiloteOutilsAuthFilter(properties.callbackApiKey(), contexteStore),
            AnonymousAuthenticationFilter.class)
        .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
    return http.build();
  }

  /** Horloge des outils du copilote (dates « aujourd'hui », expiration des jetons). */
  @Bean
  @ConditionalOnMissingBean(Clock.class)
  public Clock horloge() {
    return Clock.system(ZoneId.of("Europe/Paris"));
  }
}
