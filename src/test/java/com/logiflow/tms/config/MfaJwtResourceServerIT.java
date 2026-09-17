package com.logiflow.tms.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Vérifie le branchement réel du {@link MfaJwtValidator} sur la chaîne de validation du
 * {@code JwtDecoder} auto-configuré, en profil non permissif : un jeton signé correctement mais ne
 * prouvant pas de MFA est rejeté en 401, un jeton portant {@code amr = ["pwd","otp"]} est accepté.
 */
@AutoConfigureMockMvc
class MfaJwtResourceServerIT extends AbstractIntegrationTest {

  private static final String KID = "cle-test";

  private static final KeyPair CLE_RSA = genererCleRsa();

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void proprietesMfa(DynamicPropertyRegistry registry) throws Exception {
    Path pem = Files.createTempFile("logiflow-cle-publique", ".pem");
    Files.writeString(pem, clePubliquePem(CLE_RSA));
    registry.add(
        "spring.security.oauth2.resourceserver.jwt.public-key-location", () -> "file:" + pem);
    registry.add("logiflow.security.permissive-local-profile", () -> "false");
    registry.add("logiflow.security.mfa.required", () -> "true");
  }

  @Test
  void unJetonSansPreuveMfaEstRejeteEn401() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/utilisateurs")
                .header("Authorization", "Bearer " + signerJeton(List.of("pwd"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void unJetonProuvantUneMfaEstAccepte() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/utilisateurs")
                .header("Authorization", "Bearer " + signerJeton(List.of("pwd", "otp"))))
        .andExpect(status().isOk());
  }

  private static String signerJeton(List<String> methodesAmr) throws Exception {
    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject("jean.dupont")
            .claim("amr", methodesAmr)
            .claim("roles", List.of())
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plusSeconds(300)))
            .build();
    JWSHeader entete = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KID).build();
    SignedJWT jeton = new SignedJWT(entete, claims);
    jeton.sign(new RSASSASigner(CLE_RSA.getPrivate()));
    return jeton.serialize();
  }

  private static KeyPair genererCleRsa() {
    try {
      KeyPairGenerator generateur = KeyPairGenerator.getInstance("RSA");
      generateur.initialize(2048);
      KeyPair paire = generateur.generateKeyPair();
      if (!(paire.getPublic() instanceof RSAPublicKey)) {
        throw new IllegalStateException("La clé générée n'est pas une clé RSA");
      }
      return paire;
    } catch (Exception ex) {
      throw new IllegalStateException("Impossible de générer la clé RSA de test", ex);
    }
  }

  private static String clePubliquePem(KeyPair paire) {
    String encodage =
        Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(paire.getPublic().getEncoded());
    return "-----BEGIN PUBLIC KEY-----\n" + encodage + "\n-----END PUBLIC KEY-----\n";
  }
}