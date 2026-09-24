package com.logiflow.tms.driver.domain.model;

import com.logiflow.tms.driver.domain.vo.Habilitation;
import com.logiflow.tms.driver.domain.vo.Habilitation.TypeHabilitation;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Chauffeur. Entité racine du sous-domaine Chauffeur du module {@code driver}.
 *
 * <p>Le solde de temps de conduite est un compteur porté directement par l'entité, crédité et
 * débité explicitement. Son calcul réglementaire complet (basé sur l'historique réel de conduite)
 * appartiendra au module {@code tracking} une fois le suivi d'exécution implémenté.
 *
 * <p>La photo et les pièces justificatives (permis, carte conducteur…) ne font pas partie de cet
 * agrégat : elles sont gérées, de façon optionnelle, par le module {@code document}.
 */
public final class Chauffeur {

  /**
   * Habilitations vérifiées dès qu'elles sont déclarées : présentes mais expirées à la date du
   * voyage, elles bloquent l'affectation. Absentes, elles ne bloquent pas (saisie déclarative).
   */
  private static final Set<TypeHabilitation> HABILITATIONS_CONTROLEES =
      Set.of(
          TypeHabilitation.FIMO_FCO,
          TypeHabilitation.CARTE_CONDUCTEUR,
          TypeHabilitation.VISITE_MEDICALE);

  private final UUID id;
  private final String matricule;
  private String nom;
  private String prenom;
  private ProfilChauffeur profil;
  private StatutChauffeur statut;
  private DisponibiliteChauffeur disponibilite;
  private List<Habilitation> habilitations;
  private Duration soldeTempsConduite;

  private Chauffeur(
      UUID id,
      String matricule,
      String nom,
      String prenom,
      ProfilChauffeur profil,
      StatutChauffeur statut,
      DisponibiliteChauffeur disponibilite,
      List<Habilitation> habilitations,
      Duration soldeTempsConduite) {
    this.id = Objects.requireNonNull(id, "L'identifiant du chauffeur est obligatoire");
    this.matricule = validerMatricule(matricule);
    this.nom = validerTexteObligatoire(nom, "Le nom");
    this.prenom = validerTexteObligatoire(prenom, "Le prénom");
    this.profil = profil != null ? profil : ProfilChauffeur.vide();
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.disponibilite = Objects.requireNonNull(disponibilite, "La disponibilité est obligatoire");
    this.habilitations = habilitations != null ? List.copyOf(habilitations) : List.of();
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
    return creer(id, matricule, nom, prenom, ProfilChauffeur.vide(), habilitations, soldeInitial);
  }

  /** Un nouveau chauffeur est ACTIF et DISPONIBLE. */
  public static Chauffeur creer(
      UUID id,
      String matricule,
      String nom,
      String prenom,
      ProfilChauffeur profil,
      List<Habilitation> habilitations,
      Duration soldeInitial) {
    return new Chauffeur(
        id,
        matricule,
        nom,
        prenom,
        profil,
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
      ProfilChauffeur profil,
      StatutChauffeur statut,
      DisponibiliteChauffeur disponibilite,
      List<Habilitation> habilitations,
      Duration soldeTempsConduite) {
    return new Chauffeur(
        id,
        matricule,
        nom,
        prenom,
        profil,
        statut,
        disponibilite,
        habilitations,
        soldeTempsConduite);
  }

  public void renommer(String nom, String prenom) {
    this.nom = validerTexteObligatoire(nom, "Le nom");
    this.prenom = validerTexteObligatoire(prenom, "Le prénom");
  }

  public void mettreAJourProfil(ProfilChauffeur profil) {
    this.profil = Objects.requireNonNull(profil, "Le profil est obligatoire");
  }

  public void changerStatut(StatutChauffeur nouveauStatut) {
    this.statut = Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
  }

  public void changerDisponibilite(DisponibiliteChauffeur nouvelleDisponibilite) {
    this.disponibilite =
        Objects.requireNonNull(nouvelleDisponibilite, "La nouvelle disponibilité est obligatoire");
  }

  public void mettreAJourHabilitations(List<Habilitation> habilitations) {
    this.habilitations = habilitations != null ? List.copyOf(habilitations) : List.of();
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

  /**
   * Raisons pour lesquelles ce chauffeur ne peut pas être affecté à un voyage ayant ces exigences
   * (liste vide = affectable). Règle unique, partagée par la création de voyage et l'agent de
   * planification.
   */
  public List<String> motifsNonAffectation(ExigencesAffectation exigences) {
    Objects.requireNonNull(exigences, "Les exigences sont obligatoires");
    LocalDate date = exigences.date();
    String qui = "Chauffeur " + matricule;
    List<String> motifs = new ArrayList<>();
    if (statut != StatutChauffeur.ACTIF) {
      motifs.add(qui + " : statut " + statut.name());
    }
    // EN_VOYAGE décrit l'instant présent : le chevauchement de période avec d'autres voyages est
    // contrôlé par le module planning. Les autres indisponibilités (congé, repos…) bloquent.
    if (!estDisponible() && disponibilite != DisponibiliteChauffeur.EN_VOYAGE) {
      motifs.add(qui + " : non disponible (" + disponibilite.name() + ")");
    }
    if (profil.dateExpirationPermis() != null && profil.dateExpirationPermis().isBefore(date)) {
      motifs.add(qui + " : permis de conduire expiré le " + profil.dateExpirationPermis());
    }
    CategoriePermis requis = exigences.permisRequis();
    if (requis != null
        && !profil.categoriesPermis().isEmpty()
        && !requis.estCouvertePar(profil.categoriesPermis())) {
      motifs.add(qui + " : permis " + requis.name() + " requis");
    }
    for (TypeHabilitation type : HABILITATIONS_CONTROLEES) {
      boolean declaree = habilitations.stream().anyMatch(h -> h.type() == type);
      if (declaree && !possedeHabilitation(type, date)) {
        motifs.add(qui + " : habilitation " + type.name() + " non valide au " + date);
      }
    }
    if (exigences.adr() && !possedeHabilitation(TypeHabilitation.ADR_BASE, date)) {
      motifs.add(qui + " : habilitation ADR requise (marchandises dangereuses)");
    }
    if (exigences.international()) {
      if (profil.numeroPasseport() == null
          || profil.dateExpirationPasseport() == null
          || profil.dateExpirationPasseport().isBefore(date)) {
        motifs.add(qui + " : passeport valide requis pour un voyage international");
      }
      if (profil.dateExpirationVisa() != null && profil.dateExpirationVisa().isBefore(date)) {
        motifs.add(qui + " : visa expiré le " + profil.dateExpirationVisa());
      }
    }
    return List.copyOf(motifs);
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

  public ProfilChauffeur profil() {
    return profil;
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
    if (!(o instanceof Chauffeur autre)) {
      return false;
    }
    return id.equals(autre.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
