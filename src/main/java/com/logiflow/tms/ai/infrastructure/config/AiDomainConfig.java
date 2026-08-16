package com.logiflow.tms.ai.infrastructure.config;

import com.logiflow.tms.ai.domain.service.AiDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expose les services de domaine du module {@code ai} comme beans Spring, sans faire dépendre le
 * domaine du framework (aucune annotation Spring dans {@code ai.domain}).
 */
@Configuration
public class AiDomainConfig {

  @Bean
  public AiDomainService aiDomainService() {
    return new AiDomainService();
  }
}
