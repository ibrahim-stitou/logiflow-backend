package com.logiflow.tms.dossier.infrastructure.persistence.mapper;

import com.logiflow.tms.dossier.domain.model.DossierTransport;
import com.logiflow.tms.dossier.domain.model.StatutDossier;
import com.logiflow.tms.dossier.domain.model.TypeCarrosserieRequise;
import com.logiflow.tms.dossier.domain.model.TypeTransport;
import com.logiflow.tms.dossier.domain.vo.DocumentTransport;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import com.logiflow.tms.dossier.infrastructure.persistence.entity.DossierEntity;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Traduit entre le modèle de domaine {@link DossierTransport} et l'entité JPA {@link
 * DossierEntity}. Écrit à la main pour les mêmes raisons que les autres mappers du projet.
 */
@Component
@RequiredArgsConstructor
public class DossierMapper {

  private final ObjectMapper objectMapper;

  public DossierTransport versDomaine(DossierEntity entity) {
    if (entity == null) {
      return null;
    }
    return DossierTransport.reconstituer(
        entity.getId(),
        new Reference(entity.getReference()),
        entity.getCommandeId(),
        StatutDossier.valueOf(entity.getStatut()),
        TypeTransport.valueOf(entity.getTypeTransport()),
        entity.isGroupable(),
        entity.getPoidsBrutKg(),
        entity.getVolumeM3(),
        entity.getNbPalettes(),
        entity.getFamilleMarchandise(),
        entity.getCarrosserieRequise() != null
            ? TypeCarrosserieRequise.valueOf(entity.getCarrosserieRequise())
            : null,
        entity.getTemperatureRequise(),
        versListe(
            entity.getLignesMarchandiseJson(), new TypeReference<List<LigneMarchandise>>() {}),
        versListe(entity.getSegmentsJson(), new TypeReference<List<Segment>>() {}),
        versListeOuVide(
            entity.getDocumentsJson(), new TypeReference<List<DocumentTransport>>() {}));
  }

  public DossierEntity versEntite(DossierTransport dossier) {
    if (dossier == null) {
      return null;
    }
    return DossierEntity.builder()
        .id(dossier.id())
        .reference(dossier.reference().valeur())
        .commandeId(dossier.commandeId())
        .statut(dossier.statut().name())
        .typeTransport(dossier.typeTransport().name())
        .groupable(dossier.groupable())
        .poidsBrutKg(dossier.poidsBrutKg())
        .volumeM3(dossier.volumeM3())
        .nbPalettes(dossier.nbPalettes())
        .familleMarchandise(dossier.familleMarchandise())
        .carrosserieRequise(
            dossier.carrosserieRequise() != null ? dossier.carrosserieRequise().name() : null)
        .temperatureRequise(dossier.temperatureRequise())
        .lignesMarchandiseJson(versJson(dossier.lignesMarchandise()))
        .segmentsJson(versJson(dossier.segments()))
        .documentsJson(versJson(dossier.documents()))
        .build();
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

  private <T> List<T> versListeOuVide(String json, TypeReference<List<T>> type) {
    if (json == null || json.isBlank()) {
      return List.of();
    }
    return versListe(json, type);
  }
}
