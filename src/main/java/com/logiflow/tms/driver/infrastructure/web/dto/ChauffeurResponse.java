package com.logiflow.tms.driver.infrastructure.web.dto;

import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ChauffeurResponse(
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
    List<Habilitation> habilitations) {

  public static ChauffeurResponse depuis(Chauffeur chauffeur) {
    return new ChauffeurResponse(
        chauffeur.id(),
        chauffeur.matricule(),
        chauffeur.nom(),
        chauffeur.prenom(),
        chauffeur.cin(),
        chauffeur.dateNaissance(),
        chauffeur.lieuNaissance(),
        chauffeur.nationalite(),
        chauffeur.telephone(),
        chauffeur.email(),
        chauffeur.adresse(),
        chauffeur.numeroPermis(),
        chauffeur.categoriePermis(),
        chauffeur.dateObtentionPermis(),
        chauffeur.dateExpirationPermis(),
        chauffeur.numeroPasseport(),
        chauffeur.dateDelivrancePasseport(),
        chauffeur.dateExpirationPasseport(),
        chauffeur.paysDelivrancePasseport(),
        chauffeur.numeroVisa(),
        chauffeur.typeVisa(),
        chauffeur.paysVisa(),
        chauffeur.dateDelivranceVisa(),
        chauffeur.dateExpirationVisa(),
        chauffeur.dateEmbauche(),
        chauffeur.typeContrat() != null ? chauffeur.typeContrat().name() : null,
        chauffeur.experienceAnnees(),
        chauffeur.specialisation(),
        chauffeur.statut().name(),
        chauffeur.disponibilite().name(),
        chauffeur.soldeTempsConduite().toMinutes(),
        chauffeur.habilitations());
  }
}
