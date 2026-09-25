package com.logiflow.tms.driver.domain.model;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Informations administratives d'un chauffeur (identité, contact, permis, passeport/visa, emploi),
 * modifiables d'un bloc. Tous les champs sont optionnels : seuls matricule, nom et prénom sont
 * obligatoires sur l'agrégat {@link Chauffeur}.
 *
 * <p>{@code siteRattachementId} désigne le site du référentiel où le chauffeur est basé : il sert à
 * proposer le chauffeur le plus proche du premier chargement lors de la planification.
 */
public record ProfilChauffeur(
    String cin,
    LocalDate dateNaissance,
    String lieuNaissance,
    String nationalite,
    String telephone,
    String email,
    String adresse,
    String numeroPermis,
    Set<CategoriePermis> categoriesPermis,
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
    UUID siteRattachementId) {

  public ProfilChauffeur {
    categoriesPermis =
        categoriesPermis == null || categoriesPermis.isEmpty()
            ? EnumSet.noneOf(CategoriePermis.class)
            : EnumSet.copyOf(categoriesPermis);
    if (experienceAnnees != null && experienceAnnees < 0) {
      throw new IllegalArgumentException("L'expérience ne peut pas être négative");
    }
    verifierOrdre(dateObtentionPermis, dateExpirationPermis, "du permis");
    verifierOrdre(dateDelivrancePasseport, dateExpirationPasseport, "du passeport");
    verifierOrdre(dateDelivranceVisa, dateExpirationVisa, "du visa");
  }

  public static ProfilChauffeur vide() {
    return new ProfilChauffeur(
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null);
  }

  @Override
  public Set<CategoriePermis> categoriesPermis() {
    return categoriesPermis.isEmpty()
        ? EnumSet.noneOf(CategoriePermis.class)
        : EnumSet.copyOf(categoriesPermis);
  }

  private static void verifierOrdre(LocalDate debut, LocalDate fin, String libelle) {
    if (debut != null && fin != null && !fin.isAfter(debut)) {
      throw new IllegalArgumentException(
          "La date d'expiration " + libelle + " doit être postérieure à sa date de délivrance");
    }
  }
}
