package com.logiflow.tms.driver.infrastructure.persistence.mapper;

import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.model.DisponibiliteChauffeur;
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
        entity.getCin(),
        entity.getDateNaissance(),
        entity.getLieuNaissance(),
        entity.getNationalite(),
        entity.getTelephone(),
        entity.getEmail(),
        entity.getAdresse(),
        entity.getNumeroPermis(),
        entity.getCategoriePermis(),
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
        StatutChauffeur.valueOf(entity.getStatut()),
        DisponibiliteChauffeur.valueOf(entity.getDisponibilite()),
        versHabilitations(entity.getHabilitationsJson()),
        Duration.ofMinutes(entity.getSoldeTempsConduiteMinutes()));
  }

  public ChauffeurEntity versEntite(Chauffeur chauffeur) {
    if (chauffeur == null) {
      return null;
    }
    return ChauffeurEntity.builder()
        .id(chauffeur.id())
        .matricule(chauffeur.matricule())
        .nom(chauffeur.nom())
        .prenom(chauffeur.prenom())
        .cin(chauffeur.cin())
        .dateNaissance(chauffeur.dateNaissance())
        .lieuNaissance(chauffeur.lieuNaissance())
        .nationalite(chauffeur.nationalite())
        .telephone(chauffeur.telephone())
        .email(chauffeur.email())
        .adresse(chauffeur.adresse())
        .numeroPermis(chauffeur.numeroPermis())
        .categoriePermis(chauffeur.categoriePermis())
        .dateObtentionPermis(chauffeur.dateObtentionPermis())
        .dateExpirationPermis(chauffeur.dateExpirationPermis())
        .numeroPasseport(chauffeur.numeroPasseport())
        .dateDelivrancePasseport(chauffeur.dateDelivrancePasseport())
        .dateExpirationPasseport(chauffeur.dateExpirationPasseport())
        .paysDelivrancePasseport(chauffeur.paysDelivrancePasseport())
        .numeroVisa(chauffeur.numeroVisa())
        .typeVisa(chauffeur.typeVisa())
        .paysVisa(chauffeur.paysVisa())
        .dateDelivranceVisa(chauffeur.dateDelivranceVisa())
        .dateExpirationVisa(chauffeur.dateExpirationVisa())
        .dateEmbauche(chauffeur.dateEmbauche())
        .typeContrat(chauffeur.typeContrat() != null ? chauffeur.typeContrat().name() : null)
        .experienceAnnees(chauffeur.experienceAnnees())
        .specialisation(chauffeur.specialisation())
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
