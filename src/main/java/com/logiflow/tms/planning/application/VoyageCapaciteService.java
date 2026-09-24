package com.logiflow.tms.planning.application;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierCapaciteSummary;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.DossierSurTroncons;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.ResultatVerification;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.UtilisationTroncon;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.vo.Capacite;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation de calcul de capacité remorque par tronçon d'itinéraire. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, noRollbackFor = BusinessException.class)
public class VoyageCapaciteService {

  private final VoyageRepository voyageRepository;
  private final VoyageArretRepository voyageArretRepository;
  private final DossierApi dossierApi;
  private final RemorqueApi remorqueApi;
  private final VehiculeApi vehiculeApi;
  private final CapaciteTronconDomainService capaciteTronconDomainService;

  public List<UtilisationTroncon> obtenirUtilisationParTroncon(UUID voyageId) {
    ContexteCapacite contexte = chargerContexte(voyageId);
    return capaciteTronconDomainService.calculerUtilisation(
        contexte.arrets(), contexte.dossiersSurTroncons());
  }

  public CapaciteVoyageVue obtenirVueCapacite(UUID voyageId) {
    ContexteCapacite contexte = chargerContexte(voyageId);
    Capacite capaciteRemorque = capaciteRemorque(contexte.voyage());
    List<UtilisationTroncon> troncons =
        capaciteTronconDomainService.calculerUtilisation(
            contexte.arrets(), contexte.dossiersSurTroncons());
    return new CapaciteVoyageVue(capaciteRemorque, contexte.arrets(), troncons);
  }

  public ResultatVerification verifierCapacitePourNouveauDossier(
      UUID voyageId,
      UUID arretChargementId,
      UUID arretDechargementId,
      double poidsKg,
      double volumeM3) {
    ContexteCapacite contexte = chargerContexte(voyageId);
    Map<UUID, Integer> indices = indicesParArretId(contexte.arrets());
    return verifierCapaciteSurItineraire(
        contexte,
        indiceArret(indices, arretChargementId),
        indiceArret(indices, arretDechargementId),
        poidsKg,
        volumeM3);
  }

  public ResultatVerification verifierCapacitePourNouveauDossierSurItineraire(
      UUID voyageId,
      List<ArretVoyage> arrets,
      int indiceChargement,
      int indiceDechargement,
      double poidsKg,
      double volumeM3) {
    ContexteCapacite contexte = chargerContexte(voyageId, arrets);
    return verifierCapaciteSurItineraire(
        contexte, indiceChargement, indiceDechargement, poidsKg, volumeM3);
  }

  private ResultatVerification verifierCapaciteSurItineraire(
      ContexteCapacite contexte,
      int indiceChargement,
      int indiceDechargement,
      double poidsKg,
      double volumeM3) {
    Capacite capaciteMax = capaciteRemorque(contexte.voyage());
    List<UtilisationTroncon> utilisationActuelle =
        capaciteTronconDomainService.calculerUtilisation(
            contexte.arrets(), contexte.dossiersSurTroncons());
    return capaciteTronconDomainService.verifierAjoutDossier(
        utilisationActuelle, capaciteMax, indiceChargement, indiceDechargement, poidsKg, volumeM3);
  }

  private ContexteCapacite chargerContexte(UUID voyageId) {
    return chargerContexte(voyageId, null);
  }

  private ContexteCapacite chargerContexte(UUID voyageId, List<ArretVoyage> arretsFournis) {
    Voyage voyage =
        voyageRepository
            .parId(voyageId)
            .orElseThrow(
                () -> new NotFoundException("Aucun voyage trouvé pour l'identifiant " + voyageId));

    List<ArretVoyage> arrets =
        arretsFournis != null ? arretsFournis : voyageArretRepository.parVoyageIdOrdonnes(voyageId);
    if (arrets.size() < 2) {
      throw new BusinessException(
          "Le voyage doit comporter au moins deux arrêts pour calculer la capacité par tronçon");
    }

    Map<UUID, Integer> indices = indicesParArretId(arrets);
    List<DossierCapaciteSummary> dossiers =
        dossierApi.listerPourCalculCapacite(voyage.dossierIds());

    List<DossierSurTroncons> dossiersSurTroncons =
        dossiers.stream().map(d -> versDossierSurTroncons(d, indices)).toList();

    return new ContexteCapacite(voyage, arrets, dossiersSurTroncons);
  }

  private DossierSurTroncons versDossierSurTroncons(
      DossierCapaciteSummary dossier, Map<UUID, Integer> indices) {
    if (dossier.arretChargementId() == null || dossier.arretDechargementId() == null) {
      throw new BusinessException(
          "Le dossier "
              + dossier.id()
              + " n'a pas d'arrêts voyage affectés — impossible de calculer la capacité par tronçon");
    }
    return new DossierSurTroncons(
        dossier.poidsBrutKg(),
        dossier.volumeM3(),
        indiceArret(indices, dossier.arretChargementId()),
        indiceArret(indices, dossier.arretDechargementId()));
  }

  /**
   * Capacité du support de charge : la remorque, sinon le porteur lui-même (volume et palettes à 0
   * quand ils ne sont pas renseignés, c'est-à-dire non contrôlés).
   */
  private Capacite capaciteRemorque(Voyage voyage) {
    if (voyage.remorqueId() == null) {
      return vehiculeApi
          .consulterPourPlanification(voyage.vehiculeId(), LocalDate.now())
          .filter(vehicule -> !"TRACTEUR".equals(vehicule.type()))
          .map(
              vehicule ->
                  new Capacite(
                      (int) Math.round(vehicule.chargeUtileKg()),
                      vehicule.volumeUtileM3() == null ? 0d : vehicule.volumeUtileM3(),
                      vehicule.nbPositionsPalettes() == null ? 0 : vehicule.nbPositionsPalettes()))
          .orElseThrow(
              () ->
                  new BusinessException(
                      "Le voyage n'a pas de remorque affectée — impossible de vérifier la"
                          + " capacité"));
    }
    return remorqueApi
        .consulter(voyage.remorqueId())
        .map(
            remorque ->
                new Capacite(
                    (int) Math.round(remorque.chargeUtileKg()),
                    remorque.volumeUtileM3(),
                    remorque.nbPositionsPalettes()))
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Aucune remorque trouvée pour l'identifiant " + voyage.remorqueId()));
  }

  private Map<UUID, Integer> indicesParArretId(List<ArretVoyage> arrets) {
    Map<UUID, Integer> indices = new HashMap<>();
    for (ArretVoyage arret : arrets) {
      indices.put(arret.id(), arret.indiceSequence());
    }
    return indices;
  }

  private int indiceArret(Map<UUID, Integer> indices, UUID arretId) {
    Integer indice = indices.get(arretId);
    if (indice == null) {
      throw new BusinessException(
          "L'arrêt " + arretId + " n'appartient pas à l'itinéraire du voyage");
    }
    return indice;
  }

  public record CapaciteVoyageVue(
      Capacite capaciteRemorque, List<ArretVoyage> arrets, List<UtilisationTroncon> troncons) {}

  private record ContexteCapacite(
      Voyage voyage, List<ArretVoyage> arrets, List<DossierSurTroncons> dossiersSurTroncons) {}
}
