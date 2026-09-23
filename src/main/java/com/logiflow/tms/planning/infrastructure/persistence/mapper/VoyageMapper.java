package com.logiflow.tms.planning.infrastructure.persistence.mapper;

import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.StatutVoyage;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.planning.infrastructure.persistence.entity.VoyageEntity;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Traduit entre le modèle de domaine {@link Voyage} et l'entité JPA {@link VoyageEntity}. Écrit à
 * la main pour les mêmes raisons que les autres mappers du projet.
 */
@Component
@RequiredArgsConstructor
public class VoyageMapper {

  private final ObjectMapper objectMapper;

  public Voyage versDomaine(VoyageEntity entity) {
    if (entity == null) {
      return null;
    }
    return Voyage.reconstituer(
        entity.getId(),
        new Reference(entity.getReference()),
        TypeVoyage.valueOf(entity.getTypeVoyage()),
        Portee.valueOf(entity.getPortee()),
        StatutVoyage.valueOf(entity.getStatut()),
        entity.getDepartPrevu(),
        entity.getArriveePrevue(),
        entity.getVehiculeId(),
        entity.getRemorqueId(),
        versListe(entity.getDossierIdsJson(), new TypeReference<List<UUID>>() {}),
        versObjet(entity.getTrajetJson(), Trajet.class),
        versListe(entity.getAffectationsJson(), new TypeReference<List<Affectation>>() {}),
        entity.getTauxRemplissage());
  }

  public VoyageEntity versEntite(Voyage voyage) {
    if (voyage == null) {
      return null;
    }
    return VoyageEntity.builder()
        .id(voyage.id())
        .reference(voyage.reference().valeur())
        .typeVoyage(voyage.typeVoyage().name())
        .portee(voyage.portee().name())
        .statut(voyage.statut().name())
        .departPrevu(voyage.departPrevu())
        .arriveePrevue(voyage.arriveePrevue())
        .vehiculeId(voyage.vehiculeId())
        .remorqueId(voyage.remorqueId())
        .dossierIdsJson(versJson(voyage.dossierIds()))
        .trajetJson(versJson(voyage.trajet()))
        .affectationsJson(versJson(voyage.affectations()))
        .tauxRemplissage(voyage.tauxRemplissage())
        .build();
  }

  public void mettreAJour(VoyageEntity entity, Voyage voyage) {
    if (entity == null || voyage == null) {
      return;
    }
    entity.ecraserEtatMetier(
        voyage.statut().name(),
        voyage.departPrevu(),
        voyage.arriveePrevue(),
        voyage.vehiculeId(),
        voyage.remorqueId(),
        versJson(voyage.dossierIds()),
        versJson(voyage.trajet()),
        versJson(voyage.affectations()),
        voyage.tauxRemplissage());
  }

  private String versJson(Object valeur) {
    try {
      return objectMapper.writeValueAsString(valeur);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de sérialisation JSON en persistance", e);
    }
  }

  private <T> List<T> versListe(String json, TypeReference<List<T>> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de désérialisation JSON en persistance", e);
    }
  }

  private <T> T versObjet(String json, Class<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de désérialisation JSON en persistance", e);
    }
  }
}
