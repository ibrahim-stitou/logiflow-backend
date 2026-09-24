package com.logiflow.tms.ai.domain.model.copilote;

/**
 * Disponibilité de la chaîne du copilote, affichée dans le panneau de chat.
 *
 * <p>{@code serviceIa} : le service IA répond ; {@code ollama} : UP, DOWN ou MODELE_ABSENT (serveur
 * joignable mais modèle non téléchargé) ; {@code base} : base propre du service IA ; {@code modele}
 * : modèle de chat configuré (null si le service IA est injoignable).
 */
public record EtatCopilote(boolean serviceIa, String ollama, String base, String modele) {

  public static final String INCONNU = "INCONNU";

  public static EtatCopilote serviceIaInjoignable() {
    return new EtatCopilote(false, INCONNU, INCONNU, null);
  }

  /** Le copilote peut répondre : service IA joignable et modèle prêt. */
  public boolean operationnel() {
    return serviceIa && "UP".equals(ollama) && "UP".equals(base);
  }
}
