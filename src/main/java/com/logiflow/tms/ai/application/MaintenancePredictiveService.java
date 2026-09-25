package com.logiflow.tms.ai.application;

import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.TypeInteractionIa;
import com.logiflow.tms.ai.domain.model.maintenance.ContexteMaintenance;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance.AnalyseVehicule;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.ai.domain.port.out.MaintenancePredictiveClientPort;
import com.logiflow.tms.carburant.api.CarburantApi;
import com.logiflow.tms.document.api.DocumentApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.VehiculeEtatSummary;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import com.logiflow.tms.maintenance.api.dto.OrdreTravailSummary;
import com.logiflow.tms.maintenance.api.dto.PlanEntretienSummary;
import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.planning.api.dto.ActiviteVoyageSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'utilisation de l'agent de maintenance prédictive.
 *
 * <p>Assemble l'état de la flotte via les API publiques des modules (compteurs, plans d'entretien,
 * ordres de travail, documents, voyages réalisés et planifiés, carburant), interroge l'agent du
 * service IA puis, si demandé, <b>enregistre le score de santé</b> de chaque véhicule dans le
 * module {@code maintenance} : les scores cessent d'être saisis à la main.
 */
@Service
@RequiredArgsConstructor
public class MaintenancePredictiveService {

  private static final ZoneId FUSEAU_EXPLOITATION = ZoneId.of("Europe/Paris");
  private static final int JOURS_ACTIVITE = 90;
  private static final int PAGE = 100;
  private static final int KM_AVANT_ECHEANCE_INCONNU = 999_999;
  private static final int LONGUEUR_MAX_RECOMMANDATION = 1000;
  private static final Set<String> VOYAGES_REALISES = Set.of("EN_COURS", "TERMINE", "CLOTURE");
  private static final Set<String> VOYAGES_A_VENIR = Set.of("BROUILLON", "PLANIFIE", "AFFECTE");

  private final MaintenancePredictiveClientPort clientPort;
  private final InteractionIaRepository interactionRepository;
  private final VehiculeApi vehiculeApi;
  private final MaintenanceApi maintenanceApi;
  private final VoyageApi voyageApi;
  private final DocumentApi documentApi;
  private final CarburantApi carburantApi;

  /** Demande d'analyse : un véhicule ou toute la flotte ({@code vehiculeId} null). */
  public record AnalyserMaintenanceCommand(
      UUID vehiculeId, int horizonJours, boolean enregistrerScores) {}

