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

  /**
   * {@code permissiveLocalProfile} : démarre sans JwtDecoder (pas d'IdP). {@code
   * anonymousLocalAccess} : authentifie un utilisateur fictif pour que le frontend local puisse
   * appeler l'API. Les deux restent à {@code false} hors profil {@code local}. {@code mfa} :
   * exigence MFA sur les jetons JWT des IdP (Keycloak).
   */
  public record Security(boolean permissiveLocalProfile, boolean anonymousLocalAccess, Mfa mfa) {

    public Security {
      if (mfa == null) {
        mfa = new Mfa(false, null, null);
      }
    }

    /**
     * Exigence d'une authentification multi-facteurs (MFA) sur les jetons JWT des IdP (Keycloak).
     */
    public record Mfa(boolean required, List<String> amrMethods, List<String> acrValues) {

      public Mfa {
        if (amrMethods == null || amrMethods.isEmpty()) {
          amrMethods = List.of("mfa", "otp", "totp", "webauthn");
        } else {
          amrMethods = List.copyOf(amrMethods);
        }
        acrValues = acrValues == null ? List.of() : List.copyOf(acrValues);
      }
    }
  }
}
