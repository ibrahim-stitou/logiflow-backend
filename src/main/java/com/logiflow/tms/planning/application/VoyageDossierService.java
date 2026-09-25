package com.logiflow.tms.planning.application;

import com.logiflow.tms.config.LogiflowProperties;
import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierCapaciteSummary;
import com.logiflow.tms.dossier.api.dto.DossierSummary;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.planning.application.DeviationItineraireService.ResultatVerificationDeviation;
import com.logiflow.tms.planning.application.command.AjouterDossierVoyageCommand;
import com.logiflow.tms.planning.application.command.AjouterDossierVoyageCommand.NouvelArret;
import com.logiflow.tms.planning.application.command.AjouterDossierVoyageCommand.SelectionArret;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.MotifDepassement;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.ResultatVerification;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.TronconInsuffisant;
import com.logiflow.tms.planning.domain.service.InsertionItineraireDomainService;
import com.logiflow.tms.planning.domain.service.InsertionItineraireDomainService.ResultatInsertion;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.RemorqueCapaciteDepasseeException;
import com.logiflow.tms.shared.domain.exception.RemorqueCapaciteDepasseeException.TronconEnEchec;
import com.logiflow.tms.shared.domain.exception.RouteDeviationDepasseeException;
import com.logiflow.tms.shared.domain.exception.RouteDeviationDepasseeException.PointDeviation;
import com.logiflow.tms.shared.domain.vo.Capacite;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ajout transactionnel d'un dossier à un voyage avec contrôle de déviation et de capacité. */
@Service
@RequiredArgsConstructor
public class VoyageDossierService {

  private final VoyageRepository voyageRepository;
  private final VoyageArretRepository voyageArretRepository;
  private final DossierApi dossierApi;
  private final RemorqueApi remorqueApi;
  private final DeviationItineraireService deviationItineraireService;
  private final VoyageCapaciteService voyageCapaciteService;
  private final InsertionItineraireDomainService insertionItineraireDomainService;
  private final LogiflowProperties logiflowProperties;

  public record ResultatVerificationAjout(
      boolean compatible, DeviationEchec deviation, List<TronconEnEchec> tronconsEnEchec) {

    public record DeviationEchec(
        String point, double detourKm, double detourPercent, double maxAllowedPercent) {}

    public static ResultatVerificationAjout reussi() {
      return new ResultatVerificationAjout(true, null, List.of());
    }

    public static ResultatVerificationAjout echecDeviation(DeviationEchec deviation) {
      return new ResultatVerificationAjout(false, deviation, List.of());
    }

    public static ResultatVerificationAjout echecCapacite(List<TronconEnEchec> tronconsEnEchec) {
      return new ResultatVerificationAjout(false, null, tronconsEnEchec);
    }
  }

  @Transactional(readOnly = true, noRollbackFor = BusinessException.class)
  public ResultatVerificationAjout verifierAjoutDossier(
      UUID voyageId, AjouterDossierVoyageCommand command) {
    validerSelectionArret(command.chargement(), "chargement");
    validerSelectionArret(command.dechargement(), "déchargement");

    Voyage voyage = chargerVoyage(voyageId);
    DossierSummary dossier = chargerDossier(command.dossierId());
    validerDossierTransportable(dossier);

    if (voyage.dossierIds().contains(command.dossierId())) {
      throw new BusinessException("Le dossier est déjà rattaché à ce voyage");
    }

    List<ArretVoyage> arrets = voyageArretRepository.parVoyageIdOrdonnes(voyageId);
    if (arrets.size() < 2) {
      throw new BusinessException(
          "Le voyage doit comporter au moins deux arrêts avant d'ajouter un dossier");
    }

    boolean chargementNouveau = command.chargement().nouvelArret() != null;
    boolean dechargementNouveau = command.dechargement().nouvelArret() != null;
    if (chargementNouveau || dechargementNouveau) {
      ResultatVerificationAjout.DeviationEchec deviation =
          evaluerDeviation(voyageId, command, arrets, chargementNouveau, dechargementNouveau);
      if (deviation != null) {
        return ResultatVerificationAjout.echecDeviation(deviation);
      }
    }

    IndicesResolus indices = resoudreIndicesSimules(voyageId, command, arrets);
    validerOrdreArretsDistincts(indices.indiceChargement(), indices.indiceDechargement());

    ResultatVerification capacite =
        voyageCapaciteService.verifierCapacitePourNouveauDossierSurItineraire(
            voyageId,
            indices.itineraire(),
            indices.indiceChargement(),
            indices.indiceDechargement(),
            dossier.poidsBrutKg(),
            dossier.volumeM3());

    if (!capacite.compatible()) {
      List<TronconEnEchec> echecs =
          capacite.tronconsDepasses().stream().map(this::versTronconEnEchec).toList();
      return ResultatVerificationAjout.echecCapacite(echecs);
    }

    return ResultatVerificationAjout.reussi();
  }

