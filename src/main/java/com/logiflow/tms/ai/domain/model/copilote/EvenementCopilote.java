package com.logiflow.tms.ai.domain.model.copilote;

import java.util.Objects;

/**
 * Événement du flux de réponse du copilote, relayé tel quel du service IA vers le frontend (SSE).
 *
 * <p>{@code nom} : meta, outil, token, sources, titre, fin ou erreur ; {@code donnees} : objet JSON
 * sérialisé sur une ligne. Voir docs/integration-ia.md.
 */
public record EvenementCopilote(String nom, String donnees) {

  public static final String ERREUR = "erreur";
  public static final String FIN = "fin";

  public EvenementCopilote {
    Objects.requireNonNull(nom, "Le nom de l'événement est obligatoire");
    Objects.requireNonNull(donnees, "Les données de l'événement sont obligatoires");
  }

  /** Événement d'erreur produit côté Spring (le message ne doit contenir ni guillemet ni \\). */
  public static EvenementCopilote erreur(String code, String message) {
    return new EvenementCopilote(
        ERREUR, "{\"code\":\"%s\",\"message\":\"%s\"}".formatted(code, message));
  }
}
