package com.logiflow.tms.maintenance.application;

import com.logiflow.tms.maintenance.api.EtatMaintenanceEnginModifieEvent;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.OrigineOT;
import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.Sinistre;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.domain.port.out.PlanEntretienRepository;
import com.logiflow.tms.maintenance.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.maintenance.domain.port.out.SinistreRepository;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.maintenance.domain.vo.LigneCout;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cycle de vie des ordres de travail et effets sur la flotte : un OT démarré avec immobilisation
 * met l'engin EN_MAINTENANCE ; la clôture relève les compteurs, met à jour le plan d'entretien lié
 * et remet l'engin en service s'il n'est plus retenu ni par un autre OT ni par un sinistre.
 */
@Service
@RequiredArgsConstructor
public class OrdreTravailService {

  static final String PREFIXE = "OT";

  private final OrdreTravailRepository repository;
  private final PlanEntretienRepository planRepository;
  private final SinistreRepository sinistreRepository;
  private final SequenceReferenceGenerator referenceGenerator;
  private final PrestataireService prestataireService;
  private final EnginsFlotte enginsFlotte;
  private final ApplicationEventPublisher eventPublisher;

  /** Création d'un OT ; {@code lignes} (devis) facultatives. */
  public record CreerOrdreTravail(
      EnginRef engin,
      OrigineOT origine,
      UUID planId,
      UUID sinistreId,
      OrdreTravail.DetailsOT details,
      List<LigneCout> lignes) {}

  @Transactional
  public OrdreTravail creer(CreerOrdreTravail commande) {
    enginsFlotte.verifierExiste(commande.engin());
    prestataireService.verifier(commande.details().prestataireId(), null);
    OrigineOT origine = commande.origine() == null ? OrigineOT.MANUELLE : commande.origine();
    if (commande.planId() != null) {
      PlanEntretien plan = planOuEchouer(commande.planId());
      if (!plan.engin().equals(commande.engin())) {
        throw new BusinessException("Le plan d'entretien ne concerne pas cet engin");
      }
    }
    if (commande.sinistreId() != null) {
      Sinistre sinistre = sinistreOuEchouer(commande.sinistreId());
      if (!concerne(sinistre, commande.engin())) {
        throw new BusinessException("Le sinistre ne concerne pas cet engin");
      }
    }
    OrdreTravail ot =
        OrdreTravail.creer(
            UUID.randomUUID(),
            referenceGenerator.generer(PREFIXE, Year.now().getValue()),
            commande.engin(),
            origine,
            commande.planId(),
            commande.sinistreId(),
            commande.details());
    if (commande.lignes() != null && !commande.lignes().isEmpty()) {
      ot.remplacerLignes(commande.lignes());
    }
    return repository.sauvegarder(ot);
  }

  @Transactional
  public OrdreTravail modifier(UUID id, OrdreTravail.DetailsOT details) {
    prestataireService.verifier(details.prestataireId(), null);
    OrdreTravail ot = consulter(id);
    ot.modifier(details);
    return repository.sauvegarder(ot);
  }

  @Transactional
  public OrdreTravail remplacerLignes(UUID id, List<LigneCout> lignes) {
    OrdreTravail ot = consulter(id);
    ot.remplacerLignes(lignes);
    return repository.sauvegarder(ot);
  }

  @Transactional
  public OrdreTravail changerStatut(UUID id, StatutOT statut) {
    OrdreTravail ot = consulter(id);
    ot.changerStatut(statut, LocalDateTime.now(PlanEntretienService.FUSEAU));
    OrdreTravail sauve = repository.sauvegarder(ot);
    if (statut == StatutOT.EN_COURS && sauve.details().immobilisation()) {
      enginsFlotte.immobiliser(sauve.engin(), false);
    } else if (statut == StatutOT.ANNULE) {
      remettreEnServiceSiLibre(sauve.engin());
    }
    return sauve;
  }

  @Transactional
  public OrdreTravail cloturer(UUID id, OrdreTravail.Cloture cloture) {
    OrdreTravail ot = consulter(id);
    ot.cloturer(cloture);
    OrdreTravail sauve = repository.sauvegarder(ot);
    enginsFlotte.releverCompteurs(sauve.engin(), cloture.kilometrage(), cloture.heures());
    if (sauve.planId() != null) {
      PlanEntretien plan = planOuEchouer(sauve.planId());
      plan.enregistrerRealisation(
          cloture.finReelle().toLocalDate(), cloture.kilometrage(), cloture.heures());
      planRepository.sauvegarder(plan);
    }
    remettreEnServiceSiLibre(sauve.engin());
    publierEtatModifie(sauve.engin(), "Clôture de l'OT " + sauve.reference().valeur());
    return sauve;
  }

  /**
   * Signale aux autres modules (agent de maintenance prédictive) un changement d'état de l'engin.
   */
  void publierEtatModifie(EnginRef engin, String motif) {
    eventPublisher.publishEvent(
        new EtatMaintenanceEnginModifieEvent(engin.type().name(), engin.id(), motif));
  }

  @Transactional(readOnly = true)
  public OrdreTravail consulter(UUID id) {
    return repository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun ordre de travail trouvé pour l'identifiant " + id));
  }

  @Transactional(readOnly = true)
  public Page<OrdreTravail> rechercher(OrdreTravailRepository.Filtre filtre, PageRequest page) {
    return repository.rechercher(filtre, page);
  }

  @Transactional(readOnly = true)
  public List<OrdreTravail> parPlan(UUID planId) {
    return repository.parPlanId(planId);
  }

  @Transactional(readOnly = true)
  public List<OrdreTravail> parSinistre(UUID sinistreId) {
    return repository.parSinistreId(sinistreId);
  }

  /**
   * Remet l'engin en service s'il n'est plus retenu : aucun autre OT en cours avec immobilisation,
   * aucun sinistre ouvert qui l'immobilise.
   */
  void remettreEnServiceSiLibre(EnginRef engin) {
    boolean otEnCours =
        repository
            .lister(
                new OrdreTravailRepository.Filtre(
                    engin.type(), engin.id(), null, null, null, null, null, null, null))
            .stream()
            .anyMatch(
                o ->
                    o.details().immobilisation()
                        && (o.statut() == StatutOT.EN_COURS
                            || o.statut() == StatutOT.EN_ATTENTE_PIECES));
    boolean sinistreImmobilisant =
        sinistreRepository
            .lister(new SinistreRepository.Filtre(engin.id(), null, null, null, null, null, null))
            .stream()
            .anyMatch(s -> !s.estTermine() && s.circonstances().enginImmobilise());
    if (!otEnCours && !sinistreImmobilisant) {
      enginsFlotte.remettreEnService(engin);
    }
  }

  static boolean concerne(Sinistre sinistre, EnginRef engin) {
    UUID cible =
        engin.type() == TypeEngin.VEHICULE
            ? sinistre.circonstances().vehiculeId()
            : sinistre.circonstances().remorqueId();
    return Objects.equals(cible, engin.id());
  }

  private PlanEntretien planOuEchouer(UUID id) {
    return planRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun plan d'entretien trouvé pour l'identifiant " + id));
  }

  private Sinistre sinistreOuEchouer(UUID id) {
    return sinistreRepository
        .parId(id)
        .orElseThrow(() -> new NotFoundException("Aucun sinistre trouvé pour l'identifiant " + id));
  }
}