  @Transactional
  public UUID ajouterDossier(UUID voyageId, AjouterDossierVoyageCommand command) {
    validerSelectionArret(command.chargement(), "chargement");
    validerSelectionArret(command.dechargement(), "déchargement");

    Voyage voyage = chargerVoyageAvecVerrouillage(voyageId);
    DossierSummary dossier = chargerDossier(command.dossierId());
    validerDossierTransportable(dossier);

    if (voyage.dossierIds().contains(command.dossierId())) {
      throw new BusinessException("Le dossier est déjà rattaché à ce voyage");
    }

    List<ArretVoyage> arrets = voyageArretRepository.parVoyageIdOrdonnes(voyageId);
    if (arrets.size() < 2) {
      throw new BusinessException(
          "Le voyage doit comporter au moins deux arrêts avant d'ajouter un dossier");
    }

    boolean chargementNouveau = command.chargement().nouvelArret() != null;
    boolean dechargementNouveau = command.dechargement().nouvelArret() != null;
    if (chargementNouveau || dechargementNouveau) {
      verifierDeviationSiNecessaire(
          voyageId, command, arrets, chargementNouveau, dechargementNouveau);
    }

    UUID arretChargementId =
        resoudreArretChargement(voyageId, command.chargement(), arrets, chargementNouveau);
    arrets = voyageArretRepository.parVoyageIdOrdonnes(voyageId);
    int indiceChargement = indiceArret(arrets, arretChargementId);

    UUID arretDechargementId =
        resoudreArretDechargement(
            voyageId, command.dechargement(), dechargementNouveau, indiceChargement);

    arrets = voyageArretRepository.parVoyageIdOrdonnes(voyageId);
    validerArretsDistincts(arretChargementId, arretDechargementId);
    validerOrdreArretsDistincts(
        indiceArret(arrets, arretChargementId), indiceArret(arrets, arretDechargementId));

    verifierCapacite(
        voyageId,
        arretChargementId,
        arretDechargementId,
        dossier.poidsBrutKg(),
        dossier.volumeM3());

    dossierApi.planifierSurVoyageAvecArrets(
        command.dossierId(), arretChargementId, arretDechargementId);

    voyage.ajouterDossier(command.dossierId());
    voyage.mettreAJourRemplissage(calculerTauxRemplissage(voyage, command.dossierId(), dossier));
    voyageRepository.sauvegarder(voyage);

    return command.dossierId();
  }

  private void verifierDeviationSiNecessaire(
      UUID voyageId,
      AjouterDossierVoyageCommand command,
      List<ArretVoyage> arrets,
      boolean chargementNouveau,
      boolean dechargementNouveau) {
    ResultatVerificationAjout.DeviationEchec deviation =
        evaluerDeviation(voyageId, command, arrets, chargementNouveau, dechargementNouveau);
    if (deviation == null) {
      return;
    }
    PointDeviation point =
        "pickup".equals(deviation.point()) ? PointDeviation.PICKUP : PointDeviation.DROPOFF;
    throw new RouteDeviationDepasseeException(
        point, deviation.detourKm(), deviation.detourPercent(), deviation.maxAllowedPercent());
  }

