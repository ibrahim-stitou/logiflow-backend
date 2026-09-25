package com.logiflow.tms.maintenance.application;

import com.logiflow.tms.maintenance.application.EnginsFlotte.EtatEngin;
import com.logiflow.tms.maintenance.domain.model.EtatEcheance;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.domain.port.out.PlanEntretienRepository;
import com.logiflow.tms.maintenance.domain.vo.Echeance;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Plans d'entretien et calcul des échéances de la flotte. */
@Service
@RequiredArgsConstructor
public class PlanEntretienService {

  static final ZoneId FUSEAU = ZoneId.of("Europe/Paris");

  private final PlanEntretienRepository repository;
  private final OrdreTravailRepository ordreTravailRepository;
  private final PrestataireService prestataireService;
  private final EnginsFlotte enginsFlotte;

  /** Plan et sa prochaine échéance, avec l'engin et l'usage retenu pour la projection. */
  public record EcheancePlan(
      PlanEntretien plan, EtatEngin engin, double kmParJour, Echeance echeance) {}

  @Transactional
  public PlanEntretien creer(
      EnginRef engin,
      PlanEntretien.Parametres parametres,
      PlanEntretien.DerniereRealisation derniereRealisation) {
    enginsFlotte.verifierExiste(engin);
    prestataireService.verifier(parametres.prestataireId(), null);
    return repository.sauvegarder(
        PlanEntretien.creer(UUID.randomUUID(), engin, parametres, derniereRealisation));
  }

  @Transactional
  public PlanEntretien modifier(UUID id, PlanEntretien.Parametres parametres) {
    prestataireService.verifier(parametres.prestataireId(), null);
    PlanEntretien plan = consulter(id);
    plan.modifier(parametres);
    return repository.sauvegarder(plan);
  }

  @Transactional(readOnly = true)
  public PlanEntretien consulter(UUID id) {
    return repository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun plan d'entretien trouvé pour l'identifiant " + id));
  }

  @Transactional(readOnly = true)
  public Page<PlanEntretien> rechercher(
      TypeEngin typeEngin, UUID enginId, Boolean actif, PageRequest pageRequest) {
    return repository.rechercher(typeEngin, enginId, actif, pageRequest);
  }

  /** Échéance d'un plan (fiche). */
  @Transactional(readOnly = true)
  public EcheancePlan echeance(UUID planId) {
    PlanEntretien plan = consulter(planId);
    Map<UUID, EtatEngin> etats = enginsFlotte.etats();
    return calculer(plan, etats, LocalDate.now(FUSEAU));
  }

  /**
   * Échéances des plans actifs de la flotte, des plus urgentes aux plus lointaines ; {@code
   * horizonJours} ne garde que les plans échus, en alerte ou dont l'échéance tombe dans l'horizon
   * (null = tous).
   */
  @Transactional(readOnly = true)
  public List<EcheancePlan> echeances(Integer horizonJours) {
    LocalDate aujourdHui = LocalDate.now(FUSEAU);
    Map<UUID, EtatEngin> etats = enginsFlotte.etats();
    return repository.actifs().stream()
        .filter(p -> etats.containsKey(p.engin().id()))
        .map(p -> calculer(p, etats, aujourdHui))
        .filter(
            e ->
                horizonJours == null
                    || e.echeance().etat() != EtatEcheance.OK
                    || (e.echeance().dateEcheance() != null
                        && !e.echeance().dateEcheance().isAfter(aujourdHui.plusDays(horizonJours))))
        .sorted(
            Comparator.comparing((EcheancePlan e) -> e.echeance().etat().ordinal())
                .reversed()
                .thenComparing(
                    e -> e.echeance().dateEcheance(),
                    Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
  }

  private EcheancePlan calculer(
      PlanEntretien plan, Map<UUID, EtatEngin> etats, LocalDate aujourdHui) {
    EtatEngin etat = etats.get(plan.engin().id());
    if (etat == null) {
      return new EcheancePlan(plan, null, 0, plan.prochaineEcheance(0, 0, aujourdHui, 0));
    }
    double kmParJour = kmParJour(etat, aujourdHui);
    return new EcheancePlan(
        plan,
        etat,
        kmParJour,
        plan.prochaineEcheance(etat.kilometrage(), etat.heures(), aujourdHui, kmParJour));
  }

  /**
   * Usage moyen de l'engin : écart de compteur entre les OT clôturés des 12 derniers mois, sinon
   * kilométrage moyen depuis la mise en circulation.
   */
  double kmParJour(EtatEngin etat, LocalDate aujourdHui) {
    List<OrdreTravail> releves =
        ordreTravailRepository
            .lister(
                new OrdreTravailRepository.Filtre(
                    etat.ref().type(),
                    etat.ref().id(),
                    StatutOT.TERMINE,
                    null,
                    null,
                    null,
                    null,
                    aujourdHui.minusYears(1).atStartOfDay(),
                    null))
            .stream()
            .filter(
                o -> o.realisation().kilometrage() != null && o.realisation().finReelle() != null)
            .sorted(Comparator.comparing(o -> o.realisation().finReelle()))
            .collect(Collectors.toList());
    if (releves.size() >= 2) {
      OrdreTravail premier = releves.getFirst();
      OrdreTravail dernier = releves.getLast();
      long jours =
          ChronoUnit.DAYS.between(
              premier.realisation().finReelle().toLocalDate(),
              dernier.realisation().finReelle().toLocalDate());
      int km = dernier.realisation().kilometrage() - premier.realisation().kilometrage();
      if (jours >= 30 && km > 0) {
        return (double) km / jours;
      }
    }
    return etat.kmParJourDepuisMiseEnService(aujourdHui);
  }
}
