package com.logiflow.tms.driver.infrastructure.web.dto;

import com.logiflow.tms.driver.domain.model.CategoriePermis;
import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.model.ProfilChauffeur;
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
    List<String> categoriesPermis,
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
    UUID siteRattachementId,
    String statut,
    String disponibilite,
    long soldeTempsConduiteMinutes,
    List<Habilitation> habilitations) {

  public static ChauffeurResponse depuis(Chauffeur chauffeur) {
    ProfilChauffeur profil = chauffeur.profil();
    return new ChauffeurResponse(
        chauffeur.id(),
        chauffeur.matricule(),
        chauffeur.nom(),
        chauffeur.prenom(),
        profil.cin(),
        profil.dateNaissance(),
        profil.lieuNaissance(),
        profil.nationalite(),
        profil.telephone(),
        profil.email(),
        profil.adresse(),
        profil.numeroPermis(),
        profil.categoriesPermis().stream().sorted().map(CategoriePermis::name).toList(),
        profil.dateObtentionPermis(),
        profil.dateExpirationPermis(),
        profil.numeroPasseport(),
        profil.dateDelivrancePasseport(),
        profil.dateExpirationPasseport(),
        profil.paysDelivrancePasseport(),
        profil.numeroVisa(),
        profil.typeVisa(),
        profil.paysVisa(),
        profil.dateDelivranceVisa(),
        profil.dateExpirationVisa(),
        profil.dateEmbauche(),
        profil.typeContrat() != null ? profil.typeContrat().name() : null,
        profil.experienceAnnees(),
        profil.specialisation(),
        profil.siteRattachementId(),
        chauffeur.statut().name(),
        chauffeur.disponibilite().name(),
        chauffeur.soldeTempsConduite().toMinutes(),
        chauffeur.habilitations());
  }
}
