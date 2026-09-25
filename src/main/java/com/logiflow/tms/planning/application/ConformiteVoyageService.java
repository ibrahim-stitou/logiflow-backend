package com.logiflow.tms.planning.application;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierPlanificationSummary;
import com.logiflow.tms.dossier.api.dto.SegmentPlanificationSummary;
import com.logiflow.tms.driver.api.ChauffeurApi;
import com.logiflow.tms.driver.api.dto.ChauffeurSummary;
import com.logiflow.tms.driver.api.dto.ExigencesAffectationDto;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.RemorquePlanificationSummary;
import com.logiflow.tms.fleet.api.dto.VehiculePlanificationSummary;
import com.logiflow.tms.planning.application.DisponibiliteRessourcesService.RessourcesOccupees;
import com.logiflow.tms.planning.application.command.CreerVoyageCommand;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.DossierSurTroncons;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.UtilisationTroncon;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService.ArretsDossier;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService.PointsDossier;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService.SitePlanifie;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.referential.api.SiteApi;
import com.logiflow.tms.referential.api.dto.SiteSummary;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contrôle de conformité complet d'un projet de voyage, partagé par la création (bloquant), le
 * contrôle à blanc de l'écran de planification et la revalidation des propositions de l'agent.
 *
 * <p>Les anomalies bloquantes empêchent la création ; les avertissements (fenêtres horaires,
 * carrosserie non renseignée…) sont seulement signalés. Le rapport porte aussi l'itinéraire
 * construit (arrêts ordonnés et rattachement des dossiers) et l'occupation de la capacité.
 */
@Service
@RequiredArgsConstructor
public class ConformiteVoyageService {

  private static final ZoneId FUSEAU_EXPLOITATION = ZoneId.of("Europe/Paris");

  private final DossierApi dossierApi;
  private final VehiculeApi vehiculeApi;
  private final RemorqueApi remorqueApi;
  private final ChauffeurApi chauffeurApi;
  private final SiteApi siteApi;
  private final DisponibiliteRessourcesService disponibiliteRessourcesService;
  private final ItineraireDossiersDomainService itineraireDossiersDomainService;
  private final CapaciteTronconDomainService capaciteTronconDomainService;

  /** Écart constaté ; {@code bloquante} = interdit la création du voyage. */
  public record AnomalieConformite(String code, String message, boolean bloquante) {

    static AnomalieConformite bloquante(String code, String message) {
      return new AnomalieConformite(code, message, true);
    }

    static AnomalieConformite avertissement(String code, String message) {
      return new AnomalieConformite(code, message, false);
    }
  }

  /** Résultat du contrôle : anomalies, itinéraire construit et occupation de la capacité. */
  public record RapportConformite(
      List<AnomalieConformite> anomalies,
      List<ArretVoyage> arrets,
      Map<UUID, ArretsDossier> arretsParDossier,
      double tauxRemplissage,
      double chargeMaxKg,
      double volumeMaxM3,
      int palettes,
      Double capaciteKg,
      Double capaciteM3,
      Integer capacitePalettes) {

    public boolean conforme() {
      return anomalies.stream().noneMatch(AnomalieConformite::bloquante);
    }

    public List<String> messagesBloquants() {
      return anomalies.stream()
          .filter(AnomalieConformite::bloquante)
          .map(AnomalieConformite::message)
          .toList();
    }
  }

  /** Capacité du support de charge retenu (remorque, sinon porteur). */
  private record Support(
      String libelle,
      String carrosserie,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      double chargeKg,
      Double volumeM3,
      Integer palettes) {}

