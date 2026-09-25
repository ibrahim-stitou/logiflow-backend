package com.logiflow.tms.maintenance.application;

import com.logiflow.tms.maintenance.application.OrdreTravailService.CreerOrdreTravail;
import com.logiflow.tms.maintenance.domain.model.ContratAssurance;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.OrigineOT;
import com.logiflow.tms.maintenance.domain.model.Sinistre;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.StatutSinistre;
import com.logiflow.tms.maintenance.domain.model.TypePrestataire;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.maintenance.domain.port.out.SinistreRepository;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.vo.Money;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Déclaration et suivi des sinistres : contrat d'assurance et franchise repris automatiquement,
 * immobilisation des engins, ordres de travail de réparation et coût net.
 */
@Service
@RequiredArgsConstructor
public class SinistreService {

  static final String PREFIXE = "SIN";

  private final SinistreRepository repository;
  private final OrdreTravailRepository ordreTravailRepository;
  private final OrdreTravailService ordreTravailService;
  private final ContratAssuranceService contratAssuranceService;
  private final PrestataireService prestataireService;
  private final SequenceReferenceGenerator referenceGenerator;
  private final EnginsFlotte enginsFlotte;

  /** Coûts d'un sinistre : réparations (OT terminés), indemnité, franchise et coût net. */
  public record CoutsSinistre(
      Money reparationsHt,
      Money reparationsTtc,
      Money indemnite,
      Money franchise,
      Money coutNet,
      int ordresTravail,
      int ordresOuverts) {}

  @Transactional
  public Sinistre declarer(Sinistre.Circonstances circonstances, Sinistre.SuiviAssurance suivi) {
    List<EnginRef> engins = engins(circonstances);
    engins.forEach(enginsFlotte::verifierExiste);
    Sinistre.SuiviAssurance complete = completerAssurance(circonstances, suivi, engins);
    Sinistre sinistre =
        Sinistre.declarer(
            UUID.randomUUID(),
            referenceGenerator.generer(PREFIXE, Year.now().getValue()),
            circonstances,
            complete);
    Sinistre sauve = repository.sauvegarder(sinistre);
    if (circonstances.enginImmobilise()) {
      engins.forEach(e -> enginsFlotte.immobiliser(e, true));
    }
    return sauve;
  }

  @Transactional
  public Sinistre modifier(
      UUID id, Sinistre.Circonstances circonstances, Sinistre.SuiviAssurance suivi) {
    Sinistre sinistre = consulter(id);
    boolean etaitImmobilise = sinistre.circonstances().enginImmobilise();
    List<EnginRef> engins = engins(circonstances);
    engins.forEach(enginsFlotte::verifierExiste);
    sinistre.modifier(circonstances, completerAssurance(circonstances, suivi, engins));
    Sinistre sauve = repository.sauvegarder(sinistre);
    if (!etaitImmobilise && circonstances.enginImmobilise()) {
      engins.forEach(e -> enginsFlotte.immobiliser(e, true));
    } else if (etaitImmobilise && !circonstances.enginImmobilise()) {
      engins.forEach(ordreTravailService::remettreEnServiceSiLibre);
    }
    return sauve;
  }

  @Transactional
  public Sinistre changerStatut(UUID id, StatutSinistre statut) {
    Sinistre sinistre = consulter(id);
    long ouverts =
        ordreTravailRepository.parSinistreId(id).stream().filter(OrdreTravail::estOuvert).count();
    sinistre.changerStatut(statut, LocalDate.now(PlanEntretienService.FUSEAU), ouverts);
    Sinistre sauve = repository.sauvegarder(sinistre);
    if (sauve.estTermine() && sauve.circonstances().enginImmobilise()) {
      engins(sauve.circonstances()).forEach(ordreTravailService::remettreEnServiceSiLibre);
    }
    return sauve;
  }

