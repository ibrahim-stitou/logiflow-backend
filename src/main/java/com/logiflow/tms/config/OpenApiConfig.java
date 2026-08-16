package com.logiflow.tms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation OpenAPI de l'API LogiFlow.
 *
 * <p>// TODO vérifier API springdoc-openapi 3.x (bump majeur aligné Spring Boot 4 / Spring
 * Framework 7)
 */
@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI logiflowOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("LogiFlow TMS API")
                .description(
                    "API du Transport Management System LogiFlow : commandes, dossiers de transport, "
                        + "planning des voyages, flotte, chauffeurs et maintenance.")
                .version("v0.1.0")
                .contact(new Contact().name("Équipe LogiFlow")))
        .servers(List.of(new Server().url("/").description("Serveur courant")))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
  }
}
