package com.logiflow.tms.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propriétés applicatives typées de LogiFlow, préfixe {@code logiflow.*}, validées au démarrage.
 */
@ConfigurationProperties(prefix = "logiflow")
@Validated
public record LogiflowProperties(@NotNull @Valid Cors cors, @NotNull @Valid Security security) {

  /** Configuration CORS de l'API exposée au frontend Angular. */
  public record Cors(@NotEmpty List<String> allowedOrigins) {}

  /** Bascule de sécurité permissive réservée au profil local (jamais en dev/prod). */
  public record Security(boolean permissiveLocalProfile) {}
}
