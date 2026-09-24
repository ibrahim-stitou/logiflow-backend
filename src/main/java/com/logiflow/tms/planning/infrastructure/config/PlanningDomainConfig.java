package com.logiflow.tms.planning.infrastructure.config;

import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService;
import com.logiflow.tms.planning.domain.service.ConformiteDomainService;
import com.logiflow.tms.planning.domain.service.InsertionItineraireDomainService;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expose les services de domaine du module {@code planning} comme beans Spring, sans faire dépendre
 * le domaine du framework (aucune annotation Spring dans {@code planning.domain}).
 */
@Configuration
public class PlanningDomainConfig {

  @Bean
  public ConformiteDomainService conformiteDomainService() {
    return new ConformiteDomainService();
  }

  @Bean
  public CapaciteTronconDomainService capaciteTronconDomainService() {
    return new CapaciteTronconDomainService();
  }

  @Bean
  public ItineraireDossiersDomainService itineraireDossiersDomainService() {
    return new ItineraireDossiersDomainService();
  }

  @Bean
  public InsertionItineraireDomainService insertionItineraireDomainService() {
    return new InsertionItineraireDomainService();
  }
}