  @Transactional(readOnly = true)
  public RapportConformite evaluer(CreerVoyageCommand command, UUID voyageId, UUID voyageIgnore) {
    List<AnomalieConformite> anomalies = new ArrayList<>();

    boolean periodeValide = verifierPeriode(command, anomalies);
    LocalDate dateDepart =
        command.departPrevu() == null
            ? LocalDate.now(FUSEAU_EXPLOITATION)
            : LocalDate.ofInstant(command.departPrevu(), FUSEAU_EXPLOITATION);

    List<DossierPlanificationSummary> dossiers = chargerDossiers(command, anomalies);
    boolean contientAdr = dossiers.stream().anyMatch(DossierPlanificationSummary::contientAdr);
    boolean international =
        command.portee() == Portee.INTERNATIONAL
            || dossiers.stream().anyMatch(DossierPlanificationSummary::international);
    verifierGroupage(command, dossiers, anomalies);

    RessourcesOccupees occupees =
        periodeValide
            ? disponibiliteRessourcesService.ressourcesOccupees(
                command.departPrevu(), command.arriveePrevue(), voyageIgnore)
            : new RessourcesOccupees(Map.of(), Map.of(), Map.of());

    Optional<VehiculePlanificationSummary> vehicule =
        verifierVehicule(command, dateDepart, occupees, anomalies);
    Optional<RemorquePlanificationSummary> remorque =
        verifierRemorque(command, dateDepart, occupees, vehicule, anomalies);
    Support support = support(vehicule, remorque);
    if (support != null) {
      verifierCompatibiliteMarchandises(dossiers, support, anomalies);
    }

    verifierChauffeurs(
        command, dateDepart, contientAdr, international, vehicule, occupees, anomalies);

    if (periodeValide) {
      verifierFenetres(command, dossiers, anomalies);
    }

    ItineraireDossiersDomainService.Resultat itineraire =
        construireItineraire(voyageId, command, dossiers);
    itineraire.erreurs().forEach(e -> anomalies.add(AnomalieConformite.bloquante("ITINERAIRE", e)));

    return rapport(anomalies, dossiers, itineraire, support);
  }

  // ─── Période et dossiers ─────────────────────────────────────────────────

