package com.logiflow.tms.tracking.infrastructure.config;

import com.logiflow.tms.tracking.domain.service.TrackingDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expose les services de domaine du module {@code tracking} comme beans Spring, sans faire dépendre
 * le domaine du framework (aucune annotation Spring dans {@code tracking.domain}).
 */
@Configuration
public class TrackingDomainConfig {

  @Bean
  public TrackingDomainService trackingDomainService() {
    return new TrackingDomainService();
  }
}
