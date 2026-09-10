package com.logiflow.tms.driver.domain.model;

import com.logiflow.tms.driver.domain.vo.Habilitation;
import com.logiflow.tms.driver.domain.vo.Habilitation.TypeHabilitation;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Chauffeur. Entité racine du sous-domaine Chauffeur du module {@code driver}.
 *
 * <p>Le solde de temps de conduite est un compteur porté directement par l'entité, crédité et
 * débité explicitement. Son calcul réglementaire complet (basé sur l'historique réel de conduite)
 * appartiendra au module {@code tracking} une fois le suivi d'exécution implémenté.
 *
 * <p>La photo du chauffeur ne fait pas partie de cet agrégat : elle est gérée par le module {@code
 * document} (relation polymorphe sur cet identifiant), comme pour les véhicules et remorques.
 */
public final class Chauffeur {

  private final UUID id;
  private final String matricule;
  private String nom;
  private String prenom;
  private final String cin;
  private final LocalDate dateNaissance;
  private final String lieuNaissance;
  private final String nationalite;
  private final String telephone;
  private final String email;
  private final String adresse;
  private final String numeroPermis;
  private final String categoriePermis;
  private final LocalDate dateObtentionPermis;
  private final LocalDate dateExpirationPermis;
  private final String numeroPasseport;
  private final LocalDate dateDelivrancePasseport;
  private final LocalDate dateExpirationPasseport;
  private final String paysDelivrancePasseport;
  private final String numeroVisa;
  private final String typeVisa;
  private final String paysVisa;
  private final LocalDate dateDelivranceVisa;
  private final LocalDate dateExpirationVisa;
  private final LocalDate dateEmbauche;
  private final TypeContrat typeContrat;
  private final Integer experienceAnnees;
  private final String specialisation;
  private StatutChauffeur statut;
  private DisponibiliteChauffeur disponibilite;
  private List<Habilitation> habilitations;
  private Duration soldeTempsConduite;

  private Chauffeur(
      UUID id,
      String matricule,
      String nom,
      String prenom,
      String cin,
      LocalDate dateNaissance,
      String lieuNaissance,
      String nationalite,
      String telephone,
      String email,
      String adresse,
      String numeroPermis,
      String categoriePermis,
      LocalDate dateObtentionPermis,
      LocalDate dateExpirationPermis,
      String numeroPasseport,
      LocalDate dateDelivrancePasseport,
      LocalDate dateExpirationPasseport,
      String paysDelivrancePasseport,
      String numeroVisa,
      String typeVisa,
      String paysVisa,
      LocalDate dateDelivranceVisa,
      LocalDate dateExpirationVisa,
      LocalDate dateEmbauche,
      TypeContrat typeContrat,
      Integer experienceAnnees,
      String specialisation,
      StatutChauffeur statut,
      DisponibiliteChauffeur disponibilite,
      List<Habilitation> habilitations,
      Duration soldeTempsConduite) {
    this.id = Objects.requireNonNull(id, "L'identifiant du chauffeur est obligatoire");
    this.matricule = validerMatricule(matricule);
    this.nom = validerTexteObligatoire(nom, "Le nom");
    this.prenom = validerTexteObligatoire(prenom, "Le prénom");
    this.cin = cin;
    this.dateNaissance = dateNaissance;
    this.lieuNaissance = lieuNaissance;
    this.nationalite = nationalite;
    this.telephone = telephone;
    this.email = email;
    this.adresse = adresse;
    this.numeroPermis = numeroPermis;
    this.categoriePermis = categoriePermis;
    this.dateObtentionPermis = dateObtentionPermis;
    this.dateExpirationPermis = dateExpirationPermis;
    this.numeroPasseport = numeroPasseport;
    this.dateDelivrancePasseport = dateDelivrancePasseport;
    this.dateExpirationPasseport = dateExpirationPasseport;
    this.paysDelivrancePasseport = paysDelivrancePasseport;
    this.numeroVisa = numeroVisa;
    this.typeVisa = typeVisa;
    this.paysVisa = paysVisa;
    this.dateDelivranceVisa = dateDelivranceVisa;
    this.dateExpirationVisa = dateExpirationVisa;
    this.dateEmbauche = dateEmbauche;
    this.typeContrat = typeContrat;
    this.experienceAnnees = experienceAnnees;
    this.specialisation = specialisation;
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.disponibilite = Objects.requireNonNull(disponibilite, "La disponibilité est obligatoire");
    this.habilitations = List.copyOf(habilitations);
    Objects.requireNonNull(soldeTempsConduite, "Le solde de temps de conduite est obligatoire");
    if (soldeTempsConduite.isNegative()) {
      throw new IllegalArgumentException("Le solde de temps de conduite ne peut pas être négatif");
    }
    this.soldeTempsConduite = soldeTempsConduite;
  }

  /** Création simplifiée, sans les attributs administratifs optionnels. */
  public static Chauffeur creer(
      UUID id,
      String matricule,
      String nom,
      String prenom,
      List<Habilitation> habilitations,
      Duration soldeInitial) {
    return creer(
        id, matricule, nom, prenom, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        habilitations, soldeInitial);
  }

  public static Chauffeur creer(
      UUID id,
      String matricule,
      String nom,
      String prenom,
      String cin,
      LocalDate dateNaissance,
      String lieuNaissance,
      String nationalite,
      String telephone,
      String email,
      String adresse,
      String numeroPermis,
      String categoriePermis,
      LocalDate dateObtentionPermis,
      LocalDate dateExpirationPermis,
      String numeroPasseport,
      LocalDate dateDelivrancePasseport,
      LocalDate dateExpirationPasseport,
      String paysDelivrancePasseport,
      String numeroVisa,
      String typeVisa,
      String paysVisa,
      LocalDate dateDelivranceVisa,
      LocalDate dateExpirationVisa,
      LocalDate dateEmbauche,
      TypeContrat typeContrat,
      Integer experienceAnnees,
      String specialisation,
      List<Habilitation> habilitations,
      Duration soldeInitial) {
    return new Chauffeur(
        id,
        matricule,
        nom,
        prenom,
        cin,
        dateNaissance,
        lieuNaissance,
        nationalite,
        telephone,
        email,
        adresse,
        numeroPermis,
        categoriePermis,
        dateObtentionPermis,
        dateExpirationPermis,
        numeroPasseport,
        dateDelivrancePasseport,
        dateExpirationPasseport,
        paysDelivrancePasseport,
        numeroVisa,
        typeVisa,
        paysVisa,
        dateDelivranceVisa,
        dateExpirationVisa,
        dateEmbauche,
        typeContrat,
        experienceAnnees,
        specialisation,
        StatutChauffeur.ACTIF,
        DisponibiliteChauffeur.DISPONIBLE,
        habilitations,
        soldeInitial);
  }

  public static Chauffeur reconstituer(
      UUID id,
      String matricule,
      String nom,
      String prenom,
      String cin,
      LocalDate dateNaissance,
      String lieuNaissance,
      String nationalite,
      String telephone,
      String email,
      String adresse,
      String numeroPermis,
      String categoriePermis,
      LocalDate dateObtentionPermis,
      LocalDate dateExpirationPermis,
      String numeroPasseport,
      LocalDate dateDelivrancePasseport,
      LocalDate dateExpirationPasseport,
      String paysDelivrancePasseport,
      String numeroVisa,
      String typeVisa,
      String paysVisa,
      LocalDate dateDelivranceVisa,
      LocalDate dateExpirationVisa,
      LocalDate dateEmbauche,
      TypeContrat typeContrat,
      Integer experienceAnnees,
      String specialisation,
      StatutChauffeur statut,
      DisponibiliteChauffeur disponibilite,
      List<Habilitation> habilitations,
      Duration soldeTempsConduite) {
    return new Chauffeur(
        id,
        matricule,
        nom,
        prenom,
        cin,
        dateNaissance,
        lieuNaissance,
        nationalite,
        telephone,
        email,
        adresse,
        numeroPermis,
        categoriePermis,
        dateObtentionPermis,
        dateExpirationPermis,
        numeroPasseport,
        dateDelivrancePasseport,
        dateExpirationPasseport,
        paysDelivrancePasseport,
        numeroVisa,
        typeVisa,
        paysVisa,
        dateDelivranceVisa,
        dateExpirationVisa,
        dateEmbauche,
        typeContrat,
        experienceAnnees,
        specialisation,
        statut,
        disponibilite,
        habilitations,
        soldeTempsConduite);
  }

  public void renommer(String nom, String prenom) {
    this.nom = validerTexteObligatoire(nom, "Le nom");
    this.prenom = validerTexteObligatoire(prenom, "Le prénom");
  }

  public void changerStatut(StatutChauffeur nouveauStatut) {
    this.statut = Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
  }

  public void changerDisponibilite(DisponibiliteChauffeur nouvelleDisponibilite) {
    this.disponibilite =
        Objects.requireNonNull(nouvelleDisponibilite, "La nouvelle disponibilité est obligatoire");
  }

  public void mettreAJourHabilitations(List<Habilitation> habilitations) {
    this.habilitations = List.copyOf(habilitations);
  }

  public void consommerTempsConduite(Duration duree) {
    Objects.requireNonNull(duree, "La durée est obligatoire");
    if (duree.isNegative()) {
      throw new IllegalArgumentException("La durée consommée ne peut pas être négative");
    }
    if (duree.compareTo(soldeTempsConduite) > 0) {
      throw new BusinessException(
          "Solde de temps de conduite insuffisant pour le chauffeur " + matricule);
    }
    this.soldeTempsConduite = soldeTempsConduite.minus(duree);
  }

  public void crediterTempsConduite(Duration duree) {
    Objects.requireNonNull(duree, "La durée est obligatoire");
    if (duree.isNegative()) {
      throw new IllegalArgumentException("La durée créditée ne peut pas être négative");
    }
    this.soldeTempsConduite = soldeTempsConduite.plus(duree);
  }

  public boolean possedeHabilitation(TypeHabilitation type, LocalDate date) {
    Objects.requireNonNull(type, "Le type d'habilitation est obligatoire");
    Objects.requireNonNull(date, "La date de contrôle est obligatoire");
    return habilitations.stream()
        .anyMatch(habilitation -> habilitation.type() == type && habilitation.estValide(date));
  }

  public boolean estDisponible() {
    return disponibilite == DisponibiliteChauffeur.DISPONIBLE;
  }

  private static String validerMatricule(String matricule) {
    Objects.requireNonNull(matricule, "Le matricule du chauffeur est obligatoire");
    String normalise = matricule.strip().toUpperCase(Locale.ROOT);
    if (normalise.isEmpty()) {
      throw new IllegalArgumentException("Le matricule du chauffeur ne peut pas être vide");
    }
    return normalise;
  }

  private static String validerTexteObligatoire(String valeur, String libelle) {
    Objects.requireNonNull(valeur, libelle + " est obligatoire");
    if (valeur.isBlank()) {
      throw new IllegalArgumentException(libelle + " ne peut pas être vide");
    }
    return valeur.strip();
  }

  public UUID id() {
    return id;
  }

  public String matricule() {
    return matricule;
  }

  public String nom() {
    return nom;
  }

  public String prenom() {
    return prenom;
  }

  public String cin() {
    return cin;
  }

  public LocalDate dateNaissance() {
    return dateNaissance;
  }

  public String lieuNaissance() {
    return lieuNaissance;
  }

  public String nationalite() {
    return nationalite;
  }

  public String telephone() {
    return telephone;
  }

  public String email() {
    return email;
  }

  public String adresse() {
    return adresse;
  }

  public String numeroPermis() {
    return numeroPermis;
  }

  public String categoriePermis() {
    return categoriePermis;
  }

  public LocalDate dateObtentionPermis() {
    return dateObtentionPermis;
  }

  public LocalDate dateExpirationPermis() {
    return dateExpirationPermis;
  }

  public String numeroPasseport() {
    return numeroPasseport;
  }

  public LocalDate dateDelivrancePasseport() {
    return dateDelivrancePasseport;
  }

  public LocalDate dateExpirationPasseport() {
    return dateExpirationPasseport;
  }

  public String paysDelivrancePasseport() {
    return paysDelivrancePasseport;
  }

  public String numeroVisa() {
    return numeroVisa;
  }

  public String typeVisa() {
    return typeVisa;
  }

  public String paysVisa() {
    return paysVisa;
  }

  public LocalDate dateDelivranceVisa() {
    return dateDelivranceVisa;
  }

  public LocalDate dateExpirationVisa() {
    return dateExpirationVisa;
  }

  public LocalDate dateEmbauche() {
    return dateEmbauche;
  }

  public TypeContrat typeContrat() {
    return typeContrat;
  }

  public Integer experienceAnnees() {
    return experienceAnnees;
  }

  public String specialisation() {
    return specialisation;
  }

  public StatutChauffeur statut() {
    return statut;
  }

  public DisponibiliteChauffeur disponibilite() {
    return disponibilite;
  }

  public List<Habilitation> habilitations() {
    return habilitations;
  }

  public Duration soldeTempsConduite() {
    return soldeTempsConduite;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Chauffeur chauffeur)) {
      return false;
    }
    return id.equals(chauffeur.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
