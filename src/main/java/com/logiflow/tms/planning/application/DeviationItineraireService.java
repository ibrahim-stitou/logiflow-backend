package com.logiflow.tms.planning.application;

import com.logiflow.tms.config.LogiflowProperties;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.domain.service.InsertionItineraireDomainService;
import com.logiflow.tms.planning.domain.service.InsertionItineraireDomainService.ResultatInsertion;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'utilisation de contrôle de déviation lors de l'insertion de points hors itinéraire
 * planifié.
 */
@Service
@RequiredArgsConstructor
public class DeviationItineraireService {

  private final VoyageRepository voyageRepository;
  private final VoyageArretRepository voyageArretRepository;
  private final InsertionItineraireDomainService insertionItineraireDomainService;
  private final LogiflowProperties logiflowProperties;

  @Transactional(readOnly = true)
  public ResultatInsertion trouverMeilleureInsertion(UUID voyageId, GeoPoint point) {
    List<ArretVoyage> arrets = chargerArrets(voyageId);
    return insertionItineraireDomainService.trouverMeilleureInsertion(arrets, point);
  }

  @Transactional(readOnly = true)
  public ResultatInsertion trouverMeilleureInsertion(
      UUID voyageId, GeoPoint point, int indiceArretMinimum) {
    List<ArretVoyage> arrets = chargerArrets(voyageId);
    return insertionItineraireDomainService.trouverMeilleureInsertion(
        arrets, point, indiceArretMinimum);
  }

  @Transactional(readOnly = true)
  public ResultatVerificationDeviation verifierDeviation(
      UUID voyageId,
      GeoPoint pointChargement,
      GeoPoint pointDechargement,
      @Nullable Double deviationMaxPourcent) {
    List<ArretVoyage> arrets = chargerArrets(voyageId);
    double seuil =
        deviationMaxPourcent != null
            ? deviationMaxPourcent
            : logiflowProperties.planning().deviationMaxPourcent();

    ResultatInsertion insertionChargement =
        insertionItineraireDomainService.trouverMeilleureInsertion(arrets, pointChargement);
    ResultatInsertion insertionDechargement =
        insertionItineraireDomainService.trouverMeilleureInsertion(arrets, pointDechargement);

    List<String> raisonsRejet = new ArrayList<>();
    boolean ordreValide =
        insertionDechargement.apresIndiceArret() >= insertionChargement.apresIndiceArret();
    if (!ordreValide) {
      raisonsRejet.add(
          "Le point de déchargement ne peut pas être inséré avant le point de chargement sur l'itinéraire");
    }

    boolean chargementAcceptable =
        insertionItineraireDomainService.detourAcceptable(
            insertionChargement.detourPourcent(), seuil);
    boolean dechargementAcceptable =
        ordreValide
            && insertionItineraireDomainService.detourAcceptable(
                insertionDechargement.detourPourcent(), seuil);

    if (!chargementAcceptable) {
      raisonsRejet.add(
          String.format(
              "Détour de chargement de %.1f %% supérieur au seuil de %.1f %%",
              insertionChargement.detourPourcent(), seuil));
    }
    if (ordreValide && !dechargementAcceptable) {
      raisonsRejet.add(
          String.format(
              "Détour de déchargement de %.1f %% supérieur au seuil de %.1f %%",
              insertionDechargement.detourPourcent(), seuil));
    }

    boolean accepte = ordreValide && chargementAcceptable && dechargementAcceptable;

    return new ResultatVerificationDeviation(
        insertionChargement, insertionDechargement, accepte, List.copyOf(raisonsRejet));
  }

  private List<ArretVoyage> chargerArrets(UUID voyageId) {
    voyageRepository
        .parId(voyageId)
        .orElseThrow(
            () -> new NotFoundException("Aucun voyage trouvé pour l'identifiant " + voyageId));

    List<ArretVoyage> arrets = voyageArretRepository.parVoyageIdOrdonnes(voyageId);
    if (arrets.size() < 2) {
      throw new BusinessException(
          "Le voyage doit comporter au moins deux arrêts pour évaluer une déviation d'itinéraire");
    }
    return arrets;
  }

  public record ResultatVerificationDeviation(
      ResultatInsertion chargement,
      ResultatInsertion dechargement,
      boolean accepte,
      List<String> raisonsRejet) {}
}
