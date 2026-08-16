package com.logiflow.tms.driver.infrastructure.config;

import com.logiflow.tms.driver.domain.service.DriverDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expose les services de domaine du module {@code driver} comme beans Spring, sans faire dépendre
 * le domaine du framework (aucune annotation Spring dans {@code driver.domain}).
 */
@Configuration
public class DriverDomainConfig {

  @Bean
  public DriverDomainService driverDomainService() {
    return new DriverDomainService();
  }
}
