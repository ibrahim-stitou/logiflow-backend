package com.logiflow.tms.driver.infrastructure.persistence.mapper;

import com.logiflow.tms.driver.domain.model.CategoriePermis;
import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.model.DisponibiliteChauffeur;
import com.logiflow.tms.driver.domain.model.ProfilChauffeur;
import com.logiflow.tms.driver.domain.model.StatutChauffeur;
import com.logiflow.tms.driver.domain.model.TypeContrat;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import com.logiflow.tms.driver.infrastructure.persistence.entity.ChauffeurEntity;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Traduit entre le modèle de domaine {@link Chauffeur} et l'entité JPA {@link ChauffeurEntity}.
 *
 * <p>Écrit à la main plutôt que via MapStruct, pour les mêmes raisons que {@code VehiculeMapper} :
 * fabriques statiques du domaine et sérialisation JSON des habilitations.
 */
@Component
@RequiredArgsConstructor
public class ChauffeurMapper {

  private final ObjectMapper objectMapper;

  public Chauffeur versDomaine(ChauffeurEntity entity) {
    if (entity == null) {
      return null;
    }
    return Chauffeur.reconstituer(
        entity.getId(),
        entity.getMatricule(),
        entity.getNom(),
        entity.getPrenom(),
        new ProfilChauffeur(
            entity.getCin(),
            entity.getDateNaissance(),
            entity.getLieuNaissance(),
            entity.getNationalite(),
            entity.getTelephone(),
            entity.getEmail(),
            entity.getAdresse(),
            entity.getNumeroPermis(),
            CategoriePermis.depuisTexte(entity.getCategoriePermis()),
            entity.getDateObtentionPermis(),
            entity.getDateExpirationPermis(),
            entity.getNumeroPasseport(),
            entity.getDateDelivrancePasseport(),
            entity.getDateExpirationPasseport(),
            entity.getPaysDelivrancePasseport(),
            entity.getNumeroVisa(),
            entity.getTypeVisa(),
            entity.getPaysVisa(),
            entity.getDateDelivranceVisa(),
            entity.getDateExpirationVisa(),
            entity.getDateEmbauche(),
            entity.getTypeContrat() != null ? TypeContrat.valueOf(entity.getTypeContrat()) : null,
            entity.getExperienceAnnees(),
            entity.getSpecialisation(),
            entity.getSiteRattachementId()),
        StatutChauffeur.valueOf(entity.getStatut()),
        DisponibiliteChauffeur.valueOf(entity.getDisponibilite()),
        versHabilitations(entity.getHabilitationsJson()),
        Duration.ofMinutes(entity.getSoldeTempsConduiteMinutes()));
  }

  public ChauffeurEntity versEntite(Chauffeur chauffeur) {
    if (chauffeur == null) {
      return null;
    }
    ProfilChauffeur profil = chauffeur.profil();
    return ChauffeurEntity.builder()
        .id(chauffeur.id())
        .matricule(chauffeur.matricule())
        .nom(chauffeur.nom())
        .prenom(chauffeur.prenom())
        .cin(profil.cin())
        .dateNaissance(profil.dateNaissance())
        .lieuNaissance(profil.lieuNaissance())
        .nationalite(profil.nationalite())
        .telephone(profil.telephone())
        .email(profil.email())
        .adresse(profil.adresse())
        .numeroPermis(profil.numeroPermis())
        .categoriePermis(CategoriePermis.versTexte(profil.categoriesPermis()))
        .dateObtentionPermis(profil.dateObtentionPermis())
        .dateExpirationPermis(profil.dateExpirationPermis())
        .numeroPasseport(profil.numeroPasseport())
        .dateDelivrancePasseport(profil.dateDelivrancePasseport())
        .dateExpirationPasseport(profil.dateExpirationPasseport())
        .paysDelivrancePasseport(profil.paysDelivrancePasseport())
        .numeroVisa(profil.numeroVisa())
        .typeVisa(profil.typeVisa())
        .paysVisa(profil.paysVisa())
        .dateDelivranceVisa(profil.dateDelivranceVisa())
        .dateExpirationVisa(profil.dateExpirationVisa())
        .dateEmbauche(profil.dateEmbauche())
        .typeContrat(profil.typeContrat() != null ? profil.typeContrat().name() : null)
        .experienceAnnees(profil.experienceAnnees())
        .specialisation(profil.specialisation())
        .siteRattachementId(profil.siteRattachementId())
        .statut(chauffeur.statut().name())
        .disponibilite(chauffeur.disponibilite().name())
        .soldeTempsConduiteMinutes(chauffeur.soldeTempsConduite().toMinutes())
        .habilitationsJson(versJson(chauffeur.habilitations()))
        .build();
  }

  private String versJson(Object valeur) {
    try {
      return objectMapper.writeValueAsString(valeur);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de sérialisation JSON en persistance", e);
    }
  }

  private List<Habilitation> versHabilitations(String json) {
    if (json == null || json.isBlank()) {
      return List.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<List<Habilitation>>() {});
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de désérialisation des habilitations", e);
    }
  }
}
