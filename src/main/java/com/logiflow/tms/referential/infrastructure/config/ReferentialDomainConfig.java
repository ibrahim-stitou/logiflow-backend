package com.logiflow.tms.referential.infrastructure.config;

import com.logiflow.tms.referential.domain.service.SiteDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expose les services de domaine du module {@code referential} comme beans Spring, sans faire
 * dépendre le domaine du framework (aucune annotation Spring dans {@code referential.domain}).
 */
@Configuration
public class ReferentialDomainConfig {

  @Bean
  public SiteDomainService siteDomainService() {
    return new SiteDomainService();
  }
}
