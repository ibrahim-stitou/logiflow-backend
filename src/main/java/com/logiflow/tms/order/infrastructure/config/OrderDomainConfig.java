package com.logiflow.tms.order.infrastructure.config;

import com.logiflow.tms.order.domain.service.OrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expose les services de domaine du module {@code order} comme beans Spring, sans faire dépendre le
 * domaine du framework (aucune annotation Spring dans {@code order.domain}).
 */
@Configuration
public class OrderDomainConfig {

  @Bean
  public OrderDomainService orderDomainService() {
    return new OrderDomainService();
  }
}
