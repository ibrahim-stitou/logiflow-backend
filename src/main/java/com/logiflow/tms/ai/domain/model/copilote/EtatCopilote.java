package com.logiflow.tms.ai.domain.model.copilote;

/**
 * Disponibilité de la chaîne du copilote, affichée dans le panneau de chat.
 *
 * <p>{@code serviceIa} : le service IA répond ; {@code llm} : UP, DOWN, CLE_ABSENTE, CLE_INVALIDE
 * ou MODELE_ABSENT ; {@code base} : base propre du service IA ; {@code modele} et {@code
 * fournisseur} : modèle et hôte du fournisseur LLM configurés (null si le service IA est
 * injoignable).
 */
public record EtatCopilote(
    boolean serviceIa, String llm, String base, String modele, String fournisseur) {

  public static final String INCONNU = "INCONNU";

  public static EtatCopilote serviceIaInjoignable() {
    return new EtatCopilote(false, INCONNU, INCONNU, null, null);
  }

  /** Le copilote peut répondre : service IA joignable, LLM et base prêts. */
  public boolean operationnel() {
    return serviceIa && "UP".equals(llm) && "UP".equals(base);
  }
}
