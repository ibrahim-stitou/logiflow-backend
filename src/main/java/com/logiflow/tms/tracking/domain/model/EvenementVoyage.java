package com.logiflow.tms.tracking.domain.model;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Événement d'exécution d'un voyage (départ, arrivée, incident, position GPS...). Entrée de journal
 * immuable : un événement déclaré n'est jamais modifié, seulement consulté.
 */
public final class EvenementVoyage {

  private final UUID id;
  private final UUID voyageId;
  private final TypeEvenement type;
  private final Instant horodatage;
  private final GeoPoint position;
  private final String commentaire;

  private EvenementVoyage(
      UUID id,
      UUID voyageId,
      TypeEvenement type,
      Instant horodatage,
      GeoPoint position,
      String commentaire) {
    this.id = Objects.requireNonNull(id, "L'identifiant de l'événement est obligatoire");
    this.voyageId = Objects.requireNonNull(voyageId, "Le voyage est obligatoire");
    this.type = Objects.requireNonNull(type, "Le type d'événement est obligatoire");
    this.horodatage = Objects.requireNonNull(horodatage, "L'horodatage est obligatoire");
    this.position = position;
    this.commentaire = commentaire;
  }

  public static EvenementVoyage declarer(
      UUID id,
      UUID voyageId,
      TypeEvenement type,
      Instant horodatage,
      GeoPoint position,
      String commentaire) {
    return new EvenementVoyage(id, voyageId, type, horodatage, position, commentaire);
  }

  public static EvenementVoyage reconstituer(
      UUID id,
      UUID voyageId,
      TypeEvenement type,
      Instant horodatage,
      GeoPoint position,
      String commentaire) {
    return new EvenementVoyage(id, voyageId, type, horodatage, position, commentaire);
  }

  public boolean estGeolocalise() {
    return position != null;
  }

  public UUID id() {
    return id;
  }

  public UUID voyageId() {
    return voyageId;
  }

  public TypeEvenement type() {
    return type;
  }

  public Instant horodatage() {
    return horodatage;
  }

  public GeoPoint position() {
    return position;
  }

  public String commentaire() {
    return commentaire;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvenementVoyage that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
