package com.logiflow.tms.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.logiflow.tms.config.LogiflowProperties.Cors;
import com.logiflow.tms.config.LogiflowProperties.Security;
import com.logiflow.tms.config.LogiflowProperties.Security.Mfa;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/** Tests unitaires du {@link MfaJwtValidator}. */
class MfaJwtValidatorTest {

  @Test
  void mfaNonRequiseAccepteUnJetonSansPreuve() {
    MfaJwtValidator validateur = validateur(false, List.of("otp"), List.of());
    Jwt jeton = jetonSinspreuveMfa();

    OAuth2TokenValidatorResult resultat = validateur.validate(jeton);

    assertThat(resultat.hasErrors()).isFalse();
  }

  @Test
  void mfaRequiseAccepteUnJetonAvecLaMethodeAmrConfiguree() {
    MfaJwtValidator validateur = validateur(true, List.of("otp", "totp"), List.of());
    Jwt jeton = jetonAvecClaim("amr", List.of("pwd", "otp"));

    assertThat(validateur.validate(jeton).hasErrors()).isFalse();
  }

  @Test
  void mfaRequiseAccepteUnAmrScalaire() {
    MfaJwtValidator validateur = validateur(true, List.of("webauthn"), List.of());
    Jwt jeton = jetonAvecClaim("amr", "webauthn");

    assertThat(validateur.validate(jeton).hasErrors()).isFalse();
  }

  @Test
  void mfaRequiseRejetteUnJetonSansClaimDePreuve() {
    MfaJwtValidator validateur = validateur(true, List.of("otp"), List.of());

    OAuth2TokenValidatorResult resultat = validateur.validate(jetonSinspreuveMfa());

    assertThat(resultat.hasErrors()).isTrue();
    assertThat(resultat.getErrors().iterator().next().getErrorCode())
        .isEqualTo(MfaJwtValidator.ERREUR);
  }

  @Test
  void mfaRequiseRejetteUnAmrNeContenantAucuneMethodeConnue() {
    MfaJwtValidator validateur = validateur(true, List.of("otp"), List.of());
    Jwt jeton = jetonAvecClaim("amr", List.of("pwd", "smartcard"));

    assertThat(validateur.validate(jeton).hasErrors()).isTrue();
  }

  @Test
  void mfaRequiseAccepteUnJetonAvecUneValeurAcrConfiguree() {
    MfaJwtValidator validateur = validateur(true, List.of(), List.of("loa3", "phr"));
    Jwt jeton = jetonAvecClaim("acr", "phr");

    assertThat(validateur.validate(jeton).hasErrors()).isFalse();
  }

  @Test
  void mfaRequiseRejetteUneValeurAcrNonConfiguree() {
    MfaJwtValidator validateur = validateur(true, List.of(), List.of("loa3"));
    Jwt jeton = jetonAvecClaim("acr", "basic");

    assertThat(validateur.validate(jeton).hasErrors()).isTrue();
  }

  @Test
  void mfaRequiseAvecConfigurationParDefautRejetteUneAuthentificationASimpleFacteur() {
    MfaJwtValidator validateur = validateur(true, null, null);
    Jwt jeton = jetonAvecClaim("amr", List.of("pwd"));

    assertThat(validateur.validate(jeton).hasErrors()).isTrue();
  }

  private static MfaJwtValidator validateur(
      boolean requis, List<String> methodes, List<String> acr) {
    Security security = new Security(false, false, new Mfa(requis, methodes, acr));
    return new MfaJwtValidator(
        new LogiflowProperties(
            new Cors(List.of("http://localhost:4200")),
            security,
            new LogiflowProperties.Planning(25)));
  }

  private static Jwt jetonSinspreuveMfa() {
    return Jwt.withTokenValue("jeton").header("alg", "none").subject("utilisateur").build();
  }

  private static Jwt jetonAvecClaim(String nomClaim, Object valeur) {
    return Jwt.withTokenValue("jeton")
        .header("alg", "none")
        .subject("utilisateur")
        .claim(nomClaim, valeur)
        .build();
  }
}
