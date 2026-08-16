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
 */
public final class Chauffeur {

  private final UUID id;
  private final String matricule;
  private String nomComplet;
  private StatutChauffeur statut;
  private List<Habilitation> habilitations;
  private Duration soldeTempsConduite;

  private Chauffeur(
      UUID id,
      String matricule,
      String nomComplet,
      StatutChauffeur statut,
      List<Habilitation> habilitations,
      Duration soldeTempsConduite) {
    this.id = Objects.requireNonNull(id, "L'identifiant du chauffeur est obligatoire");
    this.matricule = validerMatricule(matricule);
    this.nomComplet = validerNomComplet(nomComplet);
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.habilitations = List.copyOf(habilitations);
    Objects.requireNonNull(soldeTempsConduite, "Le solde de temps de conduite est obligatoire");
    if (soldeTempsConduite.isNegative()) {
      throw new IllegalArgumentException("Le solde de temps de conduite ne peut pas être négatif");
    }
    this.soldeTempsConduite = soldeTempsConduite;
  }

  public static Chauffeur creer(
      UUID id,
      String matricule,
      String nomComplet,
      List<Habilitation> habilitations,
      Duration soldeInitial) {
    return new Chauffeur(
        id, matricule, nomComplet, StatutChauffeur.DISPONIBLE, habilitations, soldeInitial);
  }

  public static Chauffeur reconstituer(
      UUID id,
      String matricule,
      String nomComplet,
      StatutChauffeur statut,
      List<Habilitation> habilitations,
      Duration soldeTempsConduite) {
    return new Chauffeur(id, matricule, nomComplet, statut, habilitations, soldeTempsConduite);
  }

  public void renommer(String nomComplet) {
    this.nomComplet = validerNomComplet(nomComplet);
  }

  public void changerStatut(StatutChauffeur nouveauStatut) {
    this.statut = Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
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
    return statut == StatutChauffeur.DISPONIBLE;
  }

  private static String validerMatricule(String matricule) {
    Objects.requireNonNull(matricule, "Le matricule du chauffeur est obligatoire");
    String normalise = matricule.strip().toUpperCase(Locale.ROOT);
    if (normalise.isEmpty()) {
      throw new IllegalArgumentException("Le matricule du chauffeur ne peut pas être vide");
    }
    return normalise;
  }

  private static String validerNomComplet(String nomComplet) {
    Objects.requireNonNull(nomComplet, "Le nom complet est obligatoire");
    if (nomComplet.isBlank()) {
      throw new IllegalArgumentException("Le nom complet ne peut pas être vide");
    }
    return nomComplet.strip();
  }

  public UUID id() {
    return id;
  }

  public String matricule() {
    return matricule;
  }

  public String nomComplet() {
    return nomComplet;
  }

  public StatutChauffeur statut() {
    return statut;
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
