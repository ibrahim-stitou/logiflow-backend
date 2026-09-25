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
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.RemorqueEtatSummary;
import com.logiflow.tms.fleet.api.dto.VehiculeEtatSummary;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import com.logiflow.tms.maintenance.api.dto.OrdreTravailSummary;
import com.logiflow.tms.maintenance.api.dto.PlanEntretienSummary;
import com.logiflow.tms.maintenance.api.dto.SinistreSummary;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'utilisation de l'agent de maintenance prédictive.
 *
 * <p>Assemble l'état de la flotte (véhicules et remorques) via les API publiques des modules :
 * compteurs, plans d'entretien avec leur échéance, ordres de travail, sinistres des 12 derniers
 * mois, documents, voyages réalisés et planifiés, carburant. Interroge l'agent du service IA puis,
 * si demandé, <b>enregistre le score de santé</b> de chaque engin dans le module {@code
 * maintenance} : les scores cessent d'être saisis à la main.
 */
@Service
@RequiredArgsConstructor
public class MaintenancePredictiveService {

  private static final ZoneId FUSEAU_EXPLOITATION = ZoneId.of("Europe/Paris");
  private static final int JOURS_ACTIVITE = 90;
  private static final int MOIS_SINISTRES = 12;
  private static final int PAGE = 100;
  private static final int KM_AVANT_ECHEANCE_INCONNU = 999_999;
  private static final int LONGUEUR_MAX_RECOMMANDATION = 1000;
  private static final Set<String> VOYAGES_REALISES = Set.of("EN_COURS", "TERMINE", "CLOTURE");
  private static final Set<String> VOYAGES_A_VENIR = Set.of("BROUILLON", "PLANIFIE", "AFFECTE");

  private final MaintenancePredictiveClientPort clientPort;
  private final InteractionIaRepository interactionRepository;
  private final VehiculeApi vehiculeApi;
  private final RemorqueApi remorqueApi;
  private final MaintenanceApi maintenanceApi;
  private final VoyageApi voyageApi;
  private final DocumentApi documentApi;
  private final CarburantApi carburantApi;

  /**
   * Demande d'analyse : un engin (véhicule ou remorque, {@code vehiculeId}) ou toute la flotte
   * ({@code vehiculeId} null).
   */
  public record AnalyserMaintenanceCommand(
      UUID vehiculeId, int horizonJours, boolean enregistrerScores) {}

  /** Engin de la flotte ramené aux compteurs utiles à l'analyse. */
  private record Engin(
      UUID id,
      String typeEngin,
      String immatriculation,
      String type,
      String statut,
      int kilometrage,
      int heures,
      Integer annee) {}

