package com.logiflow.tms.driver.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA du chauffeur, isolée du modèle de domaine {@link
 * com.logiflow.tms.driver.domain.model.Chauffeur}.
 */
@Getter
@Entity
@Table(
    name = "chauffeur",
    schema = "driver",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_chauffeur_matricule",
            columnNames = {"matricule"}))
public class ChauffeurEntity extends BaseEntity {

  @Column(name = "matricule", nullable = false, length = 30)
  private String matricule;

  @Column(name = "nom", nullable = false, length = 100)
  private String nom;

  @Column(name = "prenom", nullable = false, length = 100)
  private String prenom;

  @Column(name = "cin", length = 30)
  private String cin;

  @Column(name = "date_naissance")
  private LocalDate dateNaissance;

  @Column(name = "lieu_naissance", length = 100)
  private String lieuNaissance;

  @Column(name = "nationalite", length = 50)
  private String nationalite;

  @Column(name = "telephone", length = 30)
  private String telephone;

  @Column(name = "email", length = 150)
  private String email;

  @Column(name = "adresse", columnDefinition = "text")
  private String adresse;

  @Column(name = "numero_permis", length = 50)
  private String numeroPermis;

  @Column(name = "categorie_permis", length = 50)
  private String categoriePermis;

  @Column(name = "date_obtention_permis")
  private LocalDate dateObtentionPermis;

  @Column(name = "date_expiration_permis")
  private LocalDate dateExpirationPermis;

  @Column(name = "numero_passeport", length = 50)
  private String numeroPasseport;

  @Column(name = "date_delivrance_passeport")
  private LocalDate dateDelivrancePasseport;

  @Column(name = "date_expiration_passeport")
  private LocalDate dateExpirationPasseport;

  @Column(name = "pays_delivrance_passeport", length = 100)
  private String paysDelivrancePasseport;

  @Column(name = "numero_visa", length = 50)
  private String numeroVisa;

  @Column(name = "type_visa", length = 50)
  private String typeVisa;

  @Column(name = "pays_visa", length = 100)
  private String paysVisa;

  @Column(name = "date_delivrance_visa")
  private LocalDate dateDelivranceVisa;

  @Column(name = "date_expiration_visa")
  private LocalDate dateExpirationVisa;

  @Column(name = "date_embauche")
  private LocalDate dateEmbauche;

  @Column(name = "type_contrat", length = 30)
  private String typeContrat;

  @Column(name = "experience_annees")
  private Integer experienceAnnees;

  @Column(name = "specialisation", length = 100)
  private String specialisation;

  @Column(name = "statut", nullable = false, length = 30)
  private String statut;

  @Column(name = "disponibilite", nullable = false, length = 30)
  private String disponibilite;

  @Column(name = "solde_temps_conduite_minutes", nullable = false)
  private long soldeTempsConduiteMinutes;

  @Column(name = "habilitations_json", columnDefinition = "text")
  private String habilitationsJson;

  protected ChauffeurEntity() {}

  @Builder
  public ChauffeurEntity(
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
      String typeContrat,
      Integer experienceAnnees,
      String specialisation,
      String statut,
      String disponibilite,
      long soldeTempsConduiteMinutes,
      String habilitationsJson) {
    definirId(id);
    this.matricule = matricule;
    this.nom = nom;
    this.prenom = prenom;
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
    this.statut = statut;
    this.disponibilite = disponibilite;
    this.soldeTempsConduiteMinutes = soldeTempsConduiteMinutes;
    this.habilitationsJson = habilitationsJson;
  }
}
