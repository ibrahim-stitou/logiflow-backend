package com.logiflow.tms.fleet.application;

import com.logiflow.tms.document.api.DocumentApi;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.dto.RemorqueEtatSummary;
import com.logiflow.tms.fleet.api.dto.RemorquePlanificationSummary;
import com.logiflow.tms.fleet.api.dto.RemorqueSummary;
import com.logiflow.tms.fleet.application.command.CreerRemorqueCommand;
import com.logiflow.tms.fleet.domain.model.Remorque;
import com.logiflow.tms.fleet.domain.model.StatutVehicule;
import com.logiflow.tms.fleet.domain.port.out.RemorqueRepository;
import com.logiflow.tms.fleet.domain.service.FleetDomainService;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.vo.Capacite;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Remorque. */
@Service
@RequiredArgsConstructor
public class RemorqueService implements RemorqueApi {

  private final RemorqueRepository remorqueRepository;
  private static final String TYPE_ENTITE_DOCUMENTABLE = "REMORQUE";
  private static final int PAGE_PLANIFICATION = 100;

  private final FleetDomainService fleetDomainService;
  private final DocumentApi documentApi;

  @Transactional
  public UUID creerRemorque(CreerRemorqueCommand command) {
    Immatriculation immatriculation = new Immatriculation(command.immatriculation());
    fleetDomainService.verifierImmatriculationDisponible(
        immatriculation.valeur(),
        remorqueRepository.existeParImmatriculation(immatriculation.valeur()));
    Remorque remorque =
        Remorque.creer(
            UUID.randomUUID(),
            immatriculation,
            command.type(),
            command.carrosserie(),
            command.numeroParc(),
            command.vin(),
            command.marque(),
            command.modele(),
            command.anneeFabrication(),
            command.poidsVideKg() != null ? new Poids(command.poidsVideKg()) : null,
            new Capacite(
                (int) Math.round(command.chargeUtileKg()),
                command.volumeUtileM3(),
                command.nbPositionsPalettes()),
            command.longueurM(),
            command.largeurM(),
            command.hauteurM(),
            command.groupeFroid(),
            command.temperatureMin(),
            command.temperatureMax(),
            command.datePremiereMiseCirculation(),
            command.dateAcquisition(),
            command.dateMiseEnService());
    return remorqueRepository.sauvegarder(remorque).id();
  }

  @Transactional
  public void relever(UUID id, int kilometrage, int heuresGroupeFroid) {
    Remorque remorque = trouverOuEchouer(id);
    remorque.relever(kilometrage, heuresGroupeFroid);
    remorqueRepository.sauvegarder(remorque);
  }

  @Transactional
  public void changerStatut(UUID id, StatutVehicule statut) {
    Remorque remorque = trouverOuEchouer(id);
    remorque.changerStatut(statut);
    remorqueRepository.sauvegarder(remorque);
  }

  @Transactional
  public void sortir(
      UUID id,
      LocalDate dateSortie,
      String motifSortie,
      Integer kilometrageSortie,
      Integer heuresGroupeFroidSortie) {
    Remorque remorque = trouverOuEchouer(id);
    remorque.sortir(dateSortie, motifSortie, kilometrageSortie, heuresGroupeFroidSortie);
    remorqueRepository.sauvegarder(remorque);
  }