  private ResultatVerificationAjout.DeviationEchec evaluerDeviation(
      UUID voyageId,
      AjouterDossierVoyageCommand command,
      List<ArretVoyage> arrets,
      boolean chargementNouveau,
      boolean dechargementNouveau) {
    GeoPoint pointChargement = pointDepuisSelection(command.chargement(), arrets);
    GeoPoint pointDechargement = pointDepuisSelection(command.dechargement(), arrets);
    double seuil =
        command.deviationMaxPourcent() != null
            ? command.deviationMaxPourcent()
            : logiflowProperties.planning().deviationMaxPourcent();

    ResultatVerificationDeviation deviation =
        deviationItineraireService.verifierDeviation(
            voyageId, pointChargement, pointDechargement, seuil);

    if (deviation.accepte()) {
      return null;
    }

    if (deviation.dechargement().apresIndiceArret() < deviation.chargement().apresIndiceArret()) {
      throw new BusinessException(
          "Le point de déchargement ne peut pas être inséré avant le point de chargement sur l'itinéraire");
    }

    ResultatInsertion insertionChargement = deviation.chargement();
    if (chargementNouveau && insertionChargement.detourPourcent() > seuil) {
      return new ResultatVerificationAjout.DeviationEchec(
          "pickup", insertionChargement.detourKm(), insertionChargement.detourPourcent(), seuil);
    }

    ResultatInsertion insertionDechargement = deviation.dechargement();
    if (dechargementNouveau && insertionDechargement.detourPourcent() > seuil) {
      return new ResultatVerificationAjout.DeviationEchec(
          "dropoff",
          insertionDechargement.detourKm(),
          insertionDechargement.detourPourcent(),
          seuil);
    }

    throw new BusinessException(
        deviation.raisonsRejet().isEmpty()
            ? "Déviation d'itinéraire refusée"
            : deviation.raisonsRejet().getFirst());
  }

  private record IndicesResolus(
      List<ArretVoyage> itineraire, int indiceChargement, int indiceDechargement) {}

  private IndicesResolus resoudreIndicesSimules(
      UUID voyageId, AjouterDossierVoyageCommand command, List<ArretVoyage> arretsInitials) {
    List<ArretVoyage> itineraire = arretsInitials;
    int indiceChargement;

    if (command.chargement().nouvelArret() == null) {
      indiceChargement = indiceArret(itineraire, command.chargement().arretExistantId());
    } else {
      NouvelArret nouvel = command.chargement().nouvelArret();
      ResultatInsertion insertion =
          insertionItineraireDomainService.trouverMeilleureInsertion(
              itineraire, new GeoPoint(nouvel.latitude(), nouvel.longitude()), 0);
      indiceChargement = insertion.apresIndiceArret() + 1;
      itineraire = simulerInsertion(voyageId, itineraire, insertion, nouvel);
    }

    int indiceDechargement;
    if (command.dechargement().nouvelArret() == null) {
      indiceDechargement = indiceArret(itineraire, command.dechargement().arretExistantId());
    } else {
      NouvelArret nouvel = command.dechargement().nouvelArret();
      ResultatInsertion insertion =
          insertionItineraireDomainService.trouverMeilleureInsertion(
              itineraire, new GeoPoint(nouvel.latitude(), nouvel.longitude()), indiceChargement);
      indiceDechargement = insertion.apresIndiceArret() + 1;
      itineraire = simulerInsertion(voyageId, itineraire, insertion, nouvel);
    }

    return new IndicesResolus(itineraire, indiceChargement, indiceDechargement);
  }

