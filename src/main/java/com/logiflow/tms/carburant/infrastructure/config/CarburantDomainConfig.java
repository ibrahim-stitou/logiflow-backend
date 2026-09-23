package com.logiflow.tms.carburant.infrastructure.config;

import com.logiflow.tms.carburant.domain.service.StationDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CarburantDomainConfig {

  @Bean
  public StationDomainService stationDomainService() {
    return new StationDomainService();
  }
}
