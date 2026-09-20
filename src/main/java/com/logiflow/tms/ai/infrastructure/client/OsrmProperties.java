package com.logiflow.tms.ai.infrastructure.client;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuration du moteur OSRM utilisé pour la géométrie des itinéraires cartographiques. */
@Validated
@ConfigurationProperties(prefix = "logiflow.osrm")
public record OsrmProperties(
    @NotBlank String baseUrl,
    Duration connectTimeout,
    Duration readTimeout) {

  public OsrmProperties {
    if (connectTimeout == null) {
      connectTimeout = Duration.ofSeconds(2);
    }
    if (readTimeout == null) {
      readTimeout = Duration.ofSeconds(8);
    }
  }
}
