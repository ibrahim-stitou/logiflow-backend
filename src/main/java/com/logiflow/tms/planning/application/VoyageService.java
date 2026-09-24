package com.logiflow.tms.planning.application;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.planning.api.dto.VoyageSummary;
import com.logiflow.tms.planning.application.ConformiteVoyageService.RapportConformite;
import com.logiflow.tms.planning.application.command.CreerVoyageCommand;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.model.StatutVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ValidationException;
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
  private final ConformiteVoyageService conformiteVoyageService;
  private final VoyageArretRepository voyageArretRepository;
  private final DossierApi dossierApi;
  private final VoyageArretMaintenanceService voyageArretMaintenanceService;

  @Transactional
  public UUID creerVoyage(CreerVoyageCommand command) {
    UUID voyageId = UUID.randomUUID();
    RapportConformite rapport = conformiteVoyageService.evaluer(command, voyageId, null);
    if (!rapport.conforme()) {
      throw new ValidationException("Affectation non conforme", rapport.messagesBloquants());
    }

    var reference = referenceGenerator.generer(PREFIXE_REFERENCE, Year.now().getValue());
    Voyage voyage =
        Voyage.creer(
            voyageId,
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
            Math.min(1d, rapport.tauxRemplissage()));
    voyageRepository.sauvegarder(voyage);
    voyageArretRepository.sauvegarderTous(rapport.arrets());
    rapport
        .arretsParDossier()
        .forEach(
            (dossierId, arrets) ->
                dossierApi.planifierSurVoyageAvecArrets(
                    dossierId, arrets.arretChargementId(), arrets.arretDechargementId()));
    return voyageId;
  }

  /** Contrôle de conformité à blanc (aucune écriture), pour l'écran de planification. */
  @Transactional(readOnly = true)
  public RapportConformite evaluerConformite(CreerVoyageCommand command) {
    return conformiteVoyageService.evaluer(command, UUID.randomUUID(), null);
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
  public List<ArretVoyage> listerArrets(UUID voyageId) {
    trouverOuEchouer(voyageId);
    return voyageArretRepository.parVoyageIdOrdonnes(voyageId);
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

  @Transactional(readOnly = true)
  public List<Voyage> listerParChauffeur(UUID chauffeurId) {
    return voyageRepository.parChauffeurId(chauffeurId);
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