  private List<ArretVoyage> simulerInsertion(
      UUID voyageId,
      List<ArretVoyage> arrets,
      ResultatInsertion insertion,
      NouvelArret nouvelArret) {
    int position = insertion.apresIndiceArret() + 1;
    List<ArretVoyage> majores = new ArrayList<>(arrets.size() + 1);
    for (ArretVoyage arret : arrets) {
      if (arret.indiceSequence() >= position) {
        majores.add(arret.avecIndiceSequence(arret.indiceSequence() + 1));
      } else {
        majores.add(arret);
      }
    }
    majores.add(
        ArretVoyage.creer(
            UUID.randomUUID(),
            voyageId,
            position,
            nouvelArret.libelle(),
            new GeoPoint(nouvelArret.latitude(), nouvelArret.longitude()),
            null,
            false));
    return majores.stream().sorted(Comparator.comparingInt(ArretVoyage::indiceSequence)).toList();
  }

  private UUID resoudreArretChargement(
      UUID voyageId, SelectionArret selection, List<ArretVoyage> arrets, boolean estNouveau) {
    if (!estNouveau) {
      UUID arretId = selection.arretExistantId();
      verifierArretDuVoyage(arrets, arretId);
      return arretId;
    }
    return insererNouvelArret(voyageId, selection.nouvelArret(), 0);
  }

  private UUID resoudreArretDechargement(
      UUID voyageId, SelectionArret selection, boolean estNouveau, int indiceChargement) {
    if (!estNouveau) {
      UUID arretId = selection.arretExistantId();
      List<ArretVoyage> arrets = voyageArretRepository.parVoyageIdOrdonnes(voyageId);
      verifierArretDuVoyage(arrets, arretId);
      return arretId;
    }
    return insererNouvelArret(voyageId, selection.nouvelArret(), indiceChargement);
  }

  private UUID insererNouvelArret(UUID voyageId, NouvelArret nouvelArret, int indiceMinimum) {
    GeoPoint point = new GeoPoint(nouvelArret.latitude(), nouvelArret.longitude());
    ResultatInsertion insertion =
        deviationItineraireService.trouverMeilleureInsertion(voyageId, point, indiceMinimum);
    ArretVoyage aCreer =
        ArretVoyage.creer(
            UUID.randomUUID(),
            voyageId,
            insertion.apresIndiceArret() + 1,
            nouvelArret.libelle(),
            point,
            null,
            false);
    return voyageArretRepository
        .insererApresIndice(voyageId, aCreer, insertion.apresIndiceArret())
        .id();
  }

  private void verifierCapacite(
      UUID voyageId,
      UUID arretChargementId,
      UUID arretDechargementId,
      double poidsKg,
      double volumeM3) {
    ResultatVerification resultat =
        voyageCapaciteService.verifierCapacitePourNouveauDossier(
            voyageId, arretChargementId, arretDechargementId, poidsKg, volumeM3);
    if (resultat.compatible()) {
      return;
    }
    List<TronconEnEchec> echecs =
        resultat.tronconsDepasses().stream().map(this::versTronconEnEchec).toList();
    throw new RemorqueCapaciteDepasseeException(echecs);
  }

  private TronconEnEchec versTronconEnEchec(TronconInsuffisant troncon) {
    return new TronconEnEchec(
        troncon.arretDepartId(),
        troncon.arretArriveeId(),
        troncon.motif() == MotifDepassement.POIDS ? "weight" : "volume",
        troncon.depassementKg() > 0 ? troncon.depassementKg() : null,
        troncon.depassementM3() > 0 ? troncon.depassementM3() : null);
  }

