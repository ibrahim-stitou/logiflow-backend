package com.logiflow.tms.fleet.infrastructure.config;

import com.logiflow.tms.fleet.domain.service.FleetDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expose les services de domaine du module {@code fleet} comme beans Spring, sans faire dépendre le
 * domaine du framework (aucune annotation Spring dans {@code fleet.domain}).
 */
@Configuration
public class FleetDomainConfig {

  @Bean
  public FleetDomainService fleetDomainService() {
    return new FleetDomainService();
  }
}
