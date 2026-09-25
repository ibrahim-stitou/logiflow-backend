package com.logiflow.tms.ai.application;

import com.logiflow.tms.ai.application.command.ProposerVoyagesCommand;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.TypeInteractionIa;
import com.logiflow.tms.ai.domain.model.planification.ContextePlanification;
import com.logiflow.tms.ai.domain.model.planification.ContextePlanification.FenetreSite;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification.Option;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.ai.domain.port.out.PlanificationClientPort;
import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierPlanificationSummary;
import com.logiflow.tms.dossier.api.dto.SegmentPlanificationSummary;
import com.logiflow.tms.driver.api.ChauffeurApi;
import com.logiflow.tms.driver.api.dto.ChauffeurPlanificationSummary;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.RemorquePlanificationSummary;
import com.logiflow.tms.fleet.api.dto.VehiculePlanificationSummary;
import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.planning.api.dto.ConformiteVoyageSummary;
import com.logiflow.tms.planning.api.dto.ProjetVoyageDto;
import com.logiflow.tms.planning.api.dto.RessourcesOccupeesSummary;
import com.logiflow.tms.referential.api.SiteApi;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'utilisation de l'agent de planification de voyage (remplace l'ancien agent de groupage).
 *
 * <p>Assemble le contexte via les API publiques des modules (dossiers {@code CREE} de la période,
 * ressources libres et conformes), interroge l'agent du service IA, puis <b>revalide chaque
 * option</b> avec les règles de création de voyage du module {@code planning} : une proposition
 * n'est jamais présentée comme conforme sur la seule parole de l'agent.
 */
@Service
@RequiredArgsConstructor
public class PlanificationVoyageService {

  private static final ZoneId FUSEAU_EXPLOITATION = ZoneId.of("Europe/Paris");
  private static final Duration PERIODE_MAX = Duration.ofDays(14);
  private static final Set<String> STATUTS_HORS_EXPLOITATION =
      Set.of("EN_MAINTENANCE", "IMMOBILISE", "HORS_SERVICE");

  private final PlanificationClientPort planificationClientPort;
  private final InteractionIaRepository interactionRepository;
  private final DossierApi dossierApi;
  private final SiteApi siteApi;
  private final VehiculeApi vehiculeApi;
  private final RemorqueApi remorqueApi;
  private final ChauffeurApi chauffeurApi;
  private final VoyageApi voyageApi;

  /** Option de l'agent accompagnée du verdict de conformité du module planning. */
  public record OptionValidee(Option option, ConformiteVoyageSummary conformite) {}

  /** Résultat présenté à l'exploitant, avec les libellés des ressources citées. */
  public record PropositionsVoyage(
      String portee,
      List<OptionValidee> options,
      String comparaison,
      List<String> dossiersNonPlanifiables,
      int nbDossiersCandidats,
      String sourceDistances,
      String sourceRedaction,
      Map<String, String> libellesDossiers,
      Map<String, String> libellesVehicules,
      Map<String, String> libellesRemorques,
      Map<String, String> libellesChauffeurs) {}

  @Transactional
  public PropositionsVoyage proposer(ProposerVoyagesCommand command) {
    if (!command.fin().isAfter(command.debut())) {
      throw new BusinessException("La fin de la période doit être postérieure à son début");
    }
    if (Duration.between(command.debut(), command.fin()).compareTo(PERIODE_MAX) > 0) {
      throw new BusinessException("La période de planification est limitée à 14 jours");
    }

    List<DossierPlanificationSummary> dossiers =
        dossierApi.candidatsPlanification(command.debut(), command.fin()).stream()
            .filter(d -> d.chargement() != null && d.dechargement() != null)
            .filter(d -> "INTERNATIONAL".equals(command.portee()) == d.international())
            .toList();
    if (dossiers.isEmpty()) {
      return new PropositionsVoyage(
          command.portee(),
          List.of(),
          "Aucun dossier à planifier (statut CREE, chargement dans la période) pour cette portée.",
          List.of(),
          0,
          null,
          null,
          Map.of(),
          Map.of(),
          Map.of(),
          Map.of());
    }

    LocalDate date = LocalDate.ofInstant(command.debut(), FUSEAU_EXPLOITATION);
    RessourcesOccupeesSummary occupees =
        voyageApi.ressourcesOccupees(command.debut(), command.fin());
    List<VehiculePlanificationSummary> vehicules =
        vehiculeApi.listerPourPlanification(date).stream()
            .filter(v -> !STATUTS_HORS_EXPLOITATION.contains(v.statut()) && v.documentsValides())
            .filter(v -> !occupees.vehicules().contains(v.id()))
            .toList();
    List<RemorquePlanificationSummary> remorques =
        remorqueApi.listerPourPlanification(date).stream()
            .filter(r -> !STATUTS_HORS_EXPLOITATION.contains(r.statut()) && r.documentsValides())
            .filter(r -> !occupees.remorques().contains(r.id()))
            .toList();
    List<ChauffeurPlanificationSummary> chauffeurs =
        chauffeurApi.listerPourPlanification(date).stream()
            .filter(c -> c.motifsNonAffectation().isEmpty())
            .filter(c -> !occupees.chauffeurs().contains(c.id()))
            .toList();

    ContextePlanification contexte = contexte(command, dossiers, vehicules, remorques, chauffeurs);
    String resume =
        "%s %s → %s : %d dossier(s), %d véhicule(s), %d remorque(s), %d chauffeur(s)"
            .formatted(
                command.typeVoyage(),
                command.debut(),
                command.fin(),
                dossiers.size(),
                vehicules.size(),
                remorques.size(),
                chauffeurs.size());
    Instant debut = Instant.now();
    ResultatPlanification resultat;
    try {
      resultat = planificationClientPort.proposer(contexte);
      journaliser(true, debut, resume + " — " + nbOptions(resultat), null);
    } catch (ServiceIndisponibleException e) {
      journaliser(false, debut, resume, e.getMessage());
      throw e;
    }

    List<OptionValidee> options =
        resultat.options().stream()
            .map(o -> new OptionValidee(o, voyageApi.evaluerConformite(projet(o, command))))
            .toList();

    return new PropositionsVoyage(
        command.portee(),
        options,
        resultat.comparaison(),
        resultat.dossiersNonPlanifiables(),
        dossiers.size(),
        resultat.sourceDistances(),
        resultat.sourceRedaction(),
        libelles(dossiers, d -> d.id().toString(), DossierPlanificationSummary::reference),
        libelles(vehicules, v -> v.id().toString(), VehiculePlanificationSummary::immatriculation),
        libelles(remorques, r -> r.id().toString(), RemorquePlanificationSummary::immatriculation),
        libelles(
            chauffeurs,
            c -> c.id().toString(),
            c -> c.prenom() + " " + c.nom() + " (" + c.matricule() + ")"));
  }

  private static String nbOptions(ResultatPlanification resultat) {
    return resultat.options().size() + " option(s), distances " + resultat.sourceDistances();
  }

  private ContextePlanification contexte(
      ProposerVoyagesCommand command,
      List<DossierPlanificationSummary> dossiers,
      List<VehiculePlanificationSummary> vehicules,
      List<RemorquePlanificationSummary> remorques,
      List<ChauffeurPlanificationSummary> chauffeurs) {
    Set<UUID> siteIds = new HashSet<>();
    dossiers.forEach(
        d -> {
          siteIds.add(d.chargement().siteId());
          siteIds.add(d.dechargement().siteId());
        });
    chauffeurs.stream()
        .map(ChauffeurPlanificationSummary::siteRattachementId)
        .filter(java.util.Objects::nonNull)
        .forEach(siteIds::add);

    return new ContextePlanification(
        command.debut(),
        command.fin(),
        command.typeVoyage(),
        command.nbOptions(),
        dossiers.stream()
            .map(
                d ->
                    new ContextePlanification.Dossier(
                        d.id().toString(),
                        d.reference(),
                        d.poidsBrutKg(),
                        d.volumeM3(),
                        d.nbPalettes(),
                        d.contientAdr(),
                        d.groupable(),
                        d.international(),
                        d.carrosserieRequise(),
                        d.temperatureRequise(),
                        fenetre(d.chargement()),
                        fenetre(d.dechargement())))
            .toList(),
        siteApi.consulterTous(siteIds).stream()
            .map(
                s ->
                    new ContextePlanification.Site(
                        s.id().toString(),
                        s.code() + " — " + s.libelle(),
                        s.latitude(),
                        s.longitude()))
            .toList(),
        vehicules.stream()
            .map(
                v ->
                    new ContextePlanification.Vehicule(
                        v.id().toString(),
                        v.immatriculation(),
                        v.type(),
                        v.chargeUtileKg(),
                        v.volumeUtileM3(),
                        v.nbPositionsPalettes(),
                        v.carrosserie(),
                        v.groupeFroid(),
                        v.temperatureMin(),
                        v.temperatureMax()))
            .toList(),
        remorques.stream()
            .map(
                r ->
                    new ContextePlanification.Remorque(
                        r.id().toString(),
                        r.immatriculation(),
                        r.carrosserie(),
                        r.chargeUtileKg(),
                        r.volumeUtileM3(),
                        r.nbPositionsPalettes(),
                        r.groupeFroid(),
                        r.temperatureMin(),
                        r.temperatureMax()))
            .toList(),
        chauffeurs.stream()
            .map(
                c ->
                    new ContextePlanification.Chauffeur(
                        c.id().toString(),
                        c.matricule(),
                        c.nom(),
                        c.prenom(),
                        c.soldeTempsConduiteMinutes(),
                        c.categoriesPermis(),
                        c.habilitationsValides(),
                        c.passeportValide(),
                        c.siteRattachementId() == null ? null : c.siteRattachementId().toString()))
            .toList(),
        null);
  }

  private static FenetreSite fenetre(SegmentPlanificationSummary segment) {
    return new FenetreSite(segment.siteId().toString(), segment.debut(), segment.fin());
  }

  static ProjetVoyageDto projet(Option option, ProposerVoyagesCommand command) {
    return new ProjetVoyageDto(
        option.typeVoyage(),
        command.portee(),
        option.departPrevu(),
        option.arriveePrevue(),
        uuid(option.vehiculeId()),
        uuid(option.remorqueId()),
        option.dossierIds().stream().map(UUID::fromString).toList(),
        option.chauffeurIds().stream().map(UUID::fromString).toList(),
        option.indicateurs().dureeConduiteMin(),
        option.arrets().stream().map(a -> UUID.fromString(a.siteId())).toList());
  }

  private static UUID uuid(String valeur) {
    return valeur == null || valeur.isBlank() ? null : UUID.fromString(valeur);
  }

  private static <T> Map<String, String> libelles(
      List<T> elements, Function<T, String> cle, Function<T, String> libelle) {
    return elements.stream()
        .collect(Collectors.toMap(cle, libelle, (a, b) -> a, LinkedHashMap::new));
  }

  private void journaliser(boolean succes, Instant debut, String resume, String erreur) {
    long dureeMs = Duration.between(debut, Instant.now()).toMillis();
    interactionRepository.sauvegarder(
        InteractionIa.enregistrer(
            UUID.randomUUID(),
            TypeInteractionIa.PLANIFICATION,
            null,
            succes,
            dureeMs,
            resume,
            erreur));
  }
}
