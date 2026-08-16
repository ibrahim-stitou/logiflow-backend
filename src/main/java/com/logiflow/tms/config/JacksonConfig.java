package com.logiflow.tms.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;

/**
 * Personnalisation Jackson.
 *
 * <p>Spring Boot 4.1 utilise par défaut Jackson 3 (module {@code spring-boot-jackson}, package
 * {@code tools.jackson.*}) pour {@code spring-boot-starter-web}, et non plus Jackson 2 ({@code
 * com.fasterxml.jackson.*}) comme dans les lignes Boot antérieures. Les dates {@code java.time.*}
 * sont sérialisées en ISO-8601 nativement (le support JSR-310 est intégré au cœur de {@code
 * jackson-databind} 3.x) : le bascule {@code WRITE_DATES_AS_TIMESTAMPS} historique de Jackson 2
 * n'existe plus et n'est plus nécessaire.
 *
 * <p>Seule personnalisation retenue ici : tolérer les propriétés JSON inconnues en entrée, pour que
 * l'API reste compatible avec un frontend Angular qui évolue à un rythme différent du backend.
 */
@Configuration
public class JacksonConfig {

  @Bean
  public JsonMapperBuilderCustomizer jacksonCustomizer() {
    return builder -> builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }
}