  @Transactional
  public ResultatMaintenance analyser(AnalyserMaintenanceCommand command) {
    Instant maintenant = Instant.now();
    LocalDate aujourdHui = LocalDate.ofInstant(maintenant, FUSEAU_EXPLOITATION);
    UUID cible = command.vehiculeId();
    List<Engin> engins =
        engins().stream().filter(e -> cible == null || e.id().equals(cible)).toList();
    if (cible != null && engins.isEmpty()) {
      throw new NotFoundException("Aucun engin en service pour l'identifiant " + cible);
    }

    Map<UUID, List<PlanEntretienSummary>> plans =
        toutes(maintenanceApi::plansEntretien, cible).stream()
            .collect(Collectors.groupingBy(PlanEntretienSummary::enginId));
    Map<UUID, List<OrdreTravailSummary>> ordres =
        toutes(maintenanceApi::ordresTravail, cible).stream()
            .collect(Collectors.groupingBy(OrdreTravailSummary::enginId));
    Map<UUID, List<SinistreSummary>> sinistres =
        parEngin(
            maintenanceApi.sinistres(cible, aujourdHui.minusMonths(MOIS_SINISTRES), aujourdHui),
            s -> Stream.of(s.vehiculeId(), s.remorqueId()));
    Map<UUID, List<ActiviteVoyageSummary>> voyages =
        parEngin(
            voyageApi.activiteVehicules(
                maintenant.minus(Duration.ofDays(JOURS_ACTIVITE)),
                maintenant.plus(Duration.ofDays(command.horizonJours()))),
            a -> Stream.of(a.vehiculeId(), a.remorqueId()));

    List<ContexteMaintenance.Vehicule> contexteEngins = new ArrayList<>();
    for (Engin e : engins) {
      List<ActiviteVoyageSummary> activite = voyages.getOrDefault(e.id(), List.of());
      double kmRealises =
          activite.stream()
              .filter(a -> VOYAGES_REALISES.contains(a.statut()))
              .filter(a -> !a.arriveePrevue().isAfter(maintenant))
              .mapToDouble(ActiviteVoyageSummary::distanceKm)
              .sum();
      double litres =
          "VEHICULE".equals(e.typeEngin())
              ? carburantApi
                  .consommation(e.id(), aujourdHui.minusDays(JOURS_ACTIVITE), aujourdHui)
                  .litresTotal()
              : 0;
      contexteEngins.add(
          new ContexteMaintenance.Vehicule(
              e.id().toString(),
              e.typeEngin(),
              e.immatriculation(),
              e.type(),
              e.statut(),
              e.kilometrage(),
              e.heures(),
              e.annee(),
              kmRealises,
              litres,
              plans.getOrDefault(e.id(), List.of()).stream()
                  .map(MaintenancePredictiveService::versPlan)
                  .toList(),
              ordres.getOrDefault(e.id(), List.of()).stream()
                  .map(MaintenancePredictiveService::versOrdre)
                  .toList(),
              documentApi.lister(e.typeEngin(), e.id()).stream()
                  .map(d -> new ContexteMaintenance.Document(d.typeDocument(), d.dateExpiration()))
                  .toList(),
              sinistres.getOrDefault(e.id(), List.of()).stream()
                  .map(MaintenancePredictiveService::versSinistre)
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
        new ContexteMaintenance(maintenant, command.horizonJours(), contexteEngins, null);
    String resume =
        "%d engin(s), horizon %d jours".formatted(engins.size(), command.horizonJours());
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

  private List<Engin> engins() {
    List<Engin> engins = new ArrayList<>();
    for (VehiculeEtatSummary v : vehiculeApi.listerPourMaintenance()) {
      engins.add(
          new Engin(
              v.id(),
              "VEHICULE",
              v.immatriculation(),
              v.type(),
              v.statut(),
              v.kilometrage(),
              v.heuresMoteur(),
              v.anneeMiseEnCirculation()));
    }
    for (RemorqueEtatSummary r : remorqueApi.listerPourMaintenance()) {
      engins.add(
          new Engin(
              r.id(),
              "REMORQUE",
              r.immatriculation(),
              r.carrosserie(),
              r.statut(),
              r.kilometrage(),
              r.heuresGroupeFroid(),
              r.anneeFabrication()));
    }
    return engins;
  }

  private static ContexteMaintenance.Plan versPlan(PlanEntretienSummary p) {
    return new ContexteMaintenance.Plan(
        p.id().toString(),
        p.libelle(),
        p.type(),
        p.periodiciteKm(),
        p.periodiciteMois(),
        p.periodiciteHeures(),
        p.seuilAlerteKm(),
        p.dureeEstimeeMin(),
        p.derniereDate(),
        p.derniereKm(),
        p.kmRestant(),
        p.dateEcheance(),
        p.etat());
  }

  private static ContexteMaintenance.Ordre versOrdre(OrdreTravailSummary o) {
    return new ContexteMaintenance.Ordre(
        o.reference(),
        o.type(),
        o.nature(),
        o.statut(),
        o.origine(),
        o.planId() == null ? null : o.planId().toString(),
        (o.finReelle() != null ? o.finReelle() : o.debutPlanifie())
            .atZone(FUSEAU_EXPLOITATION)
            .toInstant(),
        o.immobilisation(),
        o.totalTtc());
  }

  private static ContexteMaintenance.Sinistre versSinistre(SinistreSummary s) {
    return new ContexteMaintenance.Sinistre(
        s.reference(),
        s.dateSurvenance().toLocalDate(),
        s.type(),
        s.gravite(),
        s.responsabilite(),
        s.statut(),
        s.enginImmobilise(),
        s.coutNet());
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

  /** Regroupe des éléments par engin, un élément pouvant concerner plusieurs engins. */
  private static <T> Map<UUID, List<T>> parEngin(
      List<T> elements, Function<T, Stream<UUID>> engins) {
    Map<UUID, List<T>> index = new HashMap<>();
    for (T element : elements) {
      engins
          .apply(element)
          .filter(Objects::nonNull)
          .distinct()
          .forEach(id -> index.computeIfAbsent(id, k -> new ArrayList<>()).add(element));
    }
    return index;
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
