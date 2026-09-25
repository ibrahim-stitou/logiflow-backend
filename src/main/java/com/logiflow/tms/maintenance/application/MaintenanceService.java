package com.logiflow.tms.maintenance.application;

import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import com.logiflow.tms.maintenance.api.dto.IndisponibiliteSummary;
import com.logiflow.tms.maintenance.api.dto.OrdreTravailSummary;
import com.logiflow.tms.maintenance.api.dto.PlanEntretienSummary;
import com.logiflow.tms.maintenance.api.dto.ScoreSanteSummary;
import com.logiflow.tms.maintenance.api.dto.SinistreSummary;
import com.logiflow.tms.maintenance.application.PlanEntretienService.EcheancePlan;
import com.logiflow.tms.maintenance.application.command.CalculerScoreSanteCommand;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.ScoreSante;
import com.logiflow.tms.maintenance.domain.model.Sinistre;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.domain.port.out.PlanEntretienRepository;
import com.logiflow.tms.maintenance.domain.port.out.ScoreSanteRepository;
import com.logiflow.tms.maintenance.domain.port.out.SinistreRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scores de santé et façade publique {@link MaintenanceApi} du module (lectures pour les autres
 * modules : copilote, agent de maintenance prédictive, planification des voyages).
 */
@Service
@RequiredArgsConstructor
public class MaintenanceService implements MaintenanceApi {

  /** Durée retenue pour un OT planifié sans fin prévue. */
  private static final int DUREE_PAR_DEFAUT_HEURES = 8;

  private final ScoreSanteRepository scoreSanteRepository;
  private final OrdreTravailRepository ordreTravailRepository;
  private final PlanEntretienRepository planEntretienRepository;
  private final SinistreRepository sinistreRepository;
  private final PlanEntretienService planEntretienService;
  private final EnginsFlotte enginsFlotte;
  private final VehiculeApi vehiculeApi;
  private final RemorqueApi remorqueApi;

  // ─── Scores de santé ───────────────────────────────────────────────────

  @Transactional
  public UUID calculerScoreSante(CalculerScoreSanteCommand command) {
    if (vehiculeApi.consulter(command.vehiculeId()).isEmpty()
        && remorqueApi.consulter(command.vehiculeId()).isEmpty()) {
      throw new NotFoundException("Aucun engin trouvé pour l'identifiant " + command.vehiculeId());
    }
    ScoreSante score =
        ScoreSante.calculer(
            UUID.randomUUID(),
            command.vehiculeId(),
            LocalDate.now(PlanEntretienService.FUSEAU),
            command.score(),
            command.kmAvantEcheance(),
            command.dateEcheanceProjetee(),
            command.recommandation());
    return scoreSanteRepository.sauvegarder(score).id();
  }

  @Transactional(readOnly = true)
  public ScoreSante consulterScoreSante(UUID id) {
    return scoreSanteRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun score de santé trouvé pour l'identifiant " + id));
  }