  @Transactional(readOnly = true)
  public Remorque consulterRemorque(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Remorque> listerRemorques(String texteRecherche, PageRequest pageRequest) {
    return remorqueRepository.rechercher(texteRecherche, pageRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RemorqueSummary> consulter(UUID remorqueId) {
    return remorqueRepository.parId(remorqueId).map(this::versResume);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean estDisponible(UUID remorqueId) {
    return remorqueRepository.parId(remorqueId).map(Remorque::estDisponible).orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean documentsValides(UUID remorqueId, LocalDate date) {
    return remorqueRepository.parId(remorqueId).isPresent()
        && documentApi.tousValides(TYPE_ENTITE_DOCUMENTABLE, remorqueId, date);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RemorquePlanificationSummary> consulterPourPlanification(
      UUID remorqueId, LocalDate date) {
    return remorqueRepository.parId(remorqueId).map(r -> versPlanification(r, date));
  }

  @Override
  @Transactional(readOnly = true)
  public List<RemorquePlanificationSummary> listerPourPlanification(LocalDate date) {
    List<RemorquePlanificationSummary> resultat = new ArrayList<>();
    PageRequest page = PageRequest.premiere(PAGE_PLANIFICATION);
    Page<Remorque> courante;
    do {
      courante = remorqueRepository.rechercherParStatut("", null, page);
      courante.contenu().stream()
          .filter(r -> r.statut() != StatutVehicule.HORS_SERVICE)
          .map(r -> versPlanification(r, date))
          .forEach(resultat::add);
      page = new PageRequest(page.numero() + 1, PAGE_PLANIFICATION);
    } while (page.numero() < courante.totalPages());
    return resultat;
  }

  @Override
  @Transactional
  public void signalerImmobilisation(UUID id, boolean sinistre) {
    Remorque engin = trouverOuEchouer(id);
    if (engin.statut() != StatutVehicule.HORS_SERVICE) {
      engin.changerStatut(sinistre ? StatutVehicule.IMMOBILISE : StatutVehicule.EN_MAINTENANCE);
      remorqueRepository.sauvegarder(engin);
    }
  }

  @Override
  @Transactional
  public void signalerRemiseEnService(UUID id) {
    Remorque engin = trouverOuEchouer(id);
    if (engin.statut() == StatutVehicule.EN_MAINTENANCE
        || engin.statut() == StatutVehicule.IMMOBILISE) {
      engin.changerStatut(StatutVehicule.DISPONIBLE);
      remorqueRepository.sauvegarder(engin);
    }
  }

  @Override
  @Transactional
  public void releverCompteurs(UUID id, Integer kilometrage, Integer heures) {
    Remorque engin = trouverOuEchouer(id);
    int km = kilometrage == null ? engin.kilometrage() : Math.max(kilometrage, engin.kilometrage());
    int h =
        heures == null ? engin.heuresGroupeFroid() : Math.max(heures, engin.heuresGroupeFroid());
    engin.relever(km, h);
    remorqueRepository.sauvegarder(engin);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RemorqueEtatSummary> listerPourMaintenance() {
    List<RemorqueEtatSummary> resultat = new ArrayList<>();
    PageRequest page = PageRequest.premiere(PAGE_PLANIFICATION);
    Page<Remorque> courante;
    do {
      courante = remorqueRepository.rechercherParStatut("", null, page);
      courante.contenu().stream()
          .filter(r -> r.statut() != StatutVehicule.HORS_SERVICE)
          .map(
              r ->
                  new RemorqueEtatSummary(
                      r.id(),
                      r.immatriculation().valeur(),
                      r.carrosserie() == null ? null : r.carrosserie().name(),
                      r.statut().name(),
                      r.kilometrage(),
                      r.heuresGroupeFroid(),
                      r.anneeFabrication()))
          .forEach(resultat::add);
      page = new PageRequest(page.numero() + 1, PAGE_PLANIFICATION);
    } while (page.numero() < courante.totalPages());
    return resultat;
  }

  private RemorquePlanificationSummary versPlanification(Remorque remorque, LocalDate date) {
    var capacite = remorque.capaciteUtile();
    return new RemorquePlanificationSummary(
        remorque.id(),
        remorque.immatriculation().valeur(),
        remorque.type() == null ? null : remorque.type().name(),
        remorque.carrosserie() == null ? null : remorque.carrosserie().name(),
        capacite.poidsKg(),
        capacite.volumeM3(),
        capacite.positionsPalettes(),
        remorque.groupeFroid(),
        remorque.temperatureMin(),
        remorque.temperatureMax(),
        remorque.statut().name(),
        documentApi.tousValides(TYPE_ENTITE_DOCUMENTABLE, remorque.id(), date));
  }

  private RemorqueSummary versResume(Remorque remorque) {
    var capacite = remorque.capaciteUtile();
    return new RemorqueSummary(
        remorque.id(),
        remorque.immatriculation().valeur(),
        capacite.volumeM3(),
        capacite.positionsPalettes(),
        capacite.poidsKg(),
        remorque.statut().name());
  }

  private Remorque trouverOuEchouer(UUID id) {
    return remorqueRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucune remorque trouvée pour l'identifiant " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<RemorqueSummary> rechercher(String texte, String statut, PageRequest pageRequest) {
    return remorqueRepository.rechercherParStatut(texte, statut, pageRequest).map(this::versResume);
  }

  @Override
  public List<String> statutsConnus() {
    return Arrays.stream(StatutVehicule.values()).map(Enum::name).toList();
  }
}
