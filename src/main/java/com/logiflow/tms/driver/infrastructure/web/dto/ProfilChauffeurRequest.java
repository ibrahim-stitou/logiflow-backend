package com.logiflow.tms.driver.infrastructure.web.dto;

import com.logiflow.tms.driver.domain.model.CategoriePermis;
import com.logiflow.tms.driver.domain.model.ProfilChauffeur;
import com.logiflow.tms.driver.domain.model.TypeContrat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/** Partie administrative (optionnelle) d'une requête de création ou de mise à jour. */
public record ProfilChauffeurRequest(
    @Size(max = 30) String cin,
    LocalDate dateNaissance,
    @Size(max = 100) String lieuNaissance,
    @Size(max = 50) String nationalite,
    @Size(max = 30) String telephone,
    @Email @Size(max = 150) String email,
    String adresse,
    @Size(max = 50) String numeroPermis,
    List<CategoriePermis> categoriesPermis,
    LocalDate dateObtentionPermis,
    LocalDate dateExpirationPermis,
    @Size(max = 50) String numeroPasseport,
    LocalDate dateDelivrancePasseport,
    LocalDate dateExpirationPasseport,
    @Size(max = 100) String paysDelivrancePasseport,
    @Size(max = 50) String numeroVisa,
    @Size(max = 50) String typeVisa,
    @Size(max = 100) String paysVisa,
    LocalDate dateDelivranceVisa,
    LocalDate dateExpirationVisa,
    LocalDate dateEmbauche,
    TypeContrat typeContrat,
    @PositiveOrZero Integer experienceAnnees,
    @Size(max = 100) String specialisation,
    UUID siteRattachementId) {

  public ProfilChauffeur versProfil() {
    return new ProfilChauffeur(
        vide(cin),
        dateNaissance,
        vide(lieuNaissance),
        vide(nationalite),
        vide(telephone),
        vide(email),
        vide(adresse),
        vide(numeroPermis),
        categoriesPermis == null || categoriesPermis.isEmpty()
            ? EnumSet.noneOf(CategoriePermis.class)
            : EnumSet.copyOf(categoriesPermis),
        dateObtentionPermis,
        dateExpirationPermis,
        vide(numeroPasseport),
        dateDelivrancePasseport,
        dateExpirationPasseport,
        vide(paysDelivrancePasseport),
        vide(numeroVisa),
        vide(typeVisa),
        vide(paysVisa),
        dateDelivranceVisa,
        dateExpirationVisa,
        dateEmbauche,
        typeContrat,
        experienceAnnees,
        vide(specialisation),
        siteRattachementId);
  }

  /** Les champs texte vides du formulaire sont stockés comme absents. */
  private static String vide(String valeur) {
    return valeur == null || valeur.isBlank() ? null : valeur.strip();
  }
}
