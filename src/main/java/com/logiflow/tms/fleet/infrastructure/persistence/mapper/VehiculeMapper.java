package com.logiflow.tms.fleet.infrastructure.persistence.mapper;

import com.logiflow.tms.fleet.domain.model.StatutVehicule;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.domain.model.Vehicule;
import com.logiflow.tms.fleet.domain.vo.DocumentVehicule;
import com.logiflow.tms.fleet.infrastructure.persistence.entity.VehiculeEntity;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Traduit entre le modèle de domaine {@link Vehicule} et l'entité JPA {@link VehiculeEntity}.
 *
 * <p>Écrit à la main plutôt que via MapStruct, pour les mêmes raisons que {@code SiteMapper} :
 * fabriques statiques du domaine et sérialisation JSON des documents.
 */
@Component
@RequiredArgsConstructor
public class VehiculeMapper {

  private final ObjectMapper objectMapper;

  public Vehicule versDomaine(VehiculeEntity entity) {
    if (entity == null) {
      return null;
    }
    return Vehicule.reconstituer(
        entity.getId(),
        new Immatriculation(entity.getImmatriculation()),
        TypeVehicule.valueOf(entity.getType()),
        new Poids(entity.getPtacKg()),
        new Poids(entity.getChargeUtileKg()),
        entity.getKilometrage(),
        entity.getHeuresMoteur(),
        StatutVehicule.valueOf(entity.getStatut()),
        versDocuments(entity.getDocumentsJson()));
  }

  public VehiculeEntity versEntite(Vehicule vehicule) {
    if (vehicule == null) {
      return null;
    }
    return VehiculeEntity.builder()
        .id(vehicule.id())
        .immatriculation(vehicule.immatriculation().valeur())
        .type(vehicule.type().name())
        .ptacKg(vehicule.ptac().kg())
        .chargeUtileKg(vehicule.chargeUtile().kg())
        .kilometrage(vehicule.kilometrage())
        .heuresMoteur(vehicule.heuresMoteur())
        .statut(vehicule.statut().name())
        .documentsJson(versJson(vehicule.documents()))
        .build();
  }

  private String versJson(Object valeur) {
    try {
      return objectMapper.writeValueAsString(valeur);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de sérialisation JSON en persistance", e);
    }
  }

  private List<DocumentVehicule> versDocuments(String json) {
    if (json == null || json.isBlank()) {
      return List.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<List<DocumentVehicule>>() {});
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de désérialisation des documents véhicule", e);
    }
  }
}
