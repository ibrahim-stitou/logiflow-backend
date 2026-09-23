package com.logiflow.tms.planning.application;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierSummary;
import com.logiflow.tms.driver.api.ChauffeurApi;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.planning.api.dto.VoyageSummary;
import com.logiflow.tms.planning.application.command.CreerVoyageCommand;
import com.logiflow.tms.planning.domain.model.StatutVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.domain.service.ConformiteDomainService;
import com.logiflow.tms.planning.domain.service.ConformiteDomainService.CriteresConformite;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.vo.Capacite;
import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Voyage : création, affectation, conformité. */
@Service
@RequiredArgsConstructor
public class VoyageService implements VoyageApi {

  private static final String PREFIXE_REFERENCE = "VOY";

  private final VoyageRepository voyageRepository;
  private final SequenceReferenceGenerator referenceGenerator;
  private final ConformiteDomainService conformiteDomainService;
  private final DossierApi dossierApi;
  private final VoyageArretMaintenanceService voyageArretMaintenanceService;
  private final VehiculeApi vehiculeApi;
  private final RemorqueApi remorqueApi;
  private final ChauffeurApi chauffeurApi;

  @Transactional
  public UUID creerVoyage(CreerVoyageCommand command) {
    LocalDate aujourdHui = LocalDate.now();

    List<DossierSummary> dossiers =
        command.dossierIds().stream()
            .map(
                id ->
                    dossierApi
                        .consulter(id)
                        .orElseThrow(
                            () ->
                                new NotFoundException(
                                    "Aucun dossier de transport trouvé pour l'identifiant " + id)))
            .toList();
    for (DossierSummary dossier : dossiers) {
      if (!"CREE".equals(dossier.statut())) {
        throw new BusinessException(
            "Seul un dossier au statut CREE peut être planifié sur un voyage ("
                + dossier.reference()
                + ")");
      }
    }
    boolean contientAdr = dossiers.stream().anyMatch(DossierSummary::contientAdr);

    boolean vehiculeDisponible = vehiculeApi.estDisponible(command.vehiculeId());
    boolean documentsVehiculeValides =
        vehiculeApi.documentsValides(command.vehiculeId(), aujourdHui);

    boolean chauffeursDisponibles =
        command.affectations().stream()
            .allMatch(affectation -> chauffeurApi.estDisponible(affectation.chauffeurId()));
    boolean habilitationAdrConforme =
        !contientAdr
            || command.affectations().stream()
                .allMatch(
                    affectation ->
                        chauffeurApi.possedeHabilitationAdr(affectation.chauffeurId(), aujourdHui));
    boolean tempsConduiteSuffisant =
        command.affectations().stream()
            .allMatch(
                affectation ->
                    chauffeurApi
                        .consulter(affectation.chauffeurId())
                        .map(
                            chauffeur ->
                                chauffeur.soldeTempsConduiteMinutes()
                                    >= command.trajet().dureeConduiteMin())
                        .orElse(false));

    conformiteDomainService.verifierConformite(
        new CriteresConformite(
            vehiculeDisponible,
            documentsVehiculeValides,
            chauffeursDisponibles,
            habilitationAdrConforme,
            tempsConduiteSuffisant));

    double tauxRemplissage = calculerTauxRemplissage(command.remorqueId(), dossiers);

    var reference = referenceGenerator.generer(PREFIXE_REFERENCE, Year.now().getValue());
    Voyage voyage =
        Voyage.creer(
            UUID.randomUUID(),
            reference,
            command.typeVoyage(),
            command.portee(),
            command.departPrevu(),
            command.arriveePrevue(),
            command.vehiculeId(),
            command.remorqueId(),
            command.dossierIds(),
            command.trajet(),
            command.affectations(),
            tauxRemplissage);
    UUID voyageId = voyageRepository.sauvegarder(voyage).id();
    dossierApi.planifierPourVoyage(command.dossierIds());
    return voyageId;
  }

  private double calculerTauxRemplissage(UUID remorqueId, List<DossierSummary> dossiers) {
    if (remorqueId == null) {
      return 0d;
    }
    Capacite requise =
        dossiers.stream()
            .map(d -> new Capacite((int) Math.round(d.poidsBrutKg()), d.volumeM3(), d.nbPalettes()))
            .reduce(Capacite.zero(), Capacite::plus);
    return remorqueApi
        .consulter(remorqueId)
        .map(
            r ->
                requise.tauxRemplissage(
                    new Capacite(
                        (int) Math.round(r.chargeUtileKg()),
                        r.volumeUtileM3(),
                        r.nbPositionsPalettes())))
        .orElse(0d);
  }

  @Transactional
  public void changerStatut(UUID id, StatutVoyage statut) {
    Voyage voyage = trouverOuEchouer(id);
    voyage.changerStatut(statut);
    voyageRepository.sauvegarder(voyage);
    if (statut == StatutVoyage.ANNULE) {
      List<UUID> dossierIds = voyage.dossierIds();
      dossierApi.replanifierApresAnnulationVoyage(dossierIds);
      voyageArretMaintenanceService.purgerArretsOrphelins(id, dossierIds);
    }
  }

  @Transactional(readOnly = true)
  public Voyage consulterVoyage(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Voyage> listerVoyages(String texteRecherche, PageRequest pageRequest) {
    return voyageRepository.rechercher(texteRecherche, pageRequest);
  }

  @Transactional(readOnly = true)
  public List<Voyage> listerParDossier(UUID dossierId) {
    return voyageRepository.parDossierId(dossierId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<VoyageSummary> consulter(UUID voyageId) {
    return voyageRepository.parId(voyageId).map(this::versResume);
  }

  private VoyageSummary versResume(Voyage v) {
    return new VoyageSummary(
        v.id(),
        v.reference().valeur(),
        v.statut().name(),
        v.vehiculeId(),
        v.remorqueId(),
        v.dossierIds());
  }

  private Voyage trouverOuEchouer(UUID id) {
    return voyageRepository
        .parId(id)
        .orElseThrow(() -> new NotFoundException("Aucun voyage trouvé pour l'identifiant " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<VoyageSummary> rechercher(String texte, String statut, PageRequest pageRequest) {
    return voyageRepository.rechercherParStatut(texte, statut, pageRequest).map(this::versResume);
  }

  @Override
  public List<String> statutsConnus() {
    return Arrays.stream(StatutVoyage.values()).map(Enum::name).toList();
  }
}
