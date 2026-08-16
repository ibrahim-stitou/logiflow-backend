package com.logiflow.tms.referential.domain.model;

import com.logiflow.tms.referential.domain.vo.ContraintesAcces;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Site géolocalisé : point de chargement, de déchargement ou tiers, éventuellement rattaché à un
 * client. Entité racine du sous-domaine Site du module {@code referential}.
 */
public final class Site {

  private final UUID id;
  private final String code;
  private String libelle;
  private UUID clientId;
  private GeoPoint localisation;
  private String adresse;
  private Horaires horaires;
  private ContraintesAcces contraintesAcces;
  private boolean actif;

  private Site(
      UUID id,
      String code,
      String libelle,
      UUID clientId,
      GeoPoint localisation,
      String adresse,
      Horaires horaires,
      ContraintesAcces contraintesAcces,
      boolean actif) {
    this.id = Objects.requireNonNull(id, "L'identifiant du site est obligatoire");
    this.code = validerCode(code);
    this.libelle = validerLibelle(libelle);
    this.clientId = clientId;
    this.localisation =
        Objects.requireNonNull(localisation, "La localisation du site est obligatoire");
    this.adresse = adresse;
    this.horaires = horaires != null ? horaires : Horaires.aucun();
    this.contraintesAcces = contraintesAcces != null ? contraintesAcces : ContraintesAcces.aucune();
    this.actif = actif;
  }

  /** Crée un nouveau site, actif par défaut. */
  public static Site creer(
      UUID id,
      String code,
      String libelle,
      UUID clientId,
      GeoPoint localisation,
      String adresse,
      Horaires horaires,
      ContraintesAcces contraintesAcces) {
    return new Site(
        id, code, libelle, clientId, localisation, adresse, horaires, contraintesAcces, true);
  }

  /** Reconstitue un site depuis la persistance, sans repasser par les règles de création. */
  public static Site reconstituer(
      UUID id,
      String code,
      String libelle,
      UUID clientId,
      GeoPoint localisation,
      String adresse,
      Horaires horaires,
      ContraintesAcces contraintesAcces,
      boolean actif) {
    return new Site(
        id, code, libelle, clientId, localisation, adresse, horaires, contraintesAcces, actif);
  }

  public void modifier(
      String libelle,
      UUID clientId,
      GeoPoint localisation,
      String adresse,
      Horaires horaires,
      ContraintesAcces contraintesAcces) {
    this.libelle = validerLibelle(libelle);
    this.clientId = clientId;
    this.localisation =
        Objects.requireNonNull(localisation, "La localisation du site est obligatoire");
    this.adresse = adresse;
    this.horaires = horaires != null ? horaires : Horaires.aucun();
    this.contraintesAcces = contraintesAcces != null ? contraintesAcces : ContraintesAcces.aucune();
  }

  public void desactiver() {
    this.actif = false;
  }

  public void activer() {
    this.actif = true;
  }

  /** Un site est accessible pour un gabarit donné s'il n'excède pas ses contraintes d'accès. */
  public boolean accessiblePour(double poidsVehiculeKg) {
    if (contraintesAcces.interditPoidsLourd()) {
      return false;
    }
    Double poidsMaxTonnes = contraintesAcces.poidsMaxTonnes();
    return poidsMaxTonnes == null || (poidsVehiculeKg / 1000.0) <= poidsMaxTonnes;
  }

  private static String validerCode(String code) {
    Objects.requireNonNull(code, "Le code du site est obligatoire");
    String normalise = code.strip().toUpperCase(Locale.ROOT);
    if (normalise.isEmpty()) {
      throw new IllegalArgumentException("Le code du site ne peut pas être vide");
    }
    return normalise;
  }

  private static String validerLibelle(String libelle) {
    Objects.requireNonNull(libelle, "Le libellé du site est obligatoire");
    if (libelle.isBlank()) {
      throw new IllegalArgumentException("Le libellé du site ne peut pas être vide");
    }
    return libelle.strip();
  }

  public UUID id() {
    return id;
  }

  public String code() {
    return code;
  }

  public String libelle() {
    return libelle;
  }

  public UUID clientId() {
    return clientId;
  }

  public GeoPoint localisation() {
    return localisation;
  }

  public String adresse() {
    return adresse;
  }

  public Horaires horaires() {
    return horaires;
  }

  public ContraintesAcces contraintesAcces() {
    return contraintesAcces;
  }

  public boolean estActif() {
    return actif;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Site site)) {
      return false;
    }
    return id.equals(site.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
