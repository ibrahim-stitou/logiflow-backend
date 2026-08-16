package com.logiflow.tms.iam.infrastructure.config;

import com.logiflow.tms.iam.domain.service.IamDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expose les services de domaine du module {@code iam} comme beans Spring, sans faire dépendre le
 * domaine du framework (aucune annotation Spring dans {@code iam.domain}).
 */
@Configuration
public class IamDomainConfig {

  @Bean
  public IamDomainService iamDomainService() {
    return new IamDomainService();
  }
}