  @Transactional
  public ResultatMaintenance analyser(AnalyserMaintenanceCommand command) {
    Instant maintenant = Instant.now();
    LocalDate aujourdHui = LocalDate.ofInstant(maintenant, FUSEAU_EXPLOITATION);
    List<VehiculeEtatSummary> vehicules =
        vehiculeApi.listerPourMaintenance().stream()
            .filter(v -> command.vehiculeId() == null || v.id().equals(command.vehiculeId()))
            .toList();
    if (command.vehiculeId() != null && vehicules.isEmpty()) {
      throw new NotFoundException(
          "Aucun véhicule en service pour l'identifiant " + command.vehiculeId());
    }

    Map<UUID, List<PlanEntretienSummary>> plans =
        parVehicule(
            toutes(maintenanceApi::plansEntretien, command.vehiculeId()),
            PlanEntretienSummary::vehiculeId);
    Map<UUID, List<OrdreTravailSummary>> ordres =
        parVehicule(
            toutes(maintenanceApi::ordresTravail, command.vehiculeId()),
            OrdreTravailSummary::vehiculeId);
    Map<UUID, List<ActiviteVoyageSummary>> voyages =
        voyageApi
            .activiteVehicules(
                maintenant.minus(Duration.ofDays(JOURS_ACTIVITE)),
                maintenant.plus(Duration.ofDays(command.horizonJours())))
            .stream()
            .collect(Collectors.groupingBy(ActiviteVoyageSummary::vehiculeId));

    List<ContexteMaintenance.Vehicule> contexteVehicules = new ArrayList<>();
    for (VehiculeEtatSummary v : vehicules) {
      List<ActiviteVoyageSummary> activite = voyages.getOrDefault(v.id(), List.of());
      double kmRealises =
          activite.stream()
              .filter(a -> VOYAGES_REALISES.contains(a.statut()))
              .filter(a -> !a.arriveePrevue().isAfter(maintenant))
              .mapToDouble(ActiviteVoyageSummary::distanceKm)
              .sum();
      double litres =
          carburantApi
              .consommation(v.id(), aujourdHui.minusDays(JOURS_ACTIVITE), aujourdHui)
              .litresTotal();
      contexteVehicules.add(
          new ContexteMaintenance.Vehicule(
              v.id().toString(),
              v.immatriculation(),
              v.type(),
              v.statut(),
              v.kilometrage(),
              v.heuresMoteur(),
              v.anneeMiseEnCirculation(),
              kmRealises,
              litres,
              plans.getOrDefault(v.id(), List.of()).stream()
                  .map(
                      p ->
                          new ContexteMaintenance.Plan(
                              p.id().toString(),
                              p.libelle(),
                              p.periodiciteKm(),
                              p.periodiciteMois(),
                              p.seuilAlerteKm(),
                              p.dureeEstimeeMin()))
                  .toList(),
              ordres.getOrDefault(v.id(), List.of()).stream()
                  .map(
                      o ->
                          new ContexteMaintenance.Ordre(
                              o.type(),
                              o.statut(),
                              o.datePlanifiee() == null
                                  ? null
                                  : o.datePlanifiee().atZone(FUSEAU_EXPLOITATION).toInstant()))
                  .toList(),
              documentApi.lister("VEHICULE", v.id()).stream()
                  .map(d -> new ContexteMaintenance.Document(d.typeDocument(), d.dateExpiration()))
                  .toList(),
              activite.stream()
                  .filter(a -> VOYAGES_A_VENIR.contains(a.statut()))
                  .filter(a -> a.departPrevu().isAfter(maintenant))
                  .map(
                      a ->
                          new ContexteMaintenance.VoyagePlanifie(
                              a.reference(), a.departPrevu(), a.arriveePrevue(), a.distanceKm()))
                  .toList()));
    }

    ContexteMaintenance contexte =
        new ContexteMaintenance(maintenant, command.horizonJours(), contexteVehicules, null);
    String resume =
        "%d véhicule(s), horizon %d jours".formatted(vehicules.size(), command.horizonJours());
    Instant debut = Instant.now();
    ResultatMaintenance resultat;
    try {
      resultat = clientPort.recommander(contexte);
      journaliser(true, debut, resume, null);
    } catch (ServiceIndisponibleException e) {
      journaliser(false, debut, resume, e.getMessage());
      throw e;
    }

    if (command.enregistrerScores()) {
      resultat.vehicules().forEach(a -> enregistrer(a, aujourdHui, command.horizonJours()));
    }
    return resultat;
  }

  private void enregistrer(AnalyseVehicule analyse, LocalDate aujourdHui, int horizon) {
    String recommandation = analyse.explication();
    if (recommandation != null && recommandation.length() > LONGUEUR_MAX_RECOMMANDATION) {
      recommandation = recommandation.substring(0, LONGUEUR_MAX_RECOMMANDATION - 1) + "…";
    }
    maintenanceApi.enregistrerScoreSante(
        UUID.fromString(analyse.vehiculeId()),
        analyse.score(),
        analyse.kmAvantEcheance() == null ? KM_AVANT_ECHEANCE_INCONNU : analyse.kmAvantEcheance(),
        analyse.dateEcheance() == null ? aujourdHui.plusDays(horizon) : analyse.dateEcheance(),
        recommandation);
  }

  /** Toutes les pages d'une recherche paginée du module maintenance. */
  private static <T> List<T> toutes(
      BiFunction<UUID, PageRequest, Page<T>> recherche, UUID vehiculeId) {
    List<T> resultat = new ArrayList<>();
    PageRequest page = PageRequest.premiere(PAGE);
    Page<T> courante;
    do {
      courante = recherche.apply(vehiculeId, page);
      resultat.addAll(courante.contenu());
      page = new PageRequest(page.numero() + 1, PAGE);
    } while (page.numero() < courante.totalPages());
    return resultat;
  }

  private static <T> Map<UUID, List<T>> parVehicule(
      List<T> elements, java.util.function.Function<T, UUID> vehicule) {
    return elements.stream().collect(Collectors.groupingBy(vehicule));
  }

  private void journaliser(boolean succes, Instant debut, String resume, String erreur) {
    long dureeMs = Duration.between(debut, Instant.now()).toMillis();
    interactionRepository.sauvegarder(
        InteractionIa.enregistrer(
            UUID.randomUUID(),
            TypeInteractionIa.MAINTENANCE,
            null,
            succes,
            dureeMs,
            resume,
            erreur));
  }
}