  private double calculerTauxRemplissage(
      Voyage voyage, UUID nouveauDossierId, DossierSummary nouveauDossier) {
    if (voyage.remorqueId() == null) {
      return voyage.tauxRemplissage();
    }
    List<DossierCapaciteSummary> dossiers =
        dossierApi.listerPourCalculCapacite(voyage.dossierIds());
    Capacite requise =
        dossiers.stream()
            .map(d -> new Capacite((int) Math.round(d.poidsBrutKg()), d.volumeM3(), 0))
            .reduce(
                new Capacite(
                    (int) Math.round(nouveauDossier.poidsBrutKg()), nouveauDossier.volumeM3(), 0),
                Capacite::plus);
    return remorqueApi
        .consulter(voyage.remorqueId())
        .map(
            remorque ->
                requise.tauxRemplissage(
                    new Capacite(
                        (int) Math.round(remorque.chargeUtileKg()),
                        remorque.volumeUtileM3(),
                        remorque.nbPositionsPalettes())))
        .orElse(voyage.tauxRemplissage());
  }

  private GeoPoint pointDepuisSelection(SelectionArret selection, List<ArretVoyage> arrets) {
    if (selection.nouvelArret() != null) {
      return new GeoPoint(selection.nouvelArret().latitude(), selection.nouvelArret().longitude());
    }
    return arrets.stream()
        .filter(a -> a.id().equals(selection.arretExistantId()))
        .findFirst()
        .orElseThrow(
            () ->
                new BusinessException(
                    "L'arrêt " + selection.arretExistantId() + " n'appartient pas au voyage"))
        .localisation();
  }

  private void validerSelectionArret(SelectionArret selection, String libelle) {
    boolean aExistant = selection.arretExistantId() != null;
    boolean aNouveau = selection.nouvelArret() != null;
    if (aExistant == aNouveau) {
      throw new BusinessException(
          "La sélection d'arrêt pour le "
              + libelle
              + " doit contenir soit un arrêt existant, soit un nouvel arrêt");
    }
  }

  private Voyage chargerVoyage(UUID voyageId) {
    return voyageRepository
        .parId(voyageId)
        .orElseThrow(
            () -> new NotFoundException("Aucun voyage trouvé pour l'identifiant " + voyageId));
  }

  private Voyage chargerVoyageAvecVerrouillage(UUID voyageId) {
    return voyageRepository
        .parIdAvecVerrouillage(voyageId)
        .orElseThrow(
            () -> new NotFoundException("Aucun voyage trouvé pour l'identifiant " + voyageId));
  }

  private void validerDossierTransportable(DossierSummary dossier) {
    if (dossier.poidsBrutKg() <= 0 && dossier.volumeM3() <= 0) {
      throw new BusinessException(
          "Le dossier doit comporter un poids ou un volume strictement positif");
    }
  }

  private void validerArretsDistincts(UUID arretChargementId, UUID arretDechargementId) {
    if (arretChargementId.equals(arretDechargementId)) {
      throw new BusinessException(
          "Les arrêts de chargement et de déchargement doivent être distincts");
    }
  }

  private void validerOrdreArretsDistincts(int indiceChargement, int indiceDechargement) {
    if (indiceChargement >= indiceDechargement) {
      throw new BusinessException(
          "L'arrêt de déchargement doit être distinct et situé après l'arrêt de chargement");
    }
  }

  private DossierSummary chargerDossier(UUID dossierId) {
    return dossierApi
        .consulter(dossierId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Aucun dossier de transport trouvé pour l'identifiant " + dossierId));
  }

  private void verifierArretDuVoyage(List<ArretVoyage> arrets, UUID arretId) {
    boolean present = arrets.stream().anyMatch(a -> a.id().equals(arretId));
    if (!present) {
      throw new BusinessException(
          "L'arrêt " + arretId + " n'appartient pas à l'itinéraire du voyage");
    }
  }

  private int indiceArret(List<ArretVoyage> arrets, UUID arretId) {
    return arrets.stream()
        .filter(a -> a.id().equals(arretId))
        .findFirst()
        .orElseThrow(
            () -> new BusinessException("L'arrêt " + arretId + " n'appartient pas au voyage"))
        .indiceSequence();
  }
}
