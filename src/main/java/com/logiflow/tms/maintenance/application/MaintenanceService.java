package com.logiflow.tms.maintenance.application;

import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import com.logiflow.tms.maintenance.api.dto.ScoreSanteSummary;
import com.logiflow.tms.maintenance.application.command.CalculerScoreSanteCommand;
import com.logiflow.tms.maintenance.application.command.CreerOrdreTravailCommand;
import com.logiflow.tms.maintenance.application.command.CreerPlanEntretienCommand;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.ScoreSante;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.domain.port.out.PlanEntretienRepository;
import com.logiflow.tms.maintenance.domain.port.out.ScoreSanteRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'utilisation applicatifs du module {@code maintenance} : plans, ordres de travail, scores de
 * santé.
 */
@Service
@RequiredArgsConstructor
public class MaintenanceService implements MaintenanceApi {

  private final PlanEntretienRepository planEntretienRepository;
  private final OrdreTravailRepository ordreTravailRepository;
  private final ScoreSanteRepository scoreSanteRepository;
  private final VehiculeApi vehiculeApi;

  @Transactional
  public UUID creerPlanEntretien(CreerPlanEntretienCommand command) {
    verifierVehiculeExiste(command.vehiculeId());
    PlanEntretien plan =
        PlanEntretien.creer(
            UUID.randomUUID(),
            command.vehiculeId(),
            command.libelle(),
            command.periodiciteKm(),
            command.periodiciteMois(),
            command.seuilAlerteKm(),
            command.dureeEstimeeMin());
    return planEntretienRepository.sauvegarder(plan).id();
  }

  @Transactional
  public UUID creerOrdreTravail(CreerOrdreTravailCommand command) {
    verifierVehiculeExiste(command.vehiculeId());
    OrdreTravail ordreTravail =
        OrdreTravail.creer(
            UUID.randomUUID(),
            command.vehiculeId(),
            command.type(),
            command.datePlanifiee(),
            command.coutEstime());
    return ordreTravailRepository.sauvegarder(ordreTravail).id();
  }

  @Transactional
  public void changerStatutOrdreTravail(UUID id, StatutOT statut) {
    OrdreTravail ordreTravail = trouverOrdreTravailOuEchouer(id);
    ordreTravail.changerStatut(statut);
    ordreTravailRepository.sauvegarder(ordreTravail);
  }

  @Transactional
  public UUID calculerScoreSante(CalculerScoreSanteCommand command) {
    verifierVehiculeExiste(command.vehiculeId());
    ScoreSante score =
        ScoreSante.calculer(
            UUID.randomUUID(),
            command.vehiculeId(),
            LocalDate.now(),
            command.score(),
            command.kmAvantEcheance(),
            command.dateEcheanceProjetee(),
            command.recommandation());
    return scoreSanteRepository.sauvegarder(score).id();
  }

  @Transactional(readOnly = true)
  public OrdreTravail consulterOrdreTravail(UUID id) {
    return trouverOrdreTravailOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<OrdreTravail> listerOrdresTravail(UUID vehiculeId, PageRequest pageRequest) {
    return ordreTravailRepository.rechercher(vehiculeId, pageRequest);
  }

  @Transactional(readOnly = true)
  public OrdreTravailRepository.OrdreTravailStats statsOrdresTravail(
      UUID vehiculeId, StatutOT statut) {
    return ordreTravailRepository.stats(vehiculeId, statut);
  }

  @Transactional(readOnly = true)
  public Page<PlanEntretien> listerPlansEntretien(UUID vehiculeId, PageRequest pageRequest) {
    return planEntretienRepository.rechercher(vehiculeId, pageRequest);
  }

  @Transactional(readOnly = true)
  public PlanEntretien consulterPlanEntretien(UUID id) {
    return planEntretienRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun plan d'entretien trouvé pour l'identifiant " + id));
  }

  @Transactional(readOnly = true)
  public ScoreSante consulterScoreSante(UUID id) {
    return scoreSanteRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun score de santé trouvé pour l'identifiant " + id));
  }

  @Transactional(readOnly = true)
  public Optional<ScoreSante> consulterDernierScoreSante(UUID vehiculeId) {
    return scoreSanteRepository.dernierParVehiculeId(vehiculeId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ScoreSanteSummary> dernierScoreSante(UUID vehiculeId) {
    return scoreSanteRepository
        .dernierParVehiculeId(vehiculeId)
        .map(
            s ->
                new ScoreSanteSummary(
                    s.vehiculeId(),
                    s.calculeLe(),
                    s.score(),
                    s.statut().name(),
                    s.necessiteIntervention()));
  }

  private void verifierVehiculeExiste(UUID vehiculeId) {
    if (vehiculeApi.consulter(vehiculeId).isEmpty()) {
      throw new NotFoundException("Aucun véhicule trouvé pour l'identifiant " + vehiculeId);
    }
  }

  private OrdreTravail trouverOrdreTravailOuEchouer(UUID id) {
    return ordreTravailRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun ordre de travail trouvé pour l'identifiant " + id));
  }
}