  @Transactional(readOnly = true)
  public Optional<ScoreSante> consulterDernierScoreSante(UUID enginId) {
    return scoreSanteRepository.dernierParVehiculeId(enginId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ScoreSanteSummary> dernierScoreSante(UUID enginId) {
    return scoreSanteRepository
        .dernierParVehiculeId(enginId)
        .map(
            s ->
                new ScoreSanteSummary(
                    s.vehiculeId(),
                    s.calculeLe(),
                    s.score(),
                    s.statut().name(),
                    s.necessiteIntervention()));
  }

  @Override
  @Transactional
  public ScoreSanteSummary enregistrerScoreSante(
      UUID enginId,
      double score,
      int kmAvantEcheance,
      LocalDate dateEcheanceProjetee,
      String recommandation) {
    calculerScoreSante(
        new CalculerScoreSanteCommand(
            enginId, score, kmAvantEcheance, dateEcheanceProjetee, recommandation));
    return dernierScoreSante(enginId).orElseThrow();
  }

  // ─── Façade de lecture ─────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public Page<OrdreTravailSummary> ordresTravail(UUID enginId, PageRequest pageRequest) {
    return ordreTravailRepository
        .rechercher(
            new OrdreTravailRepository.Filtre(
                null, enginId, null, null, null, null, null, null, null),
            pageRequest)
        .map(MaintenanceService::versResume);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PlanEntretienSummary> plansEntretien(UUID enginId, PageRequest pageRequest) {
    Map<UUID, EcheancePlan> echeances =
        planEntretienService.echeances(null).stream()
            .collect(java.util.stream.Collectors.toMap(e -> e.plan().id(), e -> e));
    return planEntretienRepository
        .rechercher(null, enginId, null, pageRequest)
        .map(p -> versResume(p, echeances.get(p.id())));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlanEntretienSummary> echeances(Integer horizonJours) {
    return planEntretienService.echeances(horizonJours).stream()
        .map(e -> versResume(e.plan(), e))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<SinistreSummary> sinistres(UUID enginId, LocalDate debut, LocalDate fin) {
    return sinistreRepository
        .lister(
            new SinistreRepository.Filtre(
                enginId,
                null,
                null,
                null,
                null,
                debut == null ? null : debut.atStartOfDay(),
                fin == null ? null : fin.plusDays(1).atStartOfDay()))
        .stream()
        .map(this::versResume)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<IndisponibiliteSummary> indisponibilites(Instant debut, Instant fin) {
    LocalDateTime de = LocalDateTime.ofInstant(debut, PlanEntretienService.FUSEAU);
    LocalDateTime a = LocalDateTime.ofInstant(fin, PlanEntretienService.FUSEAU);
    return ordreTravailRepository.lister(OrdreTravailRepository.Filtre.aucun()).stream()
        .filter(OrdreTravail::mobiliseEngin)
        .filter(o -> chevauche(o, de, a))
        .map(
            o ->
                new IndisponibiliteSummary(
                    o.engin().type().name(),
                    o.engin().id(),
                    o.reference().valeur(),
                    o.details().titre(),
                    o.details().debutPlanifie().atZone(PlanEntretienService.FUSEAU).toInstant(),
                    finPrevue(o).atZone(PlanEntretienService.FUSEAU).toInstant()))
        .toList();
  }

  /** Un OT en cours retient l'engin jusqu'à sa clôture ; un OT planifié sur sa période prévue. */
  private static boolean chevauche(OrdreTravail o, LocalDateTime debut, LocalDateTime fin) {
    LocalDateTime deb =
        o.realisation().debutReel() != null
            ? o.realisation().debutReel()
            : o.details().debutPlanifie();
    LocalDateTime finOt = o.statut() == StatutOT.PLANIFIE ? finPrevue(o) : LocalDateTime.MAX;
    return deb.isBefore(fin) && finOt.isAfter(debut);
  }

  private static LocalDateTime finPrevue(OrdreTravail o) {
    return o.details().finPlanifiee() != null
        ? o.details().finPlanifiee()
        : o.details().debutPlanifie().plusHours(DUREE_PAR_DEFAUT_HEURES);
  }

  static OrdreTravailSummary versResume(OrdreTravail o) {
    return new OrdreTravailSummary(
        o.id(),
        o.reference().valeur(),
        o.engin().type().name(),
        o.engin().id(),
        o.details().type().name(),
        o.details().nature().name(),
        o.statut().name(),
        o.details().titre(),
        o.details().debutPlanifie(),
        o.details().finPlanifiee(),
        o.realisation().finReelle(),
        o.realisation().kilometrage(),
        o.details().immobilisation(),
        o.totalTtc().montant());
  }

  static PlanEntretienSummary versResume(PlanEntretien p, EcheancePlan echeance) {
    var params = p.parametres();
    var derniere = p.derniereRealisation();
    var e = echeance == null ? null : echeance.echeance();
    return new PlanEntretienSummary(
        p.id(),
        p.engin().type().name(),
        p.engin().id(),
        params.libelle(),
        params.type().name(),
        params.periodiciteKm(),
        params.periodiciteMois(),
        params.periodiciteHeures(),
        params.seuilAlerteKm(),
        params.dureeEstimeeMin(),
        derniere == null ? null : derniere.date(),
        derniere == null ? null : derniere.kilometrage(),
        e == null ? null : e.kmRestant(),
        e == null ? null : e.dateEcheance(),
        e == null ? null : e.etat().name());
  }

  private SinistreSummary versResume(Sinistre s) {
    var c = s.circonstances();
    var couts = SinistreService.couts(s, ordreTravailRepository.parSinistreId(s.id()));
    return new SinistreSummary(
        s.id(),
        s.reference().valeur(),
        c.vehiculeId(),
        c.remorqueId(),
        c.chauffeurId(),
        c.dateSurvenance(),
        c.type().name(),
        c.gravite().name(),
        c.responsabilite().name(),
        s.statut().name(),
        c.enginImmobilise(),
        couts.coutNet().montant());
  }
}
