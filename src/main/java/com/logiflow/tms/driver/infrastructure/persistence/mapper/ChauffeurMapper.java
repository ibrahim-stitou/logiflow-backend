package com.logiflow.tms.driver.infrastructure.persistence.mapper;

import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.model.StatutChauffeur;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import com.logiflow.tms.driver.infrastructure.persistence.entity.ChauffeurEntity;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
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

  // TODO vérifier que le tenant courant sera lu depuis le contexte de sécurité une fois le module
  // iam implémenté ; en attendant, un tenant par défaut est utilisé (application mono-tenant).
  private static final UUID TENANT_PAR_DEFAUT =
      UUID.fromString("00000000-0000-0000-0000-000000000000");

  private final ObjectMapper objectMapper;

  public Chauffeur versDomaine(ChauffeurEntity entity) {
    if (entity == null) {
      return null;
    }
    return Chauffeur.reconstituer(
        entity.getId(),
        entity.getMatricule(),
        entity.getNomComplet(),
        StatutChauffeur.valueOf(entity.getStatut()),
        versHabilitations(entity.getHabilitationsJson()),
        Duration.ofMinutes(entity.getSoldeTempsConduiteMinutes()));
  }

  public ChauffeurEntity versEntite(Chauffeur chauffeur) {
    if (chauffeur == null) {
      return null;
    }
    return ChauffeurEntity.builder()
        .id(chauffeur.id())
        .tenantId(TENANT_PAR_DEFAUT)
        .matricule(chauffeur.matricule())
        .nomComplet(chauffeur.nomComplet())
        .statut(chauffeur.statut().name())
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
