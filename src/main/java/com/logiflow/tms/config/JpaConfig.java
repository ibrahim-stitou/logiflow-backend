package com.logiflow.tms.config;

import com.logiflow.tms.shared.infrastructure.security.SecurityContextService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Active l'audit JPA (createdBy/updatedBy) branché sur le contexte de sécurité. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

  @Bean
  public AuditorAware<String> auditorAware(SecurityContextService securityContextService) {
    return () -> java.util.Optional.of(securityContextService.identifiantPourAudit());
  }
}
