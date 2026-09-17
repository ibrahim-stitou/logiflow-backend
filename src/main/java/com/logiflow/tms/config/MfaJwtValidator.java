package com.logiflow.tms.config;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

/**
 * Valideur JWT exigeant que le jeton atteste d'une authentification multi-facteurs (MFA).
 *
 * <p>L'authentification elle-même est déléguée à l'IdP (Keycloak) via le flux OIDC standard ; ce
 * valideur, branché sur la chaîne de validation du {@code JwtDecoder} auto-configuré par Spring
 * Boot (collecté via {@code ObjectProvider<OAuth2TokenValidator<Jwt>>}), refuse en revanche tout
 * jeton qui ne prouve pas qu'une étape MFA a été effectuée. La preuve est lue dans les claims OIDC
 * :
 *
 * <ul>
 *   <li>{@code amr} : référence des méthodes d'authentification réellement utilisées (Keycloak
 *       expose par exemple {@code ["pwd", "otp"]} quand un OTP a été validé) ;
 *   <li>{@code acr} : niveau de contexte d'authentification, si des valeurs acceptées sont
 *       configurées ({@code mfa.acr-values}).
 * </ul>
 *
 * <p>Inactif tant que {@code logiflow.security.mfa.required} est faux (comportement par défaut).
 */
public class MfaJwtValidator implements OAuth2TokenValidator<Jwt> {

  /** Message utilisateur renvoyé (401) quand le jeton ne prouve pas de MFA. */
  static final String ERREUR = "mfa_required";

  private final boolean requis;
  private final Set<String> methodesMfa;
  private final Set<String> valeursAcr;

  public MfaJwtValidator(LogiflowProperties properties) {
    LogiflowProperties.Security.Mfa mfa = properties.security().mfa();
    this.requis = mfa.required();
    this.methodesMfa =
        mfa.amrMethods().stream().map(MfaJwtValidator::normaliser).collect(Collectors.toSet());
    this.valeursAcr =
        mfa.acrValues().stream().map(MfaJwtValidator::normaliser).collect(Collectors.toSet());
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    if (!requis) {
      return OAuth2TokenValidatorResult.success();
    }
    if (prouveParAmr(jwt) || prouveParAcr(jwt)) {
      return OAuth2TokenValidatorResult.success();
    }
    return OAuth2TokenValidatorResult.failure(
        new OAuth2Error(
            ERREUR,
            "Le jeton ne prouve pas qu'une authentification multi-facteurs a été réalisée",
            null));
  }

  private boolean prouveParAmr(Jwt jwt) {
    Object claimAmr = jwt.getClaim("amr");
    if (!methodesMfa.isEmpty() && claimAmr != null) {
      if (claimAmr instanceof String methode) {
        return methodesMfa.contains(normaliser(methode));
      }
      if (claimAmr instanceof Iterable<?> methodes) {
        for (Object methode : methodes) {
          if (methode instanceof String m && methodesMfa.contains(normaliser(m))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean prouveParAcr(Jwt jwt) {
    if (valeursAcr.isEmpty()) {
      return false;
    }
    String acr = jwt.getClaimAsString("acr");
    return acr != null && valeursAcr.contains(normaliser(acr));
  }

  private static String normaliser(String valeur) {
    return StringUtils.hasText(valeur) ? valeur.strip().toLowerCase(Locale.ROOT) : "";
  }
}