  private static boolean verifierPeriode(
      CreerVoyageCommand command, List<AnomalieConformite> anomalies) {
    if (command.departPrevu() == null || command.arriveePrevue() == null) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "PERIODE", "Les dates de départ et d'arrivée sont requises"));
      return false;
    }
    if (!command.arriveePrevue().isAfter(command.departPrevu())) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "PERIODE", "L'arrivée prévue doit être postérieure au départ"));
      return false;
    }
    return true;
  }

  private List<DossierPlanificationSummary> chargerDossiers(
      CreerVoyageCommand command, List<AnomalieConformite> anomalies) {
    if (command.dossierIds().isEmpty()) {
      anomalies.add(
          AnomalieConformite.bloquante("DOSSIER", "Au moins un dossier doit être sélectionné"));
      return List.of();
    }
    List<DossierPlanificationSummary> dossiers =
        dossierApi.consulterPourPlanification(command.dossierIds());
    Set<UUID> trouves =
        dossiers.stream().map(DossierPlanificationSummary::id).collect(Collectors.toSet());
    command.dossierIds().stream()
        .filter(id -> !trouves.contains(id))
        .forEach(
            id ->
                anomalies.add(
                    AnomalieConformite.bloquante(
                        "DOSSIER", "Aucun dossier de transport trouvé pour l'identifiant " + id)));
    for (DossierPlanificationSummary dossier : dossiers) {
      if (!"CREE".equals(dossier.statut())) {
        anomalies.add(
            AnomalieConformite.bloquante(
                "DOSSIER",
                "Seul un dossier au statut CREE peut être planifié sur un voyage ("
                    + dossier.reference()
                    + ")"));
      }
    }
    return dossiers;
  }

  private static void verifierGroupage(
      CreerVoyageCommand command,
      List<DossierPlanificationSummary> dossiers,
      List<AnomalieConformite> anomalies) {
    if (dossiers.size() < 2) {
      return;
    }
    dossiers.stream()
        .filter(d -> !d.groupable())
        .forEach(
            d ->
                anomalies.add(
                    AnomalieConformite.bloquante(
                        "GROUPAGE",
                        "Le dossier " + d.reference() + " n'est pas groupable avec d'autres")));
    if (command.typeVoyage() == TypeVoyage.SIMPLE) {
      anomalies.add(
          AnomalieConformite.avertissement(
              "GROUPAGE", "Voyage SIMPLE portant plusieurs dossiers : préférez GROUPAGE"));
    }
  }

  // ─── Véhicule et remorque ────────────────────────────────────────────────

  private Optional<VehiculePlanificationSummary> verifierVehicule(
      CreerVoyageCommand command,
      LocalDate date,
      RessourcesOccupees occupees,
      List<AnomalieConformite> anomalies) {
    if (command.vehiculeId() == null) {
      anomalies.add(AnomalieConformite.bloquante("VEHICULE", "Aucun véhicule sélectionné"));
      return Optional.empty();
    }
    Optional<VehiculePlanificationSummary> vehicule =
        vehiculeApi.consulterPourPlanification(command.vehiculeId(), date);
    if (vehicule.isEmpty()) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "VEHICULE", "Aucun véhicule trouvé pour l'identifiant " + command.vehiculeId()));
      return vehicule;
    }
    VehiculePlanificationSummary v = vehicule.get();
    if (estHorsExploitation(v.statut())) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "VEHICULE",
              "Le véhicule " + v.immatriculation() + " n'est pas disponible (" + v.statut() + ")"));
    }
    if (!v.documentsValides()) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "VEHICULE",
              "Les documents du véhicule " + v.immatriculation() + " ne sont pas tous valides"));
    }
    String voyageOccupant = occupees.vehicules().get(v.id());
    if (voyageOccupant != null) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "CHEVAUCHEMENT",
              "Le véhicule " + v.immatriculation() + " est déjà engagé sur " + voyageOccupant));
    }
    return vehicule;
  }

  private Optional<RemorquePlanificationSummary> verifierRemorque(
      CreerVoyageCommand command,
      LocalDate date,
      RessourcesOccupees occupees,
      Optional<VehiculePlanificationSummary> vehicule,
      List<AnomalieConformite> anomalies) {
    if (command.remorqueId() == null) {
      if (vehicule.map(v -> "TRACTEUR".equals(v.type())).orElse(false)) {
        anomalies.add(
            AnomalieConformite.bloquante(
                "REMORQUE", "Un tracteur doit être attelé à une remorque"));
      }
      return Optional.empty();
    }
    Optional<RemorquePlanificationSummary> remorque =
        remorqueApi.consulterPourPlanification(command.remorqueId(), date);
    if (remorque.isEmpty()) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "REMORQUE", "Aucune remorque trouvée pour l'identifiant " + command.remorqueId()));
      return remorque;
    }
    RemorquePlanificationSummary r = remorque.get();
    if (estHorsExploitation(r.statut())) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "REMORQUE",
              "La remorque " + r.immatriculation() + " n'est pas disponible (" + r.statut() + ")"));
    }
    if (!r.documentsValides()) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "REMORQUE",
              "Les documents de la remorque " + r.immatriculation() + " ne sont pas tous valides"));
    }
    String voyageOccupant = occupees.remorques().get(r.id());
    if (voyageOccupant != null) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "CHEVAUCHEMENT",
              "La remorque " + r.immatriculation() + " est déjà engagée sur " + voyageOccupant));
    }
    return remorque;
  }

  /** En maintenance, immobilisé ou hors service : indisponible quelle que soit la période. */
  private static boolean estHorsExploitation(String statut) {
    return "EN_MAINTENANCE".equals(statut)
        || "IMMOBILISE".equals(statut)
        || "HORS_SERVICE".equals(statut);
  }

  private static Support support(
      Optional<VehiculePlanificationSummary> vehicule,
      Optional<RemorquePlanificationSummary> remorque) {
    if (remorque.isPresent()) {
      RemorquePlanificationSummary r = remorque.get();
      return new Support(
          "la remorque " + r.immatriculation(),
          r.carrosserie(),
          r.groupeFroid(),
          r.temperatureMin(),
          r.temperatureMax(),
          r.chargeUtileKg(),
          r.volumeUtileM3(),
          r.nbPositionsPalettes());
    }
    return vehicule
        .filter(v -> !"TRACTEUR".equals(v.type()))
        .map(
            v ->
                new Support(
                    "le véhicule " + v.immatriculation(),
                    v.carrosserie(),
                    v.groupeFroid(),
                    v.temperatureMin(),
                    v.temperatureMax(),
                    v.chargeUtileKg(),
                    v.volumeUtileM3(),
                    v.nbPositionsPalettes()))
        .orElse(null);
  }

  private static void verifierCompatibiliteMarchandises(
      List<DossierPlanificationSummary> dossiers,
      Support support,
      List<AnomalieConformite> anomalies) {
    for (DossierPlanificationSummary dossier : dossiers) {
      String requise = dossier.carrosserieRequise();
      if (requise != null) {
        if (support.carrosserie() == null) {
          anomalies.add(
              AnomalieConformite.avertissement(
                  "CARROSSERIE",
                  "Carrosserie de "
                      + support.libelle()
                      + " non renseignée : "
                      + requise
                      + " attendue pour "
                      + dossier.reference()));
        } else if (!requise.equals(support.carrosserie())) {
          anomalies.add(
              AnomalieConformite.bloquante(
                  "CARROSSERIE",
                  "Le dossier "
                      + dossier.reference()
                      + " exige une carrosserie "
                      + requise
                      + " ("
                      + support.libelle()
                      + " : "
                      + support.carrosserie()
                      + ")"));
        }
      }
      Double temperature = dossier.temperatureRequise();
      if (temperature != null) {
        boolean horsPlage =
            (support.temperatureMin() != null && temperature < support.temperatureMin())
                || (support.temperatureMax() != null && temperature > support.temperatureMax());
        if (!support.groupeFroid() || horsPlage) {
          anomalies.add(
              AnomalieConformite.bloquante(
                  "TEMPERATURE",
                  "Le dossier "
                      + dossier.reference()
                      + " doit être transporté à "
                      + temperature
                      + " °C : "
                      + support.libelle()
                      + " ne le permet pas"));
        }
      }
    }
  }

  // ─── Chauffeurs ──────────────────────────────────────────────────────────

  private void verifierChauffeurs(
      CreerVoyageCommand command,
      LocalDate date,
      boolean contientAdr,
      boolean international,
      Optional<VehiculePlanificationSummary> vehicule,
      RessourcesOccupees occupees,
      List<AnomalieConformite> anomalies) {
    List<Affectation> affectations = command.affectations();
    long titulaires =
        affectations.stream().filter(a -> a.role() == RoleChauffeur.TITULAIRE).count();
    if (titulaires != 1) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "CHAUFFEUR", "Le voyage doit avoir exactement un chauffeur titulaire"));
    }
    Set<UUID> vus = new HashSet<>();
    String permisRequis = vehicule.map(v -> permisRequis(v.type())).orElse(null);
    int nbChauffeurs = Math.max(1, affectations.size());
    long conduiteParChauffeur =
        command.trajet() == null
            ? 0
            : (long) Math.ceil((double) command.trajet().dureeConduiteMin() / nbChauffeurs);

    for (Affectation affectation : affectations) {
      UUID chauffeurId = affectation.chauffeurId();
      if (!vus.add(chauffeurId)) {
        anomalies.add(
            AnomalieConformite.bloquante(
                "CHAUFFEUR", "Un même chauffeur est affecté plusieurs fois au voyage"));
        continue;
      }
      Optional<ChauffeurSummary> chauffeur = chauffeurApi.consulter(chauffeurId);
      if (chauffeur.isEmpty()) {
        anomalies.add(
            AnomalieConformite.bloquante(
                "CHAUFFEUR", "Aucun chauffeur trouvé pour l'identifiant " + chauffeurId));
        continue;
      }
      ChauffeurSummary c = chauffeur.get();
      chauffeurApi
          .motifsNonAffectation(
              chauffeurId,
              new ExigencesAffectationDto(date, contientAdr, international, permisRequis))
          .forEach(m -> anomalies.add(AnomalieConformite.bloquante("CHAUFFEUR", m)));
      if (!chauffeurApi.documentsValides(chauffeurId, date)) {
        anomalies.add(
            AnomalieConformite.bloquante(
                "CHAUFFEUR", "Chauffeur " + c.matricule() + " : documents non valides au " + date));
      }
      if (c.soldeTempsConduiteMinutes() < conduiteParChauffeur) {
        anomalies.add(
            AnomalieConformite.bloquante(
                "CHAUFFEUR",
                "Chauffeur "
                    + c.matricule()
                    + " : solde de conduite insuffisant ("
                    + c.soldeTempsConduiteMinutes()
                    + " min pour "
                    + conduiteParChauffeur
                    + " min requises)"));
      }
      String voyageOccupant = occupees.chauffeurs().get(chauffeurId);
      if (voyageOccupant != null) {
        anomalies.add(
            AnomalieConformite.bloquante(
                "CHEVAUCHEMENT",
                "Chauffeur " + c.matricule() + " : déjà affecté sur " + voyageOccupant));
      }
    }
  }

  /** Catégorie de permis exigée par le type de véhicule (null = non contrôlée). */
  static String permisRequis(String typeVehicule) {
    return switch (typeVehicule) {
      case "TRACTEUR" -> "CE";
      case "PORTEUR" -> "C";
      case "FOURGON" -> "B";
      default -> null;
    };
  }

  // ─── Fenêtres horaires ───────────────────────────────────────────────────

  private static void verifierFenetres(
      CreerVoyageCommand command,
      List<DossierPlanificationSummary> dossiers,
      List<AnomalieConformite> anomalies) {
    Instant depart = command.departPrevu();
    Instant arrivee = command.arriveePrevue();
    for (DossierPlanificationSummary dossier : dossiers) {
      verifierFenetre(dossier.chargement(), "chargement", dossier, depart, arrivee, anomalies);
      verifierFenetre(dossier.dechargement(), "déchargement", dossier, depart, arrivee, anomalies);
    }
  }

  private static void verifierFenetre(
      SegmentPlanificationSummary segment,
      String nature,
      DossierPlanificationSummary dossier,
      Instant depart,
      Instant arrivee,
      List<AnomalieConformite> anomalies) {
    if (segment == null) {
      return;
    }
    boolean chevauche = segment.debut().isBefore(arrivee) && depart.isBefore(segment.fin());
    if (!chevauche) {
      anomalies.add(
          AnomalieConformite.avertissement(
              "FENETRE",
              "La fenêtre de "
                  + nature
                  + " du dossier "
                  + dossier.reference()
                  + " ("
                  + segment.debut()
                  + " → "
                  + segment.fin()
                  + ") est hors de la période du voyage"));
    }
  }

  // ─── Itinéraire et capacité ──────────────────────────────────────────────

  private ItineraireDossiersDomainService.Resultat construireItineraire(
      UUID voyageId, CreerVoyageCommand command, List<DossierPlanificationSummary> dossiers) {
    List<PointsDossier> points = new ArrayList<>();
    Set<UUID> siteIds = new HashSet<>();
    for (DossierPlanificationSummary dossier : dossiers) {
      SegmentPlanificationSummary chargement = dossier.chargement();
      SegmentPlanificationSummary dechargement = dossier.dechargement();
      points.add(
          new PointsDossier(
              dossier.id(),
              dossier.reference(),
              chargement == null ? null : chargement.siteId(),
              chargement == null ? Instant.EPOCH : chargement.debut(),
              dechargement == null ? null : dechargement.siteId(),
              dechargement == null ? Instant.EPOCH : dechargement.debut()));
      if (chargement != null) {
        siteIds.add(chargement.siteId());
      }
      if (dechargement != null) {
        siteIds.add(dechargement.siteId());
      }
    }
    if (command.ordreSites() != null) {
      siteIds.addAll(command.ordreSites());
    }
    Map<UUID, SitePlanifie> sites =
        siteApi.consulterTous(siteIds).stream()
            .collect(
                Collectors.toMap(
                    SiteSummary::id,
                    s ->
                        new SitePlanifie(
                            s.id(),
                            s.code() + " — " + s.libelle(),
                            new GeoPoint(s.latitude(), s.longitude())),
                    (a, b) -> a,
                    LinkedHashMap::new));
    return itineraireDossiersDomainService.construire(
        voyageId, points, sites, command.ordreSites());
  }

  private RapportConformite rapport(
      List<AnomalieConformite> anomalies,
      List<DossierPlanificationSummary> dossiers,
      ItineraireDossiersDomainService.Resultat itineraire,
      Support support) {
    double poidsTotal =
        dossiers.stream().mapToDouble(DossierPlanificationSummary::poidsBrutKg).sum();
    double volumeTotal = dossiers.stream().mapToDouble(DossierPlanificationSummary::volumeM3).sum();
    int palettes = dossiers.stream().mapToInt(DossierPlanificationSummary::nbPalettes).sum();

    double chargeMaxKg = poidsTotal;
    double volumeMaxM3 = volumeTotal;
    if (itineraire.valide() && !itineraire.arrets().isEmpty()) {
      Map<UUID, DossierPlanificationSummary> parId =
          dossiers.stream()
              .collect(Collectors.toMap(DossierPlanificationSummary::id, Function.identity()));
      List<DossierSurTroncons> surTroncons =
          itineraire.arretsParDossier().entrySet().stream()
              .map(
                  e ->
                      new DossierSurTroncons(
                          parId.get(e.getKey()).poidsBrutKg(),
                          parId.get(e.getKey()).volumeM3(),
                          e.getValue().indiceChargement(),
                          e.getValue().indiceDechargement()))
              .toList();
      List<UtilisationTroncon> troncons =
          capaciteTronconDomainService.calculerUtilisation(itineraire.arrets(), surTroncons);
      chargeMaxKg =
          troncons.stream().mapToDouble(UtilisationTroncon::poidsUtiliseKg).max().orElse(0);
      volumeMaxM3 =
          troncons.stream().mapToDouble(UtilisationTroncon::volumeUtiliseM3).max().orElse(0);
    }

    double taux = 0d;
    if (support != null) {
      verifierCapacite(support, chargeMaxKg, volumeMaxM3, palettes, anomalies);
      double tauxPoids = support.chargeKg() > 0 ? chargeMaxKg / support.chargeKg() : 0d;
      double tauxVolume =
          support.volumeM3() != null && support.volumeM3() > 0
              ? volumeMaxM3 / support.volumeM3()
              : 0d;
      taux = Math.max(tauxPoids, tauxVolume);
    }

    return new RapportConformite(
        List.copyOf(anomalies),
        itineraire.arrets(),
        itineraire.arretsParDossier(),
        taux,
        chargeMaxKg,
        volumeMaxM3,
        palettes,
        support == null ? null : support.chargeKg(),
        support == null ? null : support.volumeM3(),
        support == null ? null : support.palettes());
  }

  private static void verifierCapacite(
      Support support,
      double chargeMaxKg,
      double volumeMaxM3,
      int palettes,
      List<AnomalieConformite> anomalies) {
    if (support.chargeKg() > 0 && chargeMaxKg > support.chargeKg()) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "CAPACITE",
              String.format(
                  "Charge maximale de %.0f kg supérieure à la charge utile de %s (%.0f kg)",
                  chargeMaxKg, support.libelle(), support.chargeKg())));
    }
    if (support.volumeM3() != null && volumeMaxM3 > support.volumeM3()) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "CAPACITE",
              String.format(
                  "Volume maximal de %.1f m³ supérieur au volume utile de %s (%.1f m³)",
                  volumeMaxM3, support.libelle(), support.volumeM3())));
    }
    if (support.palettes() != null && support.palettes() > 0 && palettes > support.palettes()) {
      anomalies.add(
          AnomalieConformite.bloquante(
              "CAPACITE",
              "Palettes : "
                  + palettes
                  + " pour "
                  + support.palettes()
                  + " positions sur "
                  + support.libelle()));
    }
    if (support.chargeKg() <= 0) {
      anomalies.add(
          AnomalieConformite.avertissement(
              "CAPACITE", "Charge utile de " + support.libelle() + " non renseignée"));
    }
  }
}
