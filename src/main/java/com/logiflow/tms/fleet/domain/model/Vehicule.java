package com.logiflow.tms.fleet.domain.model;

import com.logiflow.tms.fleet.domain.vo.DocumentVehicule;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Véhicule motorisé (tracteur, porteur, fourgon). Entité racine du sous-domaine Véhicule du module
 * {@code fleet}.
 *
 * <p>La disponibilité vis-à-vis d'une période donnée dépend des voyages déjà planifiés : cette
 * vérification appartient au module {@code planning}, qui consomme {@link
 * com.logiflow.tms.fleet.api.VehiculeApi#estDisponible(UUID)} pour le statut courant puis croise
 * ses propres données de planning.
 */
public final class Vehicule {

  private final UUID id;
  private final Immatriculation immatriculation;
  private final TypeVehicule type;
  private Poids ptac;
  private Poids chargeUtile;
  private int kilometrage;
  private int heuresMoteur;
  private StatutVehicule statut;
  private List<DocumentVehicule> documents;

  private Vehicule(
      UUID id,
      Immatriculation immatriculation,
      TypeVehicule type,
      Poids ptac,
      Poids chargeUtile,
      int kilometrage,
      int heuresMoteur,
      StatutVehicule statut,
      List<DocumentVehicule> documents) {
    this.id = Objects.requireNonNull(id, "L'identifiant du véhicule est obligatoire");
    this.immatriculation =
        Objects.requireNonNull(immatriculation, "L'immatriculation est obligatoire");
    this.type = Objects.requireNonNull(type, "Le type de véhicule est obligatoire");
    this.ptac = Objects.requireNonNull(ptac, "Le PTAC est obligatoire");
    this.chargeUtile = Objects.requireNonNull(chargeUtile, "La charge utile est obligatoire");
    if (kilometrage < 0) {
      throw new IllegalArgumentException("Le kilométrage ne peut pas être négatif");
    }
    if (heuresMoteur < 0) {
      throw new IllegalArgumentException("Les heures moteur ne peuvent pas être négatives");
    }
    this.kilometrage = kilometrage;
    this.heuresMoteur = heuresMoteur;
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.documents = List.copyOf(documents);
  }

  public static Vehicule creer(
      UUID id,
      Immatriculation immatriculation,
      TypeVehicule type,
      Poids ptac,
      Poids chargeUtile,
      List<DocumentVehicule> documents) {
    return new Vehicule(
        id, immatriculation, type, ptac, chargeUtile, 0, 0, StatutVehicule.DISPONIBLE, documents);
  }

  public static Vehicule reconstituer(
      UUID id,
      Immatriculation immatriculation,
      TypeVehicule type,
      Poids ptac,
      Poids chargeUtile,
      int kilometrage,
      int heuresMoteur,
      StatutVehicule statut,
      List<DocumentVehicule> documents) {
    return new Vehicule(
        id, immatriculation, type, ptac, chargeUtile, kilometrage, heuresMoteur, statut, documents);
  }

  public void relever(int nouveauKilometrage, int nouvellesHeuresMoteur) {
    if (nouveauKilometrage < this.kilometrage) {
      throw new IllegalArgumentException("Le kilométrage relevé ne peut pas régresser");
    }
    if (nouvellesHeuresMoteur < this.heuresMoteur) {
      throw new IllegalArgumentException("Les heures moteur relevées ne peuvent pas régresser");
    }
    this.kilometrage = nouveauKilometrage;
    this.heuresMoteur = nouvellesHeuresMoteur;
  }

  public void changerStatut(StatutVehicule nouveauStatut) {
    this.statut = Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
  }

  public void mettreAJourDocuments(List<DocumentVehicule> documents) {
    this.documents = List.copyOf(documents);
  }

  /** Un véhicule est conforme si tous ses documents obligatoires sont valides à la date donnée. */
  public boolean documentsValides(LocalDate date) {
    Objects.requireNonNull(date, "La date de contrôle est obligatoire");
    return documents.stream().allMatch(document -> document.estValide(date));
  }

  public boolean estDisponible() {
    return statut == StatutVehicule.DISPONIBLE;
  }

  public UUID id() {
    return id;
  }

  public Immatriculation immatriculation() {
    return immatriculation;
  }

  public TypeVehicule type() {
    return type;
  }

  public Poids ptac() {
    return ptac;
  }

  public Poids chargeUtile() {
    return chargeUtile;
  }

  public int kilometrage() {
    return kilometrage;
  }

  public int heuresMoteur() {
    return heuresMoteur;
  }

  public StatutVehicule statut() {
    return statut;
  }

  public List<DocumentVehicule> documents() {
    return documents;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Vehicule vehicule)) {
      return false;
    }
    return id.equals(vehicule.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
