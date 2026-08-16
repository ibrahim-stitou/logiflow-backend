package com.logiflow.tms.ai.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Journal d'une interaction avec le service IA externe (question posée, dossiers analysés...),
 * conservé pour la traçabilité et l'évaluation de la qualité des agents (taux d'adoption,
 * précision/rappel). Entrée de journal immuable.
 */
public final class InteractionIa {

  private final UUID id;
  private final TypeInteractionIa type;
  private final Instant horodatage;
  private final String utilisateurId;
  private final boolean succes;
  private final long dureeMs;
  private final String resume;
  private final String erreur;

  private InteractionIa(
      UUID id,
      TypeInteractionIa type,
      Instant horodatage,
      String utilisateurId,
      boolean succes,
      long dureeMs,
      String resume,
      String erreur) {
    this.id = Objects.requireNonNull(id, "L'identifiant de l'interaction est obligatoire");
    this.type = Objects.requireNonNull(type, "Le type d'interaction est obligatoire");
    this.horodatage = Objects.requireNonNull(horodatage, "L'horodatage est obligatoire");
    this.utilisateurId = utilisateurId;
    this.succes = succes;
    if (dureeMs < 0) {
      throw new IllegalArgumentException("La durée ne peut pas être négative");
    }
    this.dureeMs = dureeMs;
    this.resume = resume;
    this.erreur = erreur;
  }

  public static InteractionIa enregistrer(
      UUID id,
      TypeInteractionIa type,
      String utilisateurId,
      boolean succes,
      long dureeMs,
      String resume,
      String erreur) {
    return new InteractionIa(
        id, type, Instant.now(), utilisateurId, succes, dureeMs, resume, erreur);
  }

  public static InteractionIa reconstituer(
      UUID id,
      TypeInteractionIa type,
      Instant horodatage,
      String utilisateurId,
      boolean succes,
      long dureeMs,
      String resume,
      String erreur) {
    return new InteractionIa(id, type, horodatage, utilisateurId, succes, dureeMs, resume, erreur);
  }

  public UUID id() {
    return id;
  }

  public TypeInteractionIa type() {
    return type;
  }

  public Instant horodatage() {
    return horodatage;
  }

  public String utilisateurId() {
    return utilisateurId;
  }

  public boolean succes() {
    return succes;
  }

  public long dureeMs() {
    return dureeMs;
  }

  public String resume() {
    return resume;
  }

  public String erreur() {
    return erreur;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InteractionIa that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