  /** Crée l'OT de réparation d'un engin du sinistre et passe le sinistre EN_REPARATION. */
  @Transactional
  public OrdreTravail creerReparation(
      UUID sinistreId, EnginRef engin, OrdreTravail.DetailsOT details) {
    Sinistre sinistre = consulter(sinistreId);
    OrdreTravail ot =
        ordreTravailService.creer(
            new CreerOrdreTravail(engin, OrigineOT.SINISTRE, null, sinistreId, details, List.of()));
    if (sinistre.statut() == StatutSinistre.DECLARE
        || sinistre.statut() == StatutSinistre.DECLARE_ASSUREUR
        || sinistre.statut() == StatutSinistre.EN_EXPERTISE) {
      sinistre.changerStatut(
          StatutSinistre.EN_REPARATION, LocalDate.now(PlanEntretienService.FUSEAU), 0);
      repository.sauvegarder(sinistre);
    }
    return ot;
  }

  @Transactional(readOnly = true)
  public Sinistre consulter(UUID id) {
    return repository
        .parId(id)
        .orElseThrow(() -> new NotFoundException("Aucun sinistre trouvé pour l'identifiant " + id));
  }

  @Transactional(readOnly = true)
  public Page<Sinistre> rechercher(SinistreRepository.Filtre filtre, PageRequest page) {
    return repository.rechercher(filtre, page);
  }

  @Transactional(readOnly = true)
  public CoutsSinistre couts(UUID id) {
    return couts(consulter(id), ordreTravailRepository.parSinistreId(id));
  }

  static CoutsSinistre couts(Sinistre sinistre, List<OrdreTravail> ordres) {
    Money zero = Money.zero(OrdreTravail.DEVISE);
    List<OrdreTravail> termines =
        ordres.stream().filter(o -> o.statut() == StatutOT.TERMINE).toList();
    Money ht = termines.stream().map(OrdreTravail::totalHt).reduce(zero, Money::plus);
    Money ttc = termines.stream().map(OrdreTravail::totalTtc).reduce(zero, Money::plus);
    Money indemnite =
        sinistre.assurance().indemnite() == null ? zero : sinistre.assurance().indemnite();
    Money franchise =
        sinistre.assurance().franchise() == null ? zero : sinistre.assurance().franchise();
    return new CoutsSinistre(
        ht,
        ttc,
        indemnite,
        franchise,
        sinistre.coutNet(ht),
        ordres.size(),
        (int) ordres.stream().filter(OrdreTravail::estOuvert).count());
  }

  /** Contrat applicable et franchise repris du contrat quand ils ne sont pas saisis. */
  private Sinistre.SuiviAssurance completerAssurance(
      Sinistre.Circonstances circonstances, Sinistre.SuiviAssurance suivi, List<EnginRef> engins) {
    Sinistre.SuiviAssurance s = suivi == null ? Sinistre.SuiviAssurance.vide() : suivi;
    prestataireService.verifier(s.expertId(), TypePrestataire.EXPERT);
    ContratAssurance contrat =
        s.contratId() != null
            ? contratAssuranceService.consulter(s.contratId())
            : engins.stream()
                .map(
                    e ->
                        contratAssuranceService.applicable(
                            e, circonstances.dateSurvenance().toLocalDate()))
                .flatMap(java.util.Optional::stream)
                .findFirst()
                .orElse(null);
    if (contrat == null) {
      return s;
    }
    return new Sinistre.SuiviAssurance(
        contrat.id(),
        s.numeroDossierAssureur(),
        s.dateDeclarationAssureur(),
        s.expertId(),
        s.dateExpertise(),
        s.estimationDommages(),
        s.franchise() != null ? s.franchise() : contrat.conditions().franchise(),
        s.indemnite());
  }

  static List<EnginRef> engins(Sinistre.Circonstances c) {
    List<EnginRef> engins = new ArrayList<>();
    if (c.vehiculeId() != null) {
      engins.add(EnginRef.vehicule(c.vehiculeId()));
    }
    if (c.remorqueId() != null) {
      engins.add(EnginRef.remorque(c.remorqueId()));
    }
    return engins;
  }
}
