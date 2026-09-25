package com.logiflow.tms.planning.domain.model;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.Objects;
import java.util.UUID;

/**
 * Arrêt ordonné sur l'itinéraire d'un voyage. Sert de référence pour le calcul de capacité par
 * tronçon et pour l'insertion de points hors route planifiée initialement.
 */
public final class ArretVoyage {

  private final UUID id;
  private final UUID voyageId;
  private final int indiceSequence;
  private final String libelle;
  private final GeoPoint localisation;
  private final UUID siteId;
  private final boolean estOriginal;

  private ArretVoyage(
      UUID id,
      UUID voyageId,
      int indiceSequence,
      String libelle,
      GeoPoint localisation,
      UUID siteId,
      boolean estOriginal) {
    this.id = Objects.requireNonNull(id, "L'identifiant de l'arrêt est obligatoire");
    this.voyageId = Objects.requireNonNull(voyageId, "Le voyage est obligatoire");
    if (indiceSequence < 0) {
      throw new IllegalArgumentException("L'indice de séquence ne peut pas être négatif");
    }
    this.indiceSequence = indiceSequence;
    this.libelle = Objects.requireNonNull(libelle, "Le libellé de l'arrêt est obligatoire");
    if (libelle.isBlank()) {
      throw new IllegalArgumentException("Le libellé de l'arrêt ne peut pas être vide");
    }
    this.localisation = Objects.requireNonNull(localisation, "La localisation est obligatoire");
    this.siteId = siteId;
    this.estOriginal = estOriginal;
  }

  public static ArretVoyage creer(
      UUID id,
      UUID voyageId,
      int indiceSequence,
      String libelle,
      GeoPoint localisation,
      UUID siteId,
      boolean estOriginal) {
    return new ArretVoyage(
        id, voyageId, indiceSequence, libelle, localisation, siteId, estOriginal);
  }

  public static ArretVoyage reconstituer(
      UUID id,
      UUID voyageId,
      int indiceSequence,
      String libelle,
      GeoPoint localisation,
      UUID siteId,
      boolean estOriginal) {
    return new ArretVoyage(
        id, voyageId, indiceSequence, libelle, localisation, siteId, estOriginal);
  }

  public UUID id() {
    return id;
  }

  public UUID voyageId() {
    return voyageId;
  }

  public int indiceSequence() {
    return indiceSequence;
  }

  public String libelle() {
    return libelle;
  }

  public GeoPoint localisation() {
    return localisation;
  }

  public UUID siteId() {
    return siteId;
  }

  public boolean estOriginal() {
    return estOriginal;
  }

  public ArretVoyage avecIndiceSequence(int nouvelIndice) {
    return reconstituer(id, voyageId, nouvelIndice, libelle, localisation, siteId, estOriginal);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArretVoyage arret)) {
      return false;
    }
    return id.equals(arret.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
