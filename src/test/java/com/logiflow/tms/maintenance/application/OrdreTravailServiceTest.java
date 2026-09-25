package com.logiflow.tms.maintenance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.maintenance.api.EtatMaintenanceEnginModifieEvent;
import com.logiflow.tms.maintenance.application.OrdreTravailService.CreerOrdreTravail;
import com.logiflow.tms.maintenance.domain.model.NatureIntervention;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.OrigineOT;
import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.PrioriteOT;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.domain.model.TypeLigneCout;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.domain.port.out.PlanEntretienRepository;
import com.logiflow.tms.maintenance.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.maintenance.domain.port.out.SinistreRepository;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.maintenance.domain.vo.LigneCout;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OrdreTravailServiceTest {

  private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 10, 1, 8, 0);

  @Mock private PlanEntretienRepository planRepository;
  @Mock private SinistreRepository sinistreRepository;
  @Mock private SequenceReferenceGenerator referenceGenerator;
  @Mock private PrestataireService prestataireService;
  @Mock private EnginsFlotte enginsFlotte;
  @Mock private ApplicationEventPublisher eventPublisher;

  /** Dépôt en mémoire : suffisant pour suivre les OT d'un engin. */
  private final Map<UUID, OrdreTravail> ordres = new HashMap<>();

  private OrdreTravailService service;
  private final EnginRef camion = EnginRef.vehicule(UUID.randomUUID());

  @BeforeEach
  void setUp() {
    OrdreTravailRepository repository =
        new OrdreTravailRepository() {
          @Override
          public OrdreTravail sauvegarder(OrdreTravail ot) {
            ordres.put(ot.id(), ot);
            return ot;
          }

          @Override
          public Optional<OrdreTravail> parId(UUID id) {
            return Optional.ofNullable(ordres.get(id));
          }

          @Override
          public com.logiflow.tms.shared.application.Page<OrdreTravail> rechercher(
              Filtre filtre, com.logiflow.tms.shared.application.PageRequest page) {
            throw new UnsupportedOperationException();
          }

          @Override
          public List<OrdreTravail> lister(Filtre filtre) {
            return ordres.values().stream()
                .filter(o -> filtre.enginId() == null || o.engin().id().equals(filtre.enginId()))
                .toList();
          }

          @Override
          public List<OrdreTravail> parPlanId(UUID planId) {
            return List.of();
          }

          @Override
          public List<OrdreTravail> parSinistreId(UUID sinistreId) {
            return List.of();
          }
        };
    service =
        new OrdreTravailService(
            repository,
            planRepository,
            sinistreRepository,
            referenceGenerator,
            prestataireService,
            enginsFlotte,
            eventPublisher);
  }

  private OrdreTravail creer(UUID planId) {
    org.mockito.Mockito.lenient()
        .when(referenceGenerator.generer(any(), any(Integer.class)))
        .thenReturn(Reference.generer("OT", 2026, ordres.size() + 1));
    return service.creer(
        new CreerOrdreTravail(
            camion,
            planId == null ? OrigineOT.MANUELLE : OrigineOT.PLAN_ENTRETIEN,
            planId,
            null,
            new OrdreTravail.DetailsOT(
                TypeIntervention.ENTRETIEN_PREVENTIF,
                NatureIntervention.PREVENTIF,
                PrioriteOT.NORMALE,
                "Révision",
                null,
                null,
                DEBUT,
                null,
                true,
                null),
            List.of(
                new LigneCout(
                    TypeLigneCout.MAIN_OEUVRE,
                    "Main-d'œuvre",
                    null,
                    BigDecimal.ONE,
                    new Money(BigDecimal.valueOf(120), OrdreTravail.DEVISE),
                    null))));
  }

  private static OrdreTravail.Cloture cloture() {
    return new OrdreTravail.Cloture(
        DEBUT.plusHours(3), 151_000, null, null, "OK", null, null, null);
  }

  @Test
  void demarrerImmobiliseLEnginEtLaClotureMetAJourPlanCompteursEtRemiseEnService() {
    PlanEntretien plan =
        PlanEntretien.creer(
            UUID.randomUUID(),
            camion,
            new PlanEntretien.Parametres(
                "Révision", null, 40_000, 12, null, 2000, 15, 120, null, null, true),
            null);
    when(planRepository.parId(plan.id())).thenReturn(Optional.of(plan));
    OrdreTravail ot = creer(plan.id());

    service.changerStatut(ot.id(), StatutOT.EN_COURS);
    verify(enginsFlotte).immobiliser(camion, false);

    service.cloturer(ot.id(), cloture());

    verify(enginsFlotte).releverCompteurs(camion, 151_000, null);
    verify(planRepository).sauvegarder(plan);
    assertThat(plan.derniereRealisation().kilometrage()).isEqualTo(151_000);
    assertThat(plan.derniereRealisation().date()).isEqualTo(LocalDate.of(2026, 10, 1));
    verify(enginsFlotte).remettreEnService(camion);
    verify(eventPublisher)
        .publishEvent(
            new EtatMaintenanceEnginModifieEvent(
                "VEHICULE", camion.id(), "Clôture de l'OT " + ot.reference().valeur()));
  }

  @Test
  void pasDeRemiseEnServiceTantQuUnAutreOtRetientLEngin() {
    OrdreTravail premier = creer(null);
    OrdreTravail second = creer(null);
    service.changerStatut(premier.id(), StatutOT.EN_COURS);
    service.changerStatut(second.id(), StatutOT.EN_COURS);

    service.cloturer(premier.id(), cloture());

    verify(enginsFlotte, never()).remettreEnService(any());
  }

  @Test
  void unPlanDUnAutreEnginEstRefuse() {
    PlanEntretien autre =
        PlanEntretien.creer(
            UUID.randomUUID(),
            EnginRef.vehicule(UUID.randomUUID()),
            new PlanEntretien.Parametres(
                "Révision", null, 40_000, null, null, 0, 0, 0, null, null, true),
            null);
    when(planRepository.parId(autre.id())).thenReturn(Optional.of(autre));

    assertThatThrownBy(() -> creer(autre.id()))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("ne concerne pas");
  }
}
