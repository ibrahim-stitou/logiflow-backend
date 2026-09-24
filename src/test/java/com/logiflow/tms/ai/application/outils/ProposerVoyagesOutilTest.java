package com.logiflow.tms.ai.application.outils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.ai.application.PlanificationVoyageService;
import com.logiflow.tms.ai.application.PlanificationVoyageService.OptionValidee;
import com.logiflow.tms.ai.application.PlanificationVoyageService.PropositionsVoyage;
import com.logiflow.tms.ai.application.command.ProposerVoyagesCommand;
import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification.Arret;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification.Indicateurs;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification.Option;
import com.logiflow.tms.planning.api.dto.ConformiteVoyageSummary;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ProposerVoyagesOutilTest {

  private final PlanificationVoyageService service = mock(PlanificationVoyageService.class);
  private final ProposerVoyagesOutil outil = new ProposerVoyagesOutil(service);

  @Test
  void resumeLesOptionsEtRenvoieVersLEcranDePlanification() {
    Instant depart = Instant.parse("2026-10-05T06:00:00Z");
    Arret arret =
        new Arret(
            0, "s1", "Lyon", 45.7, 4.8, List.of("d1"), List.of(), depart, depart, 0, 0, 6000, 0,
            true);
    Option option =
        new Option(
            1,
            "REMPLISSAGE",
            "Remplissage maximal",
            "GROUPAGE",
            List.of("d1"),
            List.of(arret),
            "v1",
            null,
            List.of("c1"),
            depart,
            depart.plus(6, ChronoUnit.HOURS),
            new Indicateurs(1, 314, 270, 360, 6000, 24, 12, 0.5, 0.3, 0, 553.4, 92d),
            List.of(),
            "Bon remplissage.",
            true);
    when(service.proposer(any()))
        .thenReturn(
            new PropositionsVoyage(
                "NATIONAL",
                List.of(
                    new OptionValidee(
                        option, new ConformiteVoyageSummary(true, List.of(), List.of()))),
                "Une option.",
                List.of(),
                1,
                "OSRM",
                "LLM",
                Map.of("d1", "DT-2026-900001"),
                Map.of("v1", "GP-002-BH"),
                Map.of(),
                Map.of("c1", "Jean Martin (DRV-0001)")));

    ResultatOutil resultat =
        outil.executer(new ArgumentsOutil(Map.of("debut", "2026-10-05", "fin", "2026-10-06")));

    ArgumentCaptor<ProposerVoyagesCommand> commande =
        ArgumentCaptor.forClass(ProposerVoyagesCommand.class);
    verify(service).proposer(commande.capture());
    assertThat(commande.getValue().typeVoyage()).isEqualTo("GROUPAGE");
    assertThat(commande.getValue().fin()).isEqualTo(Instant.parse("2026-10-06T22:00:00Z"));
    assertThat(resultat.total()).isEqualTo(1);
    assertThat(resultat.resultats().getFirst())
        .containsEntry("dossiers", List.of("DT-2026-900001"))
        .containsEntry("vehicule", "GP-002-BH")
        .containsEntry("remplissagePct", 50L)
        .doesNotContainKey("remorque");
    assertThat(resultat.sources())
        .contains(new SourceCopilote("DOSSIER", "DT-2026-900001", "d1"))
        .anyMatch(s -> s.type().equals("PLANIFICATION"));
  }
}
